/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.onap.dcaegen2.services.pmmapper.healthcheck;

import org.onap.dcaegen2.services.pmmapper.model.ServerResource;
import org.onap.dcaegen2.services.pmmapper.utils.HttpServerExchangeAdapter;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class HealthCheckHandler extends ServerResource {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(HealthCheckHandler.class));
    private static final String HEALTHCHECK_ENDPOINT = "/healthcheck";

    public HealthCheckHandler() {
        super(HEALTHCHECK_ENDPOINT);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        try {
            logger.entering(new HttpServerExchangeAdapter(exchange));
            exchange.setStatusCode(StatusCodes.OK)
                    .getResponseSender()
                    .send(StatusCodes.OK_STRING);
            logger.unwrap().info("Healthcheck request successful");
        } finally {
            logger.exiting();
        }
    }
}
