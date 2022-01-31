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

import re

class LogReader:

  def filter_unique(self, merged_logs_output, testname):
    logs = merged_logs_output.splitlines()
    del_logs = list(filter(lambda line: "|DEL|" in line, logs))
    nrs_set = set()
    ret_logs = set()
    for log in del_logs:
      filename = re.findall(testname + "\d", log)
      if len(filename) > 0 and filename[0] not in nrs_set:
        ret_logs.add(log)
        nrs_set.add(filename[0])
    return ret_logs

  def get_number_of_element_occurrences_in_logs(self, logs_output, element):
    return len(list(filter(lambda line: element in line, logs_output)))

  def get_log_files_list(self, fileNames):
    files = fileNames.split()
    return files
