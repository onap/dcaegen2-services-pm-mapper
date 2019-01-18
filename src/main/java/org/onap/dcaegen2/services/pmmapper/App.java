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

package org.onap.dcaegen2.services.pmmapper;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.util.StatusCodes;
import org.onap.dcaegen2.services.pmmapper.config.BusControllerConfig;
import org.onap.dcaegen2.services.pmmapper.datarouter.DataRouterSubscriber;
import org.onap.dcaegen2.services.pmmapper.exceptions.TooManyTriesException;

import java.net.MalformedURLException;
import java.net.URL;

public class App {

    public static void main(String[] args) throws MalformedURLException, InterruptedException, TooManyTriesException {
        DataRouterSubscriber dataRouterSubscriber = new DataRouterSubscriber(event -> {
            event.getHttpServerExchange().unDispatch();
            event.getHttpServerExchange().getResponseSender().send(StatusCodes.OK_STRING);
            System.out.println(event.getMetadata().getProductName());
        });
        BusControllerConfig config = new BusControllerConfig();
        config.setDataRouterSubscribeEndpoint(new URL("http://" + System.getenv("DMAAP_BC_SERVICE_HOST") + ":" + System.getenv("DMAAP_BC_SERVICE_PORT") + "/webapi/dr_subs"));
        dataRouterSubscriber.start(config);

        Undertow.builder()
                .addHttpListener(8081, "0.0.0.0")
                .setHandler(Handlers.routing().add("put", "/sub", dataRouterSubscriber))
                .build().start();
    }
}
