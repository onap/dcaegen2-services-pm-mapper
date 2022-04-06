#!/bin/bash
# ============LICENSE_START=======================================================
# Copyright (C) 2022 NOKIA
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

# Place the scripts in run order:
source ${SCRIPTS}/common_functions.sh

docker login -u docker -p docker nexus3.onap.org:10001

TEST_PLANS_DIR=$WORKSPACE/plans/filesprocessingconfigpmmapper

export GATEWAY_IP=172.18.0.1
export DR_NODE_IP=172.18.0.2
export DR_PROV_IP=172.18.0.3
export PMMAPPER_IP=172.18.0.4
export CBS_IP=172.18.0.5
export MARIADB_IP=172.18.0.6
export NODE_IP=172.18.0.7

for asset in provserver.properties node.properties mrserver.js cert.jks jks.pass trust.jks trust.pass config.yaml; do
  cp $TEST_PLANS_DIR/assets/${asset} /var/tmp/
done

sed -i 's/datarouter-mariadb/'$MARIADB_IP'/g' /var/tmp/provserver.properties

# ------------------------------------
#Prepare enviroment for client
#install docker sdk
echo "Uninstall docker-py and reinstall docker."
pip uninstall -y docker-py
pip uninstall -y docker
pip install -U docker==2.7.0

docker-compose -f $TEST_PLANS_DIR/docker-compose.yml up -d mariadb node

echo "Waiting for MariaDB to come up healthy..."
for i in {1..30}; do
    mariadb_state=$(docker inspect --format='{{json .State.Health.Status}}' mariadb)
    if [ $mariadb_state = '"healthy"' ]
    then
      break
    else
      sleep 2
    fi
done
[ "$mariadb_state" != '"healthy"' ] && echo "Error: MariaDB container state not healthy" && exit 1

docker-compose -f $TEST_PLANS_DIR/docker-compose.yml up -d datarouter-node datarouter-prov

unset http_proxy
unset https_proxy

docker-compose -f $TEST_PLANS_DIR/docker-compose.yml up -d pmmapper
sleep 2

# Wait for initialization of Docker container for datarouter-node, datarouter-prov and mariadb
containers_ok=false
for i in {1..5}; do
    if [ $(docker inspect --format '{{ .State.Running }}' datarouter-node) ] && \
        [ $(docker inspect --format '{{ .State.Running }}' datarouter-prov) ] && \
        [ $(docker inspect --format '{{ .State.Running }}' mariadb) ] && \
        [ $(docker inspect --format '{{ .State.Running }}' mr-simulator) ] && \
        [ $(docker inspect --format '{{ .State.Running }}' pmmapper) ]
    then
        echo "All required docker containers are up."
        containers_ok=true
        break
    else
        sleep $i
    fi
done
[ "$containers_ok" = "false" ] && echo "Error: required container not running." && exit 1

#Data Router healthcheck
for i in $(seq 10); do
  curl -sf 'http://localhost:8080/internal/prov' -o /dev/null
  curl_status=$?
  if [ curl_status -eq 0 ]; then
      break
    else
      sleep 2
  fi
done


# Data Router Configuration.
docker exec -i datarouter-prov sh -c \
    "curl -X PUT http://$DR_PROV_IP:8080/internal/api/NODES?val=dmaap-dr-node\|$GATEWAY_IP"
docker exec -i datarouter-prov sh -c \
    "curl -X PUT http://$DR_PROV_IP:8080/internal/api/PROV_AUTH_ADDRESSES?val=dmaap-dr-prov\|$GATEWAY_IP"

# Create PM Mapper feed and create PM Mapper subscriber on data router
curl -v -X POST -H "Content-Type:application/vnd.dmaap-dr.feed" -H "X-DMAAP-DR-ON-BEHALF-OF:pmmapper" \
      --data-ascii @$TEST_PLANS_DIR/assets/createFeed.json \
      --post301 --location-trusted "http://${DR_PROV_IP}:8080"
curl -v -X POST -H "Content-Type:application/vnd.dmaap-dr.subscription" -H "X-DMAAP-DR-ON-BEHALF-OF:pmmapper" \
      --data-ascii @$TEST_PLANS_DIR/assets/addSubscriber.json \
      --post301 --location-trusted "http://${DR_PROV_IP}:8080/subscribe/1"

docker cp pmmapper:/var/log/ONAP/dcaegen2/services/pm-mapper/pm-mapper_output.log /tmp/pmmapper.log
sleep 10
docker exec -it datarouter-prov sh -c "curl http://dmaap-dr-node:8080/internal/fetchProv"
curl "http://$DR_PROV_IP:8080/internal/prov"

#Pass any variables required by Robot test suites in ROBOT_VARIABLES
ROBOT_VARIABLES="-v DR_PROV_IP:${DR_PROV_IP} -v DMAAP_MR_IP:${DMAAP_MR_IP} -v CBS_IP:${CBS_IP} -v PMMAPPER_IP:${PMMAPPER_IP} -v DR_NODE_IP:${DR_NODE_IP} -v NODE_IP:${NODE_IP}"
