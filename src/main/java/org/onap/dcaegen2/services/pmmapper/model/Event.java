/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Nordix Foundation.
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
package org.onap.dcaegen2.services.pmmapper.model;

import io.undertow.server.HttpServerExchange;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig.Filter;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementFile;

/**
 * Class used to pass around relevant inbound event data.
 */
@Data
public class Event {
    @NonNull
    private HttpServerExchange httpServerExchange;
    @NonNull
    private String body;
    @NonNull
    private EventMetadata metadata;
    @NonNull
    private Map<String, String> mdc;
    @NonNull
    private String publishIdentity;

    private MeasurementFile measurement;

    private Filter filter;

    private String ves;
}
