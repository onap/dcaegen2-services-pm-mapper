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

package org.onap.dcaegen2.services.pmmapper.mapping;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.exceptions.MappingException;
import org.onap.dcaegen2.services.pmmapper.exceptions.XMLParseException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.powermock.reflect.Whitebox;
import utils.EventUtils;


@ExtendWith(MockitoExtension.class)
class MapperTest {

    private static EventMetadata eventMetadata;
    private static Schema vesSchema;
    private static MeasConverter converter;
    private Mapper objUnderTest;

    private static final Path schema = Paths.get("src/test/resources/mapper_test/CommonEventFormat_30.1-ONAP.json");
    private static final Path metadata = Paths.get("src/test/resources/valid_metadata.json");
    private static final Path mapping = Paths.get("src/main/resources/mapping.ftl");
    private static final Path dataDirectory = Paths.get("src/test/resources/mapper_test/mapping_data/");


    @BeforeAll
    static void classSetup() throws IOException {
        JSONObject ves = new JSONObject(new String(Files.readAllBytes(schema)));
        vesSchema = SchemaLoader.load(ves);

        String metadataFileContents = new String(Files.readAllBytes(metadata));
        eventMetadata = new Gson().fromJson(metadataFileContents, EventMetadata.class);
        converter = mock(MeasConverter.class);
    }

    @BeforeEach
    void setup() {
        objUnderTest = new Mapper(mapping,converter);
    }

    @ParameterizedTest
    @MethodSource("getValidEvents")
    void testValidEvent(Event testEvent) {
        when(converter.convert(any(MeasCollecFile.class))).thenReturn(testEvent.getBody());
        vesSchema.validate(new JSONObject(objUnderTest.map(testEvent)));
    }

    @Test
    void testFailureToProcess() throws IOException, TemplateException {
        Template mappingTemplateMock = mock(Template.class, RETURNS_DEEP_STUBS);
        doThrow(new TemplateException(mock(Environment.class))).when(mappingTemplateMock)
                .process(any(), any());
        Whitebox.setInternalState(objUnderTest, "mappingTemplate", mappingTemplateMock);
        Path testFile = Paths.get(dataDirectory + "/valid_data/no_measdata.xml");
        Event testEvent = EventUtils.makeMockEvent(EventUtils.fileContentsToString(testFile), eventMetadata);
        assertThrows(MappingException.class, () ->
                objUnderTest.map(testEvent));
    }

    @Test
    void testFailureToParse() {
        when(converter.convert(any(MeasCollecFile.class))).thenCallRealMethod();
        assertThrows(MappingException.class, () ->
                objUnderTest.map(EventUtils.makeMockEvent("not xml", eventMetadata)));
    }

    @Test
    void testInvalidPath() {
        assertThrows(IllegalArgumentException.class, () -> new Mapper(Paths.get("not a path"),converter));
    }

    @Test
    void testNullPath() {
        assertThrows(NullPointerException.class, () -> new Mapper(null,converter));
    }

    @Test
    void testNullEvent() {
        assertThrows(NullPointerException.class, () -> objUnderTest.map(null));
    }

    @Test
    void testNullLogger() {
        assertThrows(NullPointerException.class, () -> objUnderTest.map(mock(Event.class)));
    }

    @Test
    void testMapEvents() throws IOException {
        List<Event> events = getValidEvents();
        List<Event> expectedEvents = objUnderTest.mapEvents(events);
        expectedEvents.forEach(event->{
            when(converter.convert(any(MeasCollecFile.class))).thenReturn(event.getBody());
            assertTrue(event.getVes() != null);
        });
    }

    static List<Event> getValidEvents() throws IOException {
        return EventUtils.eventsFromDirectory(Paths.get(dataDirectory.toString() + "/valid_data/"), metadata);
    }

    static List<Event> getInvalidEvents() throws IOException {
        return EventUtils.eventsFromDirectory(Paths.get(dataDirectory.toString() + "/invalid_data/"), metadata);
    }
}

