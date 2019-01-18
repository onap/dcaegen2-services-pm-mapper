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
import io.undertow.util.HeaderValues;
import lombok.Data;
import lombok.NonNull;
import org.onap.dcaegen2.services.pmmapper.config.BusControllerConfig;
import org.onap.dcaegen2.services.pmmapper.exceptions.NoMetadataException;
import org.onap.dcaegen2.services.pmmapper.exceptions.TooManyTriesException;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.services.pmmapper.utils.RequiredFieldDeserializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;

/**
 * Subscriber for events sent from data router
 * Provides an undertow HttpHandler to be used as an endpoint for data router to send events to.
 */
@Slf4j
@Data
public class DataRouterSubscriber implements HttpHandler {

    private static final int NUMBER_OF_ATTEMPTS = 5;
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int MAX_JITTER = 50;

    private static final String METADATA_HEADER = "X-ATT-DR-META";
    private static final String BAD_METADATA_MESSAGE = "Malformed Metadata.";
    private static final String NO_METADATA_MESSAGE = "Missing Metadata.";

    private boolean limited = false;
    private Random jitterGenerator;
    private Gson metadataBuilder;
    @NonNull
    private EventReceiver eventReceiver;

    /**
     * @param eventReceiver receiver for any inbound events.
     */
    public DataRouterSubscriber(EventReceiver eventReceiver) {
        this.eventReceiver = eventReceiver;
        this.jitterGenerator = new Random();
        this.metadataBuilder = new GsonBuilder().registerTypeAdapter(EventMetadata.class, new RequiredFieldDeserializer<EventMetadata>())
                .create();
    }

    /**
     * Starts data flow by subscribing to data router through bus controller.
     *
     * @param config configuration object containing bus controller endpoint for subscription and
     *               all non constant configuration for subscription through this endpoint.
     * @throws TooManyTriesException in the event that timeout has occurred several times.
     */
    public void start(BusControllerConfig config) throws TooManyTriesException, InterruptedException {
        subscribe(NUMBER_OF_ATTEMPTS, DEFAULT_TIMEOUT, config);
    }

    private HttpURLConnection getBusControllerConnection(BusControllerConfig config, int timeout) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) config.getDataRouterSubscribeEndpoint()
                .openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

    private JsonObject getBusControllerSubscribeBody(BusControllerConfig config) {
        JsonObject subscriberObj = new JsonObject();
        subscriberObj.addProperty("dcaeLocationName", config.getDcaeLocation());
        subscriberObj.addProperty("deliveryURL", config.getDeliveryURL());
        subscriberObj.addProperty("feedId", config.getFeedId());
        subscriberObj.addProperty("lastMod", config.getLastMod());
        subscriberObj.addProperty("username", config.getUsername());
        subscriberObj.addProperty("userpwd", config.getPassword());
        return subscriberObj;
    }

    private void subscribe(int attempts, int timeout, BusControllerConfig config) throws TooManyTriesException, InterruptedException {
        int subResponse = 504;
        String subMessage = "";
        try {
            HttpURLConnection connection = getBusControllerConnection(config, timeout);

            try (OutputStream bodyStream = connection.getOutputStream();
                 OutputStreamWriter bodyWriter = new OutputStreamWriter(bodyStream, StandardCharsets.UTF_8)) {
                bodyWriter.write(getBusControllerSubscribeBody(config).toString());
            }
            subResponse = connection.getResponseCode();
            subMessage = connection.getResponseMessage();
        } catch (IOException e) {
            log.info("Timeout Failure:", e);
        }
        log.info("Request to bus controller executed with Response Code: '{}' and Response Event: '{}'.", subResponse, subMessage);
        if (subResponse >= 300 && attempts > 1) {
            Thread.sleep(timeout);
            subscribe(--attempts, (timeout * 2) + jitterGenerator.nextInt(MAX_JITTER), config);
        } else if (subResponse >= 300) {
            throw new TooManyTriesException("Failed to subscribe within appropriate amount of attempts");
        }
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
        if (limited) {
            httpServerExchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE)
                    .getResponseSender()
                    .send(StatusCodes.SERVICE_UNAVAILABLE_STRING);
        } else {
            try {
                String metadataAsString = Optional.of(httpServerExchange.getRequestHeaders()
                        .get(METADATA_HEADER))
                        .map((HeaderValues headerValues) -> headerValues.get(0))
                        .orElseThrow(() -> new NoMetadataException("Metadata Not found"));

                EventMetadata metadata = metadataBuilder.fromJson(metadataAsString, EventMetadata.class);
                httpServerExchange.getRequestReceiver()
                        .receiveFullString((callbackExchange, body) -> {
                            httpServerExchange.dispatch(() -> eventReceiver.receive(new Event(callbackExchange, body, metadata)));
                        });
            } catch (NoMetadataException exception) {
                log.info("Bad Request: no metadata found under '{}' header.", METADATA_HEADER, exception);
                httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST)
                        .getResponseSender()
                        .send(NO_METADATA_MESSAGE);
            } catch (JsonParseException exception) {
                log.info("Bad Request: Failure to parse metadata", exception);
                httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST)
                        .getResponseSender()
                        .send(BAD_METADATA_MESSAGE);
            }
        }
    }
}
