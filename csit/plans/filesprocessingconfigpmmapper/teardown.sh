#!/bin/bash
echo "Starting teardown script"
TEST_PLANS_DIR=$WORKSPACE/plans/filesprocessingconfigpmmapper
mkdir -p $WORKSPACE/archives
docker exec pmmapper /bin/sh -c "cat /var/log/ONAP/dcaegen2/services/pm-mapper/pm-mapper_output.log"
kill-instance.sh pmmapper
docker-compose -f $TEST_PLANS_DIR/docker-compose.yml logs > $WORKSPACE/archives/filesprocessingconfigpmmapper-docker-compose.log
docker-compose -f $TEST_PLANS_DIR/docker-compose.yml down -v
