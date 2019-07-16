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

package utils;

import com.google.gson.Gson;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;


public class ConfigUtils {

    /**
     * Returns A MapperConfig Object Created from two files.
     * Fails test in the event of failure to read file.
     * @param mapperConfigFile Filename for the mapper config
     * @return A Mapper Config Object
     */
    public static MapperConfig getMapperConfigFromFile(String mapperConfigFile) {
        return new Gson().fromJson(FileUtils.getFileContents(mapperConfigFile), MapperConfig.class);
    }
}
