/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 China Mobile.
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

package org.onap.dcaegen2.services.pmmapper.kpi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.VesEvent;
import org.onap.dcaegen2.services.pmmapper.kpi.service.KpiComputation;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;

import utils.FileUtils;

public class KpiComputationTest {

    private static final String KPI_CONFIG_FILE = "kpi/kpi_config.json";
    private static final String VES_MESSAGE_FILE = "kpi/ves_message.json";

    @Test
    void testKpiComputation() {

    	String strKpiConfig = FileUtils.getFileContents(KPI_CONFIG_FILE);
    	String vesMessage = FileUtils.getFileContents(VES_MESSAGE_FILE);

    	MapperConfig config = mock(MapperConfig.class);
        when(config.getKpiConfig()).thenReturn(strKpiConfig);
        HttpServerExchange exchange = mock(HttpServerExchange.class);
        EventMetadata metadata = mock(EventMetadata.class);      

        Event originalEvent = new Event(exchange, "testbody", metadata, new HashMap<String, String>(), "test identify");
        originalEvent.setVes(vesMessage);

        List<Event> vesList = new KpiComputation().checkAndDoComputation(originalEvent, config);
        
        String strVesEvent = vesList.get(0).getVes();
        Gson gson = new Gson();
        VesEvent vesEvent = gson.fromJson(strVesEvent, VesEvent.class);
        assertEquals(vesEvent.getEvent()
                             .getPerf3gppFields()
                             .getMeasDataCollection()
                             .getMeasInfoList().get(0)
                             .getMeasValuesList().get(0)
                             .getMeasResults().get(0)
                             .getSvalue(), "40");    
    }

}
