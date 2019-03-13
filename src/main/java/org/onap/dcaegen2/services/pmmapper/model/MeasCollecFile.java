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
import java.util.ArrayList;
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

/**
 * <p>
 * Generated Java class using XJC to represent 3GPP PM Measurement file
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileHeader">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="fileSender">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="localDn" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="elementType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="measCollec">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="beginTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="fileFormatVersion" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="vendorName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="dnPrefix" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="measData" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="managedElement">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="localDn" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="userLabel" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="swVersion" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="measInfo" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="job" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;attribute name="jobId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="granPeriod">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
 *                                     &lt;attribute name="endTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="repPeriod" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;choice>
 *                               &lt;element name="measTypes">
 *                                 &lt;simpleType>
 *                                   &lt;list itemType="{http://www.w3.org/2001/XMLSchema}Name" />
 *                                 &lt;/simpleType>
 *                               &lt;/element>
 *                               &lt;element name="measType" maxOccurs="unbounded" minOccurs="0">
 *                                 &lt;complexType>
 *                                   &lt;simpleContent>
 *                                     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>Name">
 *                                       &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *                                     &lt;/extension>
 *                                   &lt;/simpleContent>
 *                                 &lt;/complexType>
 *                               &lt;/element>
 *                             &lt;/choice>
 *                             &lt;element name="measValue" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;choice>
 *                                         &lt;element name="measResults">
 *                                           &lt;simpleType>
 *                                             &lt;list itemType="{http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec}measResultType" />
 *                                           &lt;/simpleType>
 *                                         &lt;/element>
 *                                         &lt;element name="r" maxOccurs="unbounded" minOccurs="0">
 *                                           &lt;complexType>
 *                                             &lt;simpleContent>
 *                                               &lt;extension base="&lt;http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec>measResultType">
 *                                                 &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *                                               &lt;/extension>
 *                                             &lt;/simpleContent>
 *                                           &lt;/complexType>
 *                                         &lt;/element>
 *                                       &lt;/choice>
 *                                       &lt;element name="suspect" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                     &lt;attribute name="measObjLdn" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                           &lt;attribute name="measInfoId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="fileFooter">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="measCollec">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="endTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fileHeader",
    "measData",
    "fileFooter"
})
@XmlRootElement(name = "measCollecFile")
public class MeasCollecFile {

    @XmlElement(required = true)
    protected MeasCollecFile.FileHeader fileHeader;
    protected List<MeasCollecFile.MeasData> measData;
    @XmlElement(required = true)
    protected MeasCollecFile.FileFooter fileFooter;

    /**
     * Gets the value of the fileHeader property.
     *
     * @return
     *     possible object is
     *     {@link MeasCollecFile.FileHeader }
     *
     */
    public MeasCollecFile.FileHeader getFileHeader() {
        return fileHeader;
    }

    /**
     * Sets the value of the fileHeader property.
     *
     * @param value
     *     allowed object is
     *     {@link MeasCollecFile.FileHeader }
     *
     */
    public void setFileHeader(MeasCollecFile.FileHeader value) {
        this.fileHeader = value;
    }

    /**
     * Gets the value of the measData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the measData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeasData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MeasCollecFile.MeasData }
     *
     *
     */
    public List<MeasCollecFile.MeasData> getMeasData() {
        if (measData == null) {
            measData = new ArrayList<MeasCollecFile.MeasData>();
        }
        return this.measData;
    }

    /**
     * Gets the value of the fileFooter property.
     *
     * @return
     *     possible object is
     *     {@link MeasCollecFile.FileFooter }
     *
     */
    public MeasCollecFile.FileFooter getFileFooter() {
        return fileFooter;
    }

    /**
     * Sets the value of the fileFooter property.
     *
     * @param value
     *     allowed object is
     *     {@link MeasCollecFile.FileFooter }
     *
     */
    public void setFileFooter(MeasCollecFile.FileFooter value) {
        this.fileFooter = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="measCollec">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="endTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "measCollec"
    })
    public static class FileFooter {

        @XmlElement(required = true)
        protected MeasCollecFile.FileFooter.MeasCollec measCollec;

        /**
         * Gets the value of the measCollec property.
         *
         * @return
         *     possible object is
         *     {@link MeasCollecFile.FileFooter.MeasCollec }
         *
         */
        public MeasCollecFile.FileFooter.MeasCollec getMeasCollec() {
            return measCollec;
        }

        /**
         * Sets the value of the measCollec property.
         *
         * @param value
         *     allowed object is
         *     {@link MeasCollecFile.FileFooter.MeasCollec }
         *
         */
        public void setMeasCollec(MeasCollecFile.FileFooter.MeasCollec value) {
            this.measCollec = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="endTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class MeasCollec {

            @XmlAttribute(name = "endTime", required = true)
            @XmlSchemaType(name = "dateTime")
            protected XMLGregorianCalendar endTime;

            /**
             * Gets the value of the endTime property.
             *
             * @return
             *     possible object is
             *     {@link XMLGregorianCalendar }
             *
             */
            public XMLGregorianCalendar getEndTime() {
                return endTime;
            }

            /**
             * Sets the value of the endTime property.
             *
             * @param value
             *     allowed object is
             *     {@link XMLGregorianCalendar }
             *
             */
            public void setEndTime(XMLGregorianCalendar value) {
                this.endTime = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="fileSender">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="localDn" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="elementType" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="measCollec">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="beginTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="fileFormatVersion" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="vendorName" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="dnPrefix" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fileSender",
        "measCollec"
    })
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

        /**
         * Gets the value of the fileSender property.
         *
         * @return
         *     possible object is
         *     {@link MeasCollecFile.FileHeader.FileSender }
         *
         */
        public MeasCollecFile.FileHeader.FileSender getFileSender() {
            return fileSender;
        }

        /**
         * Sets the value of the fileSender property.
         *
         * @param value
         *     allowed object is
         *     {@link MeasCollecFile.FileHeader.FileSender }
         *
         */
        public void setFileSender(MeasCollecFile.FileHeader.FileSender value) {
            this.fileSender = value;
        }

        /**
         * Gets the value of the measCollec property.
         *
         * @return
         *     possible object is
         *     {@link MeasCollecFile.FileHeader.MeasCollec }
         *
         */
        public MeasCollecFile.FileHeader.MeasCollec getMeasCollec() {
            return measCollec;
        }

        /**
         * Sets the value of the measCollec property.
         *
         * @param value
         *     allowed object is
         *     {@link MeasCollecFile.FileHeader.MeasCollec }
         *
         */
        public void setMeasCollec(MeasCollecFile.FileHeader.MeasCollec value) {
            this.measCollec = value;
        }

        /**
         * Gets the value of the fileFormatVersion property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getFileFormatVersion() {
            return fileFormatVersion;
        }

        /**
         * Sets the value of the fileFormatVersion property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setFileFormatVersion(String value) {
            this.fileFormatVersion = value;
        }

        /**
         * Gets the value of the vendorName property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getVendorName() {
            return vendorName;
        }

        /**
         * Sets the value of the vendorName property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setVendorName(String value) {
            this.vendorName = value;
        }

        /**
         * Gets the value of the dnPrefix property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getDnPrefix() {
            return dnPrefix;
        }

        /**
         * Sets the value of the dnPrefix property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setDnPrefix(String value) {
            this.dnPrefix = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="localDn" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="elementType" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class FileSender {

            @XmlAttribute(name = "localDn")
            protected String localDn;
            @XmlAttribute(name = "elementType")
            protected String elementType;

            /**
             * Gets the value of the localDn property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getLocalDn() {
                return localDn;
            }

            /**
             * Sets the value of the localDn property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setLocalDn(String value) {
                this.localDn = value;
            }

            /**
             * Gets the value of the elementType property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getElementType() {
                return elementType;
            }

            /**
             * Sets the value of the elementType property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setElementType(String value) {
                this.elementType = value;
            }

        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="beginTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class MeasCollec {

            @XmlAttribute(name = "beginTime", required = true)
            @XmlSchemaType(name = "dateTime")
            protected XMLGregorianCalendar beginTime;

            /**
             * Gets the value of the beginTime property.
             *
             * @return
             *     possible object is
             *     {@link XMLGregorianCalendar }
             *
             */
            public XMLGregorianCalendar getBeginTime() {
                return beginTime;
            }

            /**
             * Sets the value of the beginTime property.
             *
             * @param value
             *     allowed object is
             *     {@link XMLGregorianCalendar }
             *
             */
            public void setBeginTime(XMLGregorianCalendar value) {
                this.beginTime = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="managedElement">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="localDn" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="userLabel" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="swVersion" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="measInfo" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="job" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;attribute name="jobId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="granPeriod">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
     *                           &lt;attribute name="endTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="repPeriod" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;choice>
     *                     &lt;element name="measTypes">
     *                       &lt;simpleType>
     *                         &lt;list itemType="{http://www.w3.org/2001/XMLSchema}Name" />
     *                       &lt;/simpleType>
     *                     &lt;/element>
     *                     &lt;element name="measType" maxOccurs="unbounded" minOccurs="0">
     *                       &lt;complexType>
     *                         &lt;simpleContent>
     *                           &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>Name">
     *                             &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
     *                           &lt;/extension>
     *                         &lt;/simpleContent>
     *                       &lt;/complexType>
     *                     &lt;/element>
     *                   &lt;/choice>
     *                   &lt;element name="measValue" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;choice>
     *                               &lt;element name="measResults">
     *                                 &lt;simpleType>
     *                                   &lt;list itemType="{http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec}measResultType" />
     *                                 &lt;/simpleType>
     *                               &lt;/element>
     *                               &lt;element name="r" maxOccurs="unbounded" minOccurs="0">
     *                                 &lt;complexType>
     *                                   &lt;simpleContent>
     *                                     &lt;extension base="&lt;http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec>measResultType">
     *                                       &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
     *                                     &lt;/extension>
     *                                   &lt;/simpleContent>
     *                                 &lt;/complexType>
     *                               &lt;/element>
     *                             &lt;/choice>
     *                             &lt;element name="suspect" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *                           &lt;/sequence>
     *                           &lt;attribute name="measObjLdn" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *                 &lt;attribute name="measInfoId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "managedElement",
        "measInfo"
    })
    public static class MeasData {

        @XmlElement(required = true)
        protected MeasCollecFile.MeasData.ManagedElement managedElement;
        protected List<MeasCollecFile.MeasData.MeasInfo> measInfo;

        /**
         * Gets the value of the managedElement property.
         *
         * @return
         *     possible object is
         *     {@link MeasCollecFile.MeasData.ManagedElement }
         *
         */
        public MeasCollecFile.MeasData.ManagedElement getManagedElement() {
            return managedElement;
        }

        /**
         * Sets the value of the managedElement property.
         *
         * @param value
         *     allowed object is
         *     {@link MeasCollecFile.MeasData.ManagedElement }
         *
         */
        public void setManagedElement(MeasCollecFile.MeasData.ManagedElement value) {
            this.managedElement = value;
        }

        /**
         * Gets the value of the measInfo property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the measInfo property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMeasInfo().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MeasCollecFile.MeasData.MeasInfo }
         *
         *
         */
        public List<MeasCollecFile.MeasData.MeasInfo> getMeasInfo() {
            if (measInfo == null) {
                measInfo = new ArrayList<MeasCollecFile.MeasData.MeasInfo>();
            }
            return this.measInfo;
        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="localDn" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="userLabel" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="swVersion" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ManagedElement {

            @XmlAttribute(name = "localDn")
            protected String localDn;
            @XmlAttribute(name = "userLabel")
            protected String userLabel;
            @XmlAttribute(name = "swVersion")
            protected String swVersion;

            /**
             * Gets the value of the localDn property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getLocalDn() {
                return localDn;
            }

            /**
             * Sets the value of the localDn property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setLocalDn(String value) {
                this.localDn = value;
            }

            /**
             * Gets the value of the userLabel property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getUserLabel() {
                return userLabel;
            }

            /**
             * Sets the value of the userLabel property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setUserLabel(String value) {
                this.userLabel = value;
            }

            /**
             * Gets the value of the swVersion property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getSwVersion() {
                return swVersion;
            }

            /**
             * Sets the value of the swVersion property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setSwVersion(String value) {
                this.swVersion = value;
            }

        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="job" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;attribute name="jobId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="granPeriod">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
         *                 &lt;attribute name="endTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="repPeriod" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;choice>
         *           &lt;element name="measTypes">
         *             &lt;simpleType>
         *               &lt;list itemType="{http://www.w3.org/2001/XMLSchema}Name" />
         *             &lt;/simpleType>
         *           &lt;/element>
         *           &lt;element name="measType" maxOccurs="unbounded" minOccurs="0">
         *             &lt;complexType>
         *               &lt;simpleContent>
         *                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>Name">
         *                   &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
         *                 &lt;/extension>
         *               &lt;/simpleContent>
         *             &lt;/complexType>
         *           &lt;/element>
         *         &lt;/choice>
         *         &lt;element name="measValue" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;choice>
         *                     &lt;element name="measResults">
         *                       &lt;simpleType>
         *                         &lt;list itemType="{http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec}measResultType" />
         *                       &lt;/simpleType>
         *                     &lt;/element>
         *                     &lt;element name="r" maxOccurs="unbounded" minOccurs="0">
         *                       &lt;complexType>
         *                         &lt;simpleContent>
         *                           &lt;extension base="&lt;http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec>measResultType">
         *                             &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
         *                           &lt;/extension>
         *                         &lt;/simpleContent>
         *                       &lt;/complexType>
         *                     &lt;/element>
         *                   &lt;/choice>
         *                   &lt;element name="suspect" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
         *                 &lt;/sequence>
         *                 &lt;attribute name="measObjLdn" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *       &lt;attribute name="measInfoId" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "job",
            "granPeriod",
            "repPeriod",
            "measTypes",
            "measType",
            "measValue"
        })
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

            /**
             * Gets the value of the job property.
             *
             * @return
             *     possible object is
             *     {@link MeasCollecFile.MeasData.MeasInfo.Job }
             *
             */
            public MeasCollecFile.MeasData.MeasInfo.Job getJob() {
                return job;
            }

            /**
             * Sets the value of the job property.
             *
             * @param value
             *     allowed object is
             *     {@link MeasCollecFile.MeasData.MeasInfo.Job }
             *
             */
            public void setJob(MeasCollecFile.MeasData.MeasInfo.Job value) {
                this.job = value;
            }

            /**
             * Gets the value of the granPeriod property.
             *
             * @return
             *     possible object is
             *     {@link MeasCollecFile.MeasData.MeasInfo.GranPeriod }
             *
             */
            public MeasCollecFile.MeasData.MeasInfo.GranPeriod getGranPeriod() {
                return granPeriod;
            }

            /**
             * Sets the value of the granPeriod property.
             *
             * @param value
             *     allowed object is
             *     {@link MeasCollecFile.MeasData.MeasInfo.GranPeriod }
             *
             */
            public void setGranPeriod(MeasCollecFile.MeasData.MeasInfo.GranPeriod value) {
                this.granPeriod = value;
            }

            /**
             * Gets the value of the repPeriod property.
             *
             * @return
             *     possible object is
             *     {@link MeasCollecFile.MeasData.MeasInfo.RepPeriod }
             *
             */
            public MeasCollecFile.MeasData.MeasInfo.RepPeriod getRepPeriod() {
                return repPeriod;
            }

            /**
             * Sets the value of the repPeriod property.
             *
             * @param value
             *     allowed object is
             *     {@link MeasCollecFile.MeasData.MeasInfo.RepPeriod }
             *
             */
            public void setRepPeriod(MeasCollecFile.MeasData.MeasInfo.RepPeriod value) {
                this.repPeriod = value;
            }

            /**
             * Gets the value of the measTypes property.
             *
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the measTypes property.
             *
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getMeasTypes().add(newItem);
             * </pre>
             *
             *
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             *
             *
             */
            public List<String> getMeasTypes() {
                if (measTypes == null) {
                    measTypes = new ArrayList<String>();
                }
                return this.measTypes;
            }

            /**
             * Gets the value of the measType property.
             *
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the measType property.
             *
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getMeasType().add(newItem);
             * </pre>
             *
             *
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link MeasCollecFile.MeasData.MeasInfo.MeasType }
             *
             *
             */
            public List<MeasCollecFile.MeasData.MeasInfo.MeasType> getMeasType() {
                if (measType == null) {
                    measType = new ArrayList<MeasCollecFile.MeasData.MeasInfo.MeasType>();
                }
                return this.measType;
            }

            /**
             * Gets the value of the measValue property.
             *
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the measValue property.
             *
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getMeasValue().add(newItem);
             * </pre>
             *
             *
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link MeasCollecFile.MeasData.MeasInfo.MeasValue }
             *
             *
             */
            public List<MeasCollecFile.MeasData.MeasInfo.MeasValue> getMeasValue() {
                if (measValue == null) {
                    measValue = new ArrayList<MeasCollecFile.MeasData.MeasInfo.MeasValue>();
                }
                return this.measValue;
            }

            /**
             * Gets the value of the measInfoId property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getMeasInfoId() {
                return measInfoId;
            }

            /**
             * Sets the value of the measInfoId property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setMeasInfoId(String value) {
                this.measInfoId = value;
            }


            /**
             * <p>Java class for anonymous complex type.
             *
             * <p>The following schema fragment specifies the expected content contained within this class.
             *
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
             *       &lt;attribute name="endTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class GranPeriod {

                @XmlAttribute(name = "duration", required = true)
                protected Duration duration;
                @XmlAttribute(name = "endTime", required = true)
                @XmlSchemaType(name = "dateTime")
                protected XMLGregorianCalendar endTime;

                /**
                 * Gets the value of the duration property.
                 *
                 * @return
                 *     possible object is
                 *     {@link Duration }
                 *
                 */
                public Duration getDuration() {
                    return duration;
                }

                /**
                 * Sets the value of the duration property.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Duration }
                 *
                 */
                public void setDuration(Duration value) {
                    this.duration = value;
                }

                /**
                 * Gets the value of the endTime property.
                 *
                 * @return
                 *     possible object is
                 *     {@link XMLGregorianCalendar }
                 *
                 */
                public XMLGregorianCalendar getEndTime() {
                    return endTime;
                }

                /**
                 * Sets the value of the endTime property.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link XMLGregorianCalendar }
                 *
                 */
                public void setEndTime(XMLGregorianCalendar value) {
                    this.endTime = value;
                }

            }


            /**
             * <p>Java class for anonymous complex type.
             *
             * <p>The following schema fragment specifies the expected content contained within this class.
             *
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="jobId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Job {

                @XmlAttribute(name = "jobId", required = true)
                protected String jobId;

                /**
                 * Gets the value of the jobId property.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getJobId() {
                    return jobId;
                }

                /**
                 * Sets the value of the jobId property.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setJobId(String value) {
                    this.jobId = value;
                }

            }


            /**
             * <p>Java class for anonymous complex type.
             *
             * <p>The following schema fragment specifies the expected content contained within this class.
             *
             * <pre>
             * &lt;complexType>
             *   &lt;simpleContent>
             *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>Name">
             *       &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
             *     &lt;/extension>
             *   &lt;/simpleContent>
             * &lt;/complexType>
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value"
            })
            public static class MeasType {

                @XmlValue
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "Name")
                protected String value;
                @XmlAttribute(name = "p", required = true)
                @XmlSchemaType(name = "positiveInteger")
                protected BigInteger p;

                /**
                 * Gets the value of the value property.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Sets the value of the value property.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setValue(String value) {
                    this.value = value;
                }

                /**
                 * Gets the value of the p property.
                 *
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *
                 */
                public BigInteger getP() {
                    return p;
                }

                /**
                 * Sets the value of the p property.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *
                 */
                public void setP(BigInteger value) {
                    this.p = value;
                }

            }


            /**
             * <p>Java class for anonymous complex type.
             *
             * <p>The following schema fragment specifies the expected content contained within this class.
             *
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;choice>
             *           &lt;element name="measResults">
             *             &lt;simpleType>
             *               &lt;list itemType="{http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec}measResultType" />
             *             &lt;/simpleType>
             *           &lt;/element>
             *           &lt;element name="r" maxOccurs="unbounded" minOccurs="0">
             *             &lt;complexType>
             *               &lt;simpleContent>
             *                 &lt;extension base="&lt;http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec>measResultType">
             *                   &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
             *                 &lt;/extension>
             *               &lt;/simpleContent>
             *             &lt;/complexType>
             *           &lt;/element>
             *         &lt;/choice>
             *         &lt;element name="suspect" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
             *       &lt;/sequence>
             *       &lt;attribute name="measObjLdn" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "measResults",
                "r",
                "suspect"
            })
            public static class MeasValue {

                @XmlList
                protected List<String> measResults;
                protected List<MeasCollecFile.MeasData.MeasInfo.MeasValue.R> r;
                protected Boolean suspect;
                @XmlAttribute(name = "measObjLdn", required = true)
                protected String measObjLdn;

                /**
                 * Gets the value of the measResults property.
                 *
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the measResults property.
                 *
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getMeasResults().add(newItem);
                 * </pre>
                 *
                 *
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link String }
                 *
                 *
                 */
                public List<String> getMeasResults() {
                    if (measResults == null) {
                        measResults = new ArrayList<String>();
                    }
                    return this.measResults;
                }

                /**
                 * Gets the value of the r property.
                 *
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the r property.
                 *
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getR().add(newItem);
                 * </pre>
                 *
                 *
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link MeasCollecFile.MeasData.MeasInfo.MeasValue.R }
                 *
                 *
                 */
                public List<MeasCollecFile.MeasData.MeasInfo.MeasValue.R> getR() {
                    if (r == null) {
                        r = new ArrayList<MeasCollecFile.MeasData.MeasInfo.MeasValue.R>();
                    }
                    return this.r;
                }

                /**
                 * Gets the value of the suspect property.
                 *
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *
                 */
                public Boolean isSuspect() {
                    return suspect;
                }

                /**
                 * Sets the value of the suspect property.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *
                 */
                public void setSuspect(Boolean value) {
                    this.suspect = value;
                }

                /**
                 * Gets the value of the measObjLdn property.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getMeasObjLdn() {
                    return measObjLdn;
                }

                /**
                 * Sets the value of the measObjLdn property.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setMeasObjLdn(String value) {
                    this.measObjLdn = value;
                }


                /**
                 * <p>Java class for anonymous complex type.
                 *
                 * <p>The following schema fragment specifies the expected content contained within this class.
                 *
                 * <pre>
                 * &lt;complexType>
                 *   &lt;simpleContent>
                 *     &lt;extension base="&lt;http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec>measResultType">
                 *       &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
                 *     &lt;/extension>
                 *   &lt;/simpleContent>
                 * &lt;/complexType>
                 * </pre>
                 *
                 *
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                    "value"
                })
                public static class R {

                    @XmlValue
                    protected String value;
                    @XmlAttribute(name = "p", required = true)
                    @XmlSchemaType(name = "positiveInteger")
                    protected BigInteger p;

                    /**
                     * Gets the value of the value property.
                     *
                     * @return
                     *     possible object is
                     *     {@link String }
                     *
                     */
                    public String getValue() {
                        return value;
                    }

                    /**
                     * Sets the value of the value property.
                     *
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *
                     */
                    public void setValue(String value) {
                        this.value = value;
                    }

                    /**
                     * Gets the value of the p property.
                     *
                     * @return
                     *     possible object is
                     *     {@link BigInteger }
                     *
                     */
                    public BigInteger getP() {
                        return p;
                    }

                    /**
                     * Sets the value of the p property.
                     *
                     * @param value
                     *     allowed object is
                     *     {@link BigInteger }
                     *
                     */
                    public void setP(BigInteger value) {
                        this.p = value;
                    }

                }


                public void replaceR(List<R> filteredRs) {
                    this.r = filteredRs;

                }

                public void replaceMeasResults(List<String> filteredMeasResults) {
                   this.measResults = filteredMeasResults;

                }

            }


            /**
             * <p>Java class for anonymous complex type.
             *
             * <p>The following schema fragment specifies the expected content contained within this class.
             *
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class RepPeriod {

                @XmlAttribute(name = "duration", required = true)
                protected Duration duration;

                /**
                 * Gets the value of the duration property.
                 *
                 * @return
                 *     possible object is
                 *     {@link Duration }
                 *
                 */
                public Duration getDuration() {
                    return duration;
                }

                /**
                 * Sets the value of the duration property.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Duration }
                 *
                 */
                public void setDuration(Duration value) {
                    this.duration = value;
                }
            }


            public void replaceMeasTypes(List<String> newMeasTypes) {
                this.measTypes = newMeasTypes;
            }

            public void replaceMeasType(List<MeasType> filteredMeasTypes) {
                this.measType = filteredMeasTypes;
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
