var express = require('express'),
    port = process.env.PORT | 19918,
    app = express(),
    bodyParser = require('body-parser');

var logger = require('morgan'),
    multer = require('multer');

var fs = require('fs');
var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');

var myLog = ':remote-addr :remote-user :method :url HTTP/:http-version :status :res[content-length] - :response-time ms ":user-agent"';

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended : true }));
app.use(multer());
app.use(logger(myLog));

var list = [];
var S3 = new AWS.S3();
S3.listObjects({Bucket: 'jhmusic'}, function(err, data) {
  if (err) console.log(err, err.stack);
  else {
    for(var i=0; i<data.Contents.length; ++i) {
      list.push(data.Contents[i].Key);
    }
    console.log(list);
  }
});
app.get('/', function(req, res) {
  res.end('Hello world');
});

var swit = true;
var count = 0;
app.get('/recommendation', function(req, res) {

  var s3 = new AWS.S3();

  if(swit) {
    var params = {Bucket: 'jhmusic', Key: '안부.mp3'};
    console.log('play music 안부.mp3');
  } else {
    var params = {Bucket: 'jhmusic', Key: '재회.mp3'};
    console.log('play music 재회.mp3');
  }

  var recoMusic = s3.getObject(params).createReadStream();
  var dataLength = 0;

  recoMusic.pipe(res);
  recoMusic.on('data', function(chunk) {
    dataLength += chunk.length;
  }).
  on('end', function() {
    console.log('The length was : ' + dataLength);
    if(++count == 2) {
      count %= 2;
      if(swit) swit = false;
      else swit = true;
    }
  });
});

app.get('/music', function(req, res) {
    res.json(list);
});

// register music test... 
app.post('/', function(req, res) {
  var oldPath = req.files.uploaded.path;
  var keyName = req.files.uploaded.originalname; 
 
  console.log('%s / %s', oldPath, keyName);

  var params = { params: { Bucket: 'jhmusic', Key: keyName} };
  var s3 = new AWS.S3(params);

  s3.headObject({}, function(notExist, data) {
    if(notExist) { 
      var putStorage = fs.createReadStream(oldPath);

      s3.upload({Body: putStorage}).
        on('httpUploadProgress', function(evt) { console.log(evt); }).
        send(function(err, data) { console.log(err, data) });

      putStorage.on('close', function(){
        fs.unlink(oldPath, function(err) {
          if(err) { console.log('unlink error', err); }
        });
      });
    }
    else {
      console.log("exist:", data); 
    }
  });

  res.sendStatus(200);
});

app.listen(port, function(err) {
  if(err) return console.log(err);
  console.log('listening on %s', port);
});
