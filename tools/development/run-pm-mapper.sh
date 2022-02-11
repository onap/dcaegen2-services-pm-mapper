#!/bin/bash
# ============LICENSE_START=======================================================
# Copyright (C) 2021-2022 NOKIA
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
IMAGE=onap/org.onap.dcaegen2.services.pm-mapper:latest

TARGET_CONFIG_PATH=/app-config/application_config.yaml
docker run -d -p 8081:8081 \
  --mount type=bind,source="$PWD/certs",target="/opt/app/pm-mapper/etc/certs/" \
  --mount type=bind,source="$PWD/resources/mount_config.yaml",target="$TARGET_CONFIG_PATH" \
  -e "CONFIG_BINDING_SERVICE=$CBS_IP" \
  -e "CONFIG_BINDING_SERVICE_SERVICE_PORT=10000" \
  -e "CBS_CLIENT_CONFIG_PATH=$TARGET_CONFIG_PATH" \
  -e "PROCESSING_LIMIT_RATE=1" \
  -e "THREADS_MULTIPLIER=1" \
  -e "PROCESSING_THREADS_COUNT=1" \
  --add-host "dmaap-dr-node:$DR_NODE_IP" \
  --add-host "message-router:$NODE_IP" \
  --network=development_pmmapper-network \
  --name=pmmapper $IMAGE
