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

import org.onap.dcaegen2.services.pmmapper.exceptions.ProcessEventException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

public class DataRouterUtils {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(DataRouterUtils.class));

    private DataRouterUtils(){
        throw new IllegalStateException("Utility class;shouldn't be constructed");
    }

    /**
     * Sends Delete to DR required as part of the new guaranteed delivery mechanism.
     * @param config used to determine subscriber id and target endpoint
     * @param event event to be processed
     */
    public static String processEvent(MapperConfig config, Event event){
        logger.unwrap().info("Sending processed to DataRouter");
        String baseDelete = config.getDmaapDRDeleteEndpoint();
        String subscriberIdentity = config.getSubscriberIdentity();
        String delete = String.format("%s/%s/%s", baseDelete, subscriberIdentity, event.getPublishIdentity());
        try {
            return new RequestSender().send("DELETE", delete);
        } catch (Exception exception) {
            throw new ProcessEventException("Process event failure", exception);
        }
    }
}