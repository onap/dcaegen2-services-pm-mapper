#!/bin/bash


source ./env/containers_ip
IMAGE=onap/org.onap.dcaegen2.services.pm-mapper:latest

docker run -d -p 8081:8081 \
  --mount type=bind,source="$PWD/certs",target="/opt/app/pm-mapper/etc/certs/" \
  -e "CONFIG_BINDING_SERVICE_SERVICE_HOST=$CBS_IP" \
  -e "CONFIG_BINDING_SERVICE_SERVICE_PORT=10000" \
  -e "HOSTNAME=pmmapper" \
  -e "PROCESSING_LIMIT_RATE=2" \
  -e "THREADS_MULTIPLIER=2" \
  -e "PROCESSING_THREADS_COUNT=3" \
  --add-host "dmaap-dr-node:$DR_NODE_IP" \
  --add-host "message-router:$NODE_IP" \
  --network=development_pmmapper-network \
  --name=pmmapper $IMAGE
