var http = require('http'),
    querystring = require('querystring');

var fs = require('fs');
var AWS = require('aws-sdk');
var controller = {};

AWS.config.loadFromPath('./controller/config.json');

var inputFrame = [];
var options = {
  hostname: '52.68.192.28',
  port: 80,
  path: '/soundnerd/user/nonshared_history',
  method: 'POST',
  headers: {
    'Content-Type':'application/x-www-form-urlencoded',
    'Content-Length':0
  }
};

controller.register = function(req, res) {

  var oldPath = req.files.uploaded.path;
  var keyName = req.files.uploaded.originalname;

  console.log('%s / %s', oldPath, keyName);

  var params = { params: { Bucket: 'jhmusic', Key: keyName} };
  s3 = new AWS.S3(params);

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
};

var swit = true;
var count = 0;

controller.recommendation = function(req, res) {
  s3 = new AWS.S3();

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
};

controller.list = function(req, res) {
  var inputObj = new Object();

  inputObj.req_user_id = 'master';
  inputObj.lookup_user_id = 'test';
  inputObj.start = 0;
  inputObj.count = 10;

  inputFrame.pop();
  inputFrame.push(inputObj);

  var input = querystring.stringify({ 'data' : JSON.stringify(inputFrame[0]) }); 

  options.path = '/soundnerd/user/nonshared_history',
  options.headers = {
      'Content-Type':'application/x-www-form-urlencoded',
      'Content-Length':input.length
    }

  var bonaReq = http.request(options, function(bonaRes) {
    var body = '';

    bonaRes.on('data', function(chunk){
      body += chunk;
    })
    .on('end', function(){
      var tid = JSON.parse(body).tracks[1].track_id;

      input = querystring.stringify({ 'data' : JSON.stringify({track_id : tid, count : 10}) }); 

      options.path = '/soundnerd/music/recommend',
      options.headers = {
        'Content-Type':'application/x-www-form-urlencoded',
        'Content-Length':input.length
      }

      var recoReq = http.request(options, function(recoRes) {
        var list = [];
        body = "";
        recoRes.on('data', function(chunk) {
          body += chunk; 
        })
        .on('end', function() {
          var tracks = JSON.parse(body).tracks;
          console.log(tracks);

          for( var i = 0; i < tracks.length; ++i ) {
            list.push(tracks[i].title);
          }
          console.log(list);

          res.json(list);
        });
      });

      recoReq.end(input);
    });

  });  

  bonaReq.end(input);
};

module.exports = controller;
