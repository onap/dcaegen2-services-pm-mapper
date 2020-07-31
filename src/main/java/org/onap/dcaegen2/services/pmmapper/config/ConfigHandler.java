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
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSServerError;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.utils.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
import org.onap.dcaegen2.services.pmmapper.utils.RequiredFieldDeserializer;

import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import com.google.gson.GsonBuilder;

/**
 * Handles the retrieval of the component spec-based PM-Mapper Configuration
 * from DCAE.
 */

public class ConfigHandler {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(ConfigHandler.class));
    private RequestSender sender;
    private EnvironmentConfig environmentConfig;

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
        String mapperConfigJson = "";
        String cbsSocketAddress = this.environmentConfig.getCBSHostName() + ":" + this.environmentConfig.getCBSPort();
        String requestURL = "http://" + cbsSocketAddress + "/service_component/" + this.environmentConfig.getServiceName();
        try {
            logger.unwrap().info("Fetching pm-mapper configuration from Configbinding Service");
            mapperConfigJson = sender.send(requestURL);
        } catch (Exception exception) {
            throw new CBSServerError("Error connecting to Configbinding Service: ", exception);
        }
        return convertMapperConfigToObject(mapperConfigJson);
    }

    private MapperConfig convertMapperConfigToObject(String mapperConfigJson) {
        MapperConfig mapperConfig;
        try {
            JsonObject config = new Gson().fromJson(mapperConfigJson, JsonObject.class);
            mapperConfig = new GsonBuilder()
                    .registerTypeAdapter(MapperConfig.class, new RequiredFieldDeserializer<MapperConfig>())
                    .create()
                    .fromJson(config, MapperConfig.class);
        } catch (Exception exception) {
            String exceptionMessage = "Error parsing configuration, mapper config:\n" + mapperConfigJson;
            throw new MapperConfigException(exceptionMessage, exception);
        }
        logger.unwrap().info("Received pm-mapper configuration from ConfigBinding Service");
        logger.unwrap().debug("Mapper configuration:\n{}", mapperConfig);
        return mapperConfig;
    }
}
