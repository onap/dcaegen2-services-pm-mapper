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

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.mapping.Mapper;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.LoggerFactory;
import utils.EventUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@PrepareForTest(MapperConfig.class)
public class MetadataFilterTest {

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(Mapper.class));

    private static MapperConfig validConfig;
    private static MapperConfig emptyFilterConfig;

    private static String validConfigFileContents;
    private static String noFilterConfigFileContents;

    private MetadataFilter metadataFilter;

    private static final Path validMetadata = Paths.get("src/test/resources/valid_metadata.json");
    private static final Path incorrectMetadata = Paths.get("src/test/resources/incorrect_metadata.json");

    private static final Path validConfigPath = Paths.get("src/test/resources/valid_mapper_config.json");
    private static final Path noFilterConfigPath = Paths.get("src/test/resources/no_filter_mapper_config.json");

    private static final Path dataDirectory = Paths.get("src/test/resources/xml_validator_test/test_data/");


    @BeforeEach
    void setup() throws Exception {
        validConfigFileContents = new String(Files.readAllBytes(validConfigPath));
        validConfig = new Gson().fromJson(validConfigFileContents, MapperConfig.class);

        noFilterConfigFileContents = new String(Files.readAllBytes(noFilterConfigPath));
        emptyFilterConfig = new Gson().fromJson(noFilterConfigFileContents, MapperConfig.class);

        metadataFilter = new MetadataFilter(this.validConfig);
    }


    @ParameterizedTest
    @MethodSource("getEventsWithValidMetadata")
    void testValidMetadataPass(Event testEvent) {
        assertTrue(metadataFilter.filter(testEvent));
    }

    @ParameterizedTest
    @MethodSource("getEventsWithValidMetadata")
    void testEmptyFilterPass(Event testEvent) {
        metadataFilter.config = emptyFilterConfig;
        assertTrue(metadataFilter.filter(testEvent));
    }

    @ParameterizedTest
    @MethodSource("getEventsWithInvalidMetadata")
    void testInvalidMetadataFail(Event testEvent) {
        assertFalse(metadataFilter.filter(testEvent));
    }



    private static List<Event> getEventsWithValidMetadata() throws IOException {
        Path validDataDirectory = Paths.get(dataDirectory.toString() + "/valid/");
        return EventUtils.eventsFromDirectory(validDataDirectory, validMetadata);
    }

    private static List<Event> getEventsWithInvalidMetadata() throws IOException {
        Path validDataDirectory = Paths.get(dataDirectory.toString() + "/valid/");
        return EventUtils.eventsFromDirectory(validDataDirectory, incorrectMetadata);
    }
}