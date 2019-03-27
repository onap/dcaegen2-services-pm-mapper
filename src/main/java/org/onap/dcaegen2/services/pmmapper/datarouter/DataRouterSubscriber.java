/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.pmmapper.datarouter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.undertow.util.HeaderValues;
import lombok.Data;
import lombok.NonNull;

import org.onap.dcaegen2.services.pmmapper.config.Configurable;
import org.onap.dcaegen2.services.pmmapper.exceptions.NoMetadataException;
import org.onap.dcaegen2.services.pmmapper.exceptions.ReconfigurationException;
import org.onap.dcaegen2.services.pmmapper.exceptions.TooManyTriesException;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import org.onap.dcaegen2.services.pmmapper.utils.HttpServerExchangeAdapter;
import org.onap.dcaegen2.services.pmmapper.utils.RequiredFieldDeserializer;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Subscriber for events sent from data router
 * Provides an undertow HttpHandler to be used as an endpoint for data router to send events to.
 */
@Data
public class DataRouterSubscriber implements HttpHandler, Configurable {
    public static final String METADATA_HEADER = "X-DMAAP-DR-META";
    public static final String PUB_ID_HEADER = "X-DMAAP-DR-PUBLISH-ID";

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(DataRouterSubscriber.class));
    private static final int NUMBER_OF_ATTEMPTS = 5;
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int MAX_JITTER = 50;

    private static final String BAD_METADATA_MESSAGE = "Malformed Metadata.";
    private static final String NO_METADATA_MESSAGE = "Missing Metadata.";

    private boolean limited = false;
    private Random jitterGenerator;
    private Gson metadataBuilder;
    private MapperConfig config;
    public static String subscriberId;
    @NonNull
    private EventReceiver eventReceiver;

    /**
     * @param eventReceiver receiver for any inbound events.
     */
    public DataRouterSubscriber(EventReceiver eventReceiver, MapperConfig config) {
        this.eventReceiver = eventReceiver;
        this.jitterGenerator = new Random();
        this.metadataBuilder = new GsonBuilder().registerTypeAdapter(EventMetadata.class, new RequiredFieldDeserializer<EventMetadata>())
                .create();
        this.config = config;
        this.subscriberId="";
    }

    /**
     * Starts data flow by subscribing to data router through bus controller.
     *
     * @throws TooManyTriesException in the event that timeout has occurred several times.
     */
    public void start() throws TooManyTriesException, InterruptedException {
        try {
            logger.unwrap().info("Starting subscription to DataRouter {}", ONAPLogConstants.Markers.ENTRY);
            subscribe();
            logger.unwrap().info("Successfully started DR Subscriber");
        } finally {
            logger.unwrap().info("{}", ONAPLogConstants.Markers.EXIT);
        }
    }

    private HttpURLConnection getBusControllerConnection(String method, URL resource, int timeout) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) resource.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        final UUID invocationID = logger.invoke(ONAPLogConstants.InvocationMode.SYNCHRONOUS);
        final UUID requestID = UUID.randomUUID();
        connection.setRequestProperty(ONAPLogConstants.Headers.REQUEST_ID, requestID.toString());
        connection.setRequestProperty(ONAPLogConstants.Headers.INVOCATION_ID, invocationID.toString());
        connection.setRequestProperty(ONAPLogConstants.Headers.PARTNER_NAME, MapperConfig.CLIENT_NAME);

        return connection;
    }

    private JsonObject getBusControllerSubscribeBody(MapperConfig config) {
        JsonObject subscriberObj = new JsonObject();
        subscriberObj.addProperty("dcaeLocationName", config.getSubscriberDcaeLocation());
        subscriberObj.addProperty("deliveryURL", config.getBusControllerDeliveryUrl());
        subscriberObj.addProperty("feedId", config.getDmaapDRFeedId());
        subscriberObj.addProperty("lastMod", Instant.now().toString());
        subscriberObj.addProperty("username", config.getBusControllerUserName());
        subscriberObj.addProperty("userpwd", config.getBusControllerPassword());
        subscriberObj.addProperty("privilegedSubscriber", true);
        return subscriberObj;
    }

    private void processResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader responseBody = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String body = responseBody.lines().collect(Collectors.joining(""));
            updateSubscriberId(body);
        } catch (IOException | JsonSyntaxException | IllegalStateException e) {
            throw new IOException("Failed to process response", e);
        }
    }

    private void updateSubscriberId(String responseBody) {
            JsonParser parser = new JsonParser();
            JsonObject responseObject = parser.parse(responseBody).getAsJsonObject();
            this.subscriberId = responseObject.get("subId").getAsString();
    }

    private void subscribe() throws TooManyTriesException, InterruptedException {
        try {
            URL subscribeResource = this.config.getBusControllerSubscriptionUrl();
            JsonObject subscribeBody = this.getBusControllerSubscribeBody(this.config);
            request(NUMBER_OF_ATTEMPTS, DEFAULT_TIMEOUT, "POST", subscribeResource, subscribeBody);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Subscription URL is malformed", e);
        }

    }
    private void updateSubscriber() throws TooManyTriesException, InterruptedException {
        try {
            URL subscribeResource = this.config.getBusControllerSubscriptionUrl();
            URL updateResource = new URL(String.format("%s/%s", subscribeResource, subscriberId));
            JsonObject subscribeBody = this.getBusControllerSubscribeBody(this.config);
            request(NUMBER_OF_ATTEMPTS, DEFAULT_TIMEOUT, "PUT", updateResource, subscribeBody);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Subscription URL is malformed", e);
        }
    }

    private void request(int attempts, int timeout, String method, URL resource, JsonObject subscribeBody) throws TooManyTriesException, InterruptedException {
        int subResponse = 504;
        String subMessage = "";
        boolean processFailure = false;
        try {
            HttpURLConnection connection = getBusControllerConnection(method, resource, timeout);
            try (OutputStream bodyStream = connection.getOutputStream();
                 OutputStreamWriter bodyWriter = new OutputStreamWriter(bodyStream, StandardCharsets.UTF_8)) {
                bodyWriter.write(subscribeBody.toString());
            }
            subResponse = connection.getResponseCode();
            subMessage = connection.getResponseMessage();
            if (subResponse < 300) {
                processResponse(connection);
            }
        } catch (IOException e) {
            logger.unwrap().error("Failure to process response", e);
            processFailure = true;
        }
        logger.unwrap().info("Request to bus controller executed with Response Code: '{}' and Response Event: '{}'.", subResponse, subMessage);
        if ((subResponse >= 300 || processFailure) && attempts > 1 ) {
            Thread.sleep(timeout);
            request(--attempts, (timeout * 2) + jitterGenerator.nextInt(MAX_JITTER), method, resource, subscribeBody);
        } else if (subResponse >= 300 || processFailure) {
            throw new TooManyTriesException("Failed to subscribe within appropriate amount of attempts");
        }
    }

    private EventMetadata getMetadata(HttpServerExchange httpServerExchange) throws NoMetadataException {
        String metadata = Optional.ofNullable(httpServerExchange.getRequestHeaders()
                .get(METADATA_HEADER))
                .map((HeaderValues headerValues) -> headerValues.get(0))
                .orElseThrow(() -> new NoMetadataException("Metadata Not found"));
        return metadataBuilder.fromJson(metadata, EventMetadata.class);
    }

    /**
     * Receives inbound requests, verifies that required headers are valid
     * and passes an Event onto the eventReceiver.
     * The forwarded httpServerExchange response is the responsibility of the eventReceiver.
     *
     * @param httpServerExchange inbound http server exchange.
     */
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) {
        try{
            logger.entering(new HttpServerExchangeAdapter(httpServerExchange));
            if (limited) {
                httpServerExchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE)
                        .getResponseSender()
                        .send(StatusCodes.SERVICE_UNAVAILABLE_STRING);
            } else {
                try {

                    Map<String,String> mdc = MDC.getCopyOfContextMap();
                    EventMetadata metadata = getMetadata(httpServerExchange);
                    String publishIdentity = httpServerExchange.getRequestHeaders().get(PUB_ID_HEADER).getFirst();
                    httpServerExchange.getRequestReceiver()
                            .receiveFullString((callbackExchange, body) ->
                                httpServerExchange.dispatch(() ->
                                        eventReceiver.receive(new Event(callbackExchange, body, metadata, mdc, publishIdentity)))
                            );
                } catch (NoMetadataException exception) {
                    logger.unwrap().info("Bad Request: no metadata found under '{}' header.", METADATA_HEADER, exception);
                    httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST)
                            .getResponseSender()
                            .send(NO_METADATA_MESSAGE);
                } catch (JsonParseException exception) {
                    logger.unwrap().info("Bad Request: Failure to parse metadata", exception);
                    httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST)
                            .getResponseSender()
                            .send(BAD_METADATA_MESSAGE);
                }
            }
        } finally {
            logger.exiting();
        }
    }

    @Override
    public void reconfigure(MapperConfig config) throws ReconfigurationException {
        logger.unwrap().info("Checking new Configuration against existing.");
        if(!this.config.dmaapInfoEquals(config) || !this.config.getDmaapDRFeedId().equals(config.getDmaapDRFeedId())){
            logger.unwrap().info("DMaaP Info changes found, reconfiguring.");
            try {
                this.config = config;
                this.updateSubscriber();
            } catch (TooManyTriesException | InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ReconfigurationException("Failed to reconfigure DataRouter subscriber.", e);
            }
        }

    }
}
