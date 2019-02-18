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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

@Slf4j
public class XMLValidator {
    private Schema schema;
    public XMLValidator(Path xmlSchemaDefinition) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schema = schemaFactory.newSchema(xmlSchemaDefinition.toFile());
        } catch (SAXException exception) {
            throw new IllegalArgumentException("Bad Schema", exception);
        }
    }

    public boolean validate(@NonNull Event event) {
        try {
            Validator validator =  schema.newValidator();
            validator.validate(new StreamSource(new StringReader(event.getBody())));
            log.info("XML validation successful {}", event);
            return true;
        } catch (SAXException | IOException exception) {
            log.info("XML validation failed {}", event, exception);
            return false;
        }
    }
}