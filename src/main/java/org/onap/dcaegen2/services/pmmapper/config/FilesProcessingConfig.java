/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia.
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

import java.util.Optional;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

/**
 * Provides external configuration for files processing
 *
 */
public class FilesProcessingConfig {

    private static final String ENV_LIMIT_RATE = "PROCESSING_LIMIT_RATE";
    private static final int DEFAULT_LIMIT_RATE = 1;
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(FilesProcessingConfig.class));
    private EnvironmentReader environmentReader;

    /**
     * Creates a FilesProcessingConfig
     */
    public FilesProcessingConfig(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    /**
     * Provides reactor limit rate value from environment variable.
     * @returns value of limit rate
     * @throws EnvironmentConfigException
     */
    public int getLimitRate() throws EnvironmentConfigException {
        logger.unwrap().info("Trying to read " + ENV_LIMIT_RATE + " env.");
        try {
            return Optional.ofNullable(environmentReader.getVariable(ENV_LIMIT_RATE))
                .map(this::parseIntegerValue)
                .orElseGet(this::getDefaultLimitRate);
        } catch (NumberFormatException exception) {
            throw new EnvironmentConfigException(
                ENV_LIMIT_RATE + " environment variable has incorrect value.\n"
                    , exception);
        }
    }

    private Integer parseIntegerValue(String val) throws NumberFormatException {
        Integer value = Integer.valueOf(val);
        logger.unwrap().info(ENV_LIMIT_RATE + " value is: " + value);
        return value;
    }

    private int getDefaultLimitRate() {
        logger.unwrap().info(ENV_LIMIT_RATE + " env not present. Setting limit rate to default value: " + DEFAULT_LIMIT_RATE);
        return DEFAULT_LIMIT_RATE;
    }
}
