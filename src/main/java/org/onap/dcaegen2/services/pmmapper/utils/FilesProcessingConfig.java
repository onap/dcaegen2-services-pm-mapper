/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia.
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

import java.util.Optional;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;

public class FilesProcessingConfig {

    private static final String ENV_LIMIT_RATE = "PROCESSING_LIMIT_RATE";
    private EnvironmentReader environmentReader;

    public FilesProcessingConfig(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    public int getLimitRate() throws EnvironmentConfigException {
        return Optional.ofNullable(environmentReader.getVariable(ENV_LIMIT_RATE))
            .map(Integer::valueOf)
            .orElseThrow(() -> new EnvironmentConfigException(
                ENV_LIMIT_RATE + " environment variable is not defined or has incorrect value. It must be defined prior to pm-mapper initialization."));
    }


}
