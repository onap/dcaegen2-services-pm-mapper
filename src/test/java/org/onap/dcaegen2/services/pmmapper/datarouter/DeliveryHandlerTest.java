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

package org.onap.dcaegen2.services.pmmapper.datarouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.undertow.io.Receiver;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.StatusCodes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.onap.dcaegen2.services.pmmapper.model.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.HttpServerExchangeAdapter;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.LoggingUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DeliveryHandler.class, EnvironmentConfig.class})
public class DeliveryHandlerTest {

    private Path VALID_METADATA_PATH = Paths.get("src/test/resources/valid_metadata.json");
    private Path INVALID_METADATA_PATH = Paths.get("src/test/resources/invalid_metadata.json");

    @Mock
    private EventReceiver eventReceiver;
    @Mock
    private MapperConfig config;

    private DeliveryHandler objUnderTest;

    @Before
    public void setUp() {
        objUnderTest = new DeliveryHandler(eventReceiver, config);
    }

    @Test
    public void testRequestInboundLimitedStateServiceUnavailable() throws Exception {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        HttpServerExchangeAdapter adapterMock = PowerMockito.mock(HttpServerExchangeAdapter.class);
        PowerMockito.whenNew(HttpServerExchangeAdapter.class).withAnyArguments().thenReturn(adapterMock);

        Sender responseSender = mock(Sender.class);
        when(httpServerExchange.setStatusCode(anyInt())).thenReturn(httpServerExchange);
        when(httpServerExchange.getResponseSender()).thenReturn(responseSender);
        objUnderTest.setLimited(true);
        objUnderTest.handleRequest(httpServerExchange);
        verify(httpServerExchange).setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
    }

    @Test
    public void testRequestInboundLimitedStateServiceNoEmission() throws Exception {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        HttpServerExchangeAdapter adapterMock = PowerMockito.mock(HttpServerExchangeAdapter.class);
        PowerMockito.whenNew(HttpServerExchangeAdapter.class).withAnyArguments().thenReturn(adapterMock);

        Sender responseSender = mock(Sender.class);
        when(httpServerExchange.setStatusCode(anyInt())).thenReturn(httpServerExchange);
        when(httpServerExchange.getResponseSender()).thenReturn(responseSender);
        objUnderTest.setLimited(true);
        objUnderTest.handleRequest(httpServerExchange);
        verify(eventReceiver, times(0)).receive(any());
    }

    @Test
    public void testRequestInboundInvalidMetadata() throws Exception {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        JsonObject metadata = new JsonParser().parse(new String(Files
                .readAllBytes(INVALID_METADATA_PATH))).getAsJsonObject();
        when(httpServerExchange.getRequestHeaders().get(any(String.class)).get(anyInt()))
                .thenReturn(metadata.toString());
        when(httpServerExchange.setStatusCode(anyInt())).thenReturn(httpServerExchange);
        objUnderTest.handleRequest(httpServerExchange);
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.BAD_REQUEST);
        verify(httpServerExchange.getResponseSender(), times(1)).send("Malformed Metadata.");

    }

    @Test
    public void testRequestInboundNoMetadata() throws Exception {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        Receiver receiver = mock(Receiver.class);
        HeaderMap headers = mock(HeaderMap.class);
        when(httpServerExchange.getRequestReceiver()).thenReturn(receiver);
        when(httpServerExchange.setStatusCode(anyInt())).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestHeaders()).thenReturn(headers);
        when(headers.get(any(String.class))).thenReturn(null);

        doAnswer((Answer<Void>) invocationOnMock -> {
            Receiver.FullStringCallback callback = invocationOnMock.getArgument(0);
            callback.handle(httpServerExchange, "");
            return null;
        }).when(receiver).receiveFullString(any());
        doAnswer((Answer<Void>) invocationOnMock -> {
            Runnable runnable = invocationOnMock.getArgument(0);
            runnable.run();
            return null;
        }).when(httpServerExchange).dispatch(any(Runnable.class));
        objUnderTest.handleRequest(httpServerExchange);
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.BAD_REQUEST);
        verify(httpServerExchange.getResponseSender(), times(1)).send("Missing Metadata.");

    }

    @Test
    public void testRequestInboundSuccess() throws Exception {
        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(DeliveryHandler.class);
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        Receiver receiver = mock(Receiver.class);
        when(httpServerExchange.getRequestReceiver()).thenReturn(receiver);
        String testString = "MESSAGE BODY";
        JsonObject metadata = new JsonParser().parse(
                new String(Files.readAllBytes(VALID_METADATA_PATH))).getAsJsonObject();
        when(httpServerExchange.getRequestHeaders().get(DeliveryHandler.METADATA_HEADER).get(anyInt()))
                .thenReturn(metadata.toString());
        when(httpServerExchange.getRequestHeaders().get(DeliveryHandler.PUB_ID_HEADER).getFirst()).thenReturn("");
        doAnswer((Answer<Void>) invocationOnMock -> {
            Receiver.FullStringCallback callback = invocationOnMock.getArgument(0);
            callback.handle(httpServerExchange, testString);
            return null;
        }).when(receiver).receiveFullString(any());

        doAnswer((Answer<Void>) invocationOnMock -> {
            Runnable runnable = invocationOnMock.getArgument(0);
            runnable.run();
            return null;
        }).when(httpServerExchange).dispatch(any(Runnable.class));

        objUnderTest.handleRequest(httpServerExchange);
        verify(eventReceiver, times(1)).receive(any(Event.class));

        assertEquals(logAppender.list.get(0).getMarker().getName(), "ENTRY");
        assertNotNull(logAppender.list.get(0).getMDCPropertyMap().get("InvocationID"));
        assertNotNull(logAppender.list.get(0).getMDCPropertyMap().get("RequestID"));
        assertEquals(logAppender.list.get(1).getMarker().getName(), "EXIT");
        logAppender.stop();
    }
}