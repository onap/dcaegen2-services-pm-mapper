# ============LICENSE_START=======================================================
# org.onap.dcae
# ================================================================================
# Copyright (c) 2021-2022 Nokia. All rights reserved.
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
#

import docker
from EnvsReader import EnvsReader
from docker.types import Mount

class DockerContainerManager:

    def run_pmmapper_container(self, client_image, container_name, path_to_env, dr_node_ip, mr_ip):
        client = docker.from_env()
        environment = EnvsReader().read_env_list_from_file(path_to_env)
        environment.append("CONFIG_BINDING_SERVICE_SERVICE_PORT=10000")
        environment.append("CONFIG_BINDING_SERVICE=172.18.0.5")
        environment.append("HOSTNAME=pmmapper")
        environment.append("CBS_CLIENT_CONFIG_PATH=/app-config-input/application_config.yaml")
        client.containers.run(
            image=client_image,
            name=container_name,
            environment=environment,
            ports={'8081': 8081},
            network='filesprocessingconfigpmmapper_pmmapper-network',
            extra_hosts={'dmaap-dr-node': dr_node_ip, 'message-router': mr_ip},
            user='root',
            mounts=[Mount(target='/opt/app/pm-mapper/etc/certs/', source='/var/tmp/', type='bind'), Mount(target='/app-config-input/application_config.yaml', source='/var/tmp/config.yaml', type='bind')],
            detach=True
        )

    def remove_container(self, container_name):
        client = docker.from_env()
        container = client.containers.get(container_name)
        container.stop()
        container.remove()
