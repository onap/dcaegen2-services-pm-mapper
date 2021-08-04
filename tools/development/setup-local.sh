#!/bin/bash

SCRIPT_DIR=$PWD

source ./env/containers_ip

sed -i 's/datarouter-mariadb/'$MARIADB_IP'/g' $SCRIPT_DIR/dr-mount/provserver.properties

docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d mariadb consul cbs node files-publisher

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

#Config Consul
curl --request PUT --data @$SCRIPT_DIR/resources/cbs.json http://$CONSUL_IP:8500/v1/agent/service/register
curl 'http://'$CONSUL_IP':8500/v1/kv/pmmapper?dc=dc1' -X PUT \
      -H 'Accept: application/json' \
      -H 'Content-Type: application/json' \
      -H 'X-Requested-With: XMLHttpRequest' \
      --data @$SCRIPT_DIR/resources/config.json

