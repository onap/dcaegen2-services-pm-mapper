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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;


@ExtendWith(MockitoExtension.class)
class AppTest {

    @Test
    void testHandleBackPressureNullValue() {
        assertThrows(NullPointerException.class, () -> App.handleBackPressure(null));
    }

    @Test
    void testHandleBackPressure() {
        Event event = utils.EventUtils.makeMockEvent("", mock(EventMetadata.class));
        App.handleBackPressure(event);
        verify(event.getHttpServerExchange(), times(1)).setStatusCode(StatusCodes.TOO_MANY_REQUESTS);
        verify(event.getHttpServerExchange(), times(1)).unDispatch();
    }

    @Test
    void testReceiveRequestNullValue() {
        assertThrows(NullPointerException.class, () -> App.receiveRequest(null));
    }

    @Test
    void testReceiveRequest() {
        Event event = utils.EventUtils.makeMockEvent("", mock(EventMetadata.class));
        App.receiveRequest(event);
        verify(event.getHttpServerExchange(), times(1)).setStatusCode(StatusCodes.OK);
        verify(event.getHttpServerExchange(), times(1)).unDispatch();
    }


}
