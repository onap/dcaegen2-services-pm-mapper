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
package org.onap.dcaegen2.services.pmmapper.model;

import java.util.Optional;

import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;

public class EnvironmentConfig {

    public static final int DEFAULT_CBS_PORT = 10000;
    public static final String ENV_CBS_HOST_KEY = "CONFIG_BINDING_SERVICE_SERVICE_HOST";
    public static final String ENV_CBS_PORT_KEY = "CONFIG_BINDING_SERVICE_SERVICE_PORT";
    public static final String ENV_SERVICE_NAME_KEY = "HOSTNAME";

    public static String getServiceName() throws EnvironmentConfigException {
        return Optional.ofNullable(System.getenv(ENV_SERVICE_NAME_KEY))
                .orElseThrow(() -> new EnvironmentConfigException(
                        ENV_SERVICE_NAME_KEY+ " environment variable must be defined prior to pm-mapper initialization."));
    }

    public static String getCBSHostName() throws EnvironmentConfigException {
        return Optional.ofNullable(System.getenv(ENV_CBS_HOST_KEY))
                .orElseThrow(() -> new EnvironmentConfigException(
                        ENV_CBS_HOST_KEY+ " environment variable must be defined prior to pm-mapper initialization."));
    }

    public static Integer getCBSPort() throws EnvironmentConfigException {
        Integer port = DEFAULT_CBS_PORT;
        try {
            port = Optional.ofNullable(System.getenv(ENV_CBS_PORT_KEY))
                    .map(Integer::valueOf).orElse(DEFAULT_CBS_PORT);
        } catch (NumberFormatException e) {
            throw new EnvironmentConfigException(ENV_CBS_PORT_KEY + " must be valid: " + port);
        }
        return port;
    }
}
