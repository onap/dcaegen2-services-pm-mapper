/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  son-handler
 *  ================================================================================
 *   Copyright (C) 2019 Wipro Limited.
 *   ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *  
 *          http://www.apache.org/licenses/LICENSE-2.0
 *  
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *  
 *******************************************************************************/

package org.onap.dcaegen2.services.pmmapper.kpi.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class GsonTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        String str = "{\r\n" + 
                "   \"domain\": \"measurementsForKpi\",\r\n" + 
                "   \"methodForKPI\": [{\r\n" + 
                "       \"eventName\": \"Measurements_CORE_KPI\",\r\n" + 
                "       \"controlLoopSchemaType\": \"SLICE\",\r\n" + 
                "       \"policyScope\": \"resource=networkSlice;type=configuration\",\r\n" + 
                "       \"policyName\": \"configuration.dcae.microservice.pm-mapper.xml\",\r\n" + 
                "       \"policyVersion\": \"v0.0.1\",\r\n" + 
                "       \"kpis\": [{\r\n" + 
                "               \"measType\": \"AMFRegNbr\",\r\n" + 
                "               \"operation\": \"SUM\",\r\n" + 
                "               \"operands\": \"$.event.perf3gppFields.measDataCollection.measInfoList[*].measTypes.sMeasTypesList.RM.RegisteredSubNbrMean\",\r\n" + 
                "               \"condition\": \"$.event.perf3gppFields.measDataCollection.measInfoList[*].measTypes.sMeasTypesList.RM.RegisteredSubNbrMean*\"\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "           },\r\n" + 
                "           {\r\n" + 
                "               \"measType\": \"UpstreanThr\",\r\n" + 
                "               \"operation\": \"SUM\",\r\n" + 
                "               \"operands\": \"$.event.perf3gppFields.measDataCollection.measInfoList[*].measTypes.sMeasTypesList.GTP.InDataOctN3UPF\",\r\n" + 
                "               \"condition\": \"$.event.perf3gppFields.measDataCollection.measInfoList[*].measTypes.sMeasTypesList.GTP.InDataOctN3UPF*\"\r\n" + 
                "\r\n" + 
                "           },\r\n" + 
                "           {\r\n" + 
                "               \"measType\": \"DownstreamThr\",\r\n" + 
                "               \"operation\": \"SUM\",\r\n" + 
                "               \"operands\": \"$.event.perf3gppFields.measDataCollection.measInfoList[*].measTypes.sMeasTypesList.GTP.OutDataOctN3UPF\",\r\n" + 
                "               \"condition\": \"$.event.perf3gppFields.measDataCollection.measInfoList[*].measTypes.sMeasTypesList.GTP.OutDataOctN3UPF*\"\r\n" + 
                "           }\r\n" + 
                "       ]\r\n" + 
                "   }]\r\n" + 
                "}";

        Gson gson = new Gson();
        KpiConfig kpiConfig = gson.fromJson(str, KpiConfig.class);
        System.out.println(kpiConfig);
        
    }

}
