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
