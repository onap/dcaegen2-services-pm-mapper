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
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.onap.dcaegen2.services.pmmapper.config.Configurable;
import org.onap.dcaegen2.services.pmmapper.utils.DMaaPAdapter;
import org.onap.dcaegen2.services.pmmapper.utils.GSONRequired;
import org.onap.dcaegen2.services.pmmapper.utils.MeasFilterConfigAdapter;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class MapperConfig implements Configurable {

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
    @SerializedName("dmaap_dr_delete_endpoint")
    private String dmaapDRDeleteEndpoint;

    @GSONRequired
    @SerializedName("pm-mapper-filter")
    @JsonAdapter(MeasFilterConfigAdapter.class)
    private MeasFilterConfig filterConfig;

    @GSONRequired
    @SerializedName("aaf_identity")
    private String aafUsername;

    @GSONRequired
    @SerializedName("aaf_password")
    private String aafPassword;

    @GSONRequired
    @SerializedName("streams_subscribes")
    @JsonAdapter(DMaaPAdapter.class)
    private SubscriberConfig subscriberConfig;

    @GSONRequired
    @SerializedName("streams_publishes")
    @JsonAdapter(DMaaPAdapter.class)
    private PublisherConfig publisherConfig;

    @GSONRequired
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

    public String getPublisherPassword() {
        return this.getAafPassword();
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