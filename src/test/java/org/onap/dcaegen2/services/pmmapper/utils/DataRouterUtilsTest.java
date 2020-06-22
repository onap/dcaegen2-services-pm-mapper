/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 - 2020 Nordix Foundation.
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

package org.onap.dcaegen2.services.pmmapper.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.undertow.server.HttpServerExchange;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.dcaegen2.services.pmmapper.exceptions.ProcessEventException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.ssl.SSLContextFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import utils.EventUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
@PowerMockIgnore({"org.apache.http.conn.ssl.*", "javax.net.ssl.*" , "javax.crypto.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest(RequestSender.class)
@RunWith(PowerMockRunner.class)
public class DataRouterUtilsTest {

    private static String validConfigFileContents;
    private static MapperConfig validConfig;
    private SSLContextFactory sslContextFactory;
    private static final Path validConfigPath = Paths.get("src/test/resources/valid_mapper_config.json");

    @Test
    public void processEventSuccessful() throws Exception {
        validConfigFileContents = new String(Files.readAllBytes(validConfigPath));
        JsonObject configObject = new JsonParser().parse(validConfigFileContents).getAsJsonObject();
        validConfig = new Gson().fromJson(configObject, MapperConfig.class);
        sslContextFactory = new SSLContextFactory(validConfig);

        SSLContext sslContext = sslContextFactory.createSSLContext(validConfig);
        SSLContext.setDefault(sslContext);

        String serviceResponse = "I'm a service response ;)";
        String publishIdentity = "12";
        PowerMockito.mockStatic(Thread.class);
        MapperConfig mockMapperConfig = mock(MapperConfig.class);
        URL mockURL = mock(URL.class);
        HttpsURLConnection mockConnection = mock(HttpsURLConnection.class, RETURNS_DEEP_STUBS);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(serviceResponse.getBytes()));

        when(mockURL.openConnection()).thenReturn(mockConnection);
        when(mockURL.getProtocol()).thenReturn("https");
        when(mockMapperConfig.getDmaapDRDeleteEndpoint()).thenReturn("https://dmaap-dr-node/delete/");
        when(mockMapperConfig.getSubscriberIdentity()).thenReturn("12");

        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);

        Event testEvent = EventUtils.makeMockEvent("", mock(EventMetadata.class), publishIdentity);
        assertEquals(serviceResponse,  DataRouterUtils.processEvent(mockMapperConfig, testEvent));
        verify(mockConnection, times(1)).setRequestMethod(RequestSender.DELETE);
    }

    @Test
    public void processEventSuccessfulHttp() throws Exception {
        String serviceResponse = "I'm a service response ;)";
        String publishIdentity = "12";
        PowerMockito.mockStatic(Thread.class);
        MapperConfig mockMapperConfig = mock(MapperConfig.class);
        URL mockURL = mock(URL.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class, RETURNS_DEEP_STUBS);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(serviceResponse.getBytes()));

        when(mockURL.openConnection()).thenReturn(mockConnection);
        when(mockURL.getProtocol()).thenReturn("http");
        when(mockMapperConfig.getDmaapDRDeleteEndpoint()).thenReturn("http://dmaap-dr-node/delete/");
        when(mockMapperConfig.getSubscriberIdentity()).thenReturn("12");

        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);

        Event testEvent = EventUtils.makeMockEvent("", mock(EventMetadata.class), publishIdentity);
        assertEquals(serviceResponse,  DataRouterUtils.processEvent(mockMapperConfig, testEvent));
        verify(mockConnection, times(1)).setRequestMethod(RequestSender.DELETE);
    }

    @Test
    public void testNegativeResponse() throws Exception {
        validConfigFileContents = new String(Files.readAllBytes(validConfigPath));
        JsonObject configObject = new JsonParser().parse(validConfigFileContents).getAsJsonObject();
        validConfig = new Gson().fromJson(configObject, MapperConfig.class);
        sslContextFactory = new SSLContextFactory(validConfig);

        SSLContext sslContext = sslContextFactory.createSSLContext(validConfig);
        SSLContext.setDefault(sslContext);

        String serviceResponse = "I'm a negative service response ;)";
        String publishIdentity = "12";
        PowerMockito.mockStatic(Thread.class);
        MapperConfig mockMapperConfig = mock(MapperConfig.class);
        URL mockURL = mock(URL.class);
        HttpsURLConnection mockConnection = mock(HttpsURLConnection.class, RETURNS_DEEP_STUBS);
        when(mockConnection.getResponseCode()).thenReturn(503);
        when(mockConnection.getInputStream())
                .thenAnswer(invocationOnMock -> new ByteArrayInputStream(serviceResponse.getBytes()));

        when(mockURL.openConnection()).thenReturn(mockConnection);
        when(mockURL.getProtocol()).thenReturn("https");
        when(mockMapperConfig.getDmaapDRDeleteEndpoint()).thenReturn("dmaap-dr-node/delete/");
        when(mockMapperConfig.getSubscriberIdentity()).thenReturn("12");

        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        Event testEvent = EventUtils.makeMockEvent("", mock(EventMetadata.class), publishIdentity);
        assertEquals(serviceResponse, DataRouterUtils.processEvent(mockMapperConfig, testEvent));
        verify(mockConnection, times(5)).setRequestMethod(RequestSender.DELETE);
    }

    @Test
    public void testConstructionException() {
        assertThrows(IllegalStateException.class, () -> Whitebox.invokeConstructor(DataRouterUtils.class));
    }

    @Test
    public void testProcessEventFailure() throws Exception {
        PowerMockito.mockStatic(Thread.class);
        MapperConfig mockMapperConfig = mock(MapperConfig.class);
        URL mockURL = mock(URL.class);
        HttpsURLConnection mockConnection = mock(HttpsURLConnection.class, RETURNS_DEEP_STUBS);
        when(mockConnection.getResponseCode()).thenReturn(503);
        EventMetadata metadata = new EventMetadata();
        metadata.setFileFormatType(MeasConverter.LTE_FILE_TYPE);
        when(mockURL.openConnection()).thenReturn(mockConnection);
        when(mockURL.getProtocol()).thenReturn("https");
        when(mockMapperConfig.getDmaapDRDeleteEndpoint()).thenReturn("dmaap-dr-node/delete/");
        when(mockMapperConfig.getSubscriberIdentity()).thenReturn("12");

        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        Event testEvent = new Event(mock(
                HttpServerExchange.class, RETURNS_DEEP_STUBS), "", metadata, new HashMap<>(), "12");
        assertThrows(ProcessEventException.class, () -> DataRouterUtils.processEvent(mockMapperConfig, testEvent));
    }
}
