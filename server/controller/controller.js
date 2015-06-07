var AWS = require('aws-sdk');
AWS.config.loadFromPath('./controller/config.json');

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
var http = require('http');
var querystring = require('querystring');

var fs = require('fs');
var mongoose = require('mongoose'),
    Log = mongoose.model('log'),
    Track = mongoose.model('track'),
    MusicInfo = mongoose.model('musicinfo');

var cp = require('child_process');

var controller = {};

controller.analysis = function(req, res) {

  var info;

  Track.find(JSON.parse(req.body.playinfo), function(err, tracks) {
    var playinfo = JSON.parse(req.body.playinfo);
    var filename = (req.files.uploaded.name).split('.')[0];
    var extractPath = __dirname + "/feature/" + filename + ".txt";

    if(err) console.log("track find error");
    if(tracks == 0) {
      var extract = cp.fork('./extract.js');

      extract.on('exit', function(code, signal) {

        var fstream = fs.createReadStream(extractPath);
        var body = "";

        fstream.on('data', function(data) {
          body += data;  
        }).
        on('end', function(){
          console.log(body);
          var models = JSON.stringify({ title: playinfo.title, artist: playinfo.artist, feature: body });
          Track.create(JSON.parse(models), function(err) {
            if(err) console.log("track create err");
            else console.log("create tracks");
 
            res.sendStatus(200);
          })

          fs.unlink(extractPath);
          fs.unlink(req.files.uploaded.path);
        });

      });
      extract.send({ input: req.files.uploaded.path, output: extractPath });
    } else {
      console.log("is exist");
      res.sendStatus(200);
    }
    
  });

  info = JSON.parse(req.body.userinfo);
  if(!info.user_id) {
    var infodata = JSON.stringify({user_id: "guest", request: info.request});
    info = JSON.parse(infodata);
  }
  

  if("user_id" in info && info.user_id !== '') {
    var models = JSON.stringify({ user_id: info.user_id, request: info.request, log: req.body.infotest });
    Log.create(JSON.parse(models), function(err) {
      if(err) return console.log(err);
    });
  }

}

controller.register = function(req, res) {
  console.log(req);
  var info;

  if(req.body.info) {
    info = JSON.parse(req.body.info);
  } else {
    var infodata = JSON.stringify({user_id: "guest", request: "register"});
    info = JSON.parse(infodata);
  }

  if("user_id" in info && info.user_id !== '') {
    var models = JSON.stringify({ user_id: info.user_id, request: info.request, log: req.files.uploaded.originalname });
    Log.create(JSON.parse(models), function(err) {
      if(err) return console.log(err);
    });
  }	

  var oldPath = req.files.uploaded.path;
  var keyName = req.files.uploaded.originalname;

  //console.log('%s / %s', oldPath, keyName);

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

//var swit = true;
//var count = 0;
controller.recommendation = function(req, res) {

  var info;
  var logString = '';

  info = JSON.parse(req.body.info); 

  Track.find(JSON.parse(req.body.base), function(err, tracks) {
    var inputObj = new Object();

    inputObj.feature = tracks[0].feature;
    inputObj.count = 10;

    var input = querystring.stringify({ 'data' : JSON.stringify(inputObj) }); 

    options.path = '/soundnerd/music/similar',
    options.headers = {
      'Content-Type':'application/x-www-form-urlencoded',
      'Content-Length':input.length
    }

    var similarReq = http.request(options, function(similarRes) {
      var body = "";
      similarRes.on('data', function(chunk) {
        body += chunk; 
      })
      .on('end', function() {
        var tracks = JSON.parse(body).tracks;

        var inputObj = new Object();
         
        inputObj.artist = tracks[0].artist;
        inputObj.title = tracks[0].title;
        inputObj.start = 0;
        inputObj.count = 1;
        
        var input = querystring.stringify({ 'data' : JSON.stringify(inputObj) }); 

        options.path = '/soundnerd/music/similar',
        options.headers = {
          'Content-Type':'application/x-www-form-urlencoded',
          'Content-Length':input.length
        } 

        var searchReq = http.request(options, function(searchRes) {
          var body = "";
          searchRes.on('data', function(chunk) {
            body += chunk; 
          })
          .on('end', function() {
            var tracks = JSON.parse(body).tracks;
            res.end(querystring.stringify({ 'data' : JSON.stringify({ url: tracks[0].url }) }));

            if (!info.user_id) {
              var infodata = JSON.stringify({user_id: "guest", request: info.request, log: tracks[0].title + "-" + tracks[0].artist});
            } else {
              var infodata = JSON.stringify({user_id: info.user_id, request: info.request, log: tracks[0].title + "-" + tracks[0].artist});
            }
            info = JSON.parse(infodata);

            if("user_id" in info && info.user_id !== '') {
              var models = JSON.stringify({ user_id: info.user_id, request: info.request, log:logString });
              Log.create(JSON.parse(models), function(err) {
                if(err) return console.log(err);
              });
            }

            MusicInfo.find(JSON.parse(JSON.stringify({ track_id: tracks[0].track_id })), function(err, tracks) {
              if( err ) console.log("----------------------------|||||zero err???|||--------------"); 
              if( tracks.length == 0) console.log("-isisisisisis ZZZero");
 
              console.log("complete MusicInfo find");
            });
            
          });
        });

        searchReq.end(input);
      });
    });

    similarReq.end(input);
  });



  /*
  s3 = new AWS.S3();

  if(swit) {
    var params = {Bucket: 'jhmusic', Key: '안부.mp3'};
    logString += '안부'
  } else {
    var params = {Bucket: 'jhmusic', Key: '재회.mp3'};
    logString += '재회'
  }
  */

  /*
  var recoMusic = s3.getObject(params).createReadStream();
  var dataLength = 0;

  recoMusic.pipe(res);
  recoMusic.on('data', function(chunk) {
    dataLength += chunk.length;
  }).
  on('end', function() {
    //console.log('The length was : ' + dataLength);
    if(++count == 2) {
      count %= 2;
      if(swit) swit = false;
      else swit = true;
    }
  });
  */
};

controller.list = function(req, res) {

  var info;
  if(req.body.info) {
    info = JSON.parse(req.body.info);
  } else {
    var infoData = JSON.stringify({ user_id: "guest", request: "list" });
    info = JSON.parse(infoData);
  } 

  if("user_id" in info && info.user_id !== '') {
    var models = JSON.stringify({ user_id: info.user_id, request: info.request });
    Log.create(JSON.parse(models), function(err) {
      if(err) return console.log(err);
    })
  }

  var inputObj = new Object();

  inputObj.req_user_id = 'master';
  inputObj.lookup_user_id = 'test';
  inputObj.start = 0;
  inputObj.count = 10;

  var input = querystring.stringify({ 'data' : JSON.stringify(inputObj) }); 


  console.log(input);

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
          //console.log(tracks);

          for( var i = 0; i < tracks.length; ++i ) {
            list.push(tracks[i].title);
          }
          //console.log(list);

          res.json(list);
        });
      });

      recoReq.end(input);
    });

  });  

  bonaReq.end(input);
};

module.exports = controller;
