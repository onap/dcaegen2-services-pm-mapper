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

package org.onap.dcaegen2.services.pmmapper.model.measurement.lte;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.Data;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementData;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementFile;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementInfo;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fileHeader",
    "measData",
    "fileFooter"
})
@XmlRootElement(name = "measCollecFile")
@Data
public class MeasCollecFile implements MeasurementFile {

    @XmlElement(required = true)
    protected MeasCollecFile.FileHeader fileHeader;
    protected List<MeasCollecFile.MeasData> measData;
    @XmlElement(required = true)
    protected MeasCollecFile.FileFooter fileFooter;

    @Override
    public Optional<List<MeasurementData>> getMeasurementData() {
        try {
            List<MeasurementData> measurementDataList = new ArrayList<>(this.measData);
            return Optional.of(measurementDataList);
        } catch (NullPointerException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void replacementMeasurementData(List<MeasurementData> measurementData) {
        measData.clear();
        measurementData.forEach(measurementDatum -> {
            MeasData measDatum = new MeasData();
            measDatum.setManagedElement((MeasData.ManagedElement) measurementDatum.getManagedEntity());
            measDatum.setMeasInfo(measurementDatum.getMeasurementInfo());
            this.measData.add(measDatum);
        });
    }

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
    public static class MeasData implements MeasurementData {
        @XmlElement(required = true)
        protected MeasCollecFile.MeasData.ManagedElement managedElement;
        @XmlElement(required = true)
        protected List<MeasurementInfo> measInfo;

        @Override
        public List<MeasurementInfo> getMeasurementInfo() {
            return this.measInfo;
        }

        @Override
        public void setMeasurementInfo(List<MeasurementInfo> measurementInfo) {
            this.measInfo = measurementInfo;
        }

        @Override
        public Object getManagedEntity() {
            return this.managedElement;
        }

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
    }

}
