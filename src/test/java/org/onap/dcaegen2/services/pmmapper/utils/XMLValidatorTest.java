/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 - 2020 Nordix Foundation.
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

package org.onap.dcaegen2.services.pmmapper.utils;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import io.undertow.server.HttpServerExchange;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import utils.ArgumentCreator;
import utils.EventUtils;


@ExtendWith(MockitoExtension.class)
class XMLValidatorTest {
    private static final Path dataDirectory = Paths.get("src/test/resources/xml_validator_test/test_data/");
    private static final Path schemas = Paths.get("src/main/resources/schemas/");
    private XMLValidator objUnderTest;

    @BeforeEach
    void setup() {
        objUnderTest = new XMLValidator(schemas);
    }

    @Test
    void testBadSchema() {
        assertThrows(IllegalArgumentException.class, () ->
                new XMLValidator(Paths.get("src/test/resources/mapping_data/valid_data/meas_type_and_r.xml")));
    }

    @Test
    void testNullInput() {
        assertThrows(NullPointerException.class, () -> objUnderTest.validate(null));
    }

    @Test
    void testInvalidSchemaDirectory() {
        assertThrows(IllegalArgumentException.class, () -> new XMLValidator(Paths.get("fake dir")));
    }

    @Test
    void testInvalidSchemaFormat() {
        assertThrows(IllegalArgumentException.class, () -> new XMLValidator(Paths.get("src/test/resources/invalid_configs")));
    }

    @ParameterizedTest
    @MethodSource("getEvents")
    void testXmlValidation(boolean validity, Event testEvent) {
        assertEquals(validity, objUnderTest.validate(testEvent));
    }

    private static List<Arguments> getEvents() {
        ArgumentCreator creator = (Path path, EventMetadata metadata) -> {
            Path props = Paths.get(path.toString()+"/validity.props");
            Path testEventPath = Paths.get(path.toString()+"/test.xml");
            Properties validityProps = new Properties();
            try {
                validityProps.load(new FileInputStream(props.toFile()));
            } catch (IOException e) {
                fail("Failed to load properties for test");
            }
            boolean valid = Boolean.parseBoolean(validityProps.getProperty("valid"));
            Event testEvent = new Event(mock(
                    HttpServerExchange.class, RETURNS_DEEP_STUBS),
                    EventUtils.fileContentsToString(testEventPath), metadata, new HashMap<>(), "");
            return Arguments.of(valid, testEvent);
        };
        return EventUtils.generateEventArguments(dataDirectory, "nr", creator);
    }
}
