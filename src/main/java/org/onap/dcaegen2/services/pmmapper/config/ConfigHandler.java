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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.vavr.control.Option;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSServerError;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.mapping.Mapper;
import org.onap.dcaegen2.services.pmmapper.utils.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
import org.onap.dcaegen2.services.pmmapper.utils.RequiredFieldDeserializer;

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;

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
    private RequestSender sender;
    private EnvironmentConfig environmentConfig;
    private MapperConfig mapperConfig;

    /**
     * Creates a ConfigHandler.
     */
    public ConfigHandler() {
        this(new RequestSender(), new EnvironmentConfig());
    }

    /**
     * @see ConfigHandler#ConfigHandler()
     * @param sender A RequestSender
     */
    public ConfigHandler(RequestSender sender, EnvironmentConfig environmentConfig) {
        this.sender = sender;
        this.environmentConfig = environmentConfig;
    }

    /**
     * Retrieves PM-Mapper Configuration from DCAE's ConfigBinding Service.
     *
     * @throws EnvironmentConfigException
     */
    public MapperConfig getMapperConfig() throws EnvironmentConfigException {

        //Todo add single configuration request -> reconfiguration is handled in other PM-Mapper Class
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();
        final CbsRequest request = CbsRequests.getConfiguration(diagnosticContext);

        final CbsClientConfiguration config = CbsClientConfiguration.fromEnvironment();

        final MapperConfig mapperConfig1;
        CbsClientFactory.createCbsClient(config)
            .flatMap(cbsClient -> cbsClient.get(request))
            .subscribe(
                this::handleConfigurationFromConsul,
                this::handleError
            );

        //Todo If mapperConfig == null then return null and log that Configuration wasn't loaded from Consul
        if (mapperConfig == null) {
            logger.unwrap().error("Mapper Config is null");
        }
        return mapperConfig;
    }

    public MapperConfig getInitialConfiguration() {
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();
        final CbsRequest request = CbsRequests.getConfiguration(diagnosticContext);

        final CbsClientConfiguration config = CbsClientConfiguration.fromEnvironment();
        JsonObject jsonObject = CbsClientFactory.createCbsClient(config)
            .flatMap(cbsClient -> cbsClient.get(request))
            .block();
        handleConfigurationFromConsul(jsonObject);
        return mapperConfig;
    }

    void handleConfigurationFromConsul(JsonObject jsonObject) {
        //Todo remove jsonObject From logs
//        logger.unwrap().info("Configuration update from Consul {}", jsonObject);
        logger.unwrap().info("Configuration update from Consul");

        try {
            mapperConfig = new GsonBuilder()
                .registerTypeAdapter(MapperConfig.class, new RequiredFieldDeserializer<MapperConfig>())
                .create()
                .fromJson(jsonObject, MapperConfig.class);
        } catch (Exception exception) {
            String exceptionMessage = "Error parsing configuration, mapper config:\n" + jsonObject.getAsString();
            throw new MapperConfigException(exceptionMessage, exception);
        }
        logger.unwrap().info("Received pm-mapper configuration from ConfigBinding Service");
        logger.unwrap().info("Mapper configuration:\n{}", mapperConfig);
    }

    private void handleError(Throwable throwable) {
        logger.unwrap().error("Unexpected error occurred during fetching configuration from Consul", throwable);
    }
}
