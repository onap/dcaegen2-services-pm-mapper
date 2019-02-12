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
package org.onap.dcaegen2.pmmapper.config;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.dcaegen2.services.pmmapper.config.ConfigHandler;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSServerError;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.model.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.Gson;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import utils.LoggingUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentConfig.class)
public class ConfigHandlerTests {
    private static String validMapperConfig;
    private static String HOSTNAME = "pm-mapper-service-name";
    private static String CBS_HOST = "cbs_host";
    private static int CBS_PORT = 10000;
    private Gson gson = new Gson();
    @Mock
    private RequestSender sender;

    @BeforeClass()
    public static void beforeClass() throws Exception {
        validMapperConfig = getFileContents("valid_mapper_config.json");
    }


    @Before
    public void before() throws Exception {
        PowerMockito.mockStatic(EnvironmentConfig.class);
        PowerMockito.when(EnvironmentConfig.getCBSHostName()).thenReturn(CBS_HOST);
        PowerMockito.when(EnvironmentConfig.getCBSPort()).thenReturn(CBS_PORT);
        PowerMockito.when(EnvironmentConfig.getServiceName()).thenReturn(HOSTNAME);
    }

    @Test
    public void getMapperConfig_success() throws Exception {
        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(ConfigHandler.class);
        String validCbsUrl = "http://" + CBS_HOST + ":" + CBS_PORT +"/service_component/" + HOSTNAME;
        when(sender.send(validCbsUrl)).thenReturn(validMapperConfig);

        MapperConfig actualConfig = getMapperConfig();
        MapperConfig expectedConfig = gson.fromJson(validMapperConfig, MapperConfig.class);

        assertEquals(expectedConfig, actualConfig);
        assertEquals(logAppender.list.get(0).getMarker().getName(), "ENTRY");
        assertTrue(logAppender.list.get(1).getMessage().contains("Received pm-mapper configuration from ConfigBinding Service"));
        assertEquals(logAppender.list.get(1).getMarker().getName(), "EXIT");
        logAppender.stop();
    }

    @Test
    public void configbinding_server_error() throws Exception {
        when(sender.send(anyString())).thenThrow(CBSServerError.class);
        assertThrows(CBSServerError.class, this::getMapperConfig);
    }

    @Test
    public void configbinding_server_host_missing() throws Exception {
        PowerMockito.when(EnvironmentConfig.getCBSHostName()).thenThrow(EnvironmentConfigException.class);
        assertThrows(EnvironmentConfigException.class, this::getMapperConfig);
    }

    @Test
    public void mapper_parse_invalid_json() throws Exception {
        when(sender.send(anyString())).thenReturn("mapper config with incorrect format");
        assertThrows(MapperConfigException.class, this::getMapperConfig);
    }

    @Test
    public void mapper_parse_valid_json_missing_attributes() throws Exception {
        when(sender.send(anyString())).thenReturn(getFileContents("incomplete_mapper_config.json"));
        assertThrows(MapperConfigException.class, this::getMapperConfig);
    }

    private MapperConfig getMapperConfig()
            throws UnknownHostException, EnvironmentConfigException, CBSConfigException, Exception {
        return new ConfigHandler(sender).getMapperConfig();
    }

    private static String getFileContents(String fileName) throws IOException {
        ClassLoader classLoader = ConfigHandlerTests.class.getClassLoader();
        String fileAsString = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(classLoader.getResourceAsStream(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileAsString += line;
            }
        }
        return fileAsString;
    }

}
