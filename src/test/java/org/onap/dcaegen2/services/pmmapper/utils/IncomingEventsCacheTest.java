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

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(PowerMockRunner.class)
public class IncomingEventsCacheTest {

    @Before
    public void resetCache() {
        IncomingEventsCache.INSTANCE.resetCache();
    }

    @Test
    public void shouldContainEventAfterAddingItToCache() {
        IncomingEventsCache eventsCache = IncomingEventsCache.INSTANCE;
        final String event1 = "123.dmaap-dr-node";
        eventsCache.add(event1);
        assertEquals(1, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        assertTrue(IncomingEventsCache.INSTANCE.getCachedEvents().contains(event1));
    }

    @Test
    public void shouldRemoveEventFromCache() throws IOException {
        IncomingEventsCache eventsCache = IncomingEventsCache.INSTANCE;
        final String event1 = "123.dmaap-dr-node";
        final String event2 = "987.dmaap-dr-node";

        assertEquals(0, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        eventsCache.add(event1);
        eventsCache.add(event1);
        eventsCache.add(event2);
        assertEquals(2, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        eventsCache.remove(event1);
        assertEquals(1, IncomingEventsCache.INSTANCE.getCachedEvents().size());
        assertTrue(IncomingEventsCache.INSTANCE.getCachedEvents().contains(event2));
    }
}
