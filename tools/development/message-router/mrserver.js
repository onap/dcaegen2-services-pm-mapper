/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2022 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

function handleVesNotificationRequest(res) {
  let MEAS_COLLEC = "org.3GPP.32.435#measCollec"
  let MEAS_DATA = "org.3GPP.28.532#measData"
  let filetType = MEAS_COLLEC
  let fileUrl = "sftp://admin:admin@sftp-server:22/upload/A20181002.0000-1000-0015-1000_5G.xml.gz"
  let filePublishName = "A_28532_measData_test_onap.xml"
  res.writeHead(200, {'Content-Type': 'application/json'});
  let result = "[\"{\\\"event\\\":{\\\"commonEventHeader\\\":{\\\"startEpochMicrosec\\\":8745745764578,\\\"eventId\\\":\\\"FileReady_1797490e-10ae-4d48-9ea7-3d7d790b25e1\\\",\\\"timeZoneOffset\\\":\\\"UTC+05.30\\\",\\\"internalHeaderFields\\\":{\\\"collectorTimeStamp\\\":\\\"Thu, 05 12 2022 06:20:00 UTC\\\"},\\\"priority\\\":\\\"Normal\\\",\\\"version\\\":\\\"4.0.1\\\",\\\"reportingEntityName\\\":\\\"NOK6061ZW3\\\",\\\"sequence\\\":0,\\\"domain\\\":\\\"notification\\\",\\\"lastEpochMicrosec\\\":8745745764578,\\\"eventName\\\":\\\"Notification_gnb-Nokia_FileReady\\\",\\\"vesEventListenerVersion\\\":\\\"7.0.1\\\",\\\"sourceName\\\":\\\"NOK6061ZW3\\\"},\\\"notificationFields\\\":{\\\"notificationFieldsVersion\\\":\\\"2.0\\\",\\\"changeType\\\":\\\"FileReady\\\",\\\"changeIdentifier\\\":\\\"PM_MEAS_FILES\\\",\\\"arrayOfNamedHashMap\\\":[{\\\"name\\\":\\\""
      + filePublishName
      + "\\\",\\\"hashMap\\\":{\\\"location\\\":\\\""
      + fileUrl
      + "\\\",\\\"fileFormatType\\\":\\\""
      + filetType
      + "\\\",\\\"fileFormatVersion\\\":\\\"V1\\\",\\\"compression\\\":\\\"gzip\\\"}}]}}}\"]";

  res.end(result);
}

function handleAllTypeRequest(res, req) {
  res.writeHead(200, {'Content-Type': 'text/plain'});
  console.log('Received message');
  req.on('data', chunk => {
    console.log(`-----MESSAGE_CONTENT_BEGIN-----\n ${chunk}`);
    console.log('-----MESSAGE_CONTENT_END-----');
  });
}

function isVesNotificationRequest(req) {
  var vesNotificationTopic = "unauthenticated.VES_NOTIFICATION_OUTPUT"
  return req.url.includes(vesNotificationTopic);
}

var httpServer = function () {
  var http = require('http'),

      start = function (port) {
        var server = http.createServer(function (req, res) {
          processHttpRequest(req, res);
        });
        server.listen(port, function () {
          console.log('Listening on ' + port + '...');
        });
      },

      processHttpRequest = function (req, res) {

        if (isVesNotificationRequest(req)) {
          console.log("Received VES_NOTIFICATION request ")
          handleVesNotificationRequest(res);
          return;
        }

        handleAllTypeRequest(res, req);
        setTimeout(() => {
          res.end('Published' + ' Successfully.\n');
        }, 100)
      };

  return {
    start: start
  }
}();

httpServer.start(3904);
