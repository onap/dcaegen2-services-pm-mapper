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

package utils;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import java.util.stream.Stream;
import junit.framework.TestCase;
import org.junit.jupiter.params.provider.Arguments;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;

public class EventUtils {

    private static final String LTE_QUALIFIER = "/lte";
    private static final String NR_QUALIFIER = "/nr";

    /**
     * Reads contents of files inside the eventBodyDirectory, combines contents with metadata to make an Event Object.
     * Fails test in the event of failure to read a file.
     * @param eventBodyDirectory Path to directory with event body files.
     * @param metadataPath Path to file with metadata object.
     * @return List of Events containing the body as acquired from files inside eventBodyDirectory.
     * @throws IOException in the event it fails to read from the files.
     */
    public static List<Event> eventsFromDirectory(Path eventBodyDirectory, Path metadataPath) throws IOException {
        EventMetadata eventMetadata = new Gson().fromJson(fileContentsToString(metadataPath), EventMetadata.class);
        List<String> eventFiles = FileUtils.getFilesFromDirectory(eventBodyDirectory);
        return eventFiles.stream()
                       .map(contents -> EventUtils.makeMockEvent(contents, eventMetadata))
                       .collect(Collectors.toList());
    }

    /**
     * Create a List of Arguments containing an Event (Defaults to LTE events), and an expected outcome.
     * Fails test in the event of failure to read a file.
     * @param baseDirectory Directory containing multiple formats of events separated by a directory.
     * @param argCreator Callback to method that will generate the appropriate set of arguments for each test.
     */
    public static List<Arguments> generateEventArguments(Path baseDirectory, ArgumentCreator argCreator ) {
        List<Arguments> events = new ArrayList<>();
        try (Stream<Path> paths = Files.list(baseDirectory)) {
            paths.filter(Files::isDirectory).forEach(path -> {
                String fileFormatType = readFileFormatFromPath(path.toString());
                EventMetadata eventMetadata = new EventMetadata();
                eventMetadata.setFileFormatType(fileFormatType);
                events.addAll(getEventsArgument(path, eventMetadata, argCreator));
            });
        } catch (IOException e) {
            TestCase.fail("IOException occurred while generating test data");
        }
        return events;
    }

    private static String readFileFormatFromPath(String path) {
        if (path.contains(NR_QUALIFIER)) return MeasConverter.NR_FILE_TYPE;
        if (path.contains(LTE_QUALIFIER)) return MeasConverter.LTE_FILE_TYPE;
        return MeasConverter.NOT_SUPPORTED_TYPE;
    }

    /**
     * reads contents of file into a string.
     * fails a tests in the event failure occurs.
     * @param path path to file.
     * @return string containing files contents
     */
    public static String fileContentsToString(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException exception) {
            fail("IOException occurred while generating events.");
        }
        return null;
    }

    /**
     * Makes an event with a mock http server exchange, empty mdc and publish identity
     * @param body body for the event.
     * @param eventMetadata metadata for the event.
     * @return event with mock HttpServerExchange.
     */
    public static Event makeMockEvent(String body, EventMetadata eventMetadata) {
        Event event = new Event(mock(HttpServerExchange.class, RETURNS_DEEP_STUBS), body, eventMetadata, new HashMap<>(), "");
        event.setMeasurement(new MeasConverter().convert(event));
        return event;
    }

    /**
     * Makes an event with a mock http server exchange and empty mdc
     * @param body body for the event.
     * @param eventMetadata metadata for the event.
     * @return event with mock HttpServerExchange
     */
    public static Event makeMockEvent(String body, EventMetadata eventMetadata, String publishIdentity) {
        HttpServerExchange mockHttpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        return new Event(mockHttpServerExchange, body, eventMetadata, new HashMap<>(), publishIdentity);
    }

    private static List<Arguments> getEventsArgument(Path basePath, EventMetadata metadata, ArgumentCreator argCreator) {
        List<Arguments> events = new ArrayList<>();
        try (Stream<Path> paths = Files.list(basePath)) {
            paths.filter(Files::isDirectory).forEach(path->{
                events.add(argCreator.makeArgument(path, metadata));
            });
        } catch (IOException e) {
            TestCase.fail("IOException occurred while generating test data");
        }
        return events;
    }

}
