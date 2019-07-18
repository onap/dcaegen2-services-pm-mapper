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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.PublisherConfig;
import org.onap.dcaegen2.services.pmmapper.model.SubscriberConfig;

@ExtendWith(MockitoExtension.class)
class DMaaPAdapterTest {
    private DMaaPAdapter objUnderTest;

    @BeforeEach
    void beforeEach() {
        objUnderTest = new DMaaPAdapter();
    }

    @Test
    void testSuccessfulPublisher() throws IOException {
        String dmaapPublisher = "{\"dmaap_publisher\": {\"dmaap_info\": {"
                                        + "\"topic_url\": \"https://message-router:3905/events/org.onap.dmaap.mr.VES_PM\","
                                        + "\"client_role\": \"org.onap.dcae.pmPublisher\","
                                        + "\"location\": \"san-francisco\","
                                        + "\"client_id\": \"1562763644939\"" + "},"
                                        + "\"something\":\"completely different\"}}";
        JsonReader reader = new JsonReader(new StringReader(dmaapPublisher));
        assertTrue(objUnderTest.read(reader) instanceof PublisherConfig);
    }

    @Test
    void testSuccessfulSubscriber() throws IOException {
        String dmaapSubscriber = "{\"dmaap_subscriber\": {\"dmaap_info\": {"
                                         + "\"username\": \"username\","
                                         + "\"password\": \"password\","
                                         + "\"location\": \"san-francisco\","
                                         + "\"delivery_url\": \"http://dcae-pm-mapper:8443/delivery\","
                                         + "\"subscriber_id\": 1" + "}}}";
        JsonReader reader = new JsonReader(new StringReader(dmaapSubscriber));
        assertTrue(objUnderTest.read(reader) instanceof SubscriberConfig);
    }

    @Test
    void testFailedAdaption() {
        JsonReader reader = new JsonReader(new StringReader("{\"dmaap_subscriber\": {\"dmaap_info\": \"nope\"}"));
        assertThrows(JsonParseException.class, () -> objUnderTest.read(reader));
    }

    @Test
    void testNoAdaptionAttempt() {
        JsonReader reader = new JsonReader(new StringReader("{\"dmaap_subscriber\": {}"));
        assertThrows(JsonParseException.class, () -> objUnderTest.read(reader));
    }

    @Test
    void testInvalidAdaptionTarget() {
        JsonReader reader = new JsonReader(new StringReader("{\"A scratch? \": \"Your arm's off!\" }"));
        assertThrows(IllegalArgumentException.class, () -> objUnderTest.read(reader));
    }

    @Test
    void testFailWriting() {
        assertThrows(UnsupportedOperationException.class, () -> objUnderTest.write(null, null));
    }
}
