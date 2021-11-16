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

package org.onap.dcaegen2.services.pmmapper.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import org.onap.dcaegen2.services.pmmapper.exceptions.ReconfigurationException;
import org.onap.dcaegen2.services.pmmapper.utils.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import reactor.core.publisher.Mono;
import utils.ConfigUtils;
import java.util.ArrayList;
import utils.FileUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
@ExtendWith(MockitoExtension.class)
class DynamicConfigurationTest {
    private static final String VALID_MAPPER_CONFIG_FILE = "valid_mapper_config.json";

    private static ArrayList<Configurable> configurables;
    private DynamicConfiguration objUnderTest;
    private static String mapperConfig;
    private MapperConfig originalMapperConfig;
    private static ConfigHandler configHandler;

    @Mock(lenient = true)
    private static RequestSender sender;

    @Mock(lenient = true)
    private static EnvironmentConfig config;

    @BeforeAll
     static void setupBeforeAll() throws Exception {
        mapperConfig = FileUtils.getFileContents(VALID_MAPPER_CONFIG_FILE);
    }

    @BeforeEach
    void setup() throws Exception {
        configHandler = new ConfigHandler(sender, config);
        when(sender.send(any())).thenReturn(mapperConfig);
        originalMapperConfig = ConfigUtils.getMapperConfigFromFile(VALID_MAPPER_CONFIG_FILE);
        configurables = new ArrayList<>();
        objUnderTest = new DynamicConfiguration(configurables, originalMapperConfig, new ConfigHandler());
    }

    @Test
    void testNoChangeResponse() throws Exception {
        originalMapperConfig = configHandler.getMapperConfig();
        objUnderTest.setConfigHandler(configHandler);
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        objUnderTest.handleRequest(httpServerExchange);
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.OK);
        assertEquals(originalMapperConfig, objUnderTest.getOriginalConfig());
    }

    @Test
    void testApplyOriginalUponFailure() throws Exception {
        Configurable configurable = mock(Configurable.class);
        configurables.add(configurable);
        JsonObject modifiedConfig = new JsonParser().parse(mapperConfig).getAsJsonObject();
        modifiedConfig.addProperty("dmaap_dr_delete_endpoint","http://modified-delete-endpoint/1");
        when(sender.send(any())).thenReturn(modifiedConfig.toString());
        MapperConfig modifiedMapperConfig = configHandler.getMapperConfig();
        objUnderTest.setConfigHandler(configHandler);
        doAnswer(new Answer() {
            boolean failFirstReconfigure = true;
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (failFirstReconfigure) {
                    failFirstReconfigure = false;
                    throw new ReconfigurationException("");
                }
                return null;
            }
        }).when(configurable).reconfigure(any());

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        objUnderTest.handleRequest(httpServerExchange);
        assertEquals(originalMapperConfig, objUnderTest.getOriginalConfig());
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
        verify(configurable, times(1)).reconfigure(modifiedMapperConfig);
        verify(configurable, times(1)).reconfigure(originalMapperConfig);
    }

    @Test
    void testSuccessfulReconfiguration() throws Exception {
        Configurable configurable = mock(Configurable.class);
        configurables.add(configurable);
        JsonObject modifiedConfig = new JsonParser().parse(mapperConfig).getAsJsonObject();
        modifiedConfig.addProperty("dmaap_dr_delete_endpoint","http://modified-delete-endpoint/1");

        when(sender.send(any()))
                .thenReturn(modifiedConfig.toString());
        MapperConfig modifiedMapperConfig = configHandler.getMapperConfig();
        objUnderTest.setConfigHandler(configHandler);

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        objUnderTest.handleRequest(httpServerExchange);
        assertEquals(modifiedMapperConfig, objUnderTest.getOriginalConfig());
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.OK);
        verify(configurable, times(1)).reconfigure(modifiedMapperConfig);

    }

    @Test
    void testMapperConfigReconfiguration() throws Exception {
        JsonObject modifiedConfigJson = new JsonParser().parse(mapperConfig).getAsJsonObject();
        modifiedConfigJson.addProperty("dmaap_dr_delete_endpoint","http://modified-delete-endpoint/1");
        when(sender.send(any()))
                .thenReturn(modifiedConfigJson.toString());
        MapperConfig modifiedConfig = configHandler.getMapperConfig();
        originalMapperConfig.reconfigure(modifiedConfig);
        assertEquals(originalMapperConfig, modifiedConfig);
    }

    @Test
    void testMapperConfigReconfigurationNoChange() throws Exception {
        when(sender.send(any())).thenReturn(mapperConfig);
        MapperConfig inboundConfig = configHandler.getMapperConfig();
        originalMapperConfig.reconfigure(inboundConfig);
        assertEquals(originalMapperConfig, inboundConfig);
    }

    @Test
    void testMapper() {
        ConfigHandler configHandler = new ConfigHandler();
        String expectedConfig = FileUtils.getFileContents(VALID_MAPPER_CONFIG_FILE);

//        JsonObject jsonObject = new JsonObject(expectedConfig);
        JsonObject config = new Gson().fromJson(expectedConfig, JsonObject.class);
        Mono<JsonObject> just = Mono.just(config);

//
        final CbsClientConfiguration config2 = mock(CbsClientConfiguration.class);
//        when(config2.appName()).thenReturn("test");
        CbsClient cbsClient = mock(CbsClient.class);
        CbsRequest cbsRequest = mock(CbsRequest.class);
        when(cbsClient.get(any())).thenReturn(just);

//        cbsClient.get(cbsRequest).subscribe();
        MapperConfig mapperConfig1 = configHandler.getMockedClient(cbsClient, cbsRequest);
        System.out.println(mapperConfig1);

        JsonObject wrongConfig = new Gson().fromJson("{\"test\": \"test\"}", JsonObject.class);
        when(cbsClient.get(any())).thenReturn(Mono.just(wrongConfig));

    }

}
