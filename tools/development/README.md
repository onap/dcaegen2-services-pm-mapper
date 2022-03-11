#PM-Mapper local development
This projects is based on pm-mapper CSIT aligned for local development

##Run pm-mapper locally
To run pm-mapper execute following scripts 
```
make setup-all
```
To change pm-mapper image go to script 'run-pm-mapper.sh' and edit': 
```
IMAGE=onap/org.onap.dcaegen2.services.pm-mapper:latest
```

To clean environment (remove all containers):
```
make clean-env
```

##Send sample file

1. To list avaiable files use:
```
make list-files
```

2 a. To send MeasDataFile Enter to files publisher containers and go to files directory: 
```
./send-meas-data.sh <file name>
```
E.g:
```
./send-meas-data.sh C20190329.0000-0015.xml.gz
```
MeasDataFile Examples:

- C20190329.0000-0015.xml.gz
- PM202007171301+020024C202007171207+0200-1215+0200_45678.xml.gz

2 b. To send MeasCollec file use:  
```
./send-meas-collec.sh A20181002.0000-1000-0015-1000_5G.xml.gz
```

MeasCollec Examples:

- A20181002.0000-1000-0015-1000_5G.xml.gz

## HINTS:
File names must start with 'A', 'C' or 'PM'

Files xml should be zipped by gzip. To send raw xml a small alignment is necessary:
Go to './send-meas-data.sh' or './send-meas-collec.sh' and change endpoint:
```
https://dmaap-dr-node:8443/publish/1/Apm_TEST_REQUEST.xml.gz
```
to:
```
https://dmaap-dr-node:8443/publish/1/Apm_TEST_REQUEST.xml
```

To gzip file use:
```
gzip -k <filename>
```

##Container logs:

### PM-Mapper logs:
```
docker logs -f pmmapper
```

### Message-Router simulator logs (output of processed messages by PM-Mapper):
```
docker logs -f mr-simulator
```


#####Info:
Certificate validity - 2023 August
