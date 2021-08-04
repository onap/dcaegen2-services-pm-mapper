#!/bin/bash

source ./env/containers_ip

docker stop pmmapper
docker rm pmmapper
docker-compose down
