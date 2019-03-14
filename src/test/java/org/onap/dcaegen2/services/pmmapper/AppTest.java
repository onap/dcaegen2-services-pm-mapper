/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.dcaegen2.services.pmmapper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import io.undertow.util.StatusCodes;
import reactor.core.publisher.Flux;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.onap.dcaegen2.services.pmmapper.filtering.MeasFilterHandler;
import org.onap.dcaegen2.services.pmmapper.mapping.Mapper;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;
import org.onap.dcaegen2.services.pmmapper.utils.MeasSplitter;


@ExtendWith(MockitoExtension.class)
class AppTest {

    static ClientAndServer mockServer;
    static MockServerClient client;

    @BeforeAll
    public static void setup() {
        mockServer =  startClientAndServer(1080);
        client = new MockServerClient("127.0.0.1", 1080);
    }

    @AfterAll
    public static void teardown() {
        mockServer.stop();
    }

    @Test
    void testHandleBackPressureNullValue() {
        assertThrows(NullPointerException.class, () -> App.handleBackPressure(null));
    }

    @Test
    void testHandleBackPressure() {
        Event event = utils.EventUtils.makeMockEvent("", mock(EventMetadata.class));
        App.handleBackPressure(event);
        verify(event.getHttpServerExchange(), times(1)).setStatusCode(StatusCodes.TOO_MANY_REQUESTS);
        verify(event.getHttpServerExchange(), times(1)).unDispatch();
    }

    @Test
    void testReceiveRequestNullValue() {
        assertThrows(NullPointerException.class, () -> App.receiveRequest(null));
    }

    @Test
    void testReceiveRequest() {
        Event event = utils.EventUtils.makeMockEvent("", mock(EventMetadata.class));
        App.receiveRequest(event);
        verify(event.getHttpServerExchange(), times(1)).setStatusCode(StatusCodes.OK);
        verify(event.getHttpServerExchange(), times(1)).unDispatch();
    }

    @Test
    public void testFilter_success() {
        Event mockEvent = Mockito.mock(Event.class);
        List<Event> mockEvents = Arrays.asList(mockEvent);
        MapperConfig mockConfig = Mockito.mock(MapperConfig.class);
        boolean result = App.filter(new MeasFilterHandler(new MeasConverter()), mockEvents, mockConfig);
        assertTrue(result);
    }

    @Test
    public void testFilter_throwException() {
        HttpRequest req = HttpRequest.request();
        client.when(req).respond( response().withStatusCode(200));

        Event mockEvent = Mockito.mock(Event.class);
        List<Event> mockEvents = Arrays.asList(mockEvent);
        MeasFilterHandler mockFilter = Mockito.mock(MeasFilterHandler.class);
        MapperConfig mockConfig = Mockito.mock(MapperConfig.class);

        Mockito.when(mockConfig.getDmaapDRDeleteEndpoint()).thenReturn("http://127.0.0.1:1080");
        Mockito.when(mockConfig.getSubscriberIdentity()).thenReturn("sid");
        Mockito.when(mockEvent.getPublishIdentity()).thenReturn("pid");
        Mockito.when(mockFilter.filterByMeasType(mockEvent)).thenThrow(RuntimeException.class);

        boolean x = App.filter(mockFilter, mockEvents, mockConfig);
        assertFalse(x);

        client.clear(req);
    }

    @Test
    public void testSplit_empty_success() {
        Event mockEvent = Mockito.mock(Event.class);
        MapperConfig mockConfig = Mockito.mock(MapperConfig.class);
        Flux<List<Event>> splitResult = App.split(new MeasSplitter(new MeasConverter()), mockEvent, mockConfig);
        splitResult.equals(Flux.<List<Event>>empty());
    }

    @Test
    public void testSplit_success() {
        Event mockEvent = Mockito.mock(Event.class);
        List<Event> mockEvents = Arrays.asList(mockEvent,mockEvent);
        MapperConfig mockConfig = Mockito.mock(MapperConfig.class);
        MeasSplitter mockSplitter  = Mockito.mock(MeasSplitter.class);
        Mockito.when(mockSplitter.split(mockEvent)).thenReturn(mockEvents);

        Flux<List<Event>> splitResult = App.split(mockSplitter, mockEvent, mockConfig);

        splitResult.equals(Flux.just(mockEvents));
    }

    @Test
    public void testMapping_empty_success() {
        Event mockEvent = Mockito.mock(Event.class);
        List<Event> mockEvents = Arrays.asList(mockEvent);
        MapperConfig mockConfig = Mockito.mock(MapperConfig.class);
        Path mappingTemplate = Paths.get("src/main/resources/mapping.ftl");
        Flux<List<Event>> mappingResult = App.map(new Mapper(mappingTemplate), mockEvents, mockConfig);
        mappingResult.equals(Flux.<List<Event>>empty());
    }

}
