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

package org.onap.dcaegen2.ssl;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.ssl.SSLContextFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SSLContextFactory.class, KeyStore.class, KeyManagerFactory.class, TrustManagerFactory.class})
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

    private static final Path validConfigPath = Paths.get("src/test/resources/valid_mapper_config.json");

    private String path = "src/test/resources/keystore.jks.b64";
    private String passwordPath = "src/test/resources/password";
    private String passwordPathNotFound = "src/test/resources/nopassword";
    private String password = "pmmapper";


    SSLContextFactory sslContextFactory;

    @Before
    public void setUp() throws Exception {
        validConfigFileContents = new String(Files.readAllBytes(validConfigPath));
        validConfig = new Gson().fromJson(validConfigFileContents, MapperConfig.class);

        sslContextFactory = new SSLContextFactory(validConfig);
    }

    @Test
    public void testLoadKeystore() throws Exception {
        KeyStore result = SSLContextFactory.loadKeyStore(path, passwordPath);

        Assert.assertNotNull(result);
    }

    @Test
    public void testLoadKeystorePasswordNotFound() throws Exception {
        exception.expect(IOException.class);
        exception.expectMessage("Password not found");

        SSLContextFactory.loadKeyStore(path, passwordPathNotFound);
    }

    @Test
    public void testCreateKeyManager() throws Exception {
        PowerMockito.mockStatic(KeyManagerFactory.class);
        PowerMockito.doReturn(keyManagerFactoryMock).when(KeyManagerFactory.class, "getInstance", any());

        KeyManager[] result = SSLContextFactory.createKeyManager(keystoreMock);

        Mockito.verify(keyManagerFactoryMock).init(keystoreMock, password.toCharArray());

        assertSame(keyManagerFactoryMock.getKeyManagers(), result);
    }

    @Test
    public void testCreateTrustManager() throws Exception {
        PowerMockito.mockStatic(TrustManagerFactory.class);
        PowerMockito.doReturn(trustManagerFactoryMock).when(TrustManagerFactory.class, "getInstance", any());

        TrustManager[] result = SSLContextFactory.createTrustManager(keystoreMock);

        Mockito.verify(trustManagerFactoryMock).init(keystoreMock);

        assertSame(trustManagerFactoryMock.getTrustManagers(), result);
    }
}
