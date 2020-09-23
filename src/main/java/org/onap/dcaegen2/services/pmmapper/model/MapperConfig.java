
/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2020 China Mobile.
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
import com.google.gson.annotations.JsonAdapter;
    @GSONRequired
    @SerializedName("streams_subscribes")
    @JsonAdapter(DMaaPAdapter.class)
    private SubscriberConfig subscriberConfig;
    @GSONRequired
    @SerializedName("streams_publishes")
    @JsonAdapter(DMaaPAdapter.class)
    private PublisherConfig publisherConfig;
    @SerializedName("kpi_config")
    private String kpiConfig;
    public String getSubscriberIdentity() {
        return this.getSubscriberConfig().getSubscriberId();
    }
    public String getPublisherTopicUrl() {
        return this.getPublisherConfig().getTopicUrl();
    }
    public String getPublisherUserName() {
        return this.getAafUsername();
    }
    public void reconfigure(MapperConfig mapperConfig) {
        if (!this.equals(mapperConfig)) {
            this.filterConfig = mapperConfig.getFilterConfig();
            this.publisherConfig = mapperConfig.getPublisherConfig();
            this.subscriberConfig = mapperConfig.getSubscriberConfig();
            this.dmaapDRDeleteEndpoint = mapperConfig.getDmaapDRDeleteEndpoint();
            this.aafUsername = mapperConfig.getAafUsername();
            this.aafPassword = mapperConfig.getAafPassword();
            this.kpiConfig = mapperConfig.getKpiConfig();
        }
    }
    @Override
    public String toString() {
        return "MapperConfig{" +
                "enableHttp=" + enableHttp +
                ", keyStorePath='" + keyStorePath + '\'' +
                ", keyStorePassPath='" + keyStorePassPath + '\'' +
                ", trustStorePath='" + trustStorePath + '\'' +
                ", trustStorePassPath='" + trustStorePassPath + '\'' +
                ", dmaapDRDeleteEndpoint='" + dmaapDRDeleteEndpoint + '\'' +
                ", filterConfig=" + filterConfig +
                ", aafUsername='" + aafUsername + '\'' +
                ", aafPassword= *****" +
                ", subscriberConfig=" + subscriberConfig +
                ", publisherConfig=" + publisherConfig +
                ", kpiConfig=" + kpiConfig +
                '}';
    }
}
