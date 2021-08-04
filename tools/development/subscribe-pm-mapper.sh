#!/bin/bash

source ./env/containers_ip

# PM Mapper subscriber on data router

curl -v -X POST -H "Content-Type:application/vnd.dmaap-dr.subscription" -H "X-DMAAP-DR-ON-BEHALF-OF:pmmapper" --data-ascii @./resources/addSubscriber.json --post301 --location-trusted -k https://localhost:8443/subscribe/1
