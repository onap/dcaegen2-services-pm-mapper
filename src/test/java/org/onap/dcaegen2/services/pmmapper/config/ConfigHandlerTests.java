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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import reactor.core.publisher.Mono;
import utils.FileUtils;
import utils.LoggingUtils;


@ExtendWith(MockitoExtension.class)
class ConfigHandlerTests {
    private static String validMapperConfig;
    private static String validMapperConfigChanged;

    private static final Path INVALID_CONFIGS_DIRECTORY = Paths.get("src/test/resources/invalid_configs/");
    private static final String EXPECTED_ERROR_MESSAGE_IN_LOG = "Unexpected error occurred during fetching configuration";
    private static final String EXPECTED_CHANGED_VALUE = "https://dmaap-dr-node:8443/delete_changed";


    private final Gson gson = new Gson();
    private final CbsClient cbsClient = mock(CbsClient.class);
    private final CbsRequest cbsRequest = mock(CbsRequest.class);


    @BeforeAll
    static void beforeAll() {
        validMapperConfig = FileUtils.getFileContents("valid_mapper_config.json");
        validMapperConfigChanged = FileUtils.getFileContents("valid_mapper_config_after_change.json");
    }

    @BeforeEach
    void setup() throws Exception {
        Mono<JsonObject> just = createMonoJsonObject(validMapperConfig);
        when(cbsClient.get(any())).thenReturn(just);
    }

    @Test
    void getMapperConfig_success() throws Exception {
        MapperConfig expectedConfig = gson.fromJson(validMapperConfig, MapperConfig.class);

        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(ConfigHandler.class);
        MapperConfig actualConfig = new ConfigHandler(cbsClient, cbsRequest).getMapperConfig();

        assertEquals(expectedConfig.getPublisherTopicUrl(), actualConfig.getPublisherTopicUrl());
        assertEquals(expectedConfig.getPublisherUserName(), actualConfig.getPublisherUserName());
        assertEquals(expectedConfig.getPublisherPassword(), actualConfig.getPublisherPassword());
        assertEquals(expectedConfig, actualConfig);
        logAppender.stop();

    }

    @Test
    void configuration_should_can_be_changed() throws EnvironmentConfigException {
        MapperConfig expectedConfig = gson.fromJson(validMapperConfig, MapperConfig.class);
        Mono<JsonObject> just = createMonoJsonObject(validMapperConfig);

        when(cbsClient.get(any())).thenReturn(just);
        ConfigHandler configHandler = new ConfigHandler(cbsClient, cbsRequest);

        MapperConfig actualConfig = configHandler.getInitialConfiguration();
        assertEquals(expectedConfig.getDmaapDRDeleteEndpoint(), actualConfig.getDmaapDRDeleteEndpoint());

        Mono<JsonObject> justChanged = createMonoJsonObject(validMapperConfigChanged);
        when(cbsClient.get(any())).thenReturn(justChanged);
        MapperConfig changedConfig = configHandler.getMapperConfig();

        System.out.println(changedConfig.getDmaapDRDeleteEndpoint());
        assertEquals(EXPECTED_CHANGED_VALUE, changedConfig.getDmaapDRDeleteEndpoint());
    }

    @Test
    void should_throw_exception_when_configuration_is_not_initialized() {
        String wrongConfigJson = "{\"test\": \"test\"}";
        Mono<JsonObject> just = createMonoJsonObject(wrongConfigJson);

        when(cbsClient.get(any())).thenReturn(just);

        ConfigHandler configHandler = new ConfigHandler(cbsClient, cbsRequest);
        assertThrows(EnvironmentConfigException.class, configHandler::getMapperConfig);
    }

    @Test
    void mapper_parse_invalid_json_mapper_config() {
        String wrongConfigJson = "{\"test\": \"test\"}";
        Mono<JsonObject> just = createMonoJsonObject(wrongConfigJson);

        when(cbsClient.get(any())).thenReturn(just);
        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(ConfigHandler.class);
        new ConfigHandler(cbsClient, cbsRequest).getInitialConfiguration();

        assertConfigurationErrorIsLogged(logAppender);
        logAppender.stop();
    }

    @ParameterizedTest
    @MethodSource("getInvalidConfigs")
    void parse_valid_json_bad_values_mapper_config(String mapperConfig) throws Exception {
        Mono<JsonObject> just = createMonoJsonObject(mapperConfig);

        when(cbsClient.get(any())).thenReturn(just);
        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(ConfigHandler.class);
        new ConfigHandler(cbsClient, cbsRequest).getInitialConfiguration();

        assertConfigurationErrorIsLogged(logAppender);
        logAppender.stop();
    }

    private void assertConfigurationErrorIsLogged(ListAppender<ILoggingEvent> logAppender) {
        boolean containMessage = logAppender.list.stream()
            .anyMatch(iLoggingEvent -> iLoggingEvent.getFormattedMessage().contains(EXPECTED_ERROR_MESSAGE_IN_LOG));
        assertTrue(containMessage);
    }

    private Mono<JsonObject> createMonoJsonObject(String stringJson) {
        JsonObject configJson = new Gson().fromJson(stringJson, JsonObject.class);
        return Mono.just(configJson);
    }


    private static List<String> getInvalidConfigs() throws IOException {
        return FileUtils.getFilesFromDirectory(INVALID_CONFIGS_DIRECTORY);
    }
}
