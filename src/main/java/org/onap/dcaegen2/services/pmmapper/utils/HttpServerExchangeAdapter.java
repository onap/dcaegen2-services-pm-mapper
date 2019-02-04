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

import io.undertow.server.HttpServerExchange;
import org.onap.logging.ref.slf4j.ONAPLogAdapter.RequestAdapter;

/**
 * Logging Adapter for Undertow's {@link HttpServerExchange}
 */

public class HttpServerExchangeAdapter implements RequestAdapter<HttpServerExchangeAdapter>{

    private final HttpServerExchange myRequest;

    /**
     * Construct adapter for the request part of {@link HttpServerExchange}.
     * @param request to be wrapped;
     */
    public HttpServerExchangeAdapter(final HttpServerExchange request) {
        this.myRequest = request;
    }

    @Override
    public String getClientAddress() {
        return myRequest.getSourceAddress().getAddress().toString();
    }

    @Override
    public String getHeader(String headerName) {
        return myRequest.getRequestHeaders().getFirst(headerName);
    }

    @Override
    public String getRequestURI() {
        return myRequest.getRequestURI();
    }

    @Override
    public String getServerAddress() {
       return myRequest.getHostName();
    }

}