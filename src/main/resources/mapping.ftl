<#ftl ns_prefixes={"D":"http://www.3gpp.org/ftp/specs/archive/32_series/32.435#measCollec"}>
<#--
  ============LICENSE_START=======================================================
   Copyright (C) 2019 Nordix Foundation.
   Copyright (C) 2021 Samsung Electronics.
  ================================================================================
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  SPDX-License-Identifier: Apache-2.0
  ============LICENSE_END=========================================================
 -->
<#compress>
<#assign fileHeader = xml.measCollecFile.fileHeader>
<#assign fileFooter = xml.measCollecFile.fileFooter>
<#assign measData = xml.measCollecFile.measData>
<#setting datetime_format="iso">
{
    "event": {
        "commonEventHeader": <@commonEventHeader/>
    <#if measData?has_content>,
        "perf3gppFields": <@perf3gppFields/>
        </#if>
    }
}


<#macro commonEventHeader>
{
    "domain": "perf3gpp",
    "eventId": "${eventId}",
    "sequence": 0,
    "eventName": "perf3gpp_${metadata.productName}-${metadata.vendorName}_pmMeasResult",
    "sourceName": "${metadata.sourceName}",
    "reportingEntityName": "",
    "priority": "Normal",
    "startEpochMicrosec": ${fileHeader.measCollec.@beginTime?datetime?long?c},
    "lastEpochMicrosec": ${fileFooter.measCollec.@endTime?datetime?long?c},
    "version": "4.0",
    "vesEventListenerVersion": "7.1",
    "timeZoneOffset": "${metadata.timeZoneOffset}"
}
</#macro>


<#macro measTypes measInfo>
{
    "sMeasTypesList":[
     <#if measInfo.measType?has_content>
       <#list measInfo.measType as measType>
       "${measType}"<#sep>,</#sep>
       </#list>
     <#else>
       <#list measInfo.measTypes?split(" ") as measType>
       "${measType}"<#sep>,</#sep>
   </#list>
 </#if>
    ]
}
</#macro>


<#macro measValuesList measInfo>
[
<#list measInfo.measValue as measValue>
    {
        "measObjInstId": "${measValue.@measObjLdn[0]!}",
        "suspectFlag": "${measValue.suspect[0]! "false"}",
        "measResults": [
        <#if measValue.r?has_content>
        <#list measValue.r as r>
            {
                "p": ${r.@p},
                "sValue": "${r}"
            }<#sep>,</#sep>
        </#list>
        <#else>
        <#list measValue.measResults?split(" ") as r>
            {
                "p":${r?index+1},
                "sValue": "${r}"
            }<#sep>,</#sep>
        </#list>
        </#if>
        ]
   }
<#sep>,</#sep>
</#list>
]
</#macro>


<#macro measInfoList>
[
<#list measData.measInfo as measInfo>
    {
        "measInfoId": {
            "sMeasInfoId": "${measInfo.@measInfoId[0]!}"
        },
        "measTypes": <@measTypes measInfo/>,
        "measValuesList": <@measValuesList measInfo/>
    }<#sep>,</#sep>
</#list>
]
</#macro>


<#macro measDataCollection>
{
    "granularityPeriod": ${measData.measInfo.granPeriod.@durationInSeconds[0]!},
    "measuredEntityUserName": "${measData.managedElement.@userLabel[0]!}",
    "measuredEntityDn": "${measData.managedElement.@localDn[0]!}",
    "measuredEntitySoftwareVersion": "${measData.managedElement.@swVersion[0]!}",
    "measInfoList": <@measInfoList/>
}
</#macro>


<#macro perf3gppFields>
{
    "perf3gppFieldsVersion": "1.0",
    "measDataCollection": <@measDataCollection/>
}
</#macro>
</#compress>