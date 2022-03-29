#!/bin/bash
# ============LICENSE_START=======================================================
# Copyright (C) 2021 NOKIA
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
PUBLISH_FILE_NAME=$1

MEAS_DATA_META_DATA_HEADER="X-DMAAP-DR-META:{\"productName\":\"gnb\",\"vendorName\":\"Nokia\",\"lastEpochMicrosec\":\"1538478000000\",\"sourceName\":\"oteNB5309\",\"startEpochMicrosec\":\"1538478900000\",\"timeZoneOffset\":\"UTC+05.00\",\"location\":\"ftpes://192.168.0.101:22/ftp/rop/A20161224.1045-1100.bin.gz\",\"compression\":\"gzip\",\"fileFormatType\":\"org.3GPP.28.532#measData\",\"fileFormatVersion\":\"V9\"}"
docker exec -it files-publisher curl --post301 --location-trusted -v -k -X PUT -H "Content-Type:application/octet-stream" -H "X-ONAP-RequestID:X-ONAP-RequestID=15" -H "${MEAS_DATA_META_DATA_HEADER}" -H "X-DMAAP-DR-ON-BEHALF-OF:pm-mapper" -H "Authorization:Basic cG1tYXBwZXI6cG1tYXBwZXI=" --data-binary @"/files/${PUBLISH_FILE_NAME}" https://dmaap-dr-node:8443/publish/1/Apm_TEST_REQUEST.xml.gz


