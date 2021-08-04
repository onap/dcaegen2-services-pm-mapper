#!/bin/bash

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
docker exec -i datarouter-prov sh -c "curl -k  -X PUT https://$DR_PROV_IP:8443/internal/api/NODES?val=dmaap-dr-node\|$GATEWAY_IP"
docker exec -i datarouter-prov sh -c "curl -k  -X PUT https://$DR_PROV_IP:8443/internal/api/PROV_AUTH_ADDRESSES?val=dmaap-dr-prov\|$GATEWAY_IP"

# Create PM Mapper feed on data router
curl -v -X POST -H "Content-Type:application/vnd.dmaap-dr.feed" -H "X-DMAAP-DR-ON-BEHALF-OF:pmmapper" --data-ascii @./resources/createFeed.json --post301 --location-trusted -k https://localhost:8443
