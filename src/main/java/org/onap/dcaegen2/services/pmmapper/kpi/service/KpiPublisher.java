/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 China Mobile.
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

package org.onap.dcaegen2.services.pmmapper.kpi.service;

import java.util.ArrayList;
import java.util.List;

import org.onap.dcaegen2.services.pmmapper.messagerouter.VESPublisher;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

/**
 * KPI computation and published.
 *
 * @author Kai Lu
 */
public class KpiPublisher {

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(KpiPublisher.class));
    private VESPublisher vesPublisher;

    public KpiPublisher(VESPublisher vesPublisher) {
        this.vesPublisher = vesPublisher;
    }

    /**
     * do KPI computation.
     *
     * @param events    events
     * @return Kpi event list
     *
     */
    public Flux<List<Event>> kpiComputation(List<Event> events) {
        logger.unwrap().info("Publishing KPI VES events to messagerouter.");
        List<Event> modifiedEvents = new ArrayList<>();
        try {
            events.forEach(e -> {
                modifiedEvents.addAll(this.checkAndDoComputation(e));
            });
            logger.unwrap().info("KPI computation done successfully");
        } catch (Exception e) {
            logger.unwrap().error("KPI computation done failed.", e);
            return Flux.empty();
        }
        return Flux.just(modifiedEvents);
    }

    private List<Event> checkAndDoComputation(Event event) {
        KpiComputation computation = new KpiComputation();
        return computation.checkAndDoComputation(event, vesPublisher.getConfig());
    }

}
