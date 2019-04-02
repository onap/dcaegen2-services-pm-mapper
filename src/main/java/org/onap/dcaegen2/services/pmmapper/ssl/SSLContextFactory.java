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

import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;

import static java.nio.file.Files.readAllBytes;

public class SSLContextFactory {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(SSLContextFactory.class));
    private static MapperConfig mapperConfig;

    public SSLContextFactory(MapperConfig config) {
        mapperConfig = config;
    }

    public SSLContext createSSLContext(MapperConfig mapperConfig) throws IOException {
        SSLContext sslContext = null;

        try {
            KeyStore keyStore = loadKeyStore(mapperConfig.getKeyStorePath(), mapperConfig.getKeyStorePassPath());
            KeyManager[] keyManagers = createKeyManager(keyStore);

            KeyStore trustStore = loadKeyStore(mapperConfig.getTrustStorePath(), mapperConfig.getTrustStorePassPath());
            TrustManager[] trustManagers = createTrustManager(trustStore);

            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagers, trustManagers, null);
        } catch(KeyManagementException e) {
            logger.unwrap().error("Failed to initialize SSL Context.", e);
        } catch (NoSuchAlgorithmException e) {
            logger.unwrap().error("Failed to create SSL context.", e);
        }
        SSLContext.setDefault(sslContext);
        return sslContext;
    }

    private KeyManager[] createKeyManager(KeyStore keyStore) throws NoSuchAlgorithmException, IOException {
        KeyManager[] keyManager;
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        try {
            keyManagerFactory.init(keyStore, getPassword(mapperConfig.getKeyStorePassPath()).toCharArray());
        } catch (KeyStoreException e) {
            logger.unwrap().error("Failed to initialize keystore.", e);
        } catch (UnrecoverableKeyException e) {
            logger.unwrap().error("Incorrrect password for keystore.", e);
        }
        keyManager = keyManagerFactory.getKeyManagers();

        return keyManager;
    }

    private TrustManager[] createTrustManager(KeyStore trustStore) throws NoSuchAlgorithmException {
        TrustManager[] trustManagers;
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        try {
            trustManagerFactory.init(trustStore);
        } catch (KeyStoreException e) {
            logger.unwrap().error("Failed to initialize truststore.", e);
        }
        trustManagers = trustManagerFactory.getTrustManagers();

        return trustManagers;
    }

    private KeyStore loadKeyStore(String path, String passwordPath) throws IOException, NoSuchAlgorithmException {
        String type = "JKS";
        String encodedKeystore = new String(readAllBytes(Paths.get(path)));
        String password = getPassword(passwordPath);

        KeyStore keyStore = null;

        try {
            keyStore = KeyStore.getInstance(type);
        } catch (KeyStoreException e) {
            logger.unwrap().error("Incorrect type.", e);
        }

        byte[] decodedKeystore = Base64.getMimeDecoder().decode(encodedKeystore);
        InputStream stream = new ByteArrayInputStream(decodedKeystore);

        try {
            keyStore.load(stream, password.toCharArray());
        } catch (CertificateException e) {
            logger.unwrap().error("Failed to load Keystore from given configuration.", e);
        }
        return keyStore;
    }

    private String getPassword(String passwordPath) throws IOException {
        try {
            String password = new String(readAllBytes(Paths.get(passwordPath)));
            password = password.replace("\n", "").replace("\r", "");
            return password;
        } catch (IOException e) {
            logger.unwrap().error("Could not read password from: {}.", passwordPath, e);
            throw new IOException("Password not found");
        }
    }
}
