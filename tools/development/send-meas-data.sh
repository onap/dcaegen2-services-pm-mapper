#!/bin/bash
PUBLISH_FILE_NAME=$1

docker exec -it files-publisher curl -v -L -k -X PUT -H "Content-Type:application/octet-stream" -H "X-ONAP-RequestID:X-ONAP-RequestID=15" -H "X-DMAAP-DR-META:{\"productName\":\"gnb\",\"vendorName\":\"Nokia\",\"lastEpochMicrosec\":\"1538478000000\",\"sourceName\":\"oteNB5309\",\"startEpochMicrosec\":\"1538478900000\",\"timeZoneOffset\":\"UTC+05.00\",\"location\":\"ftpes://192.168.0.101:22/ftp/rop/A20161224.1045-1100.bin.gz\",\"compression\":\"gzip\",\"fileFormatType\":\"org.3GPP.28.532#measData\",\"fileFormatVersion\":\"V9\"}" -H "X-DMAAP-DR-ON-BEHALF-OF:pm-mapper" -H "Authorization:Basic cG1tYXBwZXI6cG1tYXBwZXI=" --data-binary @"/files/${PUBLISH_FILE_NAME}" https://dmaap-dr-node:8443/publish/1/Apm_TEST_REQUEST.xml.gz


