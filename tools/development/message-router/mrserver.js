/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia.
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
        res.writeHead(200, {'Content-Type': 'text/plain'});
        console.log('Received message');
        req.on('data', chunk => {
          console.log(`-----MESSAGE_CONTENT_BEGIN-----\n ${chunk}`);
          console.log('-----MESSAGE_CONTENT_END-----');
        });
        setTimeout(() => {
          res.end('Published' + ' Successfully.\n');
        }, 100)
      };

  return {
    start: start
  }
}();

httpServer.start(3904);
