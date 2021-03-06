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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;

public class FilesProcessingConfigTest {

    private static final String ENV_LIMIT_RATE = "PROCESSING_LIMIT_RATE";

    private static final String ENV_THREADS_MULTIPLIER = "THREADS_MULTIPLIER";
    private static final String ENV_PROCESSING_THREADS_COUNT = "PROCESSING_THREADS_COUNT";
    private static final String THREADS_4 = "4";
    private static final String MULTIPLIER_3 = "3";

    private static final int EXPECTED_4 = 4;
    private static final int EXPECTED_12 = 12;

    private final EnvironmentReader mockEnvironmentReader = mock(EnvironmentReader.class);
    private FilesProcessingConfig filesProcessingConfig;

    @Test
    public void shouldReturnCorrectValue_whenVariableIsSet() throws EnvironmentConfigException {
        when(mockEnvironmentReader.getVariable(ENV_LIMIT_RATE)).thenReturn("128");
        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        int limitRate = filesProcessingConfig.getLimitRate();

        assertEquals(128, limitRate);
    }

    @Test
    public void shouldThrowEnvironmentConfigException_whenVariableHasWrongValue() {
        when(mockEnvironmentReader.getVariable(ENV_LIMIT_RATE)).thenReturn("not-an-int");
        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        String expectedMessage = "PROCESSING_LIMIT_RATE environment variable has incorrect value.\n";
        String causeMessage = "For input string: \"not-an-int\"";

        Throwable exception = assertThrows(EnvironmentConfigException.class, () -> filesProcessingConfig.getLimitRate());
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(causeMessage, exception.getCause().getMessage());
    }

    @Test
    public void shouldReturnDefaultValue_whenVariableIsNotSet() throws EnvironmentConfigException {
        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        int limitRate = filesProcessingConfig.getLimitRate();

        assertEquals(1, limitRate);
    }

    @Test
    public void shouldReturnCorrectThreadsCount_whenVariableIsSet() throws EnvironmentConfigException {
        when(mockEnvironmentReader.getVariable(ENV_PROCESSING_THREADS_COUNT)).thenReturn(THREADS_4);
        when(mockEnvironmentReader.getVariable(ENV_THREADS_MULTIPLIER)).thenReturn(MULTIPLIER_3);

        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        int threadsCount = filesProcessingConfig.getThreadsCount();

        assertEquals(EXPECTED_12, threadsCount);
    }

    @Test
    public void shouldReturnCorrectThreadsCount_whenVariableMultiplierIsNotSet() throws EnvironmentConfigException {

        when(mockEnvironmentReader.getVariable(ENV_PROCESSING_THREADS_COUNT)).thenReturn(THREADS_4);

        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        int threadsCount = filesProcessingConfig.getThreadsCount();

        assertEquals(EXPECTED_4, threadsCount);
    }

    @Test
    public void shouldReturnCorrectThreadsCount_whenVariableThreadsIsNotSet() throws EnvironmentConfigException {
        when(mockEnvironmentReader.getVariable(ENV_THREADS_MULTIPLIER)).thenReturn(MULTIPLIER_3);

        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        int threadsCount = filesProcessingConfig.getThreadsCount();
        int expected = Runtime.getRuntime().availableProcessors() * Integer.parseInt(MULTIPLIER_3);

        assertEquals(expected, threadsCount);
    }

    @Test
    public void shouldReturnCorrectThreadsCount_whenVariableThreadsAndMultiplierIsNotSet()
        throws EnvironmentConfigException {
        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        int threadsCount = filesProcessingConfig.getThreadsCount();
        int expected = Runtime.getRuntime().availableProcessors();

        assertEquals(expected, threadsCount);
    }

    @Test
    public void shouldThrowEnvironmentConfigException_whenProcessingThreadsVariableHasWrongValue() {
        when(mockEnvironmentReader.getVariable(ENV_PROCESSING_THREADS_COUNT)).thenReturn("not-an-int");
        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        String expectedMessage = "PROCESSING_THREADS_COUNT environment variable has incorrect value.\n";
        String causeMessage = "For input string: \"not-an-int\"";

        Throwable exception = assertThrows(EnvironmentConfigException.class, () -> filesProcessingConfig.getThreadsCount());
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(causeMessage, exception.getCause().getMessage());
    }

    @Test
    public void shouldThrowEnvironmentConfigException_whenThreadsMultiplierVariableHasWrongValue() {
        when(mockEnvironmentReader.getVariable(ENV_THREADS_MULTIPLIER)).thenReturn("not-an-int");
        filesProcessingConfig = new FilesProcessingConfig(mockEnvironmentReader);
        String expectedMessage = "THREADS_MULTIPLIER environment variable has incorrect value.\n";
        String causeMessage = "For input string: \"not-an-int\"";

        Throwable exception = assertThrows(EnvironmentConfigException.class, () -> filesProcessingConfig.getThreadsCount());
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(causeMessage, exception.getCause().getMessage());
    }
}
