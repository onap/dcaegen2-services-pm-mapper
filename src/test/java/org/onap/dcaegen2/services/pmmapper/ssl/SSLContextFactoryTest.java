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

package org.onap.dcaegen2.services.pmmapper.ssl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import static org.junit.Assert.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class SSLContextFactoryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private KeyStore keystoreMock;

    @Mock
    private KeyManagerFactory keyManagerFactoryMock;

    @Mock
    private TrustManagerFactory trustManagerFactoryMock;

    private static String validConfigFileContents;
    private static MapperConfig validConfig;
    private static MapperConfig inValidConfig;


    private static final Path validConfigPath = Paths.get("src/test/resources/valid_mapper_config.json");

    private String path = "src/test/resources/testkeystore.jks.b64";
    private String passwordPath = "src/test/resources/password";
    private String password = "pmmapper";


    private SSLContextFactory objUnderTest;

    @BeforeEach
    void setUp() throws Exception {
        validConfigFileContents = new String(Files.readAllBytes(validConfigPath));
        JsonObject configObject = new JsonParser().parse(validConfigFileContents).getAsJsonObject();
        validConfig = new Gson().fromJson(configObject, MapperConfig.class);

        objUnderTest = new SSLContextFactory(validConfig);
    }

    @Test
    void testCreateSSLContext() throws IOException {
        SSLContext sslContext = objUnderTest.createSSLContext(validConfig);

        assertNotNull(sslContext);
    }

    @Test
    void testCreateSSLContextInvalidPassword() {
        JsonObject configObject = new JsonParser().parse(validConfigFileContents).getAsJsonObject();
        configObject.addProperty("key_store_pass_path", "src/test/resources/nopassword");
        inValidConfig = new Gson().fromJson(configObject, MapperConfig.class);

        assertThrows(IOException.class, () -> objUnderTest.createSSLContext(inValidConfig));
    }
}