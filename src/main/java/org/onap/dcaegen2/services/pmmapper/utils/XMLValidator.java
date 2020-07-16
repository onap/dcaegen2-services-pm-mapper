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

import java.nio.file.Files;
import java.util.HashMap;
import java.util.stream.Stream;
import lombok.NonNull;
import org.onap.dcaegen2.services.pmmapper.exceptions.NotSupportedFormatTypeException;
import org.onap.dcaegen2.services.pmmapper.mapping.Mapper;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

public class XMLValidator {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(XMLValidator.class));
    private HashMap<String, Schema> schemas;
    private SchemaFactory schemaFactory;
    public XMLValidator(Path schemaDirectory) {
        logger.unwrap().trace("Constructing schema from {}", schemaDirectory);
        schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemas = new HashMap<>();
        try (Stream<Path> paths = Files.walk(schemaDirectory)) {
            paths.filter(Files::isRegularFile).forEach(this::addSchema);
        } catch (IOException exception) {
            logger.unwrap().error("Failed to walk schema directory {}", schemaDirectory, exception);
            throw new IllegalArgumentException("Failed to walk template directory {}", exception);
        }
    }

    private void addSchema(Path schema) {
        logger.unwrap().debug("Loading schema from {}", schema.toString());
        try {
            schemas.put(schema.getFileName().toString(), schemaFactory.newSchema(schema.toFile()));
        } catch(SAXException exception) {
            logger.unwrap().error("Failed to discover a valid schema at {}", schema, exception);
            throw new IllegalArgumentException("Failed to discover a valid schema from given path", exception);
        }
    }

    public boolean validate(@NonNull Event event) {
        try {
            Validator validator = getValidatorForAccordingFileFormat(event.getMetadata().getFileFormatType());
            validator.validate(new StreamSource(new StringReader(event.getBody())));
            logger.unwrap().info("XML validation successful");
            logger.unwrap().debug(String.valueOf(event));
            return true;
        } catch (SAXException | IOException exception) {
            logger.unwrap().error("XML validation failed {}", event, exception);
            return false;
        } catch (NotSupportedFormatTypeException exception) {
            logger.unwrap().error("XML validation failed - given file format type is not supported. {}", event, exception);
            return false;
        }
    }

    private Validator getValidatorForAccordingFileFormat(String fileFormatType) throws NotSupportedFormatTypeException {
        Schema schema = schemas.get(fileFormatType);
        if (schema == null) throw new NotSupportedFormatTypeException(fileFormatType);
        return schema.newValidator();
    }
}