#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019 Nordix Foundation.
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
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================
#
FROM openjdk:8u171-jre-alpine

ENTRYPOINT ["/bin/sh", "-c", "nohup sh etc/reconfigure.sh & /usr/bin/java -jar /opt/app/pm-mapper/pm-mapper.jar"]
ARG JAR
WORKDIR /opt/app/pm-mapper

RUN mkdir -p /var/log/ONAP
RUN addgroup -S onap \
    && adduser -S pm-mapper -G onap \
    && chown -R pm-mapper:onap /opt/app/pm-mapper \
    && chmod a+rwx /var/log/ONAP

ADD target/${JAR} /opt/app/pm-mapper/pm-mapper.jar
ADD target/classes/mapping.ftl /opt/app/pm-mapper/etc/mapping.ftl
ADD target/classes/measCollec_plusString.xsd /opt/app/pm-mapper/etc/measCollec_plusString.xsd
ADD target/classes/reconfigure.sh /opt/app/pm-mapper/etc/reconfigure.sh

USER pm-mapper
