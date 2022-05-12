#!/bin/bash
# ============LICENSE_START=======================================================
# Copyright (C) 2022 Nokia. All rights reserved.
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
IMAGE=nexus3.onap.org:10003/onap/org.onap.dcaegen2.collectors.datafile.datafile-app-server:latest
TARGET_CONFIG_PATH=/app-config/application_config.yaml
TARGET_SPRING_CONFIG=/opt/app/datafile/config/application.yaml

docker run -d -p 8100:8100 -p 8000:8000 \
  --mount type=bind,source="$PWD/resources/datafile/mount_config.yaml",target="$TARGET_CONFIG_PATH" \
  --mount type=bind,source="$PWD/resources/datafile/spring_application.yaml",target="$TARGET_SPRING_CONFIG" \
  -e "JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,address=*:8000,server=y,suspend=n" \
  -e "CONFIG_BINDING_SERVICE=0.0.0.0" \
  -e "CONFIG_BINDING_SERVICE_SERVICE_PORT=10000" \
  -e "CBS_CLIENT_CONFIG_PATH=$TARGET_CONFIG_PATH" \
  --add-host "dmaap-dr-node:$DR_NODE_IP" \
  --add-host "dmaap-dr-prov:$DR_PROV_IP" \
  --network=development_pmmapper-network \
  --name=datafile-dev $IMAGE
