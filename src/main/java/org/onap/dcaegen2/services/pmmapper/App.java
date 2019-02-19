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

import org.onap.dcaegen2.services.pmmapper.config.ConfigHandler;
import org.onap.dcaegen2.services.pmmapper.datarouter.DataRouterSubscriber;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSServerError;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.TooManyTriesException;
import org.onap.dcaegen2.services.pmmapper.mapping.Mapper;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.healthcheck.HealthCheckHandler;
import org.onap.dcaegen2.services.pmmapper.utils.XMLValidator;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(App.class));
    private static Path mappingTemplate = Paths.get("/opt/app/pm-mapper/etc/mapping.ftl");
    private static Path xmlSchema = Paths.get("/opt/app/pm-mapper/etc/measCollec_plusString.xsd");

    public static void main(String[] args) throws InterruptedException, TooManyTriesException, CBSConfigException, EnvironmentConfigException, CBSServerError, MapperConfigException {
        HealthCheckHandler healthCheckHandler = new HealthCheckHandler();
        Mapper mapper = new Mapper(mappingTemplate);
        XMLValidator validator = new XMLValidator(xmlSchema);
        DataRouterSubscriber dataRouterSubscriber = new DataRouterSubscriber(event -> {
            event.getHttpServerExchange().unDispatch();
            event.getHttpServerExchange().getResponseSender().send(StatusCodes.OK_STRING);
            MDC.setContextMap(event.getMdc());
            if(!validator.validate(event)){
                logger.unwrap().info("Event failed validation against schema.");
            } else {
                String ves = mapper.map(event);
                logger.unwrap().info("Mapped Event: {}", ves);
            }
        });
        MapperConfig mapperConfig = new ConfigHandler().getMapperConfig();
        dataRouterSubscriber.start(mapperConfig);

        Undertow.builder()
                .addHttpListener(8081, "0.0.0.0")
                .setHandler(Handlers.routing().add("put", "/sub", dataRouterSubscriber)
                .add("get", "/healthcheck", healthCheckHandler))
                .build().start();
    }
}
