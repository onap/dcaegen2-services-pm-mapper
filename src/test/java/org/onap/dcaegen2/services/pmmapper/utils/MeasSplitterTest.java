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
package org.onap.dcaegen2.services.pmmapper.utils;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
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
class MeasSplitterTest {
    private static final String baseDir = "src/test/resources/split_test/";
    private MeasSplitter objUnderTest;

    @BeforeEach
    void setup() {
        objUnderTest = new MeasSplitter(new MeasConverter());
    }

    @Test
    void no_measData() {
        String inputPath = baseDir + "no_measdata";
        EventMetadata metadata = new EventMetadata();
        metadata.setFileFormatType(MeasConverter.LTE_FILE_TYPE);
        String inputXml = EventUtils.fileContentsToString(Paths.get(inputPath + ".xml"));
        Event testEvent = EventUtils.makeMockEvent(inputXml, metadata);

        assertThrows(NoSuchElementException.class, () -> objUnderTest.split(testEvent));
    }


    @ParameterizedTest
    @MethodSource("getEvents")
    void testSplit(int numberOfEvents, String[] measInfoIds, Event testEvent) {
        List<Event> splitEvents = objUnderTest.split(testEvent);
        assertEquals(numberOfEvents, splitEvents.size());
        for (int i = 0; i<splitEvents.size(); i++) {
            String measInfoId = splitEvents.get(i).getMeasurement()
                                        .getMeasurementData().get().get(0).getMeasurementInfo().get(0).getMeasInfoId();
            assertEquals(measInfoIds[i], measInfoId);
        }
    }


    private static List<Arguments> getEvents() {
        ArgumentCreator splitterCreator = (Path path, EventMetadata metadata) -> {
            Path propsPath = Paths.get(path.toString()+"/split.props");
            Path testEventPath = Paths.get(path.toString()+"/test.xml");
            Properties splitProperties = new Properties();
            try {
                splitProperties.load(new FileInputStream(propsPath.toFile()));
            } catch (IOException e) {
                fail("Failed to load properties for test");
            }
            int numberOfEvents = Integer.parseInt(splitProperties.getProperty("eventCount"));
            String [] measInfoIds = splitProperties.getProperty("measInfoIds").split(",");
            Event testEvent = EventUtils.makeMockEvent(EventUtils.fileContentsToString(testEventPath), metadata);
            return Arguments.of(numberOfEvents, measInfoIds, testEvent);
        };
        return EventUtils.generateEventArguments(Paths.get(baseDir), splitterCreator);
    }
}
