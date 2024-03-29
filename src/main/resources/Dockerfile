#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
#  Copyright (C) 2021 Samsung Electronics.
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
FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

WORKDIR /opt/app/pm-mapper

ARG user=pm-mapper
ARG group=pm-mapper

USER root
RUN apk add --no-cache curl && \
    mkdir -p /var/log/ONAP && \
    addgroup $group && adduser --system --disabled-password --no-create-home --ingroup $group $user && \
    chmod u=rwx /var/log/ONAP && \
    chown -R $user:$group /opt/app/pm-mapper /var/log/ONAP

USER $user

COPY ${project.build.directory}/${ext.dep.dir.path}/ ${ext.dep.dir.path}/
COPY --chown=$user:$group ${project.build.directory}/${JAR} .
COPY ${project.build.directory}/classes/schemas ./etc/schemas/
COPY ${project.build.directory}/classes/templates ./etc/templates/
COPY --chown=$user:$group ${project.build.directory}/classes/logback.xml ./etc/logback.xml


LABEL git.branch="${git.branch}" \
      git.build.host="${git.build.host}" \
      git.build.time="${git.build.time}" \
      git.build.user.email="${git.build.user.email}" \
      git.build.user.name="${git.build.user.name}" \
      git.build.version="${git.build.version}" \
      git.closest.tag.name="${git.closest.tag.name}" \
      git.commit.id="${git.commit.id}" \
      git.commit.message.short='${git.commit.message.short}' \
      git.commit.time="${git.commit.time}" \
      git.commit.user.email="${git.commit.user.email}" \
      git.commit.user.name="${git.commit.user.name}"


ENTRYPOINT ["java", "-jar", "${project.artifactId}-${project.version}.jar"]
ARG JAR
