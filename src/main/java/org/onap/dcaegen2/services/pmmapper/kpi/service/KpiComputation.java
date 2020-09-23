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

package org.onap.dcaegen2.services.pmmapper.kpi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.onap.dcaegen2.services.pmmapper.kpi.computation.CommandHandler;
import org.onap.dcaegen2.services.pmmapper.kpi.config.ControlLoopSchemaType;
import org.onap.dcaegen2.services.pmmapper.kpi.config.Kpi;
import org.onap.dcaegen2.services.pmmapper.kpi.config.KpiConfig;
import org.onap.dcaegen2.services.pmmapper.kpi.config.KpiJsonConversion;
import org.onap.dcaegen2.services.pmmapper.kpi.config.MethodForKpi;
import org.onap.dcaegen2.services.pmmapper.kpi.config.Operation;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.CommonEventHeader;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasDataCollection;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasInfo;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasResult;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.MeasValues;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.Perf3gppFields;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.PerformanceEvent;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.VesEvent;
import org.onap.dcaegen2.services.pmmapper.kpi.datamodule.VesJsonConversion;
import org.onap.dcaegen2.services.pmmapper.kpi.exception.KpiComputationException;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

/**
 * KPI computation and published.
 *
 * @author Kai Lu
 */
public class KpiComputation {

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(KpiComputation.class));
    private MapperConfig config;

    public KpiComputation(MapperConfig config) {
        this.config = config;
    }

    /**
     * do KPI computation.
     *
     * @param events    events
     * @return Kpi event list
     *
     */
    public Flux<List<Event>> kpiComputation(List<Event> events) {
        logger.unwrap().info("Publishing KPI VES events to messagerouter.");
        List<Event> modifiedEvents = new ArrayList<>();
        try {
            events.forEach(e -> {
                modifiedEvents.addAll(this.checkAndDoComputation(e, config));
            });
            logger.unwrap().info("KPI computation done successfully");
        } catch (Exception e) {
            logger.unwrap().error("KPI computation done failed.", e);
            return Flux.empty();
        }
        return Flux.just(modifiedEvents);
    }

    /**
     * do KPI computation.
     *
     * @param originalEvent Event
     * @param config config
     * @return Kpi ves event list
     *
     */
    public List<Event> checkAndDoComputation(Event originalEvent, MapperConfig config) {

        String ves = originalEvent.getVes();

        if (ves == null || ves.equalsIgnoreCase("{}")) {
            return null;
        }
        logger.unwrap().info(ves);
        logger.unwrap().info(config.getKpiConfig());
        KpiConfig kpiConfig = KpiJsonConversion.convertKpiConfig(config.getKpiConfig());
        if (kpiConfig == null) {
            logger.unwrap().info("No kpi config.");
            return null;
        }
        VesEvent vesEvent = VesJsonConversion.convertVesEvent(ves);
        // Get event Name
        PerformanceEvent pmEvent = vesEvent.getEvent();
        String eventName = Optional.of(pmEvent).map(PerformanceEvent::getCommonEventHeader)
                .map(CommonEventHeader::getEventName)
                .orElseThrow(() -> new KpiComputationException("Required Field: EventName not present"));
        // Get Kpi's config per event name matching event name
        MethodForKpi methodForKpi = kpiConfig.getMethodForKpi().stream()
                .filter(m -> m.getEventName().equalsIgnoreCase(eventName)).findFirst().orElse(null);
        // if ves event not exist
        if (methodForKpi == null) {
            logger.unwrap().info("No event name matched.");
            return null;
        }

        MeasDataCollection measDataCollection = Optional.of(pmEvent).map(PerformanceEvent::getPerf3gppFields)
                .map(Perf3gppFields::getMeasDataCollection)
                .orElseThrow(() -> new KpiComputationException("Required Field: MeasData not present"));
        // Do computation for each KPI
        List<Event> events = new ArrayList<>();
        List<Kpi> kpis = methodForKpi.getKpis();
        kpis.forEach(k -> {
            Map<String, List<BigDecimal>> measInfoMap = getOperands(measDataCollection, k.getOperands());
            if (measInfoMap == null) {
                logger.unwrap().info("No kpi need to do computation for {}", k.getOperands());
                return;
            }
            ControlLoopSchemaType schemaType = methodForKpi.getControlLoopSchemaType();
            String measType = k.getMeasType();
            Operation operation = k.getOperation();
            VesEvent kpiVesEvent = CommandHandler.handle(operation.value, pmEvent, schemaType, measInfoMap, measType);

            Event event = generateEvent(originalEvent);
            event.setVes(VesJsonConversion.convertVesEventToString(kpiVesEvent));
            events.add(event);

        });
        return events;
    }

    private Event generateEvent(Event event) {
        Event modifiedEvent =  new Event(event.getHttpServerExchange(),
                event.getBody(), event.getMetadata(), event.getMdc(),
                event.getPublishIdentity());
        modifiedEvent.setMeasurement(event.getMeasurement());
        modifiedEvent.setFilter(event.getFilter());
        return modifiedEvent;
    }

    private Map<String, List<BigDecimal>> getOperands(MeasDataCollection measDataCollection, String operands) {
        List<BigDecimal> kpiOperands = new ArrayList<>();
        List<MeasInfo> measInfoList = measDataCollection.getMeasInfoList();
        String[] key = new String[1];
        measInfoList.forEach(m -> {
            List<String> measTypesList = m.getMeasTypes().getMeasTypesList();
            String measValue = measTypesList.stream()
                    .filter(s -> StringUtils.substring(s, 0, operands.length())
                    .equalsIgnoreCase(operands))
                    .findFirst()
                    .orElse(null);
            if (measValue != null) {
                key[0] = measValue.substring(operands.length());
                int index = measTypesList.indexOf(measValue);
                MeasValues measValues = m.getMeasValuesList().stream().findFirst().orElse(null);
                List<MeasResult> measResults = measValues.getMeasResults();
                kpiOperands.add(new BigDecimal(measResults.get(index).getSvalue()));
            }
        });
        if (kpiOperands.size() <= 0) {
            logger.unwrap().info("No measureValues matched");
            return null;
        }
        Map<String, List<BigDecimal>> measInfoMap = new HashMap<>();
        measInfoMap.put(key[0], kpiOperands);
        return measInfoMap;
    }
}
