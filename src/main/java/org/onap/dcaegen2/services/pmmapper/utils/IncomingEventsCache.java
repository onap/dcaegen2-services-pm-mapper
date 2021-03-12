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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.dcaegen2.services.pmmapper.model.Event;


public enum IncomingEventsCache {
    INSTANCE;

    private Set<Event> events = ConcurrentHashMap.newKeySet();

    /**
     * Gets thread safe, single instance of this enum
     * @return the single instance of cache
     */
    public IncomingEventsCache getInstance() {
        return INSTANCE;
    }

    /**
     * Adds an event to cache
     * @param event to be added to cache
     */
    public void add(Event event) {
        events.add(event);
    }

    /**
     * Remove an event from cache
     * @param event to be removed from cache
     */
    public void remove(Event event) {
        events.remove(event);
    }

    /**
     * Checks if the cache contains an event
     * @param event to be found in cache
     * @return true when the event exists, false otherwise
     */
    public boolean contains(Event event) {
        return events.contains(event);
    }

    Set<Event> getCachedEvents() {
        return new HashSet<>(events);
    }

    void resetCache() {
        events = ConcurrentHashMap.newKeySet();
    }
}
