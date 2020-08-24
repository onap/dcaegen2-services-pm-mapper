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

package org.onap.dcaegen2.services.pmmapper.kpi.datamodule;

import java.util.Optional;
import java.util.function.Function;

import org.onap.dcaegen2.services.pmmapper.kpi.config.JsonToJavaObjectBiFunction;
import org.onap.dcaegen2.services.pmmapper.kpi.config.ObjectMapperSupplier;
import org.onap.dcaegen2.services.pmmapper.kpi.exception.ParsingException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VesJsonConversion {

    public static final ObjectMapper VES_OBJECT_MAPPER = new ObjectMapperSupplier().get();

    // Type reference to convert ves string to ves object
    private static final TypeReference<VesEvent> PM_EVENT_TYPE_REF = new TypeReference<VesEvent>() {
    };

    // Ves JSON conversion function
    public static final Function<String, Optional<VesEvent>> VES_EVENT_JSON_FUNCTION = new
            JsonToJavaObjectBiFunction<VesEvent>(VES_OBJECT_MAPPER).curry(PM_EVENT_TYPE_REF);

    private VesJsonConversion() {
    }

    public static VesEvent convertVesEvent(String vesEvent) {
        return VES_EVENT_JSON_FUNCTION.apply(vesEvent).orElseThrow(
                () -> new ParsingException("Unable to parse ves event String: " + vesEvent,
                        new IllegalArgumentException()));
    }

    public static String convertVesEventToString(VesEvent vesEvent) {
        try {
            return VES_OBJECT_MAPPER.writeValueAsString(vesEvent);
        } catch (JsonProcessingException e) {
            throw new ParsingException("Unable to parse ves event to String: " + vesEvent,
                    new IllegalArgumentException());
        }
    }
}
