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

import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.onap.dcaegen2.services.pmmapper.exceptions.MappingException;
import org.onap.dcaegen2.services.pmmapper.exceptions.TemplateIdentificationException;
import org.onap.dcaegen2.services.pmmapper.exceptions.XMLParseException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Mapper {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(Mapper.class));

    private HashMap<String, Template> templates;
    private MeasConverter converter;

    public Mapper(@NonNull Path templatesDirectory, MeasConverter converter) {
        logger.unwrap().trace("Constructing Mapper from {}", templatesDirectory);
        templates = new HashMap<>();
        this.converter = converter;
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setTagSyntax(Configuration.ANGLE_BRACKET_TAG_SYNTAX);
        try (Stream<Path> paths = Files.walk(templatesDirectory)) {
            paths.filter(Files::isRegularFile)
                    .forEach(template -> addTemplate(template, configuration));
        } catch (IOException exception) {
            logger.unwrap().error("Failed to walk template directory {}", templatesDirectory, exception);
            throw new IllegalArgumentException("Failed to walk template directory {}", exception);
        }
    }
    private void addTemplate(Path template, Configuration config) {
        logger.unwrap().debug("Loading template from {}", template.toString());
        try (InputStreamReader templateInputStreamReader = new InputStreamReader(Files.newInputStream(template))){
            templates.put(template.getFileName().toString(), new Template(template.getFileName().toString(), templateInputStreamReader, config, StandardCharsets.UTF_8.name()));
        } catch (IOException exception) {
            logger.unwrap().error("Failed to read template from location {}", template, exception);
            throw new IllegalArgumentException("Failed to read template from path", exception);
        }
    }
    public List<Event> mapEvents(List<Event> events) {
        events.forEach(event -> event.setVes(this.map(event)));
        return events;
    }

    public String map(@NonNull Event event) {
        logger.unwrap().info("Mapping event");
        NodeModel pmNodeModel;
        try {
            String measurements = converter.convert(event.getMeasurement());
            pmNodeModel = NodeModel.parse(new InputSource(new StringReader(measurements)));
        } catch (IOException | SAXException | ParserConfigurationException exception) {
            logger.unwrap().error("Failed to parse input as XML", exception);
            throw new XMLParseException("Failed to parse input as XML", exception);
        }
        Map<String, Object> mappingData = new HashMap<>();
        mappingData.put("xml", pmNodeModel);
        mappingData.put("metadata", event.getMetadata());
        mappingData.put("eventId", makeEventId());
        StringWriter mappedOutputWriter = new StringWriter();
        Template template = Optional.ofNullable(templates.get(event.getMetadata().getFileFormatType()))
                                    .orElseThrow(() -> new TemplateIdentificationException("Failed to identify template"));
        try {
            template.process(mappingData, mappedOutputWriter);
        } catch (IOException | TemplateException exception) {
            logger.unwrap().error("Failed to map XML", exception);
            throw new MappingException("Mapping failure", exception);
        }
        logger.unwrap().info("Data mapped successfully");
        return mappedOutputWriter.toString();
    }

    private String makeEventId(){
        return UUID.randomUUID().toString();
    }

}
