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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import io.undertow.server.HttpServerExchange;
import java.io.StringWriter;

import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.pmmapper.exceptions.MappingException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.measurement.lte.MeasCollecFile;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JAXBContext.class})
public class MeasConverterTest {

    private MeasConverter objUnderTest;

    @Before
    public void setup() {
        objUnderTest = new MeasConverter();
    }
    @Test
    public void convertToString_throws_mappingException() throws Exception {
        MeasCollecFile file = new MeasCollecFile();
        PowerMockito.mockStatic(JAXBContext.class);
        Marshaller marshallerMock = PowerMockito.mock(Marshaller.class);
        JAXBContext jaxbContext = PowerMockito.mock(JAXBContext.class);
        StringWriter w = Mockito.mock(StringWriter.class);
        PowerMockito.whenNew(StringWriter.class).withNoArguments().thenReturn(w);
        PowerMockito.when(JAXBContext.newInstance(MeasCollecFile.class)).thenReturn(jaxbContext);
        PowerMockito.when(jaxbContext.createMarshaller()).thenReturn(marshallerMock);
        PowerMockito.doThrow(new JAXBException("",""))
       .when(marshallerMock).marshal( Mockito.any(MeasCollecFile.class)
               ,Mockito.any(StringWriter.class));

        assertThrows(MappingException.class, () -> {
              objUnderTest.convert(file);
        });
    }

    @Test
    public void convertToMeasCollec_throws_mappingException() {
        EventMetadata metadata = new EventMetadata();
        metadata.setFileFormatType(MeasConverter.LTE_FILE_TYPE);
        Event event = new Event(mock(HttpServerExchange.class, RETURNS_DEEP_STUBS), "xmlString", metadata, new HashMap<>(), "");
        assertThrows(MappingException.class, () -> {
            objUnderTest.convert(event);
        });
    }

    @Test
    public void convertToMeasData_throws_mappingException() {
        EventMetadata metadata = new EventMetadata();
        metadata.setFileFormatType(MeasConverter.NR_FILE_TYPE);
        Event event = new Event(mock(HttpServerExchange.class, RETURNS_DEEP_STUBS), "xmlString", metadata, new HashMap<>(), "");
        assertThrows(MappingException.class, () -> {
            objUnderTest.convert(event);
        });
    }

}