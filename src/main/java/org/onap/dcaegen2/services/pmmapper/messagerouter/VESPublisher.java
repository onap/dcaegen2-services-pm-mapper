/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.dcaegen2.services.pmmapper.messagerouter;

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

public class VESPublisher {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(VESPublisher.class));
    private RequestSender sender;
    private MapperConfig config;

    public VESPublisher(MapperConfig config) {
        this(config, new RequestSender());
    }

    public VESPublisher(MapperConfig config, RequestSender sender) {
        this.sender = sender;
        this.config = config;
    }

    public Flux<List<Event>> publish(List<Event> events) {
        logger.unwrap().info("Publishing VES events to messagerouter.");
        try {
            events.forEach(e -> this.publish(e.getVes()));
            logger.unwrap().info("Successfully published VES events to messagerouter.");
        } catch (MRPublisherException e) {
            logger.unwrap().error("Failed to publish VES event(s) to messagerouter.", e);
            return Flux.empty();
        }
        return Flux.just(events);
    }

    public void publish(String ves) {
        try {
            String topicUrl = config.getPublisherTopicUrl();
            ves = ves.replaceAll("\n", "");
            String userCredentials =  Base64.getEncoder()
                .encodeToString((this.config.getPublisherUserName() + ":" +
                    this.config.getPublisherPassword())
                    .getBytes(StandardCharsets.UTF_8));
            sender.send("POST", topicUrl, ves, userCredentials);
        } catch (Exception e) {
            throw new MRPublisherException(e.getMessage(), e);
        }
    }

    public MapperConfig getConfig() {
        return config;
    }

}