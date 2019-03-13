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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig.Filter;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;

import io.undertow.server.HttpServerExchange;
import utils.EventUtils;

@ExtendWith(MockitoExtension.class)
class MeasFilterHandlerTest {

    private static final String baseDir = "src/test/resources/filter_test/";
    private static final Path dataDirectory = Paths.get("src/test/resources/mapper_test/mapping_data/");
    private static MeasConverter converter = new MeasConverter();
    private MeasFilterHandler objUnderTest;
    @Mock
    private HttpServerExchange exchange;
    @Mock
    private EventMetadata metaData;

    @BeforeEach
    void setup() {
        objUnderTest = new MeasFilterHandler(new MeasConverter());
    }

    @Test
    void measTypes_byCommaSeparation() throws IOException {
        String inputPath = baseDir + "meas_results";
        String expected = EventUtils.fileContentsToString(Paths.get(inputPath + "_filtered.xml"));
        Event event = generateEvent(inputPath, generateValidFilter());

        objUnderTest.filterByMeasType(event);

        String actual = converter.convert(event.getMeasCollecFile());
        assertEquals(expected, actual);
    }

    @Test
    void measType_byID() throws IOException {
        String inputPath = baseDir + "meas_type_and_r";
        String filteredString = EventUtils.fileContentsToString(Paths.get(inputPath + "_filtered.xml"));
        Event event = generateEvent(inputPath, generateValidFilter());
        MeasCollecFile f = converter.convert(filteredString);
        String expected = converter.convert(f);

        objUnderTest.filterByMeasType(event);

        String actual = converter.convert(event.getMeasCollecFile());
        assertEquals(expected, actual);
    }

    @Test
    void skip_mapping_when_no_Filters_match() {
        String inputPath = baseDir + "meas_results";
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
        String inputPath = baseDir + "meas_type_and_r_manyInfo";

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
        String inputPath = baseDir + "meas_results";
        Event event = generateEvent(inputPath, generateValidFilter());
        event.getMeasCollecFile().replaceMeasData(Arrays.asList());

        assertFalse(objUnderTest.filterByMeasType(event));
    }

    @Test
    void skip_filtering_if_filter_or_meastypes_isEmpty() {
        String inputPath = baseDir + "meas_results";

        Filter emptyMeastypesFilter = new MeasFilterConfig().new Filter();
        emptyMeastypesFilter.setMeasTypes(Arrays.asList());

        Event event = generateEvent(inputPath, emptyMeastypesFilter);
        MeasCollecFile originalMeasCollec = event.getMeasCollecFile();

        assertTrue(objUnderTest.filterByMeasType(event));
        assertEquals(originalMeasCollec,event.getMeasCollecFile());

        event.setFilter(null);
        assertTrue(objUnderTest.filterByMeasType(event));
        assertEquals(originalMeasCollec,event.getMeasCollecFile());
    }

    @Test
    void multiple_measInfos_measResults() {
        String inputPath = baseDir + "meas_results_manyInfo";
        String filteredString = EventUtils.fileContentsToString(Paths.get(inputPath + "_filtered.xml"));
        Event event = generateEvent(inputPath, generateValidFilter());

        MeasCollecFile f = converter.convert(filteredString);
        String expected = converter.convert(f);
        objUnderTest.filterByMeasType(event);

        String actual = converter.convert(event.getMeasCollecFile());
        assertEquals(expected, actual);
    }

    @Test
    void multiple_measInfos_measTypeAndR() {
        String inputPath = baseDir + "meas_type_and_r_manyInfo";
        String filteredString = EventUtils.fileContentsToString(Paths.get(inputPath + "_filtered.xml"));
        Event event = generateEvent(inputPath, generateValidFilter());

        MeasCollecFile f = converter.convert(filteredString);
        String expected = converter.convert(f);
        objUnderTest.filterByMeasType(event);

        String actual = converter.convert(event.getMeasCollecFile());
        assertEquals(expected, actual);
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
        List<String> invalidFiletypes = Arrays.asList("Bpm.xml","Dpm.xml","asdf","bsdf");
        when(event.getHttpServerExchange()).thenReturn(exchange);
        when(exchange.getRequestPath())
            .thenReturn(invalidFiletypes.toString());

        invalidFiletypes.forEach(c -> {
            assertFalse(objUnderTest.filterByFileType(event));
        });
    }


    @ParameterizedTest
    @MethodSource("getValidMeas")
    void applyFilterToValidMeasurements(Event testEvent) {
        objUnderTest.filterByMeasType(testEvent);
    }

    private Event generateEvent(String inputPath, Filter filter) {
        String inputXml = EventUtils.fileContentsToString(Paths.get(inputPath + ".xml"));
        Event event = new Event(exchange, inputXml, metaData, new HashMap<String, String>(), "");
        event.setMeasCollecFile(converter.convert(inputXml));
        event.setFilter(filter);
        return event;
    }

    private Filter generateValidFilter() {
        Filter filter;
        filter = new MeasFilterConfig().new Filter();
        filter.setDictionaryVersion("1.0");
        filter.setMeasTypes(Arrays.asList("a", "b"));
        return filter;
    }

    static List<Event> getValidMeas() throws IOException {
        final Path metadata = Paths.get("src/test/resources/valid_metadata.json");
        List<Event> events = EventUtils
                .eventsFromDirectory(Paths.get(dataDirectory.toString() + "/valid_data/"), metadata)
                .stream()
                .map(e -> {
                    MeasCollecFile m = converter.convert(e.getBody());
                    e.setMeasCollecFile(m);
                    return e;
                })
                .collect(Collectors.toList());
        return events;
    }
}
