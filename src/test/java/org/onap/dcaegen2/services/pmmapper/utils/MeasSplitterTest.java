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
import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile;

import io.undertow.server.HttpServerExchange;
import utils.EventUtils;

@ExtendWith(MockitoExtension.class)
public class MeasSplitterTest {
    private static final String baseDir = "src/test/resources/split_test/";
    private MeasSplitter objUnderTest;
    private MeasConverter converter;
    @Mock
    HttpServerExchange exchange;
    @Mock
    EventMetadata meta;
    @Mock
    Event event;
    @Mock
    MapperConfig config;

    @BeforeEach
    public void setup() {
        converter =  new MeasConverter();
        objUnderTest = new MeasSplitter(converter);
    }

    public void setupBaseEvent() {
        Mockito.when(event.getHttpServerExchange()).thenReturn(exchange);
        Mockito.when(event.getMetadata()).thenReturn(meta);
        Mockito.when(event.getMdc()).thenReturn(new HashMap<String, String>());
        Mockito.when(event.getMetadata()).thenReturn(meta);
        Mockito.when(event.getPublishIdentity()).thenReturn("");
    }


    @Test
    public void no_measData() {
        String inputPath = baseDir + "no_measdata";
        String inputXml = EventUtils.fileContentsToString(Paths.get(inputPath + ".xml"));
        Mockito.when(event.getBody()).thenReturn(inputXml);

        Assertions.assertThrows(NoSuchElementException.class, ()->{
            objUnderTest.split(event);
        });
    }

    @Test
    public void typeA_returns_only_one_event() throws JAXBException {
        String inputPath = baseDir + "meas_results_typeA";
        String inputXml = EventUtils.fileContentsToString(Paths.get(inputPath + ".xml"));
        MeasCollecFile measToBeSplit = converter.convert(inputXml);
        setupBaseEvent();
        Mockito.when(event.getBody()).thenReturn(inputXml);
        Mockito.when(event.getMeasCollecFile()).thenReturn(measToBeSplit);

        List<Event> splitEvents = objUnderTest.split(event);

        assertEquals(1,splitEvents.size());
    }

    @Test
    public void typeC_returns_multiple_events() throws JAXBException {
        String inputPath = baseDir + "meas_results_typeC";
        String inputXml = EventUtils.fileContentsToString(Paths.get(inputPath + ".xml"));
        setupBaseEvent();
        Mockito.when(event.getBody()).thenReturn(inputXml);
        MeasCollecFile measToBeSplit = converter.convert(inputXml);
        Mockito.when(event.getMeasCollecFile()).thenReturn(measToBeSplit);

        List<Event> splitEvents = objUnderTest.split(event);

        assertEquals(3,splitEvents.size());
        for (int i = 0; i < splitEvents.size(); i++) {
          String measInfoId = splitEvents.get(i).getMeasCollecFile()
                  .getMeasData().get(0).getMeasInfo().get(0).getMeasInfoId();
          Assertions.assertTrue(measInfoId.equals("measInfoId"+(i+1)));
        }
    }
}
