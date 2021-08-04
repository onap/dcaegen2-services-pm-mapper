#Pm-mapper local development
This projects is based on pm-mapper CSIT aligned for local development

##Run pm-mapper
To run pm-mapper execute following scripts 
1. Setup and config mariadb consul cbs node files-publisher, maybe "sudo" will be necessary.
```
./setup-local.sh
```
2. Run pm-mapper instance (on new terminal or new terminal tab)
```
./run-pm-mapper.sh
```
3. Config dmaap
```
./config-dmaap.sh
```

4. Subscribe pm-mapper
```
./subsribe-pm-mapper.sh
```
5. Now environment is ready, if you want restart pm-mapper - just stop and remove pm-mapper container and execute again
(Following script is running as main process - for stop send stop signal (press CTR+C ))
```
./run-pm-mapper.sh
```

##Send sample file
1. Enter to files publisher containers and go to files directory 
```
docker exec -it files-publisher sh
cd /files
```

2. Execute following request 
```
curl -v -L -k -X PUT -H "Content-Type:application/octet-stream" -H "X-ONAP-RequestID:X-ONAP-RequestID=15" -H "X-DMAAP-DR-META:{\"productName\":\"gnb\",\"vendorName\":\"Nokia\",\"lastEpochMicrosec\":\"1538478000000\",\"sourceName\":\"oteNB5309\",\"startEpochMicrosec\":\"1538478900000\",\"timeZoneOffset\":\"UTC+05.00\",\"location\":\"ftpes://192.168.0.101:22/ftp/rop/A20161224.1045-1100.bin.gz\",\"compression\":\"gzip\",\"fileFormatType\":\"org.3GPP.32.435#measCollec\",\"fileFormatVersion\":\"V9\"}" -H "X-DMAAP-DR-ON-BEHALF-OF:pm-mapper" -H "Authorization:Basic cG1tYXBwZXI6cG1tYXBwZXI=" --data-binary @A20181002.0000-1000-0015-1000_5G.xml.gz https://dmaap-dr-node:8443/publish/1/Apm_TEST_REQUEST.xml.gz
```

##Clean environment
For cleaning environment
```
./clean-environment.sh
```

#####Info:
Certificate validity - 2023 August
