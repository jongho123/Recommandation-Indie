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

controller.like = function(req, res) {
  console.log('in like');
  MusicInfo.find({ track_id: req.params.trackId }, function(err, track){
    if(err) console.log(err);
    else {
      MusicInfo.update({ track_id: req.params.trackId }, {like: ++track[0].like},  function(err, t){
        if(err) console.log(err);
        else {
          res.end(''+track[0].like);
        }
      });
    }
    Log.create({ user_id: req.params.userId, request: 'like', log: req.params.trackId }, function(err) {
      if(err) console.log(err);
    }); 
  });
}
controller.unlike = function(req, res) {
  console.log('in unlike');
  MusicInfo.find({ track_id: req.params.trackId }, function(err, track){
    if(err) console.log(err);
    else {
      MusicInfo.update({ track_id: req.params.trackId }, {unlike: ++track[0].unlike},  function(err, t){
        if(err) console.log(err);
        else {
          res.end(''+track[0].unlike);
        }
      });
    }
    Log.create({ user_id: req.params.userId, request: 'unlike', log: req.params.trackId }, function(err) {
      if(err) console.log(err);
    }); 
  });
}

controller.musicinfo = function(req, res) {
  console.log('send music info');
  var trackId = req.params.trackId;
  var videoId = req.params.videoId;
  var userId = req.params.userId;
  
  MusicInfo.find({ track_id: req.params.trackId }, function(err, track){
    if(err) console.log(err);
    else {
      var resMessage = JSON.stringify({ track_id: trackId, url: videoId, title: track[0].title, artist: track[0].artist, like: track[0].like, unlike: track[0].unlike });
      res.end(resMessage);

      MusicInfo.update({ track_id: req.params.trackId }, {count: ++track[0].count},  function(err, t){
        if(err) console.log(err);
        else {
        }
      });
    }
    Log.create({ user_id: req.params.userId, request: 'musicinfo', log: req.params.trackId }, function(err) {
      if(err) console.log(err);
    }); 
  });
}
controller.streaming = function(req, res) {
  console.log(req.params);

  var params = { params: { Bucket: 'jhmusic', Key: req.params.trackId + '.mp3'} };
  s3 = new AWS.S3(params);

  var filename = './music/' + req.params.trackId;

  s3.headObject({}, function(notExist, data) {
    console.log(notExist + "------------------ " + data);
    if(notExist) {
      console.log("exec youtube-dl");
      var youtubedl = cp.fork('./youtube-dl.js');
      var body = "";
 
      youtubedl.on('exit', function(code, signal) {
        console.log('start create music in S3');
        var putStorage = fs.createReadStream(filename + '.mp3');

        s3.upload({Body: putStorage}).on('httpUploadProgress', function(evt) { 
        }).
        send(function(err, data) { 
          console.log(err, data) 
          var musicstream = fs.createReadStream(filename + '.mp3');
          musicstream.pipe(res);
          musicstream.on('data', function(data) {
            body += data;
          }).
          on('end', function(){
            console.log("streaming end");
            console.log("and extract");
            var extract = cp.fork('./extract');
            extract.on('exit', function(code, signal) {
              var featurebody="";
              var fstream = fs.createReadStream(filename + '.txt');

              fstream.on('data', function(data) {
                featurebody += data;
              }).
              on('end', function(){
                console.log('end extract : ' + featurebody);
                var models = JSON.stringify({ track_id: req.params.trackId });
                MusicInfo.find(JSON.parse(models), function(err, tracks) {
                  if(err) console.log("track create err");
                  else if(tracks.length == 0) console.log("streaming not music track");
                  else {
                    var models = JSON.stringify({ title: tracks[0].title, artist: tracks[0].artist, feature: featurebody });
                    Track.create(JSON.parse(models), function(err) {
                      if(err) console.log("track create err");
                      else console.log("create tracks");
                    });
                  }
                });

                fs.unlink(filename + ".txt");
                fs.unlink(filename + ".mp3");
              });

            });
 
            extract.send({ input: filename + ".mp3", output: filename + ".txt" });
          });
        });
      });

      youtubedl.send({ url: "https://www.youtube.com/watch?v=" + req.params.videoId, filename: filename + ".m4a" });
    } else {
      console.log('exist music in s3');
      var musicstream = s3.getObject().createReadStream();
      musicstream.pipe(res);

      musicstream.on('end', function(){
        res.end();
        console.log("S3 to Server musicstream end");
      });
    }
  });
}

controller.analysis = function(req, res) {
  console.log("in analysis");

  var info;

  Track.find(JSON.parse(req.body.playinfo), function(err, tracks) {
    var playinfo = JSON.parse(req.body.playinfo);
    var filename = (req.files.uploaded.name).split('.')[0];
    var extractPath = "./music/" + filename + ".txt";

    if(err) console.log("track find error");
    if(tracks == 0) {
      console.log("analysis extract");
      var extract = cp.fork('./extract.js');

      extract.on('exit', function(code, signal) {

        var fstream = fs.createReadStream(extractPath);
        var body = "";

        fstream.on('data', function(data) {
          body += data;  
          console.log(data);
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
      console.log('input :', req.files.uploaded.path, 'output :', extractPath);
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
  console.log(req.body.userinfo);
  console.log(req.body.musicinfo);
  var userinfo = JSON.parse(req.body.userinfo);
  var musicinfo = JSON.parse(req.body.musicinfo);

  var track_id = '';
  var i=20;
  while(i--) { track_id += Math.floor(Math.random() * 10); }
  console.log('created id : ' +  track_id);

  var filename = './music/' + track_id;
  var extract = cp.fork('./extract');
  extract.on('exit', function(code, signal) {
    var featurebody="";
    var fstream = fs.createReadStream(filename + '.txt');

    fstream.on('data', function(data) {
      featurebody += data;
    }).
    on('end', function(){
      console.log('end extract : ' + featurebody);
      var models = JSON.stringify({ title: musicinfo.title, artist: musicinfo.artist });
      Track.find(JSON.parse(models), function(err, tracks) {
        if(err) console.log("track create err");
        else if(tracks.length == 0) {
          var models = JSON.stringify({ title: musicinfo.title, artist: musicinfo.artist, feature: featurebody });
          Track.create(JSON.parse(models), function(err) {
            if(err) console.log("track create err");
            else console.log("create tracks");
          });

          var models = JSON.stringify({ user_id: userinfo.user_id, request: userinfo.request, log: track_id });
          Log.create(JSON.parse(models), function(err) {
            if(err) return console.log(err);
          });

          var params = { params: { Bucket: 'jhmusic', Key: track_id + '.mp3' } };
          s3 = new AWS.S3(params);

          s3.headObject({}, function(notExist, data) {
            if(notExist) {
              var putStorage = fs.createReadStream(req.files.uploaded.path);

              s3.upload({Body: putStorage}).on('httpUploadProgress', function(evt) { console.log(evt); }).
              send(function(err, data) { console.log(err, data) });

              putStorage.on('close', function(){
                res.sendStatus(200);
                fs.unlink(req.files.uploaded.path, function(err) {
                  if(err) { console.log('unlink error', err); }
                });
              });
            } else {
              console.log("exist:", data);
            }
          });
        }
      });

      fs.unlink(filename + '.txt');


      MusicInfo.find({ title: musicinfo.title, artist: musicinfo.artist}, function(err, tracks) {
        if(err) console.log(err);
        else if( tracks.length == 0 ) {
          MusicInfo.create({ track_id: track_id, title: musicinfo.title, artist: musicinfo.artist, like:0, unlike:0, count:0 }, function(err, track){
            if(err) console.log(err);
            else console.log('create musicinfo in register');
          });

          var inputObj = new Object();

          inputObj.user_id = 'singer';
          inputObj.artist = userinfo.user_id;
          inputObj.title = musicinfo.title;
          inputObj.album = 'created';
          inputObj.feature = featurebody;

          var input = querystring.stringify({ 'data' : JSON.stringify(inputObj) }); 

          options.path = '/soundnerd/user/play',
          options.headers = {
            'Content-Type':'application/x-www-form-urlencoded',
            'Content-Length':input.length
          }
 
          var playReq = http.request(options, function(playRes) {
            var body = "";
            playRes.on('data', function(chunk){
              body += chunk;
            }).
            on('end', function(){
              console.log(body);
              console.log('------------------end play req-------------------');
            });
          });
          playReq.end(input);
        } else {
          console.log('musicinfo exist in register');
        }
      });
    });
  });

  extract.send({ input: req.files.uploaded.path, output: filename + '.txt' });
};

controller.recommendation = function(req, res) {
  console.log(req.body.base);
  console.log(req.body.info);

  var info;
  var logString = '';

  info = JSON.parse(req.body.info); 

  Track.find(JSON.parse(req.body.base), function(err, tracks) {
    if(err) console.log("recommendation track find error");
    if(tracks.length == 0) { 
      console.log("no tracks");
    }
 
    var inputObj = new Object();

    inputObj.feature = tracks[0].feature;
    inputObj.count = 50;

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
        console.log('--------------------------------------------------similar search----------');
        console.log(JSON.parse(body));
        var tracks = JSON.parse(body).tracks;
	var i=0;

	if(tracks.length == 0) {
          res.end("no track");
          return;
        }

	while(i < tracks.length && Number(tracks[i].score) > 90) { ++i }
        var tmp = i;
        i = Math.floor(Math.random() * i);
        
        console.log('-----------RAND--------------'); 
        console.log(tmp, 'TO', i+1); 
        console.log('title :', tracks[i].title, 'artist', tracks[i].artist);
        console.log('-----------------------------'); 

        var searchObj = new Object();
         
        searchObj.artist = tracks[i].artist;
        searchObj.title = tracks[i].title;
        searchObj.start = 0;
        searchObj.count = 1;
        
        var input = querystring.stringify({ 'data' : JSON.stringify(searchObj) }); 

        options.path = '/soundnerd/music/search',
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
	    console.log('-------------------------------------search raw data--------------------');
	    console.log(body);
            console.log('--------------------------------------------------result search----------');
            console.log(JSON.parse(body));
            
            var recommendedTrack = JSON.parse(body).tracks[0];
            var videoId = recommendedTrack.url.split('v=')[1];
            /*
            var youtubedl = cp.fork('./youtube-dl.js');
            var filename = "./" + tracks[0].title + "-" + track[0].artist;

            youtubedl.on('exit', function(code, signal) {

              var musicstream = fs.createReadStream(filename + ".mp4");
              var body = "";

              musicstream.pipe(res);

              musicstream.on('data', function(data) {
                body += data;
              }).
              on('end', function(){
                console.log(body);
                var extract = cp.fork("./extract");
                extract.on('exit', function(code, signal) {
                  var featurebody="";
                  var fstream = fs.createReadStream(filename + ".txt"); 
                  fstream.on('data', function(data) {
                    featurebody += data; 
                  }).
                  on('end', function(){
                    var models = JSON.stringify({ title: tracks[0].title, artist: tracks[0].artist, feature: featurebody });
                    Track.create(JSON.parse(models), function(err) {
                      if(err) console.log("track create err");
                      else console.log("create tracks");
                    })

                    fs.unlink(filename + ".mp4");
                    fs.unlink(filename + ".txt");
                  });
                });

                extract.send({ input: filename + ".mp4", output: filename + ".txt" }); 
              }); 
            });

            youtubedl.send({ url: tracks[0].url, filename: filename });
            */
            if (!info.user_id) {
              var infodata = JSON.stringify({user_id: "guest", request: info.request, log: recommendedTrack.track_id});
            } else {
              var infodata = JSON.stringify({user_id: info.user_id, request: info.request, log: recommendedTrack.track_id});
            }
            info = JSON.parse(infodata);

            if("user_id" in info && info.user_id !== '') {
              var models = JSON.stringify({ user_id: info.user_id, request: info.request, log:info.log });
              Log.create(JSON.parse(models), function(err) {
                if(err) return console.log(err);
              });
            }

            MusicInfo.find(JSON.parse(JSON.stringify({ track_id: recommendedTrack.track_id })), function(err, track) {
              if (err) console.log("MusicInfo find error"); 
              if (track.length == 0) {
                var createObj = new Object(); 
                createObj.track_id = recommendedTrack.track_id;
                createObj.artist = tracks[i].artist;
                createObj.title = tracks[i].title;
                createObj.like = 0;
                createObj.unlike = 0;
                createObj.count = 1;

                var models = JSON.stringify(createObj);
                MusicInfo.create(JSON.parse(models), function(err) {
                  if(err) return console.log(err);
              var resMessage = JSON.stringify({ track_id: recommendedTrack.track_id, url: videoId, title: recommendedTrack.title, artist: recommendedTrack.artist, like: 0, unlike: 0 });
              res.end(resMessage);

console.log(JSON.stringify({ track_id: recommendedTrack.track_id, url: videoId, title: tracks[0].title, artist: tracks[0].artist, like: 0, unlike: 0 }));
                });
              } else {
              var resMessage = JSON.stringify({ track_id: recommendedTrack.track_id, url: videoId, title: track[0].title, artist: track[0].artist, like: track[0].like, unlike: track[0].unlike });
              res.end(resMessage);

console.log('-------------------------------------------------');
console.log(track[0]);
console.log('-------------------------------------------------');
console.log(JSON.stringify({ track_id: recommendedTrack.track_id, url: videoId, title: track[0].title, artist: track[0].artist, like: track[0].like, unlike: track[0].unlike }));
                var models = JSON.stringify({ track_id : track[0].track_id });
                MusicInfo.update(JSON.parse(models), { count: ++track[0].count }, function(err, track){
                  if(err) console.log(err);
                  else {
                    //console.log(track);
                  }
                });
              }
              //console.log("complete MusicInfo find");
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
