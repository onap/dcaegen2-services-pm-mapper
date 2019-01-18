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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;


/**
 * Extension of the default deserializer with support for GSONRequired annotation.
 * @param <T> Type of object for deserialization process.
 */
public class RequiredFieldDeserializer<T> implements JsonDeserializer<T> {

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        T obj = new Gson().fromJson(jsonElement, type);
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.getAnnotation(GSONRequired.class) != null) {
                field.setAccessible(true);
                try {
                    if (field.get(obj) == null) {
                        throw new JsonParseException(String.format("Field: '%s', is required but not found.", field.getName()));
                    }
                } catch (Exception exception) {
                    throw new JsonParseException("Failed to check fields.", exception);
                }
            }
        }

        return obj;
    }

}
