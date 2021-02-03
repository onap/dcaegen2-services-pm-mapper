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
 */
public class FilesProcessingConfig {

    private static final String ENV_LIMIT_RATE = "PROCESSING_LIMIT_RATE";
    private static final int DEFAULT_LIMIT_RATE = 1;
    private static final String ENV_THREADS_MULTIPLIER = "THREADS_MULTIPLIER";
    private static final String ENV_PROCESSING_THREADS_COUNT = "PROCESSING_THREADS_COUNT";
    private static final int DEFAULT_MULTIPLIER = 1;

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(
        LoggerFactory.getLogger(FilesProcessingConfig.class));
    private final EnvironmentReader environmentReader;

    /**
     * Creates a FilesProcessingConfig
     */
    public FilesProcessingConfig(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    /**
     * Provides reactor limit rate value from environment variable.
     *
     * @throws EnvironmentConfigException
     * @returns value of limit rate
     */
    public int getLimitRate() throws EnvironmentConfigException {
        logger.unwrap().info("Trying to read " + ENV_LIMIT_RATE + " env.");
        try {
            return Optional.ofNullable(environmentReader.getVariable(ENV_LIMIT_RATE))
                .map(Integer::valueOf)
                .orElseGet(this::getDefaultLimitRate);
        } catch (NumberFormatException exception) {
            throw new EnvironmentConfigException(ENV_LIMIT_RATE + " environment variable has incorrect value.\n",
                exception);
        }
    }

    /**
     * Provides reactor parallel threads count from environment variable.
     *
     * @throws EnvironmentConfigException
     * @returns value of threads count
     */
    public int getThreadsCount() throws EnvironmentConfigException {
        logger.unwrap().info("Attempt to read threads configuration");
        int processingThreadsCount = getProcessingThreadsCount();
        int threadsMultiplier = getThreadsMultiplier();
        int processingThreadsAmount = processingThreadsCount * threadsMultiplier;

        logger.unwrap().info(
            "Processing threads configuration: Processing threads count - {}, Processing threads multiplier - {} ",
            processingThreadsCount, threadsMultiplier);
        logger.unwrap().info("Amount of files processing threads: {} ", processingThreadsAmount);

        return processingThreadsAmount;
    }

    private int getDefaultLimitRate() {
        logger.unwrap()
            .info(ENV_LIMIT_RATE + " env not present. Setting limit rate to default value: " + DEFAULT_LIMIT_RATE);
        return DEFAULT_LIMIT_RATE;
    }

    private int getThreadsMultiplier() throws EnvironmentConfigException {
        try {
            return Optional.ofNullable(environmentReader.getVariable(ENV_THREADS_MULTIPLIER))
                .map(Integer::valueOf)
                .orElseGet(this::getDefaultMultiplier);
        } catch (NumberFormatException exception) {
            throw new EnvironmentConfigException(
                ENV_THREADS_MULTIPLIER + " environment variable has incorrect value.\n", exception);
        }
    }

    private int getDefaultMultiplier() {
        logger.unwrap().info(ENV_THREADS_MULTIPLIER +
            " env not present. Setting multiplier to default value: " + DEFAULT_MULTIPLIER);
        return DEFAULT_MULTIPLIER;
    }

    private int getProcessingThreadsCount() throws EnvironmentConfigException {
        try {
            return Optional.ofNullable(environmentReader.getVariable(ENV_PROCESSING_THREADS_COUNT))
                .map(Integer::valueOf)
                .orElseGet(this::getDefaultThreadsCount);
        } catch (NumberFormatException exception) {
            throw new EnvironmentConfigException(
                ENV_PROCESSING_THREADS_COUNT + " environment variable has incorrect value.\n", exception);
        }
    }

    private int getDefaultThreadsCount() {
        int defaultThreadsCount = Runtime.getRuntime().availableProcessors();
        logger.unwrap().info(ENV_PROCESSING_THREADS_COUNT +
                " env not present. Setting threads count to available cores: " + defaultThreadsCount);
        return defaultThreadsCount;
    }
}
