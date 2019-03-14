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

package org.onap.dcaegen2.services.pmmapper.filtering;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile.MeasData;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile.MeasData.MeasInfo;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile.MeasData.MeasInfo.MeasType;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile.MeasData.MeasInfo.MeasValue;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile.MeasData.MeasInfo.MeasValue.R;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig.Filter;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

/**
 * Provides filtering of the contents of the 3GPP PM Measurement file.
 **/
public class MeasFilterHandler {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(MeasFilterHandler.class));
    private Filter filter;
    private MeasConverter converter;

    public MeasFilterHandler(MeasConverter converter) {
        this.converter = converter;
    }

    public MeasFilterHandler() {
        this.converter = new MeasConverter();
    }

    public void setFilter(Filter filter) {
       this.filter = filter;
    }

    /**
     * Filters each measInfo node for measTypes that match the given measTypes from filters.
     **/
    public boolean filterByMeasType(Event event) {
      logger.unwrap().debug("Filtering the measurement file by measTypes.");

        MeasCollecFile measCollecFile = event.getMeasCollecFile();
        if(filter.getMeasTypes().isEmpty() || measCollecFile.getMeasData().isEmpty()) {
            return false;
        }

        MeasData measData = measCollecFile.getMeasData().get(0);
        List<MeasInfo> measInfos = measData.getMeasInfo();
        List<MeasInfo> filteredMeasInfos = new ArrayList<>();

        for (int i = 0; i < measInfos.size(); i++) {
            MeasInfo currentMeasInfo = measInfos.get(i);
            List<String> measTypesNode = currentMeasInfo.getMeasTypes();
            if(!measTypesNode.isEmpty()) {
                setMeasInfosFromMeasTypes(currentMeasInfo,filteredMeasInfos);
            }else {
                setMeasInfoFromMeasType(currentMeasInfo,filteredMeasInfos);
            }
        }

        if (filteredMeasInfos.isEmpty()) {
            return false;
        }

        measData.setMeasInfo(filteredMeasInfos);
        String filteredXMl = converter.convert(measCollecFile);
        event.setBody(filteredXMl);
        return true;
    }

    /**
     * Filters the measurement by file type. Measurement files starting with A or C are valid.
     **/
    public boolean filterByFileType(Event event) {
        logger.unwrap().debug("Filtering the measurement by file type.");
        String requestPath  = event.getHttpServerExchange().getRequestPath();
        String fileName = requestPath.substring(requestPath.lastIndexOf('/')+1);
        return (fileName.startsWith("C") || fileName.startsWith("A"));
    }

    private void setMeasInfoFromMeasType(MeasInfo currentMeasInfo,  List<MeasInfo> filteredMeasInfos) {
        MeasValue currentMeasValue = currentMeasInfo.getMeasValue()
                .get(0);
        List<R> measResultsRNodes = currentMeasValue.getR();
        Map<BigInteger, R> mappedR = measResultsRNodes.stream()
                .collect(Collectors.toMap(R::getP, Function.identity()));
        List<R> filteredRs = new ArrayList<>();
        List<MeasType> filteredMeasTypes = currentMeasInfo.getMeasType()
                .stream().filter(mt -> {
                    List<String> measTypeFilters = filter.getMeasTypes();
                    if (measTypeFilters.contains(mt.getValue())) {
                        filteredRs.add(mappedR.get(mt.getP()));
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (!filteredMeasTypes.isEmpty()) {
            currentMeasInfo.replaceMeasType(filteredMeasTypes);
            currentMeasValue.replaceR(filteredRs);
            filteredMeasInfos.add(currentMeasInfo);
        }

    }

    private void setMeasInfosFromMeasTypes(MeasInfo currentMeasInfo, List<MeasInfo> filteredMeasInfos) {
        MeasValue currentMeasValue = currentMeasInfo.getMeasValue()
                .get(0);
        List<String> measTypesNode = currentMeasInfo.getMeasTypes();
        List<String> measResultsNode = currentMeasValue.getMeasResults();
        List<String> filteredMeasResults = new ArrayList<>();

        List<String> filteredMeasTypes = new ArrayList<>();
        for (int j = 0; j < measTypesNode.size(); j++) {
            String currentMeasType = measTypesNode.get(j);
            List<String> measTypeFilters = filter.getMeasTypes();
            if (measTypeFilters.contains(currentMeasType)) {
                filteredMeasTypes.add(currentMeasType);
                filteredMeasResults.add(measResultsNode.get(j));
            }
        }

        if (!filteredMeasTypes.isEmpty()) {
            currentMeasInfo.replaceMeasTypes(filteredMeasTypes);
            currentMeasValue.replaceMeasResults(filteredMeasResults);
            filteredMeasInfos.add(currentMeasInfo);
        }
    }

}
