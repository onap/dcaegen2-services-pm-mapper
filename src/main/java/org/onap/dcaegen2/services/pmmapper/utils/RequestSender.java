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
package org.onap.dcaegen2.services.pmmapper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.onap.dcaegen2.services.pmmapper.exceptions.ServerResponseException;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.jboss.logging.MDC;

public class RequestSender {
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_INTERVAL = 1000;
    private static final String SERVER_ERROR_MESSAGE = "Error on Server";
    private static final int ERROR_START_RANGE = 300;
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(RequestSender.class));
    public static final String DELETE = "DELETE";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final int DEFAULT_READ_TIMEOUT = 5000;

    /**
     * Works just like {@link RequestSender#send(method,urlString)}, except {@code method }
     * is set to {@code GET} by default.
     * @see RequestSender#send(String,String,String)
     */
    public String send(final String urlString) throws Exception {
        return send("GET", urlString);
    }

    /**
     * Works just like {@link RequestSender#send(method,urlString,body)}, except {@code body }
     * is set to empty String by default.
     * @see RequestSender#send(String,String,String)
     */
    public String send(String method, final String urlString) throws Exception {
       return send(method,urlString,"");
    }

    /**
     * Works just like {@link RequestSender#send(method,urlString,body, basicAuth)}, except {@code basicAuth }
     * is set to empty String by default.
     * @see RequestSender#send(String,String,String,String)
     */
    public String send(String method, final String urlString, final String body) throws Exception {
        return send(method,urlString,body,"");
    }

    /**
     * Sends an http request to a given endpoint.
     * @param method of the outbound request
     * @param urlString representing given endpoint
     * @param body of the request as json
     * @param encodedCredentials base64-encoded username password credentials
     * @return http response body
     * @throws Exception
     */
    public String send(String method, final String urlString, final String body, final String encodedCredentials) throws Exception {
       String invocationID = Optional.ofNullable((String)MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID))
            .orElse(logger.invoke(ONAPLogConstants.InvocationMode.SYNCHRONOUS).toString());
        String requestID =  Optional.ofNullable((String)MDC.get(ONAPLogConstants.MDCs.REQUEST_ID))
            .orElse( UUID.randomUUID().toString());
        String result = "";

        for (int i = 1; i <= MAX_RETRIES; i++) {
            final URL url = new URL(urlString);
            final HttpURLConnection connection = getHttpURLConnection(method, url, invocationID, requestID);


            if("https".equalsIgnoreCase(url.getProtocol())) {
                HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
            }

            if(!encodedCredentials.isEmpty()) {
                connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
            }

            if(!body.isEmpty()) {
                setMessageBody(connection, body);
            }

            logger.unwrap().info("Sending {} request to {}.", method, urlString);

            try (InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                result = reader.lines()
                        .collect(Collectors.joining("\n"));
                int responseCode = connection.getResponseCode();
                if (!(isWithinErrorRange(responseCode))) {
                    logger.unwrap().info("Response code: {}, Server Response Received:\n{}",responseCode, result);
                    break;
                }
            } catch (Exception e) {
                if (retryLimitReached(i)) {
                    logger.unwrap().error("Execution error: {}", connection.getResponseMessage(), e);
                    throw new ServerResponseException(SERVER_ERROR_MESSAGE + ": " + connection.getResponseMessage(), e);
                }
            }

            Thread.sleep(RETRY_INTERVAL);
        }
        return result;
    }

    private HttpURLConnection getHttpURLConnection(String method, URL url, String invocationID, String requestID) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
        connection.setRequestProperty(ONAPLogConstants.Headers.REQUEST_ID, requestID);
        connection.setRequestProperty(ONAPLogConstants.Headers.INVOCATION_ID, invocationID.toString());
        connection.setRequestProperty(ONAPLogConstants.Headers.PARTNER_NAME, MapperConfig.CLIENT_NAME);
        connection.setRequestMethod(method);

        return connection;
    }

    private void setMessageBody(HttpURLConnection connection, String body) throws IOException {
        connection.setRequestProperty("Content-Type",DEFAULT_CONTENT_TYPE);
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(body.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }

    private boolean retryLimitReached(final int retryCount) {
        return retryCount >= MAX_RETRIES;
    }

    private boolean isWithinErrorRange(final int responseCode) {
        return responseCode >= ERROR_START_RANGE;
    }
}