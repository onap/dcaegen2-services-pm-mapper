#!/usr/bin/env sh
while true
do
    sleep 60
    echo $(curl -sI -X GET https://localhost:8443/reconfigure -k | head -n1) >> /var/log/ONAP/dcaegen2/services/pm-mapper/reconfigure.log
done
