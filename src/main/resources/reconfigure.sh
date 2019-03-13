#!/usr/bin/env sh
while true
do
    sleep 60
    echo $(wget -S --spider localhost:8081/reconfigure 2>&1) >> /var/log/ONAP/dcaegen2/services/pm-mapper/reconfigure.log
done
