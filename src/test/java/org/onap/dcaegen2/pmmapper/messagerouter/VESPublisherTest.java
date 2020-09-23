
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

package org.onap.dcaegen2.pmmapper.messagerouter;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.onap.dcaegen2.services.pmmapper.exceptions.RequestFailure;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import reactor.test.StepVerifier;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.pmmapper.messagerouter.VESPublisher;
import org.onap.dcaegen2.services.pmmapper.utils.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Flux;
@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentConfig.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class VESPublisherTest {
    private static String topicURL = "http://mr/topic";
    private static RequestSender sender;
    private static MapperConfig config;
    private VESPublisher sut;
    private String ves = "{}";
    @Before
    public void before() throws Exception {
        config = mock(MapperConfig.class);
        sender = mock(RequestSender.class);
        sut = new VESPublisher(config, sender);
        when(config.getPublisherTopicUrl()).thenReturn(topicURL);
    }
    @Test
    public void publish_multiple_success() throws Exception {
        Event event = mock(Event.class);
        List<Event> events  = Arrays.asList(event,event,event);
        when(event.getVes()).thenReturn(ves);
        Flux<List<Event>> flux = sut.publish(events);
        verify(sender, times(3)).send(Mockito.anyString(),Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        StepVerifier.create(flux)
            .expectNextMatches(events::equals)
            .expectComplete()
            .verify();
    }
    @Test
    public void publish_multiple_fail() throws Exception {
        Event event = mock(Event.class);
        List<Event> events  = Arrays.asList(event,event,event);
        when(event.getVes()).thenReturn(ves);
        when(sender.send("POST",topicURL,ves,"base64encoded")).thenThrow(RequestFailure.class);
        Flux<List<Event>> flux = sut.publish(events);
        StepVerifier.create(flux)
        .expectNext(events)
            .verifyComplete();
    }
}
