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

package org.onap.dcaegen2.services.pmmapper.kpi.config;

import java.util.Optional;
import java.util.function.Function;

import org.onap.dcaegen2.services.pmmapper.kpi.exception.ParsingException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Kpi Json Conversion
 *
 * @author Kai Lu
 *
 */
public class KpiJsonConversion {

    public static final ObjectMapper KPI_OBJECT_MAPPER = new ObjectMapperSupplier().get();

    // Type reference to convert kpi config string to object
    private static final TypeReference<KpiConfig> KPI_CONFIG_TYPE_REF = new TypeReference<KpiConfig>() {
    };

    // KPI JSON conversion function
    public static final Function<String, Optional<KpiConfig>> KPI_CONFIG_JSON_FUNCTION = new
            JsonToJavaObjectBiFunction<KpiConfig>(KPI_OBJECT_MAPPER).curry(KPI_CONFIG_TYPE_REF);

    /**
     * Constructor.
     *
     * @author Kai Lu
     *
     */
    private KpiJsonConversion() {
    }

    /**
     * Convert String Kpi Config Object.
     *
     * @param kpiConfigString kpiConfigString
     *
     * @return kpiConfig
     *
     */
    public static KpiConfig convertKpiConfig(String kpiConfigString) {
        return KPI_CONFIG_JSON_FUNCTION.apply(kpiConfigString).orElseThrow(
                () -> new ParsingException("Unable to parse kpi Config String: " + kpiConfigString,
                        new IllegalArgumentException()));
    }
}
