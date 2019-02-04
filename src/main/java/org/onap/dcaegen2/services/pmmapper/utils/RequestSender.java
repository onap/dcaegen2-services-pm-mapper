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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.stream.Collectors;

import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

public class RequestSender {
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_INTERVAL = 1000;
    private static final String SERVER_ERROR_MESSAGE = "Error on Server";
    private static final int ERROR_START_RANGE = 300;
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(RequestSender.class));

    /**
     * Sends an Http GET request to a given endpoint.
     *
     * @return http response body
     * @throws Exception
     * @throws InterruptedException
     */

    public String send(final String url) throws Exception {
        final UUID invocationID = logger.invoke(ONAPLogConstants.InvocationMode.SYNCHRONOUS);
        final UUID requestID = UUID.randomUUID();
        String result = "";

        for (int i = 1; i <= MAX_RETRIES; i++) {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestProperty(ONAPLogConstants.Headers.REQUEST_ID, requestID.toString());
            connection.setRequestProperty(ONAPLogConstants.Headers.INVOCATION_ID, invocationID.toString());
            connection.setRequestProperty(ONAPLogConstants.Headers.PARTNER_NAME, MapperConfig.CLIENT_NAME);
            logger.unwrap()
                    .info("Sending:\n{}", connection.getRequestProperties());

            try (InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                result = reader.lines()
                        .collect(Collectors.joining("\n"));
                int responseCode = connection.getResponseCode();
                if (!(isWithinErrorRange(responseCode))) {
                    logger.unwrap()
                            .info("Received:\n{}", result);
                    break;
                }

            } catch (Exception e) {
                if (retryLimitReached(i)) {
                    logger.unwrap()
                            .error("Execution error: "+connection.getResponseMessage(), e);
                    throw new Exception(SERVER_ERROR_MESSAGE + ": " + connection.getResponseMessage(), e);
                }
            }

            Thread.sleep(RETRY_INTERVAL);
        }
        return result;
    }

    private boolean retryLimitReached(final int retryCount) {
        return retryCount >= MAX_RETRIES;
    }

    private boolean isWithinErrorRange(final int responseCode) {
        return responseCode >= ERROR_START_RANGE;
    }
}
