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

import org.onap.dcaegen2.services.pmmapper.exceptions.CBSConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSServerError;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.model.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
import org.onap.dcaegen2.services.pmmapper.utils.RequiredFieldDeserializer;

import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.LoggerFactory;
import com.google.gson.GsonBuilder;

/**
 * Handles the retrieval of the component spec-based PM-Mapper Configuration
 * from DCAE.
 */

public class ConfigHandler {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(ConfigHandler.class));
    private RequestSender sender;

    /**
     * Creates a ConfigHandler.
     */
    public ConfigHandler() {
        this(new RequestSender());
    }

    /**
     * @see ConfigHandler#ConfigHandler()
     * @param sender A RequestSender
     */
    public ConfigHandler(RequestSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves PM-Mapper Configuration from DCAE's ConfigBinding Service.
     *
     * @throws EnvironmentConfigException
     * @throws ConsulServerError
     * @throws CBSConfigException
     * @throws CBSServerError
     * @throws MapperConfigException
     */
    public MapperConfig getMapperConfig() throws EnvironmentConfigException,
            CBSServerError, MapperConfigException {
        String mapperConfigJson = "";
        String cbsSocketAddress = EnvironmentConfig.getCBSHostName() + ":" + EnvironmentConfig.getCBSPort();
        String requestURL = "http://" + cbsSocketAddress + "/service_component/" + EnvironmentConfig.getServiceName();
        try {
            logger.unwrap().info(ONAPLogConstants.Markers.ENTRY, "Fetching pm-mapper configuration from Configbinding Service");
            mapperConfigJson = sender.send(requestURL);
        } catch (Exception exception) {
            throw new CBSServerError("Error connecting to Configbinding Service: ", exception);
        } finally {
            logger.unwrap().info(ONAPLogConstants.Markers.EXIT, "Received pm-mapper configuration from ConfigBinding Service:\n{}", mapperConfigJson);
        }

        return convertMapperConfigToObject(mapperConfigJson);
    }

    private MapperConfig convertMapperConfigToObject(String mapperConfigJson) throws MapperConfigException {
        MapperConfig mapperConfig;
        try {
            mapperConfig = new GsonBuilder()
                    .registerTypeAdapter(MapperConfig.class, new RequiredFieldDeserializer<MapperConfig>())
                    .create()
                    .fromJson(mapperConfigJson, MapperConfig.class);
        } catch (Exception exception) {
            throw new MapperConfigException("Error parsing mapper configuration:\n{}" + mapperConfigJson, exception);
        }

        logger.unwrap().debug("Mapper configuration:\n{}", mapperConfig);
        return mapperConfig;
    }
}
