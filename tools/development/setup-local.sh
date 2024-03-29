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
SCRIPT_DIR=$PWD

source ./env/containers_ip

sed -i 's/datarouter-mariadb/'$MARIADB_IP'/g' $SCRIPT_DIR/dr-mount/provserver.properties

docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d mariadb node files-publisher sftp

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

docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d datarouter-node datarouter-prov
