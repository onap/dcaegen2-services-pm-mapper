/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
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
import java.util.HashMap;
import java.util.List;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.exceptions.MappingException;
import org.onap.dcaegen2.services.pmmapper.exceptions.TemplateIdentificationException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;
import org.powermock.reflect.Whitebox;
import utils.ArgumentCreator;
import utils.EventUtils;


@ExtendWith(MockitoExtension.class)
class MapperTest {

    private static EventMetadata fourthGenerationMetadata;
    private static EventMetadata fifthGenerationMetadata;
    private static Schema vesSchema;
    private static MeasConverter converter;
    private Mapper objUnderTest;

    private static final Path schema = Paths.get("src/test/resources/mapper_test/CommonEventFormat_30.1-ONAP.json");
    private static final Path METADATA_DIRECTORY = Paths.get("src/test/resources/metadata/");
    private static final Path FIFTH_GENERATION_METADATA = Paths.get(METADATA_DIRECTORY.toString()+"/valid_5g_metadata.json");
    private static final Path FOURTH_GENERATION_METADATA = Paths.get(METADATA_DIRECTORY.toString()+"/valid_4g_metadata.json");
    private static final Path TEMPLATES_DIRECTORY = Paths.get("src/main/resources/templates/");
    private static final Path dataDirectory = Paths.get("src/test/resources/mapper_test/mapping_data/");


    @BeforeAll
    static void classSetup() throws IOException {
        JSONObject ves = new JSONObject(new String(Files.readAllBytes(schema)));
        vesSchema = SchemaLoader.load(ves);

        String fourthGenMetadataFileContents = new String(Files.readAllBytes(FOURTH_GENERATION_METADATA));
        fourthGenerationMetadata = new Gson().fromJson(fourthGenMetadataFileContents, EventMetadata.class);
        String fifthGenMetadataFileContents = new String(Files.readAllBytes(FIFTH_GENERATION_METADATA));
        fifthGenerationMetadata =  new Gson().fromJson(fifthGenMetadataFileContents, EventMetadata.class);
        converter = new MeasConverter();
    }

    @BeforeEach
    void setup() {
        objUnderTest = new Mapper(TEMPLATES_DIRECTORY, converter);
    }

    @ParameterizedTest
    @MethodSource("getValidEvents")
    void testValidEvent(Event testEvent) {
        vesSchema.validate(new JSONObject(objUnderTest.map(testEvent)));
    }

    @Test
    void testFailureToProcessLte() throws Exception {
        Template mappingTemplateMock = mock(Template.class, RETURNS_DEEP_STUBS);
        doThrow(new TemplateException(mock(Environment.class))).when(mappingTemplateMock)
                .process(any(), any());
        HashMap<String, Template> templates = new HashMap<>();
        templates.put("org.3GPP.32.435#measCollec", mappingTemplateMock);
        Whitebox.setInternalState(objUnderTest, "templates", templates);
        Path testFile = Paths.get(dataDirectory + "/32.435/no_measdata/test.xml");
        Event testEvent = EventUtils.makeMockEvent(EventUtils.fileContentsToString(testFile), fourthGenerationMetadata);
        assertThrows(MappingException.class, () -> objUnderTest.map(testEvent));
    }

    @Test
    void testFailureToProcessNr() throws Exception {
        Template mappingTemplateMock = mock(Template.class, RETURNS_DEEP_STUBS);
        doThrow(new TemplateException(mock(Environment.class))).when(mappingTemplateMock)
                .process(any(), any());
        HashMap<String, Template> templates = new HashMap<>();
        templates.put("org.3GPP.28.532#measData", mappingTemplateMock);
        Whitebox.setInternalState(objUnderTest, "templates", templates);
        Path testFile = Paths.get(dataDirectory + "/28.532/no_measdata/test.xml");
        Event testEvent = EventUtils.makeMockEvent(EventUtils.fileContentsToString(testFile), fifthGenerationMetadata);
        assertThrows(MappingException.class, () -> objUnderTest.map(testEvent));
    }

    @Test
    void testFailureToParseLte() {
        assertThrows(MappingException.class, () ->
                objUnderTest.map(EventUtils.makeMockEvent("not xml", fourthGenerationMetadata)));
    }

    @Test
    void testFailureToParseNr() {
        assertThrows(MappingException.class, () ->
                 objUnderTest.map(EventUtils.makeMockEvent("not xml", fifthGenerationMetadata)));
    }

    @Test
    void testInvalidPath() {
        assertThrows(IllegalArgumentException.class, () -> new Mapper(Paths.get("not a path"),converter));
    }

    @Test
    void testInvalidTemplateDirectory() {
        assertThrows(IllegalArgumentException.class, () -> new Mapper(Paths.get("fake dir"), new MeasConverter()));
    }
    @Test
    void testTemplateNotFound() {
        EventMetadata testMetadata = mock(EventMetadata.class);
        when(testMetadata.getFileFormatType()).thenReturn(MeasConverter.LTE_FILE_TYPE, "InvalidFormat");
        Path testFile = Paths.get(dataDirectory + "/32.435/no_measdata/test.xml");
        Event testEvent = EventUtils.makeMockEvent(EventUtils.fileContentsToString(testFile), testMetadata);
        assertThrows(TemplateIdentificationException.class, () -> objUnderTest.map(testEvent));
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

    static List<Arguments> getValidEvents() {
        ArgumentCreator creator = (Path path, EventMetadata metadata) -> {
            Path testEventPath = Paths.get(path.toString()+"/test.xml");
            Path metadataPath = Paths.get(path.toString()+"/metadata.json");
            EventMetadata eventMetadata = null;
            try {
                eventMetadata = new Gson().fromJson(new String(Files.readAllBytes(metadataPath)), EventMetadata.class);
            } catch (IOException e) {
                fail("Failed to read contents of Metadata");
            }
            Event testEvent = EventUtils.makeMockEvent(EventUtils.fileContentsToString(testEventPath), eventMetadata);
            return Arguments.of(testEvent);
        };
        return EventUtils.generateEventArguments(dataDirectory, creator);
    }
}

