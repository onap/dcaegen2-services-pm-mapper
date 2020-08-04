#!/usr/bin/env sh
echo $(curl -sI -X GET https://localhost:8443/reconfigure -k | head -n1) >> /var/log/ONAP/dcaegen2/services/pm-mapper/reconfigure.log
