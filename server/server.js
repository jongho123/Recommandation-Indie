var express = require('express'),
    port = process.env.PORT | 19918,
    app = express(),
    bodyParser = require('body-parser');

var logger = require('morgan'),
    multer = require('multer');

var fs = require('fs');
var myLog = ':remote-addr :remote-user :method :url HTTP/:http-version :status :res[content-length] - :response-time ms ":user-agent"';

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended : true }));
app.use(multer());
app.use(logger(myLog));

app.get('/', function(req, res) {
  res.end('hello world');
});

app.get('/recommendation', function(req, res) {
  console.log('');
  var readStream = fs.createReadStream('./music/안부.mp3');
  var dataLength = 0;
  readStream.pipe(res); 

  readStream.on('data', function(data) {
    dataLength += data.length;
    console.log(data.length);
  })
  .on('end', function() {
    console.log('The loegth was : ' + dataLength);
  });
});

/* // register music test... 
app.post('/', function(req, res) {
  console.log("reqeust mesg");
  //console.log(req.get('Contents-Length'));
  //console.log(req);

  //var outStream = fs.createWriteStream('./music/sample.amr');
  var dataLength = 0;
  var data = req.body;
  fs.writeFile('./music/sample.amr', data, function(err) {
    if(err) return console.log(err);
    //console.log('file length is : %d', req.body.length); 
  }); 

  req.on('data',function(chunk) {
    console.log('this is chunk : %d', chunk.length);
  });

  var outStream = fs.createWriteStream('./music/sample.amr');
  var dataLength = 0;
  outStream.pipe(req);
  outStream.on('data', function(data) {
    dataLength += data.length;
  })
  .on('end', function() {
    console.log('The length was : ' + dataLength);
  });
  res.sendStatus(200);
});
*/

app.listen(port, function(err) {
  if(err) return console.log(err);
  console.log('listening on %s', port);
});


