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

package org.onap.dcaegen2.services.pmmapper.model.measurement.nr;

import lombok.Data;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementData;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementFile;
import org.onap.dcaegen2.services.pmmapper.model.measurement.common.MeasurementInfo;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "fileHeader",
        "measData",
        "fileFooter"
})
@XmlRootElement(name = "measDataFile")
@Data
public class MeasDataFile implements MeasurementFile {

    @XmlElement(required = true)
    protected MeasDataFile.FileHeader fileHeader;
    protected List<MeasDataFile.MeasData> measData;
    @XmlElement(required = true)
    protected MeasDataFile.FileFooter fileFooter;

    @Override
    public Optional<List<MeasurementData>> getMeasurementData() {
        try {
            List<MeasurementData> measDataList = new ArrayList<>(this.measData);
            return Optional.of(measDataList);
        } catch (NullPointerException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void replacementMeasurementData(List<MeasurementData> measurementData) {
        measData.clear();
        measurementData.forEach(measurementDatum -> {
            MeasData measDatum = new MeasData();
            measDatum.setMeasEntity((MeasData.MeasEntity) measurementDatum.getManagedEntity());
            measDatum.setMeasInfo(measurementDatum.getMeasurementInfo());
            this.measData.add(measDatum);
        });
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "measData"
    })
    @Data
    public static class FileFooter {

        @XmlElement(name = "measData", required = true)
        protected MeasDataFile.FileFooter.MeasData measData;

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class MeasData {
            @XmlAttribute(name = "endTime", required = true)
            @XmlSchemaType(name = "dateTime")
            protected XMLGregorianCalendar endTime;
        }

    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "fileSender",
            "measData"
    })
    @Data
    public static class FileHeader {
        @XmlElement(required = true)
        protected MeasDataFile.FileHeader.FileSender fileSender;
        @XmlElement(name = "measData", required = true)
        protected MeasDataFile.FileHeader.MeasData measData;
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
            @XmlAttribute(name = "senderName")
            protected String senderName;
            @XmlAttribute(name = "elementType")
            protected String elementType;
        }


        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Data
        public static class MeasData {
            @XmlAttribute(name = "beginTime", required = true)
            @XmlSchemaType(name = "dateTime")
            protected XMLGregorianCalendar beginTime;
        }

    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"measEntity",
            "measInfo"
    })
    @Data
    public static class MeasData implements MeasurementData {
        @XmlElement(required = true)
        protected MeasEntity measEntity;
        @XmlElement()
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
            return this.measEntity;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Data
        public static class MeasEntity {
            @XmlAttribute(name = "localDn")
            protected String localDn;
            @XmlAttribute(name = "userLabel")
            protected String userLabel;
            @XmlAttribute(name = "swVersion")
            protected String swVersion;
        }

    }

}
