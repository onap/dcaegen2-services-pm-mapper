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

package org.onap.dcaegen2.services.pmmapper.model;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fileHeader",
    "measData",
    "fileFooter"
})
@XmlRootElement(name = "measCollecFile")
@Data
public class MeasCollecFile {

    @XmlElement(required = true)
    protected MeasCollecFile.FileHeader fileHeader;
    protected List<MeasCollecFile.MeasData> measData;
    @XmlElement(required = true)
    protected MeasCollecFile.FileFooter fileFooter;
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "measCollec"
    })
    @Data
    public static class FileFooter {

        @XmlElement(required = true)
        protected MeasCollecFile.FileFooter.MeasCollec measCollec;

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class MeasCollec {
            @XmlAttribute(name = "endTime", required = true)
            @XmlSchemaType(name = "dateTime")
            protected XMLGregorianCalendar endTime;
        }

    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fileSender",
        "measCollec"
    })
    @Data
    public static class FileHeader {
        @XmlElement(required = true)
        protected MeasCollecFile.FileHeader.FileSender fileSender;
        @XmlElement(required = true)
        protected MeasCollecFile.FileHeader.MeasCollec measCollec;
        @XmlAttribute(name = "fileFormatVersion", required = true)
        protected String fileFormatVersion;
        @XmlAttribute(name = "vendorName")
        protected String vendorName;
        @XmlAttribute(name = "dnPrefix")
        protected String dnPrefix;

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Data
        public static class FileSender {
            @XmlAttribute(name = "localDn")
            protected String localDn;
            @XmlAttribute(name = "elementType")
            protected String elementType;
        }


        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Data
        public static class MeasCollec {
            @XmlAttribute(name = "beginTime", required = true)
            @XmlSchemaType(name = "dateTime")
            protected XMLGregorianCalendar beginTime;
        }

    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "managedElement",
        "measInfo"
    })
    @Data
    public static class MeasData {
        @XmlElement(required = true)
        protected MeasCollecFile.MeasData.ManagedElement managedElement;
        protected List<MeasCollecFile.MeasData.MeasInfo> measInfo;

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Data
        public static class ManagedElement {
            @XmlAttribute(name = "localDn")
            protected String localDn;
            @XmlAttribute(name = "userLabel")
            protected String userLabel;
            @XmlAttribute(name = "swVersion")
            protected String swVersion;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "job",
            "granPeriod",
            "repPeriod",
            "measTypes",
            "measType",
            "measValue"
        })
        @Data
        public static class MeasInfo {

            protected MeasCollecFile.MeasData.MeasInfo.Job job;
            @XmlElement(required = true)
            protected MeasCollecFile.MeasData.MeasInfo.GranPeriod granPeriod;
            protected MeasCollecFile.MeasData.MeasInfo.RepPeriod repPeriod;
            @XmlList
            protected List<String> measTypes;
            protected List<MeasCollecFile.MeasData.MeasInfo.MeasType> measType;
            protected List<MeasCollecFile.MeasData.MeasInfo.MeasValue> measValue;
            @XmlAttribute(name = "measInfoId")
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
            @XmlType(name = "", propOrder = {
                "value"
            })
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
            @XmlType(name = "", propOrder = {
                "measResults",
                "r",
                "suspect"
            })
            @Data
            public static class MeasValue {
                @XmlList
                protected List<String> measResults;
                protected List<MeasCollecFile.MeasData.MeasInfo.MeasValue.R> r;
                protected Boolean suspect;
                @XmlAttribute(name = "measObjLdn", required = true)
                protected String measObjLdn;

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                    "value"
                })
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


        public void setMeasInfo(List<MeasInfo> filteredMeasInfos) {
            this.measInfo  = filteredMeasInfos;
        }

    }


    public void replaceMeasData(List<MeasData> measDataList) {
        this.measData = measDataList;
    }

}
