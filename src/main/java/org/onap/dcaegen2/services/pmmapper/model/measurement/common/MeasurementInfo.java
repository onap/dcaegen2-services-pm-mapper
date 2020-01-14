/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.dcaegen2.services.pmmapper.model.measurement.common;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"job", "granPeriod", "repPeriod", "measTypes", "measType", "measValue", "measInfoId"})
@Data
public class MeasurementInfo {

    @XmlElement
    protected Job job;
    @XmlElement(required = true)
    protected GranPeriod granPeriod;
    protected RepPeriod repPeriod;
    @XmlList
    protected List<String> measTypes;
    protected List<MeasType> measType;
    protected List<MeasValue> measValue;
    @XmlAttribute
    protected String measInfoId;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Data
    public static class GranPeriod {

        @XmlAttribute(name = "duration", required = true)
        protected Duration duration;
        @XmlAttribute(name = "endTime", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar endTime;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Data
    public static class Job {

        @XmlAttribute(name = "jobId", required = true)
        protected String jobId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"value"})
    @Data
    public static class MeasType {

        @XmlValue
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "Name")
        protected String value;
        @XmlAttribute(name = "p", required = true)
        @XmlSchemaType(name = "positiveInteger")
        protected BigInteger p;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"measResults", "r", "suspect"})
    @Data
    public static class MeasValue {

        @XmlList
        protected List<String> measResults;
        protected List<MeasurementInfo.MeasValue.R> r;
        protected Boolean suspect;
        @XmlAttribute(name = "measObjLdn", required = true)
        protected String measObjLdn;

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {"value"})
        @Data
        public static class R {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "p", required = true)
            @XmlSchemaType(name = "positiveInteger")
            protected BigInteger p;
        }

        public void replaceR(List<R> filteredRs) {
            this.r = filteredRs;

        }

        public void replaceMeasResults(List<String> filteredMeasResults) {
            this.measResults = filteredMeasResults;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Data
    public static class RepPeriod {

        @XmlAttribute(name = "duration", required = true)
        protected Duration duration;
    }

    public void replaceMeasTypes(List<String> newMeasTypes) {
        this.measTypes = newMeasTypes;
    }

    public void replaceMeasType(List<MeasType> filteredMeasTypes) {
        this.measType = filteredMeasTypes;
    }

    public void replaceMeasValue(List<MeasValue> filteredMeasValues) {
        this.measValue = filteredMeasValues;
    }

}