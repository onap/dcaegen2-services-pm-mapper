/*-
 * ============LICENSE_START=======================================================
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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.verify.VerificationTimes;
import org.onap.dcaegen2.services.sdk.model.streams.ImmutableAafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.DmaapResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class DmaapRequestSenderTest {

    private static final ImmutableAafCredentials AAF_CREDENTIALS = ImmutableAafCredentials.builder()
            .username("")
            .password("")
            .build();
    public static final List<String> SINGLE = Collections.singletonList("any");

    private static ClientAndServer mockServer;
    private final MockServerClient client = mockClient();

    @BeforeClass
    public static void setup() {
        mockServer = startClientAndServer(35454);
    }

    @AfterClass
    public static void teardown() {
        mockServer.stop();
    }

    @Before
    public void setUp() {
        client.reset();
    }

    @Test
    public void send_success() {
        client.when(request()).respond(response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withBody("ResponseBody"));

        Flux<MessageRouterPublishResponse> result = new DmaapRequestSender()
                .send("http://127.0.0.1:35454/once", SINGLE, AAF_CREDENTIALS);

        StepVerifier.create(result)
                .expectNextMatches(DmaapResponse::successful)
                .verifyComplete();
        client.verify(request(), VerificationTimes.once());
    }

    @Test
    public void host_unavailable_retry_mechanism() {
        client.when(request())
                .respond(response().withStatusCode(HttpStatusCode.SERVICE_UNAVAILABLE_503.code()));

        Flux<MessageRouterPublishResponse> result = new DmaapRequestSender()
                .send("http://127.0.0.1:35454/anypath", SINGLE, AAF_CREDENTIALS);

        StepVerifier.create(result)
                .expectNextMatches(DmaapResponse::failed)
                .verifyComplete();
        client.verify(request(), VerificationTimes.exactly(5));
    }

    @Test
    public void host_unknown() {
        Flux<MessageRouterPublishResponse> result = new DmaapRequestSender()
                .send("http://unknown-host:35454/host-is-unknown", SINGLE, AAF_CREDENTIALS);

        StepVerifier.create(result)
                .verifyError();
        client.verify(request(), VerificationTimes.exactly(0));
    }

    private MockServerClient mockClient() {
        return new MockServerClient("127.0.0.1", 35454);
    }
}
