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

package org.onap.dcaegen2.services.pmmapper;

import ch.qos.logback.classic.util.ContextInitializer;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.util.StatusCodes;

import java.util.Arrays;
import lombok.Data;
import lombok.NonNull;
import org.onap.dcaegen2.services.pmmapper.config.ConfigHandler;
import org.onap.dcaegen2.services.pmmapper.config.DynamicConfiguration;
import org.onap.dcaegen2.services.pmmapper.datarouter.DeliveryHandler;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSServerError;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.ProcessEventException;
import org.onap.dcaegen2.services.pmmapper.filtering.MetadataFilter;
import org.onap.dcaegen2.services.pmmapper.filtering.MeasFilterHandler;
import org.onap.dcaegen2.services.pmmapper.mapping.Mapper;
import org.onap.dcaegen2.services.pmmapper.messagerouter.VESPublisher;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.healthcheck.HealthCheckHandler;
import org.onap.dcaegen2.services.pmmapper.model.ServerHandler;
import org.onap.dcaegen2.services.pmmapper.ssl.SSLContextFactory;
import org.onap.dcaegen2.services.pmmapper.utils.DataRouterUtils;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;
import org.onap.dcaegen2.services.pmmapper.utils.MeasSplitter;
import org.onap.dcaegen2.services.pmmapper.utils.XMLValidator;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Data
public class App {
    static {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "/opt/app/pm-mapper/etc/logback.xml");
    }

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(App.class));
    private static final int HTTP_PORT = 8081;
    private static final int HTTPS_PORT = 8443;
    private static Path mappingTemplate = Paths.get("/opt/app/pm-mapper/etc/mapping.ftl");
    private static Path xmlSchema = Paths.get("/opt/app/pm-mapper/etc/measCollec_plusString.xsd");

    private MapperConfig mapperConfig;
    private MetadataFilter metadataFilter;
    private MeasConverter measConverter;
    private MeasFilterHandler filterHandler;
    private Mapper mapper;
    private MeasSplitter splitter;
    private XMLValidator validator;
    private VESPublisher vesPublisher;
    private DeliveryHandler deliveryHandler;
    private DynamicConfiguration dynamicConfiguration;
    private HealthCheckHandler healthCheckHandler;
    private int httpPort;
    private int httpsPort;

    private Undertow applicationServer;
    private List<ServerHandler> serverHandlers;
    private Flux<Event> flux;
    private FluxSink<Event> fluxSink;

    /**
     * Creates an instance of the application.
     * @param mappingTemplate path to template used to convert xml to VES.
     * @param xmlSchema path to schema used to verify incoming XML will work with template.
     * @param configHandler instance of the ConfigurationHandler used to acquire config.
     */
    public App(Path mappingTemplate, Path xmlSchema, int httpPort, int httpsPort, ConfigHandler configHandler) {
        try {
            this.mapperConfig = configHandler.getMapperConfig();
        } catch (EnvironmentConfigException | CBSServerError | MapperConfigException e) {
            logger.unwrap().error("Failed to acquire initial configuration, Application cannot start", e);
            throw new IllegalStateException("Config acquisition failed");
        }
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.metadataFilter = new MetadataFilter(mapperConfig);
        this.measConverter = new MeasConverter();
        this.filterHandler = new MeasFilterHandler(measConverter);
        this.mapper = new Mapper(mappingTemplate, this.measConverter);
        this.splitter =  new MeasSplitter(measConverter);
        this.validator = new XMLValidator(xmlSchema);
        this.vesPublisher = new VESPublisher(mapperConfig);
        this.flux = Flux.create(eventFluxSink -> this.fluxSink = eventFluxSink);

        this.flux.onBackpressureDrop(App::handleBackPressure)
                .doOnNext(App::receiveRequest)
                .limitRate(1)
                .parallel()
                .runOn(Schedulers.newParallel(""), 1)
                .doOnNext(event -> MDC.setContextMap(event.getMdc()))
                .filter(this.metadataFilter::filter)
                .filter(event -> App.filterByFileType(this.filterHandler, event, this.mapperConfig))
                .filter(event -> App.validate(this.validator, event, this.mapperConfig))
                .concatMap(event -> App.split(this.splitter,event, this.mapperConfig))
                .filter(events -> App.filter(this.filterHandler, events, this.mapperConfig))
                .concatMap(events -> App.map(this.mapper, events, this.mapperConfig))
                .concatMap(this.vesPublisher::publish)
                .subscribe(event -> App.sendEventProcessed(this.mapperConfig, event));

        this.healthCheckHandler = new HealthCheckHandler();
        this.deliveryHandler = new DeliveryHandler(fluxSink::next);
        this.dynamicConfiguration = new DynamicConfiguration(Arrays.asList(mapperConfig), mapperConfig);
        this.serverHandlers = Arrays.asList(healthCheckHandler, deliveryHandler, dynamicConfiguration);
        try {
            this.applicationServer = server(this.mapperConfig, this.serverHandlers);
        } catch (IOException e) {
            logger.unwrap().error("Failed to create server instance.", e);
            throw new IllegalStateException("Server instantiation failed");
        }
    }

    /**
     * Starts the application server.
     */
    public void start() {
        this.applicationServer.start();
    }

    /**
     * Stops the application server.
     */
    public void stop() {
        this.applicationServer.stop();
    }

    private Undertow server(MapperConfig config, List<ServerHandler> serverHandlers) throws IOException {
        SSLContextFactory sslContextFactory = new SSLContextFactory(config);
        SSLContext sslContext = sslContextFactory.createSSLContext(config);
        SSLContext.setDefault(sslContext);
        Undertow.Builder builder = Undertow.builder();
        if (config.getEnableHttp()) {
            builder.addHttpListener(this.httpPort, "0.0.0.0");
        }
        RoutingHandler routes = new RoutingHandler();
        serverHandlers.forEach(handler -> routes.add(handler.getMethod(), handler.getTemplate(), handler.getHandler()));
        return builder.addHttpsListener(this.httpsPort, "0.0.0.0", sslContext)
                .setHandler(routes)
                .build();
    }

    public static void main(String[] args) {
        new App(mappingTemplate, xmlSchema, HTTP_PORT, HTTPS_PORT, new ConfigHandler()).start();
    }

    public static boolean filterByFileType(MeasFilterHandler filterHandler,Event event, MapperConfig config) {
        boolean hasValidFileName = false;
        try {
            hasValidFileName = filterHandler.filterByFileType(event);
            if(!hasValidFileName) {
                sendEventProcessed(config,event);
            }
        } catch (Exception exception) {
            logger.unwrap().error("Unable to filter by file type", exception);
            sendEventProcessed(config,event);
        }
        return hasValidFileName;
    }

    public static boolean validate(XMLValidator validator, Event event, MapperConfig config) {
        boolean isValidXML = false;
        try {
            isValidXML = validator.validate(event);
            if(!isValidXML) {
                sendEventProcessed(config,event);
            }
        } catch (Exception exception) {
            logger.unwrap().error("Unable to validate XML",exception);
            sendEventProcessed(config,event);
        }
        return isValidXML;
    }

    public static boolean filter(MeasFilterHandler filterHandler, List<Event> events, MapperConfig config) {
        Event event = events.get(0);
        boolean hasMatchingFilter = false;
        try {
            hasMatchingFilter = filterHandler.filterByMeasType(events);
            if(!hasMatchingFilter) {
                sendEventProcessed(config,event);
            }
        } catch (Exception exception) {
            logger.unwrap().error("Unable to filter by Meas Types",exception);
            sendEventProcessed(config,event);
        }
        return hasMatchingFilter;
    }

    public static Flux<List<Event>> map(Mapper mapper, List<Event> events, MapperConfig config) {
        List<Event> mappedEvents  = new ArrayList<>();
        try {
            mappedEvents = mapper.mapEvents(events);
        } catch (Exception exception) {
            logger.unwrap().error("Unable to map XML to VES",exception);
            sendEventProcessed(config,events.get(0));
            return Flux.<List<Event>>empty();
        }
        return Flux.just(mappedEvents);
    }

    public static Flux<List<Event>> split(MeasSplitter splitter, Event event, MapperConfig config) {
        List<Event> splitEvents  = new ArrayList<>();
        try {
            splitEvents = splitter.split(event);
        } catch (Exception exception) {
            logger.unwrap().error("Unable to split MeasCollecFile",exception);
            sendEventProcessed(config,event);
            return Flux.<List<Event>>empty();
        }
        return Flux.just(splitEvents);
    }

    public static void sendEventProcessed(MapperConfig config, Event event) {
      try {
          DataRouterUtils.processEvent(config, event);
      } catch (ProcessEventException exception) {
          logger.unwrap().error("Process event failure", exception);
      }
    }

    /**
     * Takes the exchange from an event, responds with a 429 and un-dispatches the exchange.
     * @param event to be ignored.
     */
    public static void handleBackPressure(@NonNull Event event) {
        logger.unwrap().debug("Event will not be processed, responding with 429");
        event.getHttpServerExchange()
                .setStatusCode(StatusCodes.TOO_MANY_REQUESTS)
                .getResponseSender()
                .send(StatusCodes.TOO_MANY_REQUESTS_STRING);
        event.getHttpServerExchange()
                .unDispatch();
    }

    /**
     * Takes the exchange from an event, responds with a 200 and un-dispatches the exchange.
     * @param event to be received.
     */
    public static void receiveRequest(@NonNull Event event) {
        logger.unwrap().debug("Event will be processed, responding with 200");
        event.getHttpServerExchange()
                .setStatusCode(StatusCodes.OK)
                .getResponseSender()
                .send(StatusCodes.OK_STRING);
        event.getHttpServerExchange()
                .unDispatch();
    }
}
