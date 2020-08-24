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

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.pmmapper.kpi.config.KpiConfig;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.VesEvent;

import com.google.gson.Gson;

public class KpiTest {

    @Test
    void testKpiConfigValidate() {

        String strKpiConfig = "{\r\n" + 
                "    \"domain\": \"measurementsForKpi\",\r\n" + 
                "    \"methodForKpi\": [{\r\n" + 
                "            \"eventName\": \"perf3gpp_CORE_AMF_pmMeasResult\",\r\n" + 
                "            \"controlLoopSchemaType\": \"SLICE\",\r\n" + 
                "            \"policyScope\": \"resource=networkSlice;type=configuration\",\r\n" + 
                "            \"policyName\": \"configuration.dcae.microservice.pm-mapper.xml\",\r\n" + 
                "            \"policyVersion\": \"v0.0.1\",\r\n" + 
                "            \"kpis\": [{\r\n" + 
                "                \"measType\": \"AMFRegNbr\",\r\n" + 
                "                \"operation\": \"SUM\",\r\n" + 
                "                \"operands\": \"RM.RegisteredSubNbrMean\"\r\n" + 
                "            }]\r\n" + 
                "        },\r\n" + 
                "        {\r\n" + 
                "            \"eventName\": \"perf3gpp_CORE_UPF_pmMeasResult\",\r\n" + 
                "            \"controlLoopSchemaType\": \"SLICE\",\r\n" + 
                "            \"policyScope\": \"resource=networkSlice;type=configuration\",\r\n" + 
                "            \"policyName\": \"configuration.dcae.microservice.pm-mapper.xml\",\r\n" + 
                "            \"policyVersion\": \"v0.0.1\",\r\n" + 
                "            \"kpis\": [{\r\n" + 
                "                    \"measType\": \"UpstreamThr\",\r\n" + 
                "                    \"operation\": \"SUM\",\r\n" + 
                "                    \"operands\": \"GTP.InDataOctN3UPF\"\r\n" + 
                "\r\n" + 
                "                },\r\n" + 
                "                {\r\n" + 
                "                    \"measType\": \"DownstreamThr\",\r\n" + 
                "                    \"operation\": \"SUM\",\r\n" + 
                "                    \"operands\": \"GTP.OutDataOctN3UPF\"\r\n" + 
                "                }\r\n" + 
                "            ]\r\n" + 
                "        }\r\n" + 
                "    ]\r\n" + 
                "}";

        Gson gson = new Gson();
        KpiConfig kpiConfig = gson.fromJson(strKpiConfig, KpiConfig.class);
        assertEquals(kpiConfig.getDomain(), "measurementsForKpi");      
    }

    @Test
    void testVesEventValidate() {

        String strVesEvent = "{\r\n" + 
                "    \"event\":{\r\n" + 
                "        \"commonEventHeader\":{\r\n" + 
                "            \"domain\":\"perf3gpp\",\r\n" + 
                "            \"eventId\":\"23025c2d-ea39-4bda-8356-f4ddb122737c\",\r\n" + 
                "            \"sequence\":0,\r\n" + 
                "            \"eventName\":\"perf3gpp_AcmeNode-Acme_pmMeasResult\",\r\n" + 
                "            \"sourceName\":\"oteNB5309\",\r\n" + 
                "            \"reportingEntityName\":\"\",\r\n" + 
                "            \"priority\":\"Normal\",\r\n" + 
                "            \"startEpochMicrosec\":1591099200000,\r\n" + 
                "            \"lastEpochMicrosec\":1591100100000,\r\n" + 
                "            \"version\":\"4.0\",\r\n" + 
                "            \"vesEventListenerVersion\":\"7.1\",\r\n" + 
                "            \"timeZoneOffset\":\"UTC+05:00\"\r\n" + 
                "        },\r\n" + 
                "        \"perf3gppFields\":{\r\n" + 
                "            \"perf3gppFieldsVersion\":\"1.0\",\r\n" + 
                "            \"measDataCollection\":{\r\n" + 
                "                \"granularityPeriod\":1591100100000,\r\n" + 
                "                \"measuredEntityUserName\":\"\",\r\n" + 
                "                \"measuredEntityDn\":\"AMFMeasurement\",\r\n" + 
                "                \"measuredEntitySoftwareVersion\":\"r0.1\",\r\n" + 
                "                \"measInfoList\":[\r\n" + 
                "                    {\r\n" + 
                "                        \"measInfoId\":{\r\n" + 
                "                            \"sMeasInfoId\":\"AMFFunction0\"\r\n" + 
                "                        },\r\n" + 
                "                        \"measTypes\":{\r\n" + 
                "                            \"sMeasTypesList\":[\r\n" + 
                "                                \"RM.RegisteredSubNbrMean.08_010101\"\r\n" + 
                "                            ]\r\n" + 
                "                        },\r\n" + 
                "                        \"measValuesList\":[\r\n" + 
                "                            {\r\n" + 
                "                                \"measObjInstId\":\"\",\r\n" + 
                "                                \"suspectFlag\":\"false\",\r\n" + 
                "                                \"measResults\":[\r\n" + 
                "                                    {\r\n" + 
                "                                        \"p\":1,\r\n" + 
                "                                        \"sValue\":\"10\"\r\n" + 
                "                                    }\r\n" + 
                "                                ]\r\n" + 
                "                            }\r\n" + 
                "                        ]\r\n" + 
                "                    },\r\n" + 
                "                    {\r\n" + 
                "                        \"measInfoId\":{\r\n" + 
                "                            \"sMeasInfoId\":\"AMFFunction1\"\r\n" + 
                "                        },\r\n" + 
                "                        \"measTypes\":{\r\n" + 
                "                            \"sMeasTypesList\":[\r\n" + 
                "                                \"RM.RegisteredSubNbrMean.08_010101\"\r\n" + 
                "                            ]\r\n" + 
                "                        },\r\n" + 
                "                        \"measValuesList\":[\r\n" + 
                "                            {\r\n" + 
                "                                \"measObjInstId\":\"\",\r\n" + 
                "                                \"suspectFlag\":\"false\",\r\n" + 
                "                                \"measResults\":[\r\n" + 
                "                                    {\r\n" + 
                "                                        \"p\":1,\r\n" + 
                "                                        \"sValue\":\"20\"\r\n" + 
                "                                    }\r\n" + 
                "                                ]\r\n" + 
                "                            }\r\n" + 
                "                        ]\r\n" + 
                "                    }\r\n" + 
                "                ]\r\n" + 
                "            }\r\n" + 
                "        }\r\n" + 
                "    }\r\n" + 
                "}";
        Gson gson = new Gson();
        VesEvent vesEvent = gson.fromJson(strVesEvent, VesEvent.class);
        assertEquals(vesEvent.getEvent().getCommonEventHeader().getDomain(), "perf3gpp");
    }
    

}
