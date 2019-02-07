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

    private static Integer consulPort = 8500;

    public static String getConsulHost() throws EnvironmentConfigException {
        return Optional.ofNullable(System.getenv("CONSUL_SERVER_UI_SERVICE_HOST"))
                .orElseThrow(() -> new EnvironmentConfigException(
                        "$CONSUL_HOST environment variable must be defined prior to pm-mapper initialization"));
    }

    public static Integer getConsultPort() throws EnvironmentConfigException {
        Integer port = consulPort;
        try {
            port = Optional.ofNullable(System.getenv("CONSUL_SERVER_UI_SERVICE_PORT_CONSUL_UI"))
                    .map(Integer::valueOf)
                    .orElse(consulPort);
        } catch (NumberFormatException e) {
            throw new EnvironmentConfigException("CONSUL_PORT must be valid: " + port);
        }
        return port;

    }

    public static String getCbsName() throws EnvironmentConfigException {
        return Optional.ofNullable(System.getenv("CONFIG_BINDING_SERVICE"))
                .orElseThrow(() -> new EnvironmentConfigException(
                        "$CONFIG_BINDING_SERVICE environment variable must be defined prior to pm-mapper initialization."));
    }

    public static String getServiceName() throws EnvironmentConfigException {
        return Optional.ofNullable(System.getenv("HOSTNAME"))
                .orElseThrow(() -> new EnvironmentConfigException(
                        "$HOSTNAME environment variable must be defined prior to pm-mapper initialization."));
    }
}
