/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2021 Samsung Electronics.
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

public class MeasFilterConfigAdapter extends TypeAdapter<MeasFilterConfig> {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(
            LoggerFactory.getLogger(MeasFilterConfigAdapter.class));

    @Override
    public void write(JsonWriter jsonWriter, MeasFilterConfig o) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MeasFilterConfig read(JsonReader jsonReader) throws IOException {
        JsonElement rootElement = JsonParser.parseReader(jsonReader);
        if (rootElement.isJsonObject()) {
            logger.unwrap().debug("Reading filter as an object.");
            return new Gson().fromJson(rootElement, MeasFilterConfig.class);
        } else if (rootElement.isJsonPrimitive()) {
            logger.unwrap().debug("Reading filter as an object in a JSON string.");
            return new Gson().fromJson(rootElement.getAsString(), MeasFilterConfig.class);
        } else {
            logger.unwrap().error("Filter does not appear to be formatted correctly.");
            throw new UnsupportedOperationException("Expected an Object or Object as JSON String.");
        }
    }
}
