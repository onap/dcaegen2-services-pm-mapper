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

public enum IncomingEventsCache {
    INSTANCE;

    private Set<String> eventsIds = ConcurrentHashMap.newKeySet();

    /**
     * Gets thread safe, single instance of this enum
     * @return the single instance of cache
     */
    public IncomingEventsCache getInstance() {
        return INSTANCE;
    }

    /**
     * Adds publishIdentity of an event to cache
     * @param id to be added to cache
     */
    public void add(String id) {
        eventsIds.add(id);
    }

    /**
     * Remove publishIdentity of an event from cache
     * @param id to be removed from cache
     */
    public void remove(String id) {
        eventsIds.remove(id);
    }

    /**
     * Checks if the cache contains a publishIdentity
     * @param id to be found in cache
     * @return true when the id exists, false otherwise
     */
    public boolean contains(String id) {
        return eventsIds.contains(id);
    }

    Set<String> getCachedEvents() {
        return new HashSet<>(eventsIds);
    }

    void resetCache() {
        eventsIds = ConcurrentHashMap.newKeySet();
    }
}
