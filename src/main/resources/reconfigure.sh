#!/usr/bin/env sh
while true
do
    sleep 60
    echo $(wget -S --spider --no-check-certificate https://localhost:8443/reconfigure 2>&1) >> /var/log/ONAP/dcaegen2/services/pm-mapper/reconfigure.log
done
