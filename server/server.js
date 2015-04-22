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

var swit = true;
var count = 0;
app.get('/recommendation', function(req, res) {
  var readStream;
  var dataLength = 0;

  if(swit) {
    readStream = fs.createReadStream('./music/안부.mp3');
    console.log('play music 안부.mp3');
  } else {
    readStream = fs.createReadStream('./music/재회.mp3');
    console.log('play music 재회.mp3');
  }

  readStream.pipe(res); 

  readStream.on('data', function(data) {
    dataLength += data.length;
  })
  .on('end', function() {
    console.log('The length was : ' + dataLength);
    if(++count == 2) {
      count %= 2;
      if(swit) swit = false;
      else swit = true;
    }
  });
  
});

// register music test... 
app.post('/', function(req, res) {
  console.log("reqeust mesg");

  //var inStream = fs.createReadStream(req.files.uploaded.path);
  //var outStream = fs.createWriteStream('./music/sample.amr');
  var dataLength = 0;
  var oldPath = req.files.uploaded.path;
  var newPath = __dirname + "/music/" + req.files.uploaded.originalname; 
  console.log(req.files);

  fs.rename(oldPath, newPath, function(err) {
    if(err) return console.log(err);
    console.log('The file written %s to %s', oldPath, newPath);
  });
  /*
  inStream.pipe(outStream);
  inStream.on('data', function(data) {
    dataLength += data.length;
  })
  .on('end', function() {
    console.log('The length was : ' + dataLength);
  });
  */
  res.sendStatus(200);

});

app.listen(port, function(err) {
  if(err) return console.log(err);
  console.log('listening on %s', port);
});


