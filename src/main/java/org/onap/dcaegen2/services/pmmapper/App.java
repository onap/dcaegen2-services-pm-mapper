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
import org.onap.dcaegen2.services.pmmapper.datarouter.DataRouterSubscriber;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSServerError;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.TooManyTriesException;
import org.onap.dcaegen2.services.pmmapper.filtering.MetadataFilter;
import org.onap.dcaegen2.services.pmmapper.mapping.Mapper;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.healthcheck.HealthCheckHandler;
import org.onap.dcaegen2.services.pmmapper.utils.XMLValidator;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(App.class));
    private static Path mappingTemplate = Paths.get("/opt/app/pm-mapper/etc/mapping.ftl");
    private static Path xmlSchema = Paths.get("/opt/app/pm-mapper/etc/measCollec_plusString.xsd");
    private static FluxSink<Event> fluxSink;

    public static void main(String[] args) throws InterruptedException, TooManyTriesException, CBSConfigException, EnvironmentConfigException, CBSServerError, MapperConfigException {
        Flux<Event> flux = Flux.create(eventFluxSink -> fluxSink = eventFluxSink);
        HealthCheckHandler healthCheckHandler = new HealthCheckHandler();

        MapperConfig mapperConfig = new ConfigHandler().getMapperConfig();

        MetadataFilter metadataFilter = new MetadataFilter(mapperConfig);
        Mapper mapper = new Mapper(mappingTemplate);
        XMLValidator validator = new XMLValidator(xmlSchema);
        flux.onBackpressureDrop(App::handleBackPressure)
                .doOnNext(App::receiveRequest)
                .limitRate(1)
                .parallel()
                .runOn(Schedulers.newParallel(""), 1)
                .doOnNext(event -> MDC.setContextMap(event.getMdc()))
                .filter(metadataFilter::filter)
                .filter(validator::validate)
                .map(mapper::map)
                .subscribe(event -> logger.unwrap().info("Event Processed"));

        DataRouterSubscriber dataRouterSubscriber = new DataRouterSubscriber(fluxSink::next);
        dataRouterSubscriber.start(mapperConfig);

        Undertow.builder()
                .addHttpListener(8081, "0.0.0.0")
                .setHandler(Handlers.routing().add("put", "/delivery/{filename}", dataRouterSubscriber)
                        .add("get", "/healthcheck", healthCheckHandler))
                .build().start();
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
