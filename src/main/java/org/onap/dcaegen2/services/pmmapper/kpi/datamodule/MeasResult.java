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

import org.onap.dcaegen2.services.pmmapper.kpi.config.BaseModule;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Measment Result.
 *
 * @author Kai Lu
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MeasResult extends BaseModule {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * p.
     */
    @SerializedName("p")
    private int pvalue;

    /**
     * sValue.
     */
    @SerializedName("sValue")
    private String svalue;

}
