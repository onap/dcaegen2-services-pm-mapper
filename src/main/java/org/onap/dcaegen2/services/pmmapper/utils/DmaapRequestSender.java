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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.vavr.control.Try;
import org.onap.dcaegen2.services.sdk.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DmaapClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.DmaapRetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.DmaapTimeoutConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableDmaapRetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableDmaapTimeoutConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableMessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.onap.dcaegen2.services.pmmapper.utils.SendersConfig.MAX_RETRIES;
import static org.onap.dcaegen2.services.pmmapper.utils.SendersConfig.RETRY_INTERVAL;

public class DmaapRequestSender {
    private static final ONAPLogAdapter LOGGER = new ONAPLogAdapter(LoggerFactory.getLogger(DmaapRequestSender.class));

    private static final DmaapRetryConfig RETRY_CONFIG = ImmutableDmaapRetryConfig.builder()
            .retryCount(MAX_RETRIES)
            .retryIntervalInSeconds((int) RETRY_INTERVAL.getSeconds())
            .build();
    private static final MessageRouterPublisherConfig CLIENT_CONFIGURATION =
            ImmutableMessageRouterPublisherConfig.builder()
                    .retryConfig(RETRY_CONFIG)
                    .build();
    private static final DmaapTimeoutConfig READ_TIMEOUT = ImmutableDmaapTimeoutConfig.builder()
            .timeout(SendersConfig.READ_TIMEOUT)
            .build();
    private static final MessageRouterPublisher PUBLISHER =
            DmaapClientFactory.createMessageRouterPublisher(CLIENT_CONFIGURATION);

    /**
     * Sends an http request to a given dmaap-mr topic.
     *
     * @param topicUrl    representing given topic
     * @param vesEvents   of the requests as json
     * @param credentials base64-encoded username password credentials
     * @return dmaap-mr response
     */
    public Flux<MessageRouterPublishResponse> send(final String topicUrl, final List<String> vesEvents, final AafCredentials credentials) {
        MessageRouterPublishRequest request = ImmutableMessageRouterPublishRequest.builder()
                .contentType(ContentType.TEXT_PLAIN)
                .sinkDefinition(sink(topicUrl, credentials))
                .timeoutConfig(READ_TIMEOUT)
                .diagnosticContext(diagnosticContext())
                .build();

        return PUBLISHER.put(request, jsonBatch(vesEvents));
    }

    private static MessageRouterSink sink(String topicUrl, AafCredentials credentials) {
        return ImmutableMessageRouterSink.builder()
                .aafCredentials(credentials)
                .topicUrl(topicUrl)
                .build();
    }

    private static RequestDiagnosticContext diagnosticContext() {
        UUID invocationId = uuid(LoggingUtils.invocationID(LOGGER));
        UUID requestId = uuid(LoggingUtils.requestID());
        return ImmutableRequestDiagnosticContext.builder()
                .invocationId(invocationId)
                .requestId(requestId)
                .build();
    }

    private static Flux<JsonElement> jsonBatch(List<String> events) {
        return Flux.fromIterable(getAsJsonElements(events));
    }

    private static List<JsonElement> getAsJsonElements(List<String> events) {
        return events.stream()
                .map(JsonParser::parseString)
                .collect(Collectors.toList());
    }

    private static UUID uuid(String s) {
        return Try.of(() -> UUID.fromString(s))
                .getOrElse(UUID::randomUUID);
    }
}
