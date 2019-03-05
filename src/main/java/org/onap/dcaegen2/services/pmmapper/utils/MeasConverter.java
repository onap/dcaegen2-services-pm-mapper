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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.onap.dcaegen2.services.pmmapper.exceptions.MappingException;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

/**
 * Converts 3GPP PM Measurement xml string to MeasCollecFil and vice versa.
 **/
public class MeasConverter {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(MeasConverter.class));

    /**
     * Converts 3GPP Measurement xml string to MeasCollecFile.
     **/
    public MeasCollecFile convert(String eventBody) {
        logger.unwrap().debug("Converting 3GPP xml string to MeasCollecFile");
        MeasCollecFile measCollecFile = null;
        try {
            JAXBContext jaxbContext = null;
            jaxbContext = JAXBContext.newInstance(MeasCollecFile.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            measCollecFile = (MeasCollecFile) unmarshaller.unmarshal(new StringReader(eventBody));
        } catch (JAXBException e) {
            throw new MappingException("Unable to convert 3GPP xml to MeasCollecFile", e);
        }
        return measCollecFile;
    }

    /**
     * Converts MeasCollecFile to 3GPP Measurement xml string.
     **/
    public String convert(MeasCollecFile measCollecFile) {
        logger.unwrap().debug("Converting MeasCollecFile to 3GPP xml string");
        StringWriter writer = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MeasCollecFile.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(measCollecFile, writer);
        } catch (JAXBException e) {
            throw new MappingException("Unable to convert MeasCollecFile to 3GPP xml", e);
        }
        return writer.toString();
    }
}
