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

package org.onap.dcaegen2.services.pmmapper.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import utils.EventUtils;


@ExtendWith(MockitoExtension.class)
class XMLValidatorTest {
    private static final Path metadata = Paths.get("src/test/resources/valid_metadata.json");
    private static final Path dataDirectory = Paths.get("src/test/resources/xml_validator_test/test_data/");
    private static final Path xsd = Paths.get("src/main/resources/measCollec_plusString.xsd");
    private XMLValidator objUnderTest;

    @BeforeEach
    void setup() {
        objUnderTest = new XMLValidator(xsd);
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

    @ParameterizedTest
    @MethodSource("getValidEvents")
    void testValidEventsPass(Event testEvent) {
        assertTrue(objUnderTest.validate(testEvent));
    }

    @ParameterizedTest
    @MethodSource("getInvalidEvents")
    void testInvalidEventsFail(Event testEvent) {
        assertFalse(objUnderTest.validate(testEvent));
    }

    private static List<Event> getValidEvents() throws IOException {
        Path validDataDirectory = Paths.get(dataDirectory.toString() + "/valid/");
        return EventUtils.eventsFromDirectory(validDataDirectory, metadata);
    }

    private static List<Event> getInvalidEvents() throws IOException {
        Path invalidDataDirectory = Paths.get(dataDirectory.toString() + "/invalid/");
        return EventUtils.eventsFromDirectory(invalidDataDirectory, metadata);
    }

}
