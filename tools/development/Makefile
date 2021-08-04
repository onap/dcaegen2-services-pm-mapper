# ============LICENSE_START=======================================================
# Copyright (C) 2021 NOKIA
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

setup-all: setup-local run-pm-mapper config-dmaap subscribe-pm-mapper

test:
	@echo test

setup-local:
	@./setup-local.sh

run-pm-mapper:
	@./run-pm-mapper.sh

config-dmaap:
	@./config-dmaap.sh

subscribe-pm-mapper:
	@./subscribe-pm-mapper.sh

clean-env:
	@./clean-environment.sh

list-files:
	@ls files-publisher/files
