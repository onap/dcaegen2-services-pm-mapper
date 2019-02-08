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
package org.onap.dcaegen2.pmmapper.config.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.verify.VerificationTimes;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import utils.LoggingUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RequestSender.class)

public class RequestSenderTests {
    private static ClientAndServer mockServer;
    private MockServerClient client = mockClient();

    @BeforeClass
    public static void setup() {
        mockServer = startClientAndServer(1080);
    }

    @AfterClass
    public static void teardown() {
        mockServer.stop();
    }

    @Test
    public void send_success() throws Exception {
        String url = "http://127.0.0.1:1080/once";
        String uuidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
        ListAppender<ILoggingEvent> logAppender = LoggingUtils.getLogListAppender(RequestSender.class);
        HttpRequest req = HttpRequest.request();

        client.when(req
                .withHeader(ONAPLogConstants.Headers.REQUEST_ID, uuidRegex)
                .withHeader(ONAPLogConstants.Headers.INVOCATION_ID, uuidRegex))
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("ResponseBody"));
        String result = new RequestSender().send(url);

        client.verify(req, VerificationTimes.atLeast(1));
        assertEquals(result, "ResponseBody");
        assertTrue(logAppender.list.get(1).getMessage().contains("Sending"));
        assertTrue(logAppender.list.get(2).getMessage().contains("Received"));
        logAppender.stop();
        client.clear(req);
    }

    @Test
    public void host_unavailable_retry_mechanism() throws Exception {
        PowerMockito.mockStatic(Thread.class);

        client.when(request())
                .respond(response().withStatusCode(HttpStatusCode.SERVICE_UNAVAILABLE_503.code()));

        assertThrows(Exception.class, () -> {
            new RequestSender().send("http://127.0.0.1:1080/anypath");
        });

        client.verify(request(), VerificationTimes.exactly(5));
        client.clear(request());
    }

    @Test
    public void host_unknown() throws IOException {
        PowerMockito.mockStatic(Thread.class);
        URL url = PowerMockito.mock(URL.class);
        PowerMockito.when(url.openConnection())
                .thenThrow(UnknownHostException.class);

        assertThrows(Exception.class, () -> {
            new RequestSender().send("http://127.0.0.1:1080/host-is-unknown");
        });

        client.verify(request(), VerificationTimes.exactly(5));
        client.clear(request());
    }

    private MockServerClient mockClient() {
        return new MockServerClient("127.0.0.1", 1080);
    }

}
