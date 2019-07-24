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

WORKDIR /opt/app/pm-mapper

RUN apk --no-cache add curl

RUN mkdir -p /var/log/ONAP

RUN adduser pm-mapper; \
    chmod a+rwx /var/log/ONAP; \
    chown -R pm-mapper /opt/app/pm-mapper

USER pm-mapper

COPY ${project.build.directory}/${ext.dep.dir.path}/ ${ext.dep.dir.path}/
COPY ${project.build.directory}/${JAR} .

COPY ${project.build.directory}/classes/mapping.ftl ./etc/mapping.ftl
COPY ${project.build.directory}/classes/measCollec_plusString.xsd ./etc/measCollec_plusString.xsd
COPY ${project.build.directory}/classes/reconfigure.sh ./etc/reconfigure.sh
COPY --chown=pm-mapper ${project.build.directory}/classes/logback.xml .

RUN echo $(whoami) > ./test.txt

LABEL git.branch="${git.branch}" \
      git.build.host="${git.build.host}" \
      git.build.time="${git.build.time}" \
      git.build.user.email="${git.build.user.email}" \
      git.build.user.name="${git.build.user.name}" \
      git.build.version="${git.build.version}" \
      git.closest.tag.name="${git.closest.tag.name}" \
      git.commit.id="${git.commit.id}" \
      git.commit.message.short="${git.commit.message.short}" \
      git.commit.time="${git.commit.time}" \
      git.commit.user.email="${git.commit.user.email}" \
      git.commit.user.name="${git.commit.user.name}"

ENTRYPOINT ["/bin/sh", "-c", "nohup sh etc/reconfigure.sh & /usr/bin/java -jar ${project.artifactId}-${project.version}.jar -DLogback.configurationFile=/opt/app/pm-mapper/logback.xml"]
ARG JAR
