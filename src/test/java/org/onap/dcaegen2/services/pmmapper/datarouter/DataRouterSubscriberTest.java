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

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.undertow.io.Receiver;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.StatusCodes;
import utils.LoggingUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.dcaegen2.services.pmmapper.exceptions.TooManyTriesException;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.utils.HttpServerExchangeAdapter;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DataRouterSubscriber.class)
public class DataRouterSubscriberTest {


    @Mock
    private EventReceiver eventReceiver;

    private DataRouterSubscriber objUnderTest;

    @Before
    public void setUp() {
        objUnderTest = new DataRouterSubscriber(eventReceiver);
    }

    @Test
    public void testStartTooManyTriesWithResponse() throws IOException {
        PowerMockito.mockStatic(Thread.class);

        URL subEndpoint = mock(URL.class);
        MapperConfig config = mock(MapperConfig.class);
        when(config.getBusControllerSubscriptionUrl()).thenReturn(subEndpoint);
        HttpURLConnection huc = mock(HttpURLConnection.class, RETURNS_DEEP_STUBS);
        when(subEndpoint.openConnection()).thenReturn(huc);
        when(huc.getResponseCode()).thenReturn(300);
        Assertions.assertThrows(TooManyTriesException.class, () -> objUnderTest.start(config));
    }

    @Test
    public void testStartImmediateSuccess() throws IOException, TooManyTriesException, InterruptedException {
        URL subEndpoint = mock(URL.class);
        MapperConfig config = mock(MapperConfig.class);
        when(config.getBusControllerSubscriptionUrl()).thenReturn(subEndpoint);
        HttpURLConnection huc = mock(HttpURLConnection.class, RETURNS_DEEP_STUBS);
        when(subEndpoint.openConnection()).thenReturn(huc);
        when(huc.getResponseCode()).thenReturn(200);
        objUnderTest.start(config);
        verify(huc, times(1)).getResponseCode();
    }

    @Test
    public void testStartDelayedSuccess() throws IOException, TooManyTriesException, InterruptedException {
        PowerMockito.mockStatic(Thread.class);

        URL subEndpoint = mock(URL.class);
        MapperConfig config = mock(MapperConfig.class);
        when(config.getBusControllerSubscriptionUrl()).thenReturn(subEndpoint);
        HttpURLConnection huc = mock(HttpURLConnection.class, RETURNS_DEEP_STUBS);
        when(subEndpoint.openConnection()).thenReturn(huc);
        doAnswer(new Answer() {
            boolean forceRetry = true;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (forceRetry) {
                    forceRetry = false;
                    throw new IOException();
                }
                return 200;
            }
        }).when(huc).getResponseCode();
        objUnderTest.start(config);
        verify(huc, times(2)).getResponseCode();
    }

    @Test
    public void testStartReadTimeout() throws IOException {
        PowerMockito.mockStatic(Thread.class);

        URL subEndpoint = mock(URL.class);
        MapperConfig config = mock(MapperConfig.class);
        when(config.getBusControllerSubscriptionUrl()).thenReturn(subEndpoint);
        HttpURLConnection huc = mock(HttpURLConnection.class, RETURNS_DEEP_STUBS);
        when(subEndpoint.openConnection()).thenReturn(huc);
        doThrow(new IOException()).when(huc).getResponseCode();
        Assertions.assertThrows(TooManyTriesException.class, () -> objUnderTest.start(config));
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
                .readAllBytes(Paths.get("src/test/resources/invalid_metadata.json")))).getAsJsonObject();
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
        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(DataRouterSubscriber.class);
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        Receiver receiver = mock(Receiver.class);
        when(httpServerExchange.getRequestReceiver()).thenReturn(receiver);
        String testString = "MESSAGE BODY";
        JsonObject metadata = new JsonParser().parse(
                new String(Files.readAllBytes(Paths.get("src/test/resources/valid_metadata.json")))).getAsJsonObject();
        when(httpServerExchange.getRequestHeaders().get(any(String.class)).get(anyInt()))
                .thenReturn(metadata.toString());
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
