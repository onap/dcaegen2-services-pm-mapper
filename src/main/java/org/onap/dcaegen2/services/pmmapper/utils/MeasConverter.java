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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.onap.dcaegen2.services.pmmapper.exceptions.MappingException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementFile;
import org.onap.dcaegen2.services.pmmapper.model.measurement.lte.MeasCollecFile;
import org.onap.dcaegen2.services.pmmapper.model.measurement.nr.MeasDataFile;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Converts 3GPP PM Measurement xml string to MeasCollecFil and vice versa.
 **/
public class MeasConverter {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(MeasConverter.class));

    public static final String LTE_FILE_TYPE = "org.3GPP.32.435#measCollec";
    public static final String NR_FILE_TYPE = "org.3GPP.28.550#measData";


    /**
     * Converts 3GPP Measurement xml string to MeasCollecFile.
     **/
    public MeasurementFile convert(Event event) {
        logger.unwrap().debug("Converting 3GPP xml string to PM object");
        Class targetClass = determineTargetClass(event);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(targetClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(false);
            XMLReader reader = saxFactory.newSAXParser().getXMLReader();
            SAXSource source = new SAXSource(reader,new InputSource(new StringReader(event.getBody())));
            return (MeasurementFile) unmarshaller.unmarshal(source);
        } catch (JAXBException | ParserConfigurationException | SAXException e) {
            throw new MappingException("Unable to convert 3GPP xml to PM Measurement", e);
        }
    }

    /**
     * Converts MeasCollecFile to 3GPP Measurement xml string.
     **/
    public String convert(MeasurementFile measurement) {
        logger.unwrap().debug("Converting Measurement to 3GPP xml string");
        StringWriter writer = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(measurement.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(measurement, writer);
        } catch (JAXBException e) {
            throw new MappingException("Unable to convert Measurement to 3GPP xml", e);
        }
        return writer.toString();
    }

    private Class determineTargetClass(Event event) {
        Class targetClass;
        if (event.getMetadata().getFileFormatType().equals(MeasConverter.LTE_FILE_TYPE)) {
            targetClass = MeasCollecFile.class;
        } else if(event.getMetadata().getFileFormatType().equals(NR_FILE_TYPE)) {
            targetClass = MeasDataFile.class;
        } else {
            throw new MappingException("Failed to discover file type with first class support", new RuntimeException());
        }
        return targetClass;
    }

}
