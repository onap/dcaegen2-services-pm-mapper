/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig.Filter;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementData;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementFile;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementInfo;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementInfo.MeasValue;
import org.onap.dcaegen2.services.pmmapper.utils.MeasConverter;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

/**
 * Provides filtering of the contents of the 3GPP PM Measurement file.
 **/
public class MeasFilterHandler {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(MeasFilterHandler.class));
    public static final String XML_EXTENSION = "xml";
    private static final String PM_TYPE_REGEX = "PM\\d{12}\\+\\d{4,}";
    private final MeasConverter converter;

    public MeasFilterHandler(MeasConverter converter) {
        this.converter = converter;
    }

    /**
     * Filters each measInfo node for measTypes that match the given measTypes from filters.
     **/
    public boolean filterByMeasType(Event event) {
        Optional<Filter> filter = Optional.ofNullable(event.getFilter());
        MeasurementFile measurementFile = event.getMeasurement();

        if (hasNoFilters(filter)) {
            logger.unwrap().info("Skipping filtering by measTypes as filter config does not contain measTypes.");
            return true;
        }

        if (!measurementFile.getMeasurementData().isPresent() || measurementFile.getMeasurementData().get().isEmpty()) {
            logger.unwrap().info("Measurement file will not be processed further as MeasData is empty.");
            return false;
        }

        logger.unwrap().info("Filtering the measurement file by measTypes.");
        MeasurementData measData = measurementFile.getMeasurementData().get().get(0);
        List<MeasurementInfo> measInfos = measData.getMeasurementInfo();
        List<Pattern> filterPatternList = getPatternsFromFilters(filter);

        List<MeasurementInfo> filteredMeasInfos = filterMeasInfos(measInfos, filterPatternList);

        if (filteredMeasInfos.isEmpty()) {
            logger.unwrap().info("No filter match from the current measurement file.");
            return false;
        }
        measData.setMeasurementInfo(filteredMeasInfos);
        String filteredXMl = converter.convert(measurementFile);
        event.setBody(filteredXMl);
        logger.unwrap().info("Successfully filtered the measurement by measTypes.");
        return true;
    }

    private List<MeasurementInfo> filterMeasInfos(List<MeasurementInfo> measInfos, List<Pattern> filterPatternList) {
        List<MeasurementInfo> filteredMeasInfos = new LinkedList<>();
        for (MeasurementInfo currentMeasInfo : measInfos) {
            List<String> measTypesNode = currentMeasInfo.getMeasTypes();
            if (measTypesNode != null && !measTypesNode.isEmpty()) {
                getFilteredMeasInfosFromMeasTypes(currentMeasInfo, filterPatternList).ifPresent(filteredMeasInfos::add);
            } else {
                getFilteredMeasInfoFromMeasType(currentMeasInfo, filterPatternList).ifPresent(filteredMeasInfos::add);
            }
        }
        return  filteredMeasInfos;
    }

    private List<Pattern> getPatternsFromFilters(Optional<Filter> filters) {
        List<Pattern> patternList = new LinkedList<>();
        for (String filter : filters.get().getMeasTypes()) {
            tryToCompileFilter(filter).ifPresent(patternList::add);
        }
        return patternList;
    }
    private Optional<Pattern> tryToCompileFilter(String measType) {
        try {
            return Optional.of(Pattern.compile("^" + measType + "$"));
        } catch (PatternSyntaxException exception) {
            logger.unwrap().warn("Can not parse measType filter: ", exception);
        }
        return Optional.empty();
    }

    /**
     * Filters each measInfo node in the list for measTypes that match the given measTypes from filters.
     **/
    public boolean filterByMeasType(List<Event> events) {
        List<Event> filteredList = events.stream().filter(this::filterByMeasType).collect(Collectors.toList());
        events.clear();
        events.addAll(filteredList);
        return !events.isEmpty();
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
            logger.unwrap().warn("PM measurement file must have an extension of .{}", XML_EXTENSION);
        }
        if(!isValidPMType) {
            logger.unwrap().warn("PM measurement file type not supported");
        }

        return  isXML && isValidPMType;
    }

    private boolean isValidPMType(String fileName) {
        if (fileName.startsWith("C") || fileName.startsWith("A")){
            return true;
        } else if (fileName.startsWith("PM")){
            return validPMType(fileName);
        }
        return false;
    }

    private boolean validPMType(String fileName) {
        if(isValidType(fileName)){
            if(isSpecificDataExtension(fileName)){
                return validSpecificDataExtension(fileName);
            }
            return true;
        }
        return false;
    }

    private boolean isValidType(String fileName) {
        return fileName.matches(PM_TYPE_REGEX +".*\\.xml");
    }

    private boolean isSpecificDataExtension(String fileName) {
        return fileName.matches(PM_TYPE_REGEX +"[A-Z].*\\.xml");
    }

    private boolean validSpecificDataExtension(String fileName) {
        String[] pmTypes = fileName.split(PM_TYPE_REGEX);
        return isValidPMType(pmTypes[1]);

    }

    private boolean isXMLFile(String fileName) {
        return FilenameUtils.getExtension(fileName).equals(XML_EXTENSION);
    }

    private boolean hasMatchingResults(List<MeasurementInfo.MeasType> filteredMeasTypes, MeasValue measValue ) {
        List<MeasValue.R> filteredResults = new ArrayList<>();

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

    private Optional<MeasurementInfo> getFilteredMeasInfoFromMeasType(MeasurementInfo currentMeasInfo, List<Pattern> filters) {
        List<MeasurementInfo.MeasType> filteredMeasTypes = currentMeasInfo.getMeasType().stream()
                .filter(mt -> matchFilters(filters, mt.getValue()))
                .collect(Collectors.toList());

        if(!filteredMeasTypes.isEmpty()) {
            List<MeasValue> filteredMeasValues = currentMeasInfo.getMeasValue().stream()
                    .filter( mv -> hasMatchingResults(filteredMeasTypes, mv))
                    .collect(Collectors.toList());
            currentMeasInfo.replaceMeasType(filteredMeasTypes);
            currentMeasInfo.replaceMeasValue(filteredMeasValues);
            return Optional.of(currentMeasInfo);
        }
        return Optional.empty();
    }

    private boolean matchFilters(List<Pattern> filters, String measType) {
        return filters.stream().anyMatch(filter -> filter.matcher(measType).matches());
    }

    private Optional<MeasurementInfo> getFilteredMeasInfosFromMeasTypes(MeasurementInfo currentMeasInfo, List<Pattern> filters) {
        MeasValue currentMeasValue = currentMeasInfo.getMeasValue()
                .get(0);
        List<String> measTypesNode = currentMeasInfo.getMeasTypes();
        List<String> measResultsNode = currentMeasValue.getMeasResults();
        List<String> filteredMeasResults = new ArrayList<>();

        List<String> filteredMeasTypes = new ArrayList<>();
        for (int j = 0; j < measTypesNode.size(); j++) {
            String currentMeasType = measTypesNode.get(j);
            if (matchFilters(filters, currentMeasType)) {
                filteredMeasTypes.add(currentMeasType);
                filteredMeasResults.add(measResultsNode.get(j));
            }
        }

        if (!filteredMeasTypes.isEmpty()) {
            currentMeasInfo.replaceMeasTypes(filteredMeasTypes);
            currentMeasValue.replaceMeasResults(filteredMeasResults);
            return Optional.of(currentMeasInfo);
        }
        return Optional.empty();
    }

}
