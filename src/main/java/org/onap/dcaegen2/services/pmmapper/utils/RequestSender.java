/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2021 Nokia.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

import org.onap.dcaegen2.services.pmmapper.exceptions.RequestFailure;
import org.onap.dcaegen2.services.pmmapper.exceptions.ServerResponseException;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class RequestSender {
    private static final String SERVER_ERROR_MESSAGE = "Error on Server";
    private static final int ERROR_START_RANGE = 300;
    private static final ONAPLogAdapter LOGGER = new ONAPLogAdapter(LoggerFactory.getLogger(RequestSender.class));

    /**
     * Works just like {@link RequestSender#send(method,urlString)}, except {@code method }
     * is set to {@code GET} by default.
     * @see RequestSender#send(String,String,String)
     */
    public String send(final String urlString) throws InterruptedException {
        return send("GET", urlString);
    }

    /**
     * Works just like {@link RequestSender#send(method,urlString,body)}, except {@code body }
     * is set to empty String by default.
     * @see RequestSender#send(String,String,String)
     */
    public String send(String method, final String urlString) throws InterruptedException {
       return send(method,urlString,"");
    }

    /**
     * Works just like {@link RequestSender#send(method,urlString,body, basicAuth)}, except {@code basicAuth }
     * is set to empty String by default.
     * @see RequestSender#send(String,String,String,String)
     */
    public String send(String method, final String urlString, final String body) throws InterruptedException {
        return send(method,urlString,body,"");
    }

    /**
     * Sends an http request to a given endpoint.
     * @param method of the outbound request
     * @param urlString representing given endpoint
     * @param body of the request as json
     * @param encodedCredentials base64-encoded username password credentials
     * @return http response body
     * @throws InterruptedException
     */
    public String send(String method, final String urlString, final String body, final String encodedCredentials)
             throws InterruptedException {
        String result = "";
        boolean status = false;
        int attempts = 0;
        try {
            while (!status && attempts <= SendersConfig.MAX_RETRIES) {
                if(attempts != 0) {
                    Thread.sleep(SendersConfig.RETRY_INTERVAL.toMillis());
                }
                final URL url = new URL(urlString);
                final HttpURLConnection connection = getHttpURLConnection(method, url);

                if ("https".equalsIgnoreCase(url.getProtocol())) {
                    HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
                }

                if (!encodedCredentials.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
                }

                if (!body.isEmpty()) {
                    setMessageBody(connection, body);
                }
                result = getResult(attempts, connection);
                status = !isWithinErrorRange(connection.getResponseCode());
                attempts++;
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            LOGGER.unwrap().warn("Request failure", ex);
            throw new RequestFailure(ex);
        }
        return result;
    }

    private HttpURLConnection getHttpURLConnection(String method, URL url)
                throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout((int) SendersConfig.READ_TIMEOUT.toMillis());
        connection.setRequestProperty(ONAPLogConstants.Headers.REQUEST_ID, LoggingUtils.requestID());
        connection.setRequestProperty(ONAPLogConstants.Headers.INVOCATION_ID, LoggingUtils.invocationID(LOGGER));
        connection.setRequestProperty(ONAPLogConstants.Headers.PARTNER_NAME, MapperConfig.CLIENT_NAME);
        connection.setRequestMethod(method);
        return connection;
    }

    private void setMessageBody(HttpURLConnection connection, String body) throws IOException {
        connection.setRequestProperty("Content-Type", ContentType.TEXT_PLAIN.toString());
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(body.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }

    private boolean retryLimitReached(final int retryCount) {
        return retryCount >= SendersConfig.MAX_RETRIES;
    }

    private boolean isWithinErrorRange(final int responseCode) {
        return responseCode >= ERROR_START_RANGE;
    }

    private String getResult(int attemptNumber, HttpURLConnection connection) throws IOException {
        LOGGER.unwrap().info("Sending {} request to {}.", connection.getRequestMethod(), connection.getURL());
        String result = "";
        try (InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            result = reader.lines().collect(Collectors.joining("\n"));
            int responseCode = connection.getResponseCode();
            if (!(isWithinErrorRange(responseCode))) {
                LOGGER.unwrap().info("Response code: {}, Server Response Received:\n{}", responseCode, result);
            }
        } catch (Exception e) {
            if (retryLimitReached(attemptNumber)) {
                LOGGER.unwrap().error("Execution error: {}", connection.getResponseMessage(), e);
                throw new ServerResponseException(SERVER_ERROR_MESSAGE + ": " + connection.getResponseMessage(), e);
            }
        }
        return result;
    }
}
