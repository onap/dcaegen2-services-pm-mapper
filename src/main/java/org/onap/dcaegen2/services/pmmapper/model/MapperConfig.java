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

import java.net.MalformedURLException;
import java.net.URL;

import org.onap.dcaegen2.services.pmmapper.utils.GSONRequired;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class MapperConfig {

    public static final String CLIENT_NAME = "pm-mapper";

    @GSONRequired
    @Getter(AccessLevel.PRIVATE)
    @SerializedName("streams_subscribes")
    private StreamsSubscribes streamsSubscribes;

    @GSONRequired
    @SerializedName("buscontroller_feed_subscription_endpoint")
    private String busControllerSubscriptionEndpoint;

    @GSONRequired
    @SerializedName("buscontroller_feed_id")
    private String busControllerFeedId;

    public String getBusControllerDeliveryUrl() {
        return this.getStreamsSubscribes().getDmaapSubscriber().getDmaapInfo().getDeliveryUrl();
    }

    public String getDcaeLocation() {
        return this.getStreamsSubscribes().getDmaapSubscriber().getDmaapInfo().getLocation();
    }

    public String getBusControllerUserName() {
        return this.getStreamsSubscribes().getDmaapSubscriber().getDmaapInfo().getUsername();
    }

    public String getBusControllerPassword() {
        return this.getStreamsSubscribes().getDmaapSubscriber().getDmaapInfo().getPassword();
    }

    public URL getBusControllerSubscriptionUrl() throws MalformedURLException {
        return new URL(this.getBusControllerSubscriptionEndpoint());
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
    class DmaapInfo {
        @GSONRequired
        private String location;
        private String username;
        private String password;

        @SerializedName("delivery_url")
        private String deliveryUrl;

        @SerializedName("subscriber_id")
        private String subscriberId;
    }
}




