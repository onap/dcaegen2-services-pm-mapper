/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2021-2022 Nokia.
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
package org.onap.dcaegen2.pmmapper.messagerouter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.dcaegen2.services.pmmapper.messagerouter.VESPublisher;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.DmaapRequestSender;
import org.onap.dcaegen2.services.sdk.model.streams.ImmutableAafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class VESPublisherTest {

    private static final String TOPIC_URL = "http://mr/topic";
    private static final String VES = "{}";
    private static final ImmutableAafCredentials AAF_CREDENTIALS = ImmutableAafCredentials.builder()
            .username("")
            .password("")
            .build();

    private static final MessageRouterPublishResponse SUCCESSFUL =
            ImmutableMessageRouterPublishResponse.builder().build();
    private static final MessageRouterPublishResponse FAILED =
            ImmutableMessageRouterPublishResponse.builder()
                    .failReason("failReason")
                    .build();

    private DmaapRequestSender sender;
    private VESPublisher sut;

    @Before
    public void before() {
        MapperConfig config = mock(MapperConfig.class);
        when(config.getPublisherTopicUrl()).thenReturn(TOPIC_URL);
        when(config.getPublisherUserName()).thenReturn("");
        when(config.getPublisherPassword()).thenReturn("");
        sender = mock(DmaapRequestSender.class);
        sut = new VESPublisher(config, sender);
    }

    @Test
    public void publish_multiple_success() {
        Event event = mock(Event.class);
        List<Event> events = Arrays.asList(event, event, event);
        when(event.getVes()).thenReturn(VES);
        MessageRouterPublishResponse successfulResponse = ImmutableMessageRouterPublishResponse.builder().build();
        when(sender.send(any(), any(), any())).thenReturn(Flux.just(successfulResponse, successfulResponse));

        Flux<Event> flux = sut.publish(events);

        verify(sender, times(1)).send(anyString(), any(), any());
        StepVerifier.create(flux)
                .expectNextMatches(event::equals)
                .verifyComplete();
    }

    @Test
    public void publish_multiple_fail_sender_exceptions() {
        Event event = mock(Event.class);
        List<Event> events = Arrays.asList(event, event, event);
        when(event.getVes()).thenReturn(VES);
        when(sender.send(eq(TOPIC_URL), any(), eq(AAF_CREDENTIALS)))
                .thenReturn(Flux.error(new RuntimeException()));

        Flux<Event> flux = sut.publish(events);

        StepVerifier.create(flux)
                .verifyComplete();
    }

    @Test
    public void publish_multiple_fail_mr_responses_failed() {
        Event event = mock(Event.class);
        List<Event> events = Arrays.asList(event, event, event);
        when(event.getVes()).thenReturn(VES);
        when(sender.send(eq(TOPIC_URL), any(), eq(AAF_CREDENTIALS)))
                .thenReturn(Flux.just(FAILED, FAILED, FAILED));

        Flux<Event> flux = sut.publish(events);

        StepVerifier.create(flux)
                .verifyComplete();
    }

    @Test
    public void publish_multiple_fail_and_multiple_success() {
        Event event = mock(Event.class);
        when(event.getVes()).thenReturn(VES);
        List<Event> events = Arrays.asList(event, event, event, event);
        when(sender.send(eq(TOPIC_URL), any(), eq(AAF_CREDENTIALS)))
                .thenReturn(Flux.just(SUCCESSFUL, FAILED, SUCCESSFUL, FAILED));

        Flux<Event> flux = sut.publish(events);

        StepVerifier.create(flux)
                .verifyComplete();
    }
}
