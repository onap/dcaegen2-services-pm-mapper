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

package org.onap.dcaegen2.services.pmmapper.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.onap.dcaegen2.services.pmmapper.model.Event;
import java.util.NoSuchElementException;

import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile;
import org.onap.dcaegen2.services.pmmapper.model.MeasCollecFile.MeasData;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

/**
 * Splits the MeasCollecFile based on MeasData.
 **/
public class MeasSplitter {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(MeasSplitter.class));
    private MeasConverter converter;

    public MeasSplitter(MeasConverter converter) {
        this.converter = converter;
    }

    /**
     * Splits the MeasCollecFile to multiple MeasCollecFile based on the number of MeasData
     **/
    public List<Event> split(Event event) {
        logger.unwrap().debug("Splitting 3GPP xml MeasData to individual MeasCollecFile");
        MeasCollecFile currentMeasurement = converter.convert(event.getBody());
        event.setMeasCollecFile(currentMeasurement);
        if(currentMeasurement.getMeasData().isEmpty()) {
            throw new NoSuchElementException("MeasData is empty.");
        }
        return currentMeasurement.getMeasData().stream().map( measData -> {
            Event newEvent  = generateNewEvent(event);
            MeasCollecFile newMeasCollec = generateNewMeasCollec(newEvent,measData);
            newEvent.setMeasCollecFile(newMeasCollec);
            return newEvent;
        }).collect(Collectors.toList());
    }

    private MeasCollecFile generateNewMeasCollec(Event event, MeasData measData) {
        MeasCollecFile measCollec = new MeasCollecFile();
        measCollec.replaceMeasData(Arrays.asList(measData));
        measCollec.setFileHeader(event.getMeasCollecFile().getFileHeader());
        measCollec.setFileFooter(event.getMeasCollecFile().getFileFooter());
        return measCollec;
    }

    private Event generateNewEvent(Event event) {
        Event modifiedEvent =  new Event(event.getHttpServerExchange(),
                event.getBody(), event.getMetadata(), event.getMdc(),
                event.getPublishIdentity());
        modifiedEvent.setMeasCollecFile(event.getMeasCollecFile());
        modifiedEvent.setFilter(event.getFilter());
        return modifiedEvent;
    }
}
