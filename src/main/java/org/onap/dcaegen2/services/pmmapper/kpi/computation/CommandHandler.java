/*-
 * ============LICENSE_START=======================================================
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

package org.onap.dcaegen2.services.pmmapper.kpi.computation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.onap.dcaegen2.services.pmmapper.kpi.config.ControlLoopSchemaType;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.PerformanceEvent;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.VesEvent;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

public class CommandHandler {

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(CommandHandler.class));

    /**
     * The method to handle data.
     * 
     * @param className class name
     * @param obj
     * @return VesEvent
     */
    public static VesEvent handle(String className, PerformanceEvent pmEvent, ControlLoopSchemaType schemaType,
            Map<String, List<BigDecimal>> measInfoMap, String measType) {
        try {
            // Load Command Object
            Command c = (Command) Class.forName(className).getDeclaredConstructor().newInstance();
            return c.handle(pmEvent, schemaType, measInfoMap, measType);
        } catch (Exception e) {
            logger.unwrap().error(e.getMessage());
        }
        return null;
    }

}