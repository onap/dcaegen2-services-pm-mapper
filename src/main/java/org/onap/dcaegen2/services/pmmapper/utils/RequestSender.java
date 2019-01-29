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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestSender {
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_INTERVAL = 1000;
    public static final String SERVER_ERROR_MESSAGE = "Error on Server";
    public static final int ERROR_START_RANGE = 300;

    /**
     * Sends an Http GET request to a given endpoint.
     *
     * @return http response body
     * @throws Exception
     * @throws InterruptedException
     */
    public String send(final String url) throws Exception {
        log.debug("RequestSender::send: " + url);
        String result = "";
        for (int i = 1; i <= MAX_RETRIES; i++) {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            try (InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                result = reader.lines()
                        .collect(Collectors.joining("\n"));
                int responseCode = connection.getResponseCode();
                if (!(isWithinErrorRange(responseCode))) {
                    break;
                }

            } catch (Exception e) {
                if (retryLimitReached(i)) {
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
