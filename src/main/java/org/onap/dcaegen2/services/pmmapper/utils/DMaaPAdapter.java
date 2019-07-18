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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Optional;
import org.onap.dcaegen2.services.pmmapper.model.PublisherConfig;
import org.onap.dcaegen2.services.pmmapper.model.SubscriberConfig;

public class DMaaPAdapter extends TypeAdapter<Object> {
    private static final String PUBLISHER = "dmaap_publisher";
    private static final String SUBSCRIBER = "dmaap_subscriber";
    private static final String DMAAP_INFO = "dmaap_info";

    @Override
    public void write(JsonWriter jsonWriter, Object dmaapObj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String rootName = jsonReader.nextName();
        Class configClass = getConfigClass(rootName);

        Object generatedConfig = null;
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            if (jsonReader.nextName().equals(DMAAP_INFO)) {
                generatedConfig  = new Gson().fromJson(jsonReader, configClass);
            } else {
                jsonReader.skipValue();
            }
        }
        if (generatedConfig == null) {
            throw new JsonParseException("Failed to Identify DMaaP Object");
        }

        jsonReader.endObject();
        jsonReader.endObject();
        return generatedConfig;
    }
    private Class getConfigClass(String rootName) {
        Class configClass;
        switch (rootName) {
            case PUBLISHER:
                configClass = PublisherConfig.class;
                break;
            case SUBSCRIBER:
                configClass = SubscriberConfig.class;
                break;
            default:
                String reason = String.format("This adapter expects one of: [ %s, %s]", PUBLISHER, SUBSCRIBER);
                throw new IllegalArgumentException(reason);
        }
        return configClass;
    }
}
