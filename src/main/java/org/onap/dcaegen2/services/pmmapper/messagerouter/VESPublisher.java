/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.dcaegen2.services.pmmapper.messagerouter;

import org.onap.dcaegen2.services.pmmapper.exceptions.MRPublisherException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.DmaapRequestSender;
import org.onap.dcaegen2.services.sdk.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.model.streams.ImmutableAafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.DmaapResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

public class VESPublisher {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(VESPublisher.class));
    private final DmaapRequestSender sender;
    private final MapperConfig config;

    public VESPublisher(MapperConfig config) {
        this(config, new DmaapRequestSender());
    }

    public VESPublisher(MapperConfig config, DmaapRequestSender sender) {
        this.sender = sender;
        this.config = config;
    }

    public Flux<Event> publish(List<Event> events) {
        logger.unwrap().info("Publishing VES events to messagerouter.");
        Event first = events.get(0);
        List<String> vesEvents = minifiedVesEvents(events);
        return publishEvents(vesEvents)
                .filter(DmaapResponse::failed)
                .takeLast(1)
                .flatMap(this::toFluxError)
                .defaultIfEmpty(first)
                .doOnComplete(() -> logger.unwrap().info("Successfully published VES events to messagerouter."))
                .onErrorResume(this::resume);
    }

    private List<String> minifiedVesEvents(List<Event> events) {
        return events.stream()
                .map(Event::getVes)
                .map(vesEvent -> vesEvent.replace("\n", ""))
                .collect(Collectors.toList());
    }

    private Flux<MessageRouterPublishResponse> publishEvents(List<String> vesEvents) {
        String topicUrl = config.getPublisherTopicUrl();
        AafCredentials credentials = aafCredentials();
        return sender.send(topicUrl, vesEvents, credentials);
    }

    private Flux<Event> toFluxError(MessageRouterPublishResponse response) {
        return Flux.error(new MRPublisherException(response.failReason()));
    }

    private Flux<Event> resume(Throwable t) {
        logger.unwrap().error("Failed to publish VES event(s) to messagerouter.", t);
        return Flux.empty();
    }

    private AafCredentials aafCredentials() {
        return ImmutableAafCredentials.builder()
                .username(config.getPublisherUserName())
                .password(config.getPublisherPassword())
                .build();
    }
}
