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

import org.jboss.logging.MDC;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;

import java.util.Optional;
import java.util.UUID;

final class LoggingUtils {

    private LoggingUtils() {
        throw new IllegalStateException("Utility class;shouldn't be constructed");
    }

    static String invocationID(ONAPLogAdapter logger) {
        return Optional.ofNullable((String) MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID))
                .orElse(logger.invoke(ONAPLogConstants.InvocationMode.SYNCHRONOUS).toString());
    }

    static String requestID() {
        return Optional.ofNullable((String) MDC.get(ONAPLogConstants.MDCs.REQUEST_ID))
                .orElse(UUID.randomUUID().toString());
    }
}
