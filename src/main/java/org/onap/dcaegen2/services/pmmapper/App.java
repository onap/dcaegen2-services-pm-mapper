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

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.util.StatusCodes;

import lombok.NonNull;
import org.onap.dcaegen2.services.pmmapper.config.ConfigHandler;
import org.onap.dcaegen2.services.pmmapper.config.Configurable;
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

public class App {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(App.class));
    private static Path mappingTemplate = Paths.get("/opt/app/pm-mapper/etc/mapping.ftl");
    private static Path xmlSchema = Paths.get("/opt/app/pm-mapper/etc/measCollec_plusString.xsd");
    private static FluxSink<Event> fluxSink;

    public static void main(String[] args) throws EnvironmentConfigException, CBSServerError, MapperConfigException, IOException {
        Flux<Event> flux = Flux.create(eventFluxSink -> fluxSink = eventFluxSink);
        HealthCheckHandler healthCheckHandler = new HealthCheckHandler();
        MapperConfig mapperConfig = new ConfigHandler().getMapperConfig();
        MetadataFilter metadataFilter = new MetadataFilter(mapperConfig);
        MeasConverter measConverter = new MeasConverter();
        MeasFilterHandler filterHandler = new MeasFilterHandler(measConverter);
        Mapper mapper = new Mapper(mappingTemplate, measConverter);
        MeasSplitter splitter = new MeasSplitter(measConverter);
        XMLValidator validator = new XMLValidator(xmlSchema);
        VESPublisher vesPublisher = new VESPublisher(mapperConfig);

        flux.onBackpressureDrop(App::handleBackPressure)
                .doOnNext(App::receiveRequest)
                .limitRate(1)
                .parallel()
                .runOn(Schedulers.newParallel(""), 1)
                .doOnNext(event -> MDC.setContextMap(event.getMdc()))
                .filter(metadataFilter::filter)
                .filter(event -> App.filterByFileType(filterHandler, event, mapperConfig))
                .filter(event -> App.validate(validator, event, mapperConfig))
                .concatMap(event -> App.split(splitter,event, mapperConfig))
                .filter(events -> App.filter(filterHandler, events, mapperConfig))
                .concatMap(events -> App.map(mapper, events, mapperConfig))
                .concatMap(vesPublisher::publish)
                .subscribe(event -> App.sendEventProcessed(mapperConfig, event));

        DeliveryHandler deliveryHandler = new DeliveryHandler(fluxSink::next);
        ArrayList<Configurable> configurables = new ArrayList<>();
        configurables.add(mapperConfig);
        DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(configurables, mapperConfig);

        Undertow.Builder builder = Undertow.builder();

        SSLContextFactory sslContextFactory = new SSLContextFactory(mapperConfig);
        SSLContext sslContext = sslContextFactory.createSSLContext(mapperConfig);
        SSLContext.setDefault(sslContext);

        if(mapperConfig.getEnableHttp()) {
            builder.addHttpListener(8081, "0.0.0.0");
        }

        builder.addHttpsListener(8443, "0.0.0.0", sslContext)
                .setHandler(Handlers.routing()
                        .add("put", "/delivery/{filename}", deliveryHandler)
                        .add("get", "/healthcheck", healthCheckHandler)
                        .add("get", "/reconfigure", dynamicConfiguration))
                .build().start();
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
