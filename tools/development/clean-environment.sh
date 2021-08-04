#!/bin/bash

source ./env/containers_ip

docker-compose down
docker rm pmmapper
