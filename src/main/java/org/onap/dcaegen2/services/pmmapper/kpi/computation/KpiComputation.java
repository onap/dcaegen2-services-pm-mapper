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

package org.onap.dcaegen2.services.pmmapper.kpi.computation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.onap.dcaegen2.services.pmmapper.kpi.config.Kpi;
import org.onap.dcaegen2.services.pmmapper.kpi.config.KpiConfig;
import org.onap.dcaegen2.services.pmmapper.kpi.config.KpiJsonConversion;
import org.onap.dcaegen2.services.pmmapper.kpi.config.MethodForKpi;
import org.onap.dcaegen2.services.pmmapper.kpi.config.Operation;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.CommonEventHeader;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasDataCollection;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasInfo;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasInfoId;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasResult;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasTypes;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasValues;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.PerformanceEvent;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.Perf3gppFields;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.VesEvent;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.VesJsonConversion;
import org.onap.dcaegen2.services.pmmapper.kpi.exception.KpiComputationException;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

/**
 * KPI Computation.
 *
 * @author Kai Lu
 *
 */

public class KpiComputation {

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(KpiComputation.class));

    /**
     * do KPI computation.
     *
     * @param ves    ves
     * @param config config
     * @return Kpi ves list
     *
     */
    public static List<String> checkAndDoComputation(String ves, MapperConfig config) {

        if (ves == null || ves.equalsIgnoreCase("{}")) {
            return null;
        }

        logger.unwrap().info(ves);
        KpiConfig kpiConfig = KpiJsonConversion.convertKpiConfig(config.getKpiConfig());
        if (kpiConfig == null) {
            logger.unwrap().info("No kpi config.");
            return null;
        }
        VesEvent vesEvent = VesJsonConversion.convertVesEvent(ves);

        // Get event Name
        PerformanceEvent pmEvent = vesEvent.getEvent();
        String eventName = Optional.of(pmEvent).map(PerformanceEvent ::getCommonEventHeader).map(CommonEventHeader::getEventName)
                .orElseThrow(() -> new KpiComputationException("Required Field: EventName not present"));

        // Get Kpi's config per event name matching event name
        MethodForKpi methodForKpi = kpiConfig.getMethodForKpi().stream()
                .filter(m -> m.getEventName().equalsIgnoreCase(eventName)).findFirst().orElse(null);
        // if ves event not exist
        if (methodForKpi == null) {
            logger.unwrap().info("No event name matched.");
            return null;
        }

        MeasDataCollection measDataCollection = Optional.of(pmEvent).map(PMEvent::getPerf3gppFields)
                .map(Perf3gppFields::getMeasDataCollection)
                .orElseThrow(() -> new KpiComputationException("Required Field: MeasData not present"));

        // Do computation for each KPI
        List<String> vesList = new ArrayList<>();
        List<Kpi> kpis = methodForKpi.getKpis();
        kpis.forEach(k -> {
            Map<String, List<BigDecimal>> measInfoMap = getOperands(measDataCollection, k.getOperands());
            if (measInfoMap == null) {
                logger.unwrap().info("No kpi need to do computation for {}", k.getOperands());
                return;
            }
            List<BigDecimal> kpiOperands = measInfoMap.values().stream().findAny().get();
            String keyOperands = measInfoMap.keySet().stream().findAny().get();
            Operation operation = k.getOperation();
            switch (operation) {
            case SUM:
                BigDecimal result = kpiOperands.stream().map(i -> i).reduce(BigDecimal.ZERO, BigDecimal::add);

                // Create ves kpi data
                CommonEventHeader commonEventHeader = new CommonEventHeader();
                commonEventHeader.setDomain(pmEvent.getCommonEventHeader().getDomain());
                commonEventHeader.setEventId(UUID.randomUUID().toString());
                commonEventHeader.setSequence(0);
                commonEventHeader.setEventName(eventName);
                commonEventHeader.setSourceName(pmEvent.getCommonEventHeader().getSourceName());
                commonEventHeader.setReportingEntityName(pmEvent.getCommonEventHeader().getReportingEntityName());
                commonEventHeader.setPriority(pmEvent.getCommonEventHeader().getPriority());
                commonEventHeader.setStartEpochMicrosec(pmEvent.getCommonEventHeader().getStartEpochMicrosec());
                commonEventHeader.setLastEpochMicrosec(pmEvent.getCommonEventHeader().getLastEpochMicrosec());
                commonEventHeader.setVersion(pmEvent.getCommonEventHeader().getVersion());
                commonEventHeader
                        .setVesEventListenerVersion(pmEvent.getCommonEventHeader().getVesEventListenerVersion());
                commonEventHeader.setTimeZoneOffset(pmEvent.getCommonEventHeader().getTimeZoneOffset());

                Perf3gppFields perf3gppFields = new Perf3gppFields();
                perf3gppFields.setPerf3gppFieldsVersion(pmEvent.getPerf3gppFields().getPerf3gppFieldsVersion());

                MeasDataCollection tmpMeasDataCollection = new MeasDataCollection();
                tmpMeasDataCollection.setGranularityPeriod(
                        pmEvent.getPerf3gppFields().getMeasDataCollection().getGranularityPeriod());
                tmpMeasDataCollection.setMeasuredEntityUserName(
                        pmEvent.getPerf3gppFields().getMeasDataCollection().getMeasuredEntityUserName());
                tmpMeasDataCollection
                        .setMeasuredEntityDn(pmEvent.getPerf3gppFields().getMeasDataCollection().getMeasuredEntityDn());
                tmpMeasDataCollection.setMeasuredEntitySoftwareVersion(
                        pmEvent.getPerf3gppFields().getMeasDataCollection().getMeasuredEntitySoftwareVersion());

                List<MeasInfo> measInfoList = new ArrayList<>();

                MeasInfo measInfo = new MeasInfo();

                MeasInfoId measInfoId = new MeasInfoId();
                measInfoId.setSMeasInfoId(methodForKpi.getControlLoopSchemaType().toString());
                MeasTypes measTypes = new MeasTypes();

                List<String> sMeasTypesList = new ArrayList<>();
                sMeasTypesList.add(new StringBuilder().append(k.getMeasType()).append(keyOperands).toString());
                measTypes.setSMeasTypesList(sMeasTypesList);

                List<MeasValues> measValuesList = new ArrayList<>();
                MeasValues measValue = new MeasValues();
                measValue.setSuspectFlag(false);

                List<MeasResult> measResults = new ArrayList<>();
                MeasResult measureMent = new MeasResult();
                measureMent.setP(1);
                measureMent.setSValue(result.toString());
                measResults.add(measureMent);

                measValue.setMeasResults(measResults);
                measValuesList.add(measValue);
                measInfo.setMeasInfoId(measInfoId);
                measInfo.setMeasTypes(measTypes);
                measInfo.setMeasValuesList(measValuesList);
                measInfoList.add(measInfo);

                tmpMeasDataCollection.setMeasInfoList(measInfoList);
                perf3gppFields.setMeasDataCollection(tmpMeasDataCollection);

                VesEvent kpiVesEvent = new VesEvent();
                PMEvent kpiPMEvent = new PMEvent();
                kpiPMEvent.setCommonEventHeader(commonEventHeader);
                kpiPMEvent.setPerf3gppFields(perf3gppFields);
                kpiVesEvent.setEvent(kpiPMEvent);
                vesList.add(VesJsonConversion.convertVesEventToString(kpiVesEvent));
                break;
            case RATIO:
                break;
            case MEAN:
                break;
            // ... ...
            default:
                break;
            }
        });
        return vesList;

    }

    private static Map<String, List<BigDecimal>> getOperands(MeasDataCollection measDataCollection, String operands) {
        List<BigDecimal> kpiOperands = new ArrayList<>();
        List<MeasInfo> measInfoList = measDataCollection.getMeasInfoList();
        String[] key = new String[1];
        measInfoList.forEach(m -> {
            List<String> sMeasTypesList = m.getMeasTypes().getSMeasTypesList();

            String measValue = sMeasTypesList.stream()
                    .filter(s -> StringUtils.substring(s, 0, operands.length()).equalsIgnoreCase(operands)).findFirst()
                    .orElse(null);
            if (measValue != null) {
                key[0] = measValue.substring(operands.length());
                int index = sMeasTypesList.indexOf(measValue);
                MeasValues measValues = m.getMeasValuesList().stream().findFirst().orElse(null);
                List<MeasResult> measResults = measValues.getMeasResults();
                kpiOperands.add(new BigDecimal(measResults.get(index).getSValue()));
            }
        });

        if (kpiOperands.size() <= 0) {
            logger.unwrap().info("No measureValues matched");
            return null;
        }
        Map<String, List<BigDecimal>> measInfoMap = new HashMap<>();
        measInfoMap.put(key[0], kpiOperands);
        System.out.println(kpiOperands);
        return measInfoMap;

    }
}
