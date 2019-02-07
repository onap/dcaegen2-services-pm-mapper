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
import org.onap.dcaegen2.services.pmmapper.exceptions.ConsulServerError;
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
    private static String cbsConfig;
    private static String validMapperConfig;
    private static String CONSUL_HOST = "my_consult_host";
    private static String CBS_NAME = "config-binding-service";
    private static String HOSTNAME = "pm-mapper-service-name";
    private static int CONSUL_PORT = 8500;
    private String consulURL = "http://" + CONSUL_HOST + ":" + CONSUL_PORT + "/v1/catalog/service/" + CBS_NAME;
    private String cbsURL = "http://" + CBS_NAME + ":10000/service_component/" + HOSTNAME;
    private Gson gson = new Gson();
    @Mock
    private RequestSender sender;

    @BeforeClass()
    public static void beforeClass() throws Exception {
        validMapperConfig = getFileContents("valid_mapper_config.json");
        cbsConfig = getFileContents("valid_cbs_config.json");
    }


    @Before
    public void before() throws Exception {
        PowerMockito.mockStatic(EnvironmentConfig.class);
        PowerMockito.when(EnvironmentConfig.getConsulHost()).thenReturn(CONSUL_HOST);
        PowerMockito.when(EnvironmentConfig.getConsultPort()).thenReturn(CONSUL_PORT);
        PowerMockito.when(EnvironmentConfig.getCbsName()).thenReturn(CBS_NAME);
        PowerMockito.when(EnvironmentConfig.getServiceName()).thenReturn(HOSTNAME);
    }

    @Test
    public void environmentConfig_missing_consulHost() throws Exception {
        PowerMockito.when(EnvironmentConfig.getConsulHost()).thenCallRealMethod();
        assertThrows(EnvironmentConfigException.class, this::getMapperConfig);
    }

    @Test
    public void getMapperConfig_success() throws Exception {
        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(ConfigHandler.class);
        when(sender.send(anyString())).then(invocation -> {
            String url = (String) invocation.getArguments()[0];
            System.out.println(cbsConfig);
            return url.equals(consulURL) ? cbsConfig : validMapperConfig;
        });

        MapperConfig actualConfig = getMapperConfig();
        MapperConfig expectedConfig = gson.fromJson(validMapperConfig, MapperConfig.class);

        assertEquals(expectedConfig, actualConfig);
        assertEquals(logAppender.list.get(0).getMarker().getName(), "ENTRY");
        assertTrue(logAppender.list.get(1).getMessage().contains("Received ConfigBinding Service parameters"));
        assertEquals(logAppender.list.get(1).getMarker().getName(), "EXIT");
        assertTrue(logAppender.list.get(5).getMessage().contains("Received pm-mapper configuration from ConfigBinding Service"));
        logAppender.stop();
    }

    @Test
    public void configbinding_config_format_error() throws Exception {
        when(sender.send(consulURL)).then((invocationMock) -> {
            return "some string that is not cbs config";
        });

        assertThrows(CBSConfigException.class, this::getMapperConfig);
    }

    @Test
    public void consul_host_is_unknown() throws Exception {
        when(sender.send(consulURL)).thenThrow(new UnknownHostException());
        assertThrows(ConsulServerError.class, this::getMapperConfig);
    }

    @Test
    public void configbinding_host_is_unknown() throws Exception {
        when(sender.send(anyString())).then(invocation -> {
            boolean isCBS = invocation.getArguments()[0].equals(cbsURL);
            if (isCBS) {
                throw new UnknownHostException("unknown cbs");
            }
            return cbsConfig;
        });

        assertThrows(CBSServerError.class, this::getMapperConfig);
    }

    @Test
    public void consul_port_invalid() throws Exception {
        PowerMockito.when(EnvironmentConfig.getConsulHost()).thenThrow(EnvironmentConfigException.class);
        assertThrows(EnvironmentConfigException.class, this::getMapperConfig);
    }

    @Test
    public void consul_server_error() throws Exception {
        when(sender.send(consulURL)).thenThrow(new ConsulServerError("consul server error", new Throwable()));
        assertThrows(ConsulServerError.class, this::getMapperConfig);
    }

    @Test
    public void configbinding_server_error() throws Exception {
        when(sender.send(anyString())).then(invocation -> {
            boolean isCBS = invocation.getArguments()[0].equals(cbsURL);
            if (isCBS) {
                throw new CBSServerError("unknown cbs", new Throwable());
            }
            return cbsConfig;
        });

        assertThrows(CBSServerError.class, this::getMapperConfig);
    }

    @Test
    public void mapper_parse_invalid_json() throws Exception {
        when(sender.send(anyString())).then(invocation -> {
            String url = (String) invocation.getArguments()[0];
            return url.equals(consulURL) ? cbsConfig : "mapper config with incorrect format";
        });

        assertThrows(MapperConfigException.class, this::getMapperConfig);
    }

    @Test
    public void mapper_parse_valid_json_missing_attributes() throws Exception {
        when(sender.send(anyString())).then(invocation -> {
            String incompleteConfig = getFileContents("incomplete_mapper_config.json");
            String url = (String) invocation.getArguments()[0];
            return url.equals(consulURL) ? cbsConfig : incompleteConfig;
        });

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
