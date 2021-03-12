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

import org.junit.After;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IncomingEventsCacheTest {

    private static final String id1 = "123.dmaap-dr-node";
    private static final String id2 = "987.dmaap-dr-node";

    @After
    public void resetCache() {
        IncomingEventsCache cache = IncomingEventsCache.INSTANCE;
        for(String id: cache.getCachedEvents()) {
            cache.remove(id);
        }
    }

    @Test
    public void shouldContainEventAfterAddingItToCache() {
        IncomingEventsCache eventsCache = IncomingEventsCache.INSTANCE;
        eventsCache.add(id1);
        assertEquals(1, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        assertTrue(IncomingEventsCache.INSTANCE.getCachedEvents().contains(id1));
    }

    @Test
    public void shouldRemoveEventFromCache() {
        IncomingEventsCache eventsCache = IncomingEventsCache.INSTANCE;

        assertEquals(0, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        eventsCache.add(id1);
        eventsCache.add(id1);
        eventsCache.add(id2);
        assertEquals(2, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        eventsCache.remove(id1);
        assertEquals(1, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        assertTrue(IncomingEventsCache.INSTANCE.getCachedEvents().contains(id2));
    }
}
