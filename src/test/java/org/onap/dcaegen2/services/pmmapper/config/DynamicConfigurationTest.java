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


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.onap.dcaegen2.services.pmmapper.exceptions.ReconfigurationException;
import org.onap.dcaegen2.services.pmmapper.model.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamicConfiguration.class, EnvironmentConfig.class})
public class DynamicConfigurationTest {
    private static Path VALID_CONFIG_PATH = Paths.get("src/test/resources/valid_mapper_config.json");

    private static ArrayList<Configurable> configurables;
    private DynamicConfiguration objUnderTest;
    private static String config;
    private MapperConfig originalMapperConfig;

    @Mock
    private RequestSender sender;

    @BeforeClass()
    public static void setupBeforeClass() throws Exception {
        config = new String(Files.readAllBytes(VALID_CONFIG_PATH));
    }

    @Before
    public void setup() throws Exception {
        configurables = new ArrayList<>();
        PowerMockito.mockStatic(EnvironmentConfig.class);
        PowerMockito.when(EnvironmentConfig.getCBSHostName()).thenReturn("");
        PowerMockito.when(EnvironmentConfig.getCBSPort()).thenReturn(1);
        PowerMockito.when(EnvironmentConfig.getServiceName()).thenReturn("");

        when(sender.send(any())).thenReturn(config);
        ConfigHandler configHandler = new ConfigHandler(sender);
        originalMapperConfig = configHandler.getMapperConfig();
        objUnderTest = new DynamicConfiguration(configurables, originalMapperConfig);
    }

    @Test
    public void testNoChangeResponse() throws Exception {
        ConfigHandler configHandler = new ConfigHandler(sender);
        originalMapperConfig = configHandler.getMapperConfig();
        objUnderTest.setConfigHandler(configHandler);

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        objUnderTest.handleRequest(httpServerExchange);
        assertEquals(originalMapperConfig, objUnderTest.getOriginalConfig());
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.OK);
    }

    @Test
    public void testApplyOriginalUponFailure() throws Exception {
        ConfigHandler configHandler = new ConfigHandler(sender);
        Configurable configurable = mock(Configurable.class);
        configurables.add(configurable);
        JsonObject modifiedConfig = new JsonParser().parse(config).getAsJsonObject();
        modifiedConfig.addProperty("dmaap_dr_feed_id","3");
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
    public void testSuccessfulReconfiguration() throws Exception {
        ConfigHandler configHandler = new ConfigHandler(sender);
        Configurable configurable = mock(Configurable.class);
        configurables.add(configurable);
        JsonObject modifiedConfig = new JsonParser().parse(config).getAsJsonObject();
        modifiedConfig.addProperty("dmaap_dr_feed_id","3");

        when(sender.send(any())).thenReturn(modifiedConfig.toString());
        MapperConfig modifiedMapperConfig = configHandler.getMapperConfig();
        objUnderTest.setConfigHandler(configHandler);

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class, RETURNS_DEEP_STUBS);
        objUnderTest.handleRequest(httpServerExchange);
        assertEquals(modifiedMapperConfig, objUnderTest.getOriginalConfig());
        verify(httpServerExchange, times(1)).setStatusCode(StatusCodes.OK);
        verify(configurable, times(1)).reconfigure(modifiedMapperConfig);

    }
}
