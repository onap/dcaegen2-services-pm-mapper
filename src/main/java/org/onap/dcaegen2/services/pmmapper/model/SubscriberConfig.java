/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2022 Nokia. All rights reserved.
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

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.onap.dcaegen2.services.pmmapper.utils.GSONRequired;

@Data
public class SubscriberConfig {

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("location")
    private String drLocation;

    @SerializedName("delivery_url")
    private String deliveryUrl;

    @GSONRequired
    @SerializedName("subscriber_id")
    private String subscriberId;

    @Override
    public String toString() {
        return "SubscriberConfig{" +
                "username=" + username +
                ", password= *****" +
                ", drLocation='" + drLocation + '\'' +
                ", deliveryUrl='" + deliveryUrl + '\'' +
                ", subscriberId='" + subscriberId + '\'' +
                '}';
    }
}
