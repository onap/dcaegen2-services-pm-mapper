/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia.
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

import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.EventUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
public class IncomingEventsCacheTest {

    private static final Path metadata = Paths.get("src/test/resources/valid_metadata.json");
    private static final Path testFile = Paths.get("src/test/resources/filter_test/lte/meas_results_manyinfo/test.xml");

    @Before
    public void resetCache() {
        IncomingEventsCache.INSTANCE.resetCache();
    }

    @Test
    public void shouldContainEventAfterAddingItToCache() throws IOException {
        IncomingEventsCache eventsCache = IncomingEventsCache.INSTANCE;
        Event event = prepareEvent();
        eventsCache.add(event);
        assertEquals(1, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        assertTrue(IncomingEventsCache.INSTANCE.getCachedEvents().contains(event));
    }

    @Test
    public void shouldRemoveEventFromCache() throws IOException {
        IncomingEventsCache eventsCache = IncomingEventsCache.INSTANCE;
        assertEquals(0, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        Event event = prepareEvent();
        eventsCache.add(event);
        eventsCache.add(event);
        assertEquals(1, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        eventsCache.remove(event);
        assertEquals(0, IncomingEventsCache.INSTANCE.getCachedEvents().size());
    }

    private Event prepareEvent() throws IOException {
        String metadataFileContents = new String(Files.readAllBytes(metadata));
        EventMetadata eventMetadata = new Gson().fromJson(metadataFileContents, EventMetadata.class);
        return new Event(mock(HttpServerExchange.class, RETURNS_DEEP_STUBS),
            EventUtils.fileContentsToString(testFile), eventMetadata, new HashMap<>(), "");
    }
}
