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

package org.onap.dcaegen2.services.pmmapper.kpi.computation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.onap.dcaegen2.services.pmmapper.exceptions.MRPublisherException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
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
    private RequestSender sender;
    private MapperConfig config;

    public KpiPublisher(MapperConfig config) {
        this(config, new RequestSender());
    }

    public KpiPublisher(MapperConfig config, RequestSender sender) {
        this.sender = sender;
        this.config = config;
    }

    private void publishToDMaap(String ves) {
        try {
            String topicUrl = config.getPublisherTopicUrl();
            ves = ves.replaceAll("\n", "");
            String userCredentials =  Base64.getEncoder()
                .encodeToString((this.config.getPublisherUserName()
                        + ":"
                        + this.config.getPublisherPassword())
                .getBytes(StandardCharsets.UTF_8));
            sender.send("POST", topicUrl, ves, userCredentials);
        } catch (Exception e) {
            throw new MRPublisherException(e.getMessage(), e);
        }
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
        try {
            events.forEach(e -> this.checkAndDoComputation(e));
            logger.unwrap().info("KPI computation done successfully");
        } catch (Exception e) {
            logger.unwrap().error("KPI computation done failed.", e);
            return Flux.empty();
        }
        return Flux.just(events);
    }

    private void checkAndDoComputation(Event event) {
        String ves = event.getVes();
        List<String> vesList = KpiComputation.checkAndDoComputation(ves, config);
        if (vesList == null || vesList.size() <= 0) {
            logger.unwrap().info("NO kpi data generated of event", event);
        } else {
            vesList.forEach(v -> publishToDMaap(v));
        }
    }

}
