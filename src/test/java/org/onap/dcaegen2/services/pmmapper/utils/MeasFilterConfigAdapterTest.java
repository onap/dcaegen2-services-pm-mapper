/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2021 Samsung Electronics.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

@ExtendWith(MockitoExtension.class)
class MeasFilterConfigAdapterTest {
    private MeasFilterConfigAdapter objUnderTest;
    @BeforeEach
    void setup() {
        objUnderTest = new MeasFilterConfigAdapter();
    }

    @Test
    void testValidConfigString() throws IOException {
        String filter ="\"{\\\"filters\\\": [{\\\"pmDefVsn\\\":\\\"V9\\\", \\\"nfType\\\": \\\"NrRadio\\\"," +
                "\\\"vendor\\\": \\\"Ericsson\\\", \\\"measTypes\\\": [\\\"A\\\", \\\"B\\\"]}]}\"";
        MeasFilterConfig filterConfig = objUnderTest.read(new JsonReader(new StringReader(filter)));
        assertEquals(2, filterConfig.getFilters().get(0).getMeasTypes().size());
    }

    @Test
    void testValidConfigObject() throws IOException {
        String filter = "{\"filters\": [{\"pmDefVsn\":\"V9\", \"nfType\": \"NrRadio\"," +
                "\"vendor\":\"Ericsson\", \"measTypes\": [\"A\"]}]}";
        MeasFilterConfig filterConfig = objUnderTest.read(new JsonReader(new StringReader(filter)));
        assertEquals(1, filterConfig.getFilters().get(0).getMeasTypes().size());
    }

    @Test
    void testInvalidConfigObject() throws IOException {
        String filter = "{\"filters\": \"invalid\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(filter));
        assertThrows(JsonSyntaxException.class, () -> objUnderTest.read(jsonReader));
    }

    @Test
    void testInvalidConfigString() throws IOException {
        String filter = "\"{\\\"filters\\\": [{\"pmDefVsn\":\\\"V9\\\", \\\"nfType\\\": \\\"NrRadio\\\"," +
                "\\\"vendor\\\": \\\"Ericsson\\\", \\\"measTypes\\\": [\\\"A\\\", \\\"B\\\"]}]}\"";
        JsonReader jsonReader = new JsonReader(new StringReader(filter));
        assertThrows(JsonSyntaxException.class, () -> objUnderTest.read(jsonReader));
    }

    @Test
    void testUnsupportedJSONType() throws IOException {
        String filter = "[]";
        JsonReader jsonReader = new JsonReader(new StringReader(filter));
        assertThrows(UnsupportedOperationException.class, () -> objUnderTest.read(jsonReader));
    }

    @Test
    void testFailWriting() {
        assertThrows(UnsupportedOperationException.class, () -> objUnderTest.write(null, null));
    }
}
