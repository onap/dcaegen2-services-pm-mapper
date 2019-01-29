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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.verify.VerificationTimes;
import org.onap.dcaegen2.services.pmmapper.utils.RequestSender;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

    @BeforeClass
    public static void setEnvironmentVariable() {
        System.setProperty("CONSUL_HOST", "my_consult_host");
        System.setProperty("CONFIG_BINDING_SERVICE", "config-binding-service");
        System.setProperty("HOSTNAME", "hostname");
    }

    @Test
    public void send_success() throws Exception {

        client.when(request())
                .respond(response().withStatusCode(HttpStatusCode.OK_200.code()));

        new RequestSender().send("http://127.0.0.1:1080/once");

        client.verify(request(), VerificationTimes.exactly(1));
        client.clear(request());
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
