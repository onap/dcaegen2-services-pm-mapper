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

package org.onap.dcaegen2.services.pmmapper.filtering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig.Filter;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementFile;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;

import io.undertow.server.HttpServerExchange;
import utils.ArgumentCreator;
import utils.EventUtils;

@ExtendWith(MockitoExtension.class)
class MeasFilterHandlerTest {

    private static final Path FILTER_DIRECTORY = Paths.get("src/test/resources/filter_test/");
    private static final String baseDir = "src/test/resources/filter_test/";
    private static MeasConverter converter = new MeasConverter();
    private MeasFilterHandler objUnderTest;
    @Mock
    private HttpServerExchange exchange;

    @BeforeEach
    void setup() {
        objUnderTest = new MeasFilterHandler(new MeasConverter());
    }

    @Test
    void skip_mapping_when_no_Filters_match() {
        String inputPath = baseDir + "lte/meas_results/test.xml";
        Filter noMatchFilter = new MeasFilterConfig().new Filter();
        noMatchFilter.setMeasTypes(Arrays.asList("nomatch1", "nomatch2"));
        Event event = generateEvent(inputPath, noMatchFilter);
        List<Event> events = new ArrayList<>();
        events.add(event);
        events.add(event);

        assertFalse(objUnderTest.filterByMeasType(event));
        assertFalse(objUnderTest.filterByMeasType(events));
    }

    @Test
    void remove_events_that_does_not_match_filter() {
        String inputPath = baseDir + "lte/meas_type_and_r_manyinfo/test.xml";

        Filter matchFilter = new MeasFilterConfig().new Filter();
        matchFilter.setMeasTypes(Arrays.asList("a", "b"));
        Event eventMatch = generateEvent(inputPath, matchFilter);
        Filter noMatchFilter = new MeasFilterConfig().new Filter();
        noMatchFilter.setMeasTypes(Arrays.asList("ad", "bs"));
        Event eventNoMatch = generateEvent(inputPath, noMatchFilter);

        List<Event> events = new ArrayList<>();
        events.add(eventMatch);
        events.add(eventNoMatch);
        assertTrue(objUnderTest.filterByMeasType(events));
        assertEquals(1, events.size());
    }

    @Test
    void skip_mapping_when_MeasData_isEmpty() {
        String inputPath = baseDir + "lte/meas_results/test.xml";
        Event event = generateEvent(inputPath, generateValidFilter());
        event.getMeasurement().replacementMeasurementData(Arrays.asList());

        assertFalse(objUnderTest.filterByMeasType(event));
    }

    @Test
    void skip_filtering_if_filter_or_meastypes_isEmpty() {
        String inputPath = baseDir + "lte/meas_results/test.xml";

        Filter emptyMeastypesFilter = new MeasFilterConfig().new Filter();
        emptyMeastypesFilter.setMeasTypes(Arrays.asList());

        Event event = generateEvent(inputPath, emptyMeastypesFilter);
        MeasurementFile originalMeasCollec = event.getMeasurement();

        assertTrue(objUnderTest.filterByMeasType(event));
        assertEquals(originalMeasCollec,event.getMeasurement());

        event.setFilter(null);
        assertTrue(objUnderTest.filterByMeasType(event));
        assertEquals(originalMeasCollec,event.getMeasurement());
    }

    @Test
    void valid_fileType() {
        Event event = mock(Event.class);
        when(event.getHttpServerExchange()).thenReturn(exchange);
        when(exchange.getRequestPath()).thenReturn("Apm.xml","Cpm.xml");
        assertTrue(objUnderTest.filterByFileType(event));
        assertTrue(objUnderTest.filterByFileType(event));
    }

    @Test
    void invalid_fileType() {
        Event event = mock(Event.class);
        List<String> invalidFileTypes = Arrays.asList("Bpm.xml","Dpm.xml","Apm.xml.gz","Apm.xm1","asdf","bsdf");
        when(event.getHttpServerExchange()).thenReturn(exchange);
        invalidFileTypes.forEach(fileName -> {
            when(exchange.getRequestPath())
                    .thenReturn(fileName);
            assertFalse(objUnderTest.filterByFileType(event));
        });
    }

    @ParameterizedTest
    @MethodSource("getEvents")
    void filter_valid_measurements(Event expectedEvent, Event testEvent) {
        objUnderTest.filterByMeasType(testEvent);
        String actual = converter.convert(testEvent.getMeasurement());
        String expected = converter.convert(expectedEvent.getMeasurement());
        assertEquals(expected, actual);

    }

    private Event generateEvent(String inputPath, Filter filter) {
        EventMetadata metadata = new EventMetadata();
        metadata.setFileFormatType(MeasConverter.LTE_FILE_TYPE);
        Event event = EventUtils.makeMockEvent(EventUtils.fileContentsToString(Paths.get(inputPath)), metadata);
        event.setFilter(filter);
        return event;
    }

    private static Filter generateValidFilter() {
        Filter filter;
        filter = new MeasFilterConfig().new Filter();
        filter.setDictionaryVersion("1.0");
        filter.setMeasTypes(Arrays.asList("a", "b"));
        return filter;
    }

    private static List<Arguments> getEvents() {
        ArgumentCreator creator = (Path path, EventMetadata metadata) -> {
            Path expectedEventPath = Paths.get(path.toString()+"/expected.xml");
            Path testEventPath = Paths.get(path.toString()+"/test.xml");
            Event expectedEvent = EventUtils.makeMockEvent(EventUtils.fileContentsToString(expectedEventPath), metadata);
            Event testEvent = EventUtils.makeMockEvent(EventUtils.fileContentsToString(testEventPath), metadata);
            testEvent.setFilter(generateValidFilter());
            return Arguments.of(expectedEvent, testEvent);
        };
        return EventUtils.generateEventArguments(FILTER_DIRECTORY, "/nr", creator);

    }

}
