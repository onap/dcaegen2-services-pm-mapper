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

package org.onap.dcaegen2.services.pmmapper.filtering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.powermock.core.classloader.annotations.PrepareForTest;
import utils.ConfigUtils;
import utils.EventUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@PrepareForTest(MapperConfig.class)
public class MetadataFilterTest {

    private static final String VALID_MAPPER_CONFIG_FILE = "valid_mapper_config.json";
    private static final String NO_FILTER_CONFIG_FILE = "no_filter_mapper_config.json";
    private static final String MULTIPLE_FILTER_CONFIG_FILE = "multiple_filter_mapper_config.json";
    private static final Path DATA_DIRECTORY = Paths.get("src/test/resources/xml_validator_test/test_data/");
    private static final Path VALID_METADATA = Paths.get("src/test/resources/valid_metadata.json");
    private static final Path INCORRECT_METADATA = Paths.get("src/test/resources/incorrect_metadata.json");

    private MetadataFilter metadataFilter;
    private static MapperConfig validConfig;
    private static MapperConfig noFilterConfig;
    private static MapperConfig multipleFilterConfig;

    @BeforeEach
    void setup() throws Exception {
        validConfig = ConfigUtils.getMapperConfigFromFile(VALID_MAPPER_CONFIG_FILE);
        noFilterConfig = ConfigUtils.getMapperConfigFromFile(NO_FILTER_CONFIG_FILE);
        multipleFilterConfig = ConfigUtils.getMapperConfigFromFile(MULTIPLE_FILTER_CONFIG_FILE);
        metadataFilter = new MetadataFilter(validConfig);
    }

    @ParameterizedTest
    @MethodSource("getEventsWithValidMetadata")
    void testValidMetadataPass(Event testEvent) {
        assertTrue(metadataFilter.filter(testEvent));
    }

    @ParameterizedTest
    @MethodSource("getEventsWithValidMetadata")
    void testEmptyFilterPass(Event testEvent) {
        metadataFilter.config = noFilterConfig;
        assertTrue(metadataFilter.filter(testEvent));
    }

    @ParameterizedTest
    @MethodSource("getEventsWithValidMetadata")
    void testMultipleFilterPass(Event testEvent) {
        metadataFilter.config = multipleFilterConfig;
        assertTrue(metadataFilter.filter(testEvent));
    }

    @ParameterizedTest
    @MethodSource("getEventsWithInvalidMetadata")
    void testInvalidMetadataFail(Event testEvent) {
        assertFalse(metadataFilter.filter(testEvent));
    }

    private static List<Event> getEventsWithValidMetadata() throws IOException {
        Path validDataDirectory = Paths.get(DATA_DIRECTORY.toString() + "/valid/");
        return EventUtils.eventsFromDirectory(validDataDirectory, VALID_METADATA);
    }

    private static List<Event> getEventsWithInvalidMetadata() throws IOException {
        Path validDataDirectory = Paths.get(DATA_DIRECTORY.toString() + "/valid/");
        return EventUtils.eventsFromDirectory(validDataDirectory, INCORRECT_METADATA);
    }
}