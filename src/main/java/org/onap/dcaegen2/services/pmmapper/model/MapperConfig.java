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
package org.onap.dcaegen2.services.pmmapper.model;

import org.onap.dcaegen2.services.pmmapper.config.Configurable;
import org.onap.dcaegen2.services.pmmapper.utils.GSONRequired;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class MapperConfig implements Configurable{

    public static final String CLIENT_NAME = "pm-mapper";

    @GSONRequired
    @SerializedName("enable_http")
    private Boolean enableHttp;

    @GSONRequired
    @SerializedName("key_store_path")
    private String keyStorePath;

    @GSONRequired
    @SerializedName("key_store_pass_path")
    private String keyStorePassPath;

    @GSONRequired
    @SerializedName("trust_store_path")
    private String trustStorePath;

    @GSONRequired
    @SerializedName("trust_store_pass_path")
    private String trustStorePassPath;

    @GSONRequired
    @Getter(AccessLevel.PRIVATE)
    @SerializedName("streams_subscribes")
    private StreamsSubscribes streamsSubscribes;

    @GSONRequired
    @Getter(AccessLevel.PRIVATE)
    @SerializedName("streams_publishes")
    private StreamsPublishes streamsPublishes;

    @GSONRequired
    @SerializedName("dmaap_dr_delete_endpoint")
    private String dmaapDRDeleteEndpoint;

    @GSONRequired
    @SerializedName("pm-mapper-filter")
    private MeasFilterConfig filterConfig;

    public String getSubscriberIdentity(){
        return this.getStreamsSubscribes().getDmaapSubscriber().getDmaapInfo().getSubscriberId();
    }

    public String getPublisherTopicUrl() {
        return this.getStreamsPublishes().getDmaapPublisher().getDmaapInfo().getTopicUrl();
    }

    public boolean dmaapInfoEquals(MapperConfig mapperConfig){
        return this
                .getStreamsSubscribes()
                .getDmaapSubscriber()
                .getDmaapInfo()
                .equals(mapperConfig.getStreamsSubscribes().getDmaapSubscriber().getDmaapInfo());
    }

    @Getter
    @EqualsAndHashCode
    private class StreamsSubscribes {
        @GSONRequired
        @SerializedName("dmaap_subscriber")
        DmaapSubscriber dmaapSubscriber;
    }

    @Getter
    @EqualsAndHashCode
    class DmaapSubscriber {
        @GSONRequired
        @SerializedName("dmaap_info")
        DmaapInfo dmaapInfo;
    }

    @Getter
    @EqualsAndHashCode
    private class StreamsPublishes {
        @GSONRequired
        @SerializedName("dmaap_publisher")
        DmaapPublisher dmaapPublisher;
    }

    @Getter
    @EqualsAndHashCode
    class DmaapPublisher {
        @GSONRequired
        @SerializedName("dmaap_info")
        DmaapInfo dmaapInfo;
    }

    @Getter
    @EqualsAndHashCode
    class DmaapInfo {
        private String location;
        private String username;
        private String password;

        @SerializedName("delivery_url")
        private String deliveryUrl;

        @SerializedName("subscriber_id")
        private String subscriberId;

        @SerializedName("aaf_username")
        private String aafUsername;

        @SerializedName("aaf_password")
        private String aafPassword;

        @SerializedName("client_role")
        private String clientRole;

        @SerializedName("client_id")
        private String clientId;

        @SerializedName("topic_url")
        private String topicUrl;
    }

    public String getPublisherUserName() {
        return this.getStreamsPublishes().getDmaapPublisher().getDmaapInfo().getAafUsername();
    }

    public String getPublisherPassword() {
        return this.getStreamsPublishes().getDmaapPublisher().getDmaapInfo().getAafPassword();
    }

    @Override
    public void reconfigure(MapperConfig mapperConfig) {
        if(!this.equals(mapperConfig)) {
            this.filterConfig = mapperConfig.getFilterConfig();
            this.streamsSubscribes = mapperConfig.getStreamsSubscribes();
            this.streamsPublishes = mapperConfig.getStreamsPublishes();
            this.dmaapDRDeleteEndpoint = mapperConfig.getDmaapDRDeleteEndpoint();
        }
    }
}