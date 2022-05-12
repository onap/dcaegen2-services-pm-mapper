#!/bin/bash
# ============LICENSE_START=======================================================
# Copyright (C) 2021-2022 Nokia. All rights reserved.
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
source ./env/containers_ip

#Data Router healthcheck
echo "Waiting for DataRouter ready"
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
docker exec -i datarouter-prov sh -c "curl -X PUT http://$DR_PROV_IP:8080/internal/api/NODES?val=dmaap-dr-node\|$GATEWAY_IP"
docker exec -i datarouter-prov sh -c "curl -X PUT http://$DR_PROV_IP:8080/internal/api/PROV_AUTH_ADDRESSES?val=dmaap-dr-prov\|$GATEWAY_IP"

# Create PM Mapper feed on data router
curl -v -X POST -H "Content-Type:application/vnd.dmaap-dr.feed" -H "X-DMAAP-DR-ON-BEHALF-OF:pmmapper" --data-ascii @./resources/createFeed.json --post301 --location-trusted -k http://localhost:8080
