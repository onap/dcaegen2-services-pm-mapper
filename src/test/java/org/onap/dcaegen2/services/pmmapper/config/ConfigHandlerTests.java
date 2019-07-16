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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.CBSServerError;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.exceptions.MapperConfigException;
import org.onap.dcaegen2.services.pmmapper.utils.EnvironmentConfig;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import utils.FileUtils;
import utils.LoggingUtils;

@ExtendWith(MockitoExtension.class)
class ConfigHandlerTests {
    private static String validMapperConfig;
    private static String HOSTNAME = "pm-mapper-service-name";
    private static String CBS_HOST = "cbs_host";
    private static int CBS_PORT = 10000;
    private static Path invalidConfigsDirectory = Paths.get("src/test/resources/invalid_configs/");

    private Gson gson = new Gson();

    @Mock
    private RequestSender sender;

    @Mock
    private static EnvironmentConfig config;

    @BeforeAll
    static void beforeAll() throws Exception {
        validMapperConfig = FileUtils.getFileContents("valid_mapper_config.json");
        config = mock(EnvironmentConfig.class);
        when(config.getServiceName()).thenReturn(HOSTNAME);
        when(config.getCBSPort()).thenReturn(CBS_PORT);
    }

    @BeforeEach
    void setup() throws Exception {
        when(config.getCBSHostName()).thenReturn(CBS_HOST);
    }

    @Test
    void getMapperConfig_success() throws Exception {
        when(config.getCBSHostName()).thenReturn(CBS_HOST);
        when(config.getServiceName()).thenReturn(HOSTNAME);
        when(config.getCBSPort()).thenReturn(CBS_PORT);

        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(ConfigHandler.class);
        String validCbsUrlMapperConfig = "http://" + CBS_HOST + ":" + CBS_PORT + "/service_component_all/" + HOSTNAME;
        when(sender.send(validCbsUrlMapperConfig)).thenReturn(validMapperConfig);
        MapperConfig actualConfig = getMapperConfig();
        JsonObject expectedConfigJson = gson.fromJson(validMapperConfig, JsonObject.class);
        MapperConfig expectedConfig = gson.fromJson(expectedConfigJson, MapperConfig.class);
        assertEquals(expectedConfig, actualConfig);
        assertTrue(logAppender.list.get(1).getMessage().contains("Received pm-mapper configuration from ConfigBinding Service"));
        logAppender.stop();
    }

    @Test
    void configbinding_server_error() throws Exception {
        when(sender.send(anyString())).thenThrow(CBSServerError.class);
        assertThrows(CBSServerError.class, this::getMapperConfig);
    }

    @Test
    void configbinding_server_host_missing() throws Exception {
        when(config.getCBSHostName()).thenThrow(EnvironmentConfigException.class);
        assertThrows(EnvironmentConfigException.class, this::getMapperConfig);
    }

    @Test
    void mapper_parse_invalid_json_mapper_config() throws Exception {
        when(sender.send(anyString())).thenReturn("mapper config with incorrect format");
        assertThrows(MapperConfigException.class, this::getMapperConfig);
    }

    @ParameterizedTest
    @MethodSource("getInvalidConfigs")
    void parse_valid_json_bad_values_mapper_config(String mapperConfig) throws Exception {
        when(sender.send(anyString())).thenReturn(mapperConfig);
        assertThrows(MapperConfigException.class, this::getMapperConfig);
    }

    private MapperConfig getMapperConfig()
            throws UnknownHostException, EnvironmentConfigException, CBSConfigException, Exception {
        return new ConfigHandler(sender, config).getMapperConfig();
    }

    private static List<String> getInvalidConfigs() throws IOException {
        return FileUtils.getFilesFromDirectory(invalidConfigsDirectory);
    }
}
