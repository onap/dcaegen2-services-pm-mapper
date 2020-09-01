/*
 * ================================================================================
 * Copyright (c) 2020 China Mobile. All rights reserved.
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
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.dcaegen2.services.pmmapper.kpi.datamodule;

import java.util.List;

import org.onap.dcaegen2.services.pmmapper.kpi.config.BaseModule;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MeasTypes extends BaseModule {

    /**
     * sValue
     */
    private static final long serialVersionUID = 1L;

    /**
     * sMeasTypesList
     */
    @JsonProperty(value = "sMeasTypesList")
    private List<String> sMeasTypesList;
}
