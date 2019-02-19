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

package utils;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;

public class EventUtils {


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
        try (Stream<Path> eventFileStream = Files.walk(eventBodyDirectory)) {
            return eventFileStream.filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .map(EventUtils::fileContentsToString)
                    .map(body -> EventUtils.makeMockEvent(body, eventMetadata))
                    .collect(Collectors.toList());
        }
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
     * @param body body for the event.
     * @param eventMetadata metadata for the event.
     * @return event with mock HttpServerExchange
     */
    public static Event makeMockEvent(String body, EventMetadata eventMetadata) {
        return new Event(mock(HttpServerExchange.class, RETURNS_DEEP_STUBS), body, eventMetadata, new HashMap<>());
    }
}
