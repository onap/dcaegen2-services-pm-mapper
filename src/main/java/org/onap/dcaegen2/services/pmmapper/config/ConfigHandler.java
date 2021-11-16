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
package org.onap.dcaegen2.services.pmmapper.config;

import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequiredFieldDeserializer;

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;

import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import com.google.gson.GsonBuilder;
import reactor.core.publisher.Mono;

/**
 * Handles the retrieval of the component spec-based PM-Mapper Configuration
 * from DCAE.
 */

public class ConfigHandler {

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(ConfigHandler.class));

    private MapperConfig mapperConfig;
    private final CbsClient cbsClient;
    private final CbsRequest cbsRequest;

    /**
     * Creates a ConfigHandler based on Cbs Client and Cbs Request provided by DCAE SDK
     * @param CbsClient A Cbs Client
     * @param CbsRequest A Cbs Request
     */
    public ConfigHandler(CbsClient cbsClient, CbsRequest cbsRequest){
        this.cbsClient = cbsClient;
        this.cbsRequest = cbsRequest;
    }

    /**
     * Retrieves PM-Mapper Configuration from DCAE's ConfigBinding Service.
     *
     * @throws EnvironmentConfigException
     */
    public MapperConfig getMapperConfig() throws EnvironmentConfigException {

        Mono.just(cbsClient)
            .flatMap(client -> client.get(cbsRequest))
            .subscribe(
                this::handleConfigurationFromConsul,
                this::handleError
            );

        if (mapperConfig == null) {
            logger.unwrap().error("Mapper configuration is not initialized");
            throw new EnvironmentConfigException("Mapper configuration is not initialized");
        }
        return mapperConfig;
    }

    public MapperConfig getInitialConfiguration() {
        logger.unwrap().info("Attempt to get initial configuration");
        try {
            JsonObject jsonObject = Mono.just(cbsClient)
                .flatMap(client -> client.get(cbsRequest))
                .block();
            handleConfigurationFromConsul(jsonObject);
        } catch (Exception e) {
            handleError(e);
            //Todo throw config exception
//            throw new EnvironmentConfigException(e);
        }
        return mapperConfig;
    }

    void handleConfigurationFromConsul(JsonObject jsonObject) {
        logger.unwrap().info("Attempt to process configuration object");

        try {
            mapperConfig = new GsonBuilder()
                .registerTypeAdapter(MapperConfig.class, new RequiredFieldDeserializer<MapperConfig>())
                .create()
                .fromJson(jsonObject, MapperConfig.class);
        } catch (Exception exception) {
            String exceptionMessage = "Error parsing configuration, mapper config:\n" + mapperConfig;
            throw new MapperConfigException(exceptionMessage, exception);
        }
        logger.unwrap().info("PM-mapper configuration processed successful");
        logger.unwrap().info("Mapper configuration:\n{}", mapperConfig);
    }

    private void handleError(Throwable throwable) {
        logger.unwrap().error("Unexpected error occurred during fetching configuration", throwable);
    }
}
