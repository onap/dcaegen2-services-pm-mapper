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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
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
    public static final String XML_EXTENSION = "xml";
    private MeasConverter converter;

    public MeasFilterHandler(MeasConverter converter) {
        this.converter = converter;
    }

    /**
     * Filters each measInfo node for measTypes that match the given measTypes from filters.
     **/
    public boolean filterByMeasType(Event event) {
        Optional<Filter> filter = Optional.ofNullable(event.getFilter());
        MeasCollecFile measCollecFile = event.getMeasCollecFile();

        if(hasNoFilters(filter)) {
            logger.unwrap().info("Skipping filtering by measTypes as filter config does not contain measTypes.");
            return true;
        }

        if(measCollecFile.getMeasData().isEmpty()) {
            logger.unwrap().info("Measurement file will not be processed further as MeasData is empty.");
            return false;
        }

        logger.unwrap().info("Filtering the measurement file by measTypes.");
        MeasData measData = measCollecFile.getMeasData().get(0);
        List<MeasInfo> measInfos = measData.getMeasInfo();
        List<MeasInfo> filteredMeasInfos = new ArrayList<>();

        for (int i = 0; i < measInfos.size(); i++) {
            MeasInfo currentMeasInfo = measInfos.get(i);
            List<String> measTypesNode = currentMeasInfo.getMeasTypes();
            if(!measTypesNode.isEmpty()) {
                setMeasInfosFromMeasTypes(currentMeasInfo,filteredMeasInfos, filter.get());
            }else {
                setMeasInfoFromMeasType(currentMeasInfo,filteredMeasInfos, filter.get());
            }
        }

        if (filteredMeasInfos.isEmpty()) {
            logger.unwrap().info("No filter match from the current measurement file.");
            return false;
        }
        measData.setMeasInfo(filteredMeasInfos);
        String filteredXMl = converter.convert(measCollecFile);
        event.setBody(filteredXMl);
        logger.unwrap().info("Successfully filtered the measurement by measTypes.");
        return true;
    }

    /**
     * Filters each measInfo node in the list for measTypes that match the given measTypes from filters.
     **/
    public boolean filterByMeasType(List<Event> events) {
        boolean hasMatchAnyFilter = false;
        for (int i = 0; i < events.size(); i++) {
            Event currentEvent = events.get(i);
            boolean hasMatchingFilter = filterByMeasType(currentEvent);
            if (hasMatchingFilter) {
                hasMatchAnyFilter = true;
            } else {
                events.remove(events.get(i));
            }
        }

        if (!hasMatchAnyFilter) {
            logger.unwrap().info("No filter match from all measurement files.");
            return false;
        }

        return true;
    }

    private boolean hasNoFilters(Optional<Filter> filter) {
        return !filter.isPresent() || filter.get().getMeasTypes().isEmpty();
    }


    /**
     * Filters the measurement by file type. Measurement files starting with A or C are valid.
     **/
    public boolean filterByFileType(Event event) {
        logger.unwrap().debug("Filtering the measurement by file type.");
        String requestPath  = event.getHttpServerExchange().getRequestPath();
        String fileName = requestPath.substring(requestPath.lastIndexOf('/')+1);
        boolean isXML = isXMLFile(fileName);
        boolean isValidPMType = isValidPMType(fileName);
        if(!isXML) {
            logger.unwrap().info("PM measurement file must have an extension of .{}", XML_EXTENSION);
        }
        if(!isValidPMType) {
            logger.unwrap().info("PM measurement file type not supported");
        }

        return  isXML && isValidPMType;
    }

    private boolean isValidPMType(String fileName) {
        return fileName.startsWith("C") || fileName.startsWith("A");
    }

    private boolean isXMLFile(String fileName) {
        return FilenameUtils.getExtension(fileName).equals(XML_EXTENSION);
    }

    private boolean hasMatchingResults(List<MeasType> filteredMeasTypes, MeasValue measValue ) {
        List<R> filteredResults = new ArrayList<>();

        filteredMeasTypes.forEach( mst ->
            measValue.getR().stream()
                .filter(r -> mst.getP().equals(r.getP()))
                .findFirst()
                .ifPresent(filteredResults::add)
        );

        boolean hasResults  = !filteredResults.isEmpty();
        if(hasResults) {
           measValue.replaceR(filteredResults);
        }
       return hasResults;
    }

    private void setMeasInfoFromMeasType(MeasInfo currentMeasInfo, List<MeasInfo> filteredMeasInfos, Filter filter) {
        List<MeasType> filteredMeasTypes = currentMeasInfo.getMeasType().stream()
                .filter(mt -> filter.getMeasTypes().contains(mt.getValue()))
                .collect(Collectors.toList());

        if(!filteredMeasTypes.isEmpty()) {
            List<MeasValue> filteredMeasValues = currentMeasInfo.getMeasValue().stream()
                    .filter( mv -> hasMatchingResults(filteredMeasTypes, mv))
                    .collect(Collectors.toList());
            currentMeasInfo.replaceMeasType(filteredMeasTypes);
            currentMeasInfo.replaceMeasValue(filteredMeasValues);
            filteredMeasInfos.add(currentMeasInfo);
        }
    }

    private void setMeasInfosFromMeasTypes(MeasInfo currentMeasInfo, List<MeasInfo> filteredMeasInfos, Filter filter) {
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
