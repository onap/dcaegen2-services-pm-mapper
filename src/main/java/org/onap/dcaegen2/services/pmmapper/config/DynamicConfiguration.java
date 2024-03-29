/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2022 Nokia.
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

package org.onap.dcaegen2.services.pmmapper.config;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import java.util.List;
import lombok.Data;
import org.onap.dcaegen2.services.pmmapper.exceptions.ReconfigurationException;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.model.ServerResource;
import org.onap.dcaegen2.services.pmmapper.utils.HttpServerExchangeAdapter;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

@Data
public class DynamicConfiguration extends ServerResource {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(DynamicConfiguration.class));
    private static final String RECONFIGURE_ENDPOINT = "/reconfigure";
    private List<Configurable> configurables;
    private MapperConfig originalConfig;
    private ConfigHandler configHandler;

    /**
     * Creates a Dynamic Configuration object with a list of configurable objects.
     * @param configurables list of objects to reconfigure
     * @param originalConfig original config to compare against.
     */
    public DynamicConfiguration(List<Configurable> configurables, MapperConfig originalConfig, ConfigHandler configHandler) {
        super(RECONFIGURE_ENDPOINT);
        this.configurables = configurables;
        this.originalConfig = originalConfig;
        this.configHandler = configHandler;
    }

    private void applyConfiguration(MapperConfig updatedConfig) throws ReconfigurationException {
        for (Configurable configurable : configurables) {
            logger.unwrap().debug("Reconfiguring: {}", configurable);
            configurable.reconfigure(updatedConfig);
        }
    }

    /**
     * Receives requests to pull the latest configuration from CBS.
     * @param httpServerExchange inbound http server exchange.
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        try {
            logger.entering(new HttpServerExchangeAdapter(httpServerExchange));
            boolean reconfigured = reconfigure();
            int responseCode = reconfigured? StatusCodes.OK : StatusCodes.INTERNAL_SERVER_ERROR;
            String responseMessage = reconfigured ? StatusCodes.OK_STRING : StatusCodes.INTERNAL_SERVER_ERROR_STRING;
            httpServerExchange.setStatusCode(responseCode).getResponseSender().send(responseMessage);
        } finally {
            logger.exiting();
        }
    }

    /**
     * @return Boolean to indicate if configuration attempt was successful
     * @throws Exception If environment config cannot be read, or if re-applying the original config fails
     */
    public boolean reconfigure() throws Exception {
        boolean success = true;
        MapperConfig config = configHandler.getMapperConfig();
        if (!this.originalConfig.equals(config)) {
            logger.unwrap().info("Configuration update detected.");
            logger.unwrap().info("Reconfiguring configurables");
            try {
                applyConfiguration(config);
                this.originalConfig = config;
            } catch (ReconfigurationException e) {
                success = false;
                logger.unwrap().error("Failed to apply configuration update, reverting to original config", e);
                applyConfiguration(this.originalConfig);
            }
        }
        return success;
    }
}
