/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2021 Samsung Electronics.
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.onap.dcaegen2.services.pmmapper.model.Event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.undertow.io.Receiver;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.StatusCodes;
import utils.LoggingUtils;

@ExtendWith(MockitoExtension.class)
class DeliveryHandlerTest {

    private Path VALID_METADATA_PATH = Paths.get("src/test/resources/valid_metadata.json");
    private Path INVALID_METADATA_PATH = Paths.get("src/test/resources/invalid_metadata.json");

    @Mock
    private EventReceiver eventReceiver;

    private DeliveryHandler objUnderTest;

    @BeforeEach
    void setUp() {
        objUnderTest = new DeliveryHandler(eventReceiver);
    }

    @Test
    void testRequestInboundInvalidMetadata() throws Exception {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        JsonObject metadata = JsonParser.parseString(new String(Files
            .readAllBytes(INVALID_METADATA_PATH))).getAsJsonObject();
        when(httpServerExchange.getRequestHeaders().get(any(String.class)).get(anyInt()))
                .thenReturn(metadata.toString());
        when(httpServerExchange.setStatusCode(anyInt())).thenReturn(httpServerExchange);
        objUnderTest.handleRequest(httpServerExchange);
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.BAD_REQUEST);
        verify(httpServerExchange.getResponseSender(), times(1)).send("Malformed Metadata.");

    }

    @Test
    void testRequestInboundNoMetadata() throws Exception {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        Receiver receiver = mock(Receiver.class);
        HeaderMap headers = mock(HeaderMap.class);
        when(httpServerExchange.getRequestReceiver()).thenReturn(receiver);
        when(httpServerExchange.setStatusCode(anyInt())).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestHeaders()).thenReturn(headers);
        when(headers.get(any(String.class))).thenReturn(null);
        objUnderTest.handleRequest(httpServerExchange);
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.BAD_REQUEST);
        verify(httpServerExchange.getResponseSender(), times(1)).send("Missing Metadata.");

    }

    @Test
    void testRequestInboundSuccess() throws Exception {
        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(DeliveryHandler.class);
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        Receiver receiver = mock(Receiver.class);
        when(httpServerExchange.getRequestReceiver()).thenReturn(receiver);
        String testString = "MESSAGE BODY";
        JsonObject metadata = JsonParser.parseString(new String(Files.readAllBytes(VALID_METADATA_PATH))).getAsJsonObject();
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

        assertEquals("ENTRY", logAppender.list.get(0).getMarker().getName());
        assertNotNull(logAppender.list.get(0).getMDCPropertyMap().get("InvocationID"));
        assertNotNull(logAppender.list.get(0).getMDCPropertyMap().get("RequestID"));
        assertEquals("EXIT", logAppender.list.get(1).getMarker().getName());
        logAppender.stop();
    }
}