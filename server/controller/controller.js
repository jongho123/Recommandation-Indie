var AWS = require('aws-sdk');
AWS.config.loadFromPath('./controller/config.json');
var async = require('async');

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

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 음악의 좋아요 개수를 올림.
 * @details 음악의 좋아요 개수를 올리고 변경된 뒤의 좋아요 개수를 리턴함.
 * @date 2015-06-17
 */
controller.like = function(req, res) {
  var trackId = req.params.trackId;
  var userId = req.params.userId;

  async.waterfall([
    // 음악 정보를 DB에서 찾음.
    // DB 검색시 에러가 나거나 음악 정보가 없으면 에러 리턴.
    function (callback) {
      MusicInfo.find({ track_id: trackId }, function(err, track){
        if(err) return callback(err);
        else if(track.length == 0) return callback(new Error('No track with track_id ' + trackId + 'found.'));
        callback(null, track[0]);
      });
    },
    // 찾은 음악 정보에서 like를 하나 올려 업데이트.
    // DB 업데이트시 에러가 나면 에러 리턴.
    // 업데이트된 이후의 좋아요 개수 client에 response.
    function (track, callback) {
      MusicInfo.update({track_id: trackId}, {like: ++track.like}, function(err) {
        if(err) return callback(err);
        res.end(''+track.like);
        callback(null);
      });
    },
    // 어느 유저가 어느 트랙에 좋아요 눌렀는지 로그 기록.
    // 로그 기록시 에러가 나면 에러 리턴.
    function (callback) {
      Log.create({ user_id: userId, request: 'like', log: trackId }, function(err) {
        if(err) return callback(err);
        callback(null, 'like complete');
      });
    }
  ],
  // 실행 결과 콘솔에 출력. 
  function (err, result) {
    if(err) console.log(err);
    else console.log(result);
  });
}

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 음악의 싫어요 개수를 올림.
 * @details 음악의 싫어요 개수를 올리고 변경된 뒤의 개수를 리턴함.
 * @date 2015-06-17
 */
controller.unlike = function(req, res) {
  var trackId = req.params.trackId;
  var userId = req.params.userId;

  async.waterfall([
    // 음악 정보를 DB에서 찾음.
    // DB 검색시 에러가 나거나 음악 정보가 없으면 에러 리턴.
    function (callback) {
      MusicInfo.find({ track_id: trackId }, function(err, track){
        if(err) return callback(err);
        else if(track.length == 0) return callback(new Error('No track with track_id ' + trackId + 'found.'));
        callback(null, track[0]);
      });
    },
    // 찾은 음악 정보에서 unlike를 하나 올려 업데이트.
    // DB 업데이트시 에러가 나면 에러 리턴.
    // 업데이트된 이후의 싫어요 개수 client에 response.
    function (track, callback) {
      MusicInfo.update({track_id: trackId}, {unlike: ++track.unlike}, function(err) {
        if(err) return callback(err);
        res.end(''+track.unlike);
        callback(null);
      });
    },
    // 어느 유저가 어느 트랙에 싫어요 눌렀는지 로그 기록.
    // 로그 기록시 에러가 나면 에러 리턴.
    function (callback) {
      Log.create({ user_id: userId, request: 'unlike', log: trackId }, function(err) {
        if(err) return callback(err);
        callback(null, 'unlike complete');
      });
    }
  ],
  // 실행 결과 콘솔에 출력. 
  function (err, result) {
    if(err) console.log(err);
    else console.log(result);
  });
}

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief DB에서 음악의 정보를 찾아 response.
 * @details DB에서 음악의 정보를 찾아 reponse 하고 로그를 남김.
 * @date 2015-06-17
 */
controller.musicinfo = function(req, res) {
  var trackId = req.params.trackId;
  var videoId = req.params.videoId; // youtube videos id
  var userId = req.params.userId;
  
  async.waterfall([
    // 음악 정보를 DB에서 찾음.
    // DB 검색시 에러가 나거나 음악 정보가 없으면 에러 리턴.
    function (callback) {
      MusicInfo.find({ track_id: trackId }, function(err, track) {
        if(err) return callback(err);
        else if(track.length == 0) return callback(new Error('No track with track_id ' + trackId + 'found.'));
        callback(null, track[0]);
      });
    },
    // 찾은 음악 정보를 response.
    function (track, callback) {
      var resMusicInfo = JSON.stringify({ track_id: trackId, url: videoId, title: track.title, artist: track.artist, like: track.like, unlike: track.unlike });  
      res.end(resMusicInfo);
      callback(null);
    },
    // 어느 유저가 어느 트랙의 음악 정보를 요청했는지 로그를 남김.
    function (callback) {
      Log.create({ user_id: userId, request: 'musicinfo', log: trackId }, function(err) {
        if(err) return callback(err); 
        callback(null, 'musicinfo complete');
      });
    }
  ],
  // 실행 결과 콘솔에 출력. 
  function (err, result) {
    if(err) console.log(err);
    else console.log(result);
  });
}

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 음악을 스트리밍 해준다.
 * @details 음악을 스트리밍 해준다. S3 내에 음악이 존재하지 않는 경우 youtube url을 이용하여 오디오를 추출해 S3에 등록하고 client에 스트리밍 해준다. 또한 추출한 음악의 musicinfo와 track 정보가 DB에 존재하지 않는다면 음악의 feature를 extract하여 DB에 등록한다.
 * @date 2015-06-17
 * @Todo 스트리밍 하고 음악의 count 업데이트 시키는 메소드 추가해야함, 가수가 등록한 음악 스트리밍할 수 있도록 해야함.
 */
controller.streaming = function(req, res) {
  var trackId = req.params.trackId;
  var filename = './music/' + req.params.trackId;

  async.waterfall([
    // AWS S3에 음아깅 존재하는지 학인함. 존재하면 바로 client에 스트리밍 해주고 끝남.
    function (callback) {
      var params = { params: { Bucket: 'jhmusic', Key: trackId + '.mp3'} };
      s3 = new AWS.S3(params);

      s3.headObject({}, function(notExist, data) {
        if(notExist) callback(null); 
        else {
          var musicstream = s3.getObject().createReadStream();
          musicstream.pipe(res);
          musicstream.on('end', function(){
            res.end();
            return callback(new Error("music exist, S3 to Server musicstream end"));
          });
        }
      });
    },
    // S3에 음악이 존재하지 않으면 youtube-dl을 이용하여 url로 음원을 추출해옴.
    // 추출한 결과는 ./music 폴더에 mp3 파일로 저장됨.
    function (callback) {
      var youtubedl = cp.fork('./youtube-dl.js');  
      youtubedl.on('exit', function(code, signal) {
        callback(null);  
      });
      youtubedl.send({ url: "https://www.youtube.com/watch?v=" + req.params.videoId, filename: filename + ".m4a" });
    },
    // youtube에서 가져온 음원을 AWS S3에 업로드 시킴.
    // 업로드시에 에러가 나면 에러 리턴.
    // response보다 먼저하는 이유는 음악의 크기가 크면 request가 두번씩오므로 response를 먼저하게 되면 S3에 음원이 없다고 판단하고 위의 과정을 또 반복할 수 있기 때문.
    function (callback) {
      var putStorage = fs.createReadStream(filename + '.mp3');
      s3.upload({Body: putStorage}).on('httpUploadProgress', function(evt) {
      }).
      send(function(err, data) {
        if(err) return callback(err); 
        callback(null);
      });
    },
    // client에 음악을 스트리밍(response)  해줌.
    // 업로드시에 에러가 나면 에러 리턴.
    function (callback) {
      var musicstream = fs.createReadStream(filename + '.mp3');
      musicstream.pipe(res);
      musicstream.on('end', function(){
        callback(null);
      });
    },
    // 음악의 feature를 추출함. 추출한 feature는 ./music 폴더에 .txt 파일로 저장됨.
    function (callback) {
      console.log('in extract');
      var extract = cp.fork('./extract'); 
      extract.on('exit', function(code, signal) {
        callback(null);
      });
      extract.send({ input: filename + ".mp3", output: filename + ".txt" });
    },
    // DB에 음악의 정보가 있는지 찾아봄. 
    // 음악 정보 검색시 에러가 나거나 음악 정보가 검색이 안되면 에러 리턴
    function (callback) {
      MusicInfo.find({ track_id: trackId }, function(err, track) {
        if(err) return callback(err);
        else if(track.length == 0) return callback(new Error('No track with track_id ' + trackId + 'found.'));
        callback(null, track[0]);
      });
    },
    // 이전에 만들어진 feature 데이터를 가져옴(읽음).
    // feature 데이터를 모두 읽으면 만들었던 .mp3 파일과 .txt 파일을 삭제함. 
    function (track, callback) {
      var featureData = "";
      var fstream = fs.createReadStream(filename + '.txt');
      fstream.on('data', function(data) {
        featureData += data;
      }).
      on('end', function() {
        fs.unlink(filename + '.txt');
        fs.unlink(filename + '.mp3');
        callback(null, track, featureData);
      });
    },
    // 가져온 feature 데이터로 DB에 track 정보를 추가.
    // DB에 데이터 추가시 에러 발생항면 에러 리턴 
    function (track, featureData, callback) {
      Track.create({ title: track.title, artist: track.artist, feature: featureData }, function(err) {
        if(err) return callback(err); 
        callback(null, 'streaming complete');
      });
    }
  ],
  // 실행 결과 콘솔에 출력. 
  function (err, result) {
    if(err) console.log(err);
    else console.log(result);
  });
}

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief client에서 서버에 업로드된 음악의 feature를 분석하여 DB에 저장함.
 * @details 음악을 추천 받으려면 먼저 DB에 feature가 등록되어 있어야 되는데 feature를 만드는 함수이다. client로 부터 업로드된 음악의 feature를 extract하여 title, artist, feature 의 정보를 DB에 등록한다.
 * @date 2015-06-17
 */
controller.analysis = function(req, res) {
  var playinfo = JSON.parse(req.body.playinfo);// 분석될 음악의 title과 artist 정보
  var filename = req.files.uploaded.name.split('.')[0]; // 업로드된 파일의 원래 이름
  var featureFile = './music/' + filename + '.txt';
  var tempMusicFile = req.files.uploaded.path; // 업로드된 음악 파일

  async.waterfall([
    // tracks 콜렉션에 업로드된 음악 파일의 정보가 있는지 확인한다.
    // 확인시에 에러가 나거나 정보가 이미 존재하면 에러를 리턴한다.
    function (callback) {
      Track.find(playinfo, function(err, track) {
        if (err) return callback(err);
        else if (track.length > 0) {
          res.sendStatus(200);
          return callback(new Error('Exist track'));
        }
        callback(null);
      });
    },
    // 업로드된 음악의 feature를 추출한다.
    // 추출된 feature data 는 ./music 폴더에 파일로 저장된다.
    function (callback) {
      var extract = cp.fork('./extract.js');

      extract.on('exit', function(code, signal) {
        callback(null);
      });

      extract.send({ input: tempMusicFile, output: featureFile });
    },
    // 추출한 feature data를 가져온다.(읽음)
    function (callback) {
      var fstream = fs.createReadStream(featureFile);
      var featureData = "";
      fstream.on('data', function(data) {
        featureData += data;
      }).
      on('end', function() {
        callback(null, featureData);
      });
    },
    // 가져온 feature data와 playinfo 데이터를 tracks 콜렉션에 저장한다. 
    // 저장한 뒤 업로드된 음악파일과 만들어진 feature data 파일은 삭제한다.
    function (featureData, callback) {
      Track.create({ title: playinfo.title, artist: playinfo.artist, feature: featureData }, function (err) {
        if (err) return callback(err);
        fs.unlink(featureFile);
        fs.unlink(tempMusicFile);
        res.sendStatus(200);
        callback(null, 'analysis complete');
      });
    }
  ],
  // 실행 결과 콘솔에 출력. 
  function (err, result) {
    if (err) console.log(err);
    else console.log(result);
  });
}

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief singer가 자신의 음악을 등록한다.
 * @details singer가 자신의 음악을 등록 요청하면 DB에 등록하려는 음악과 같은 음악이 있는지 확인하고 없으면 등록한다. 음악 파일은 S3에 저장되고 관련딘 데이터들은 DB에 저장된다.
 * @date 2015-06-17
 */
controller.register = function(req, res) {
  var userinfo = JSON.parse(req.body.userinfo); // 요청하는 user의 정보
  var musicinfo = JSON.parse(req.body.musicinfo); // 등록하는 음악의 정보
  var filename = './music/' // file의 이름
  var tempMusicFile = req.files.uploaded.path; // 업로드된 음악 파일
  var track_id = '';

  async.waterfall([
    // 등록하는 음악의 track_id를 생성한다. 1~9까지의 난수를 발생시켜 20자리로 구성한다.
    function (callback) {
      var i=20;
      while(i--) { track_id += Math.floor(Math.random() * 10); }
      filename += track_id;
      console.log('created id : ' +  track_id);
    },
    // 업로드된 음악파일의 feature를 추출한다.
    // 추출된 데이터는 ./music 폴더에 새로 만든 track_id로 .txt 파일로 만들어진다.
    function (callback) {
      var extract = cp.fork('./extract');
      extract.on('exit', function (code, signal) {
        callback(null);
      });
      extract.send({ input: tempMusicFile, output: filename + '.txt' });
    },
    // 추출된 feature data를 가져온다.
    function (callback) {
      var featureData = '';
      var fstream = fs.createReadStream(filename + '.txt');
      fstream.on('data', function (data) {
        featureData += data;
      }).
      on('end', function() {
        fs.unlink(filename + '.txt');
        callback(null, featureData);
      });
    },
    // tracks 콜렉션에 같은 음악이 없는지 확인하고 없으면 musicinfo data와 추출된 feature 데이터를 DB에 저장한다. 
    // 트랙을 찾거나 생성하는데 에러가 나거나 이미 같은 track이 존재하면 에러를 리턴한다.
    function (featureData, callback) {
      Track.find(musicinfo, function (err, track) {
        if (err) return callback(err);
        else if (track.length > 0) return callback(new Error('Exist track'));
        Track.create({ title: musicinfo.title, artist: musicinfo.artist, feature: featureData }, function (err) {
          if (err) return callback(err);
          //callback(null, featureData);
          callback(null);
        });
      });
    },
    /*
    // musicinfo 와 feature data를 보나셀 서버 DB에 저장한다. 
    // 기존 보나셀 DB의 음원과 새로 생성한 음원의 구분을 위해 새로 만드는 음원의 앨범명은 created로 한다.
    function (featureData, callback) {
      var inputObj = new Object();

      inputObj.user_id = 'singer';
      inputObj.artist = userinfo.user_id;
      inputObj.title = musicinfo.title;
      inputObj.album = 'created';
      inputObj.feature = featureData;
 
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
          callback(null);
        });
      });

      playReq.end(input);
    },
    */
    // musicinfos 콜렉션에 음악의 정보가 있는지 확인한다.
    // 확인시에 에러가 나거나 음악의 정보가 있으면 에러를 리턴한다. 
    function (callback) {
      MusicInfo.find(musicinfo, function (err, track) {
        if (err) return callback(err);
        else if (track.length > 0) return callback(new Error('Exist Music info'));
        callback(null);
      });
    },
    // musicinfos 콜렉션에 음악의 정보를 만든다. 
    // 만들때 에러가 나면 에러를  리턴한다. 
    function (callback) { 
      MusicInfo.create({ track_id: track_id, title: musicinfo.title, artist: musicinfo.artist, 
                         like: 0, unlike: 0, count: 0}, function(err) {
        if (err) return callback(err);
        callback(null); 
      });
    },
    // AWS S3에 음원이 있는지 확인한다. 
    // 음원이 존재하면 에러를 리턴한다. 
    function (callback) {
      var params = { params: { Bucket: 'jhmusic', Key: track_id + '.mp3' } };
      s3 = new AWS.S3(params);
      
      s3.headObject({}, function (notExist, data) {
        if (notExist) callback(null);
        else return callback(new Error('Exist music in S3'));
      });
    },
    // AWS S3에 음원을 업로드 시킨다. 
    // 모두 업로드되면 서버에 업로드 되었던 음악파일은 삭제한다.
    // 음원이 존재하면 에러를 리턴한다. 
    function (callback) {
      var putStorage = fs.createReadStream(tempMusicFile);
 
      s3.upload({Body: putStorage}).on('httpUploadProgress', function(evt) {
      }).
      send( function(err, data) { 
        if (err) return callback(err);
      });

      putStorage.on('close', function(){
        fs.unlink(tempMusicFile);
        res.sendStatus(200);
        callback(null);
      });
    },
    // 어느 가수가 어떤 track_id의 음악을 등록했는지 로그를 남긴다. 
    function (callback) {
      Log.create({ user_id: userinfo.user_id, request: userinfo.request, log: track_id }, function(err) {
        if(err) return callback(err);
        callback(null, 'register complete');
      });
    }
  ],
  // 실행 결과 콘솔에 출력. 
  function (err, result) {
    if (err) console.log(err);
    else console.log(result);
  });
};

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 보나셀 서버로부터 음악을 추천받는다. 
 * @details request 되는 음악의 정보와 서버 DB내의 정보를 가지고 보나셀 서버에서 음악을 추천받는다. 추천받은 노래의 정보를 client에 전달한다.
 * @date 2015-06-17
 */
controller.recommendation = function(req, res) {
  var base = JSON.parse(req.body.base); // 추천의 기준이 되는 음악의 정보
  var info = JSON.parse(req.body.info); // user의 정보

  async.waterfall([
    // tracks 콜렉션에 base 음악의 feature 데이터가 있는지 확인한다.
    // 확인시 에러가 나거나 음악의 정보가 존재하지 않으면 에러를 리턴한다.
    function (callback) {
      Track.find(base, function(err, featureTrack) {
        if (err) return callback(err);
        else if (featureTrack.length == 0) return callback(new Error('No track with base ' + base + 'found.'));
        callback(null, featureTrack[0]); 
      });
    },
    // base 음악의 feature를 기준으로 보나셀 서버에서 음악을 추천받는다.
    // 보나셀 서버에서 추천되는 음악이 없으면 에러를 리턴한다.
    function (featureTrack, callback) {
      console.log('--------------------------------------------------similar search----------');
      var inputObj = new Object();

      inputObj.feature = featureTrack.feature;
      inputObj.count = 50;

      var input = querystring.stringify({ 'data' : JSON.stringify(inputObj) }); 

      options.path = '/soundnerd/music/similar',
      options.headers = {
        'Content-Type':'application/x-www-form-urlencoded',
        'Content-Length':input.length
      }

      var similarReq = http.request(options, function(similarRes) {
        var body = ""; // reponse data 들이 담길 변수

        similarRes.on('data', function(chunk) {
          body += chunk; // 보나셀 서버에서의 response data
        }).
        on('end', function() {
          var tracks = JSON.parse(body).tracks; 
          if (tracks.length == 0) {
            res.end('no track');
            return callback(new Error('no track with feature :', featureTrack.title, featureTrack.artist, 'found.' ));
          }
          callback(null, tracks);
        });
      });

      similarReq.end(input); // 보나셀 서버에 request
    },
    // 추천 받은 음악들 중에 score가 90을 초과하는 음악 중 랜덤으로 하나를 선택한다. 만약 score가 90을 초과하는 곡이 없다면 가장 유사한 곡을 선택한다.
    // 선택된 곡의 정보를 가지고 보나셀에 search request를 하여 곡의 url 정보를 받아온다.
    // search로 검색되지 않으면 에러를 리턴한다.
    function (tracks, callback) {
      var i = 0;

      while(i < tracks.length && Number(tracks[i].score) > 90) { ++i }

      var tmp = i;
      i = Math.floor(Math.random() * i);

      console.log('-----------RAND--------------'); 
      console.log(tmp, 'TO', i+1); 
      console.log('title :', tracks[i].title, 'artist', tracks[i].artist);
      console.log('-----------------------------'); 
 
      var searchObj = new Object();
         
      searchObj.artist = (tracks[i].artist.indexOf(',') == -1)? tracks[i].artist : tracks[i].artist.split(',')[0];
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
        }).
        on('end', function() {
          var foundTrack = JSON.parse(body).tracks;

          console.log(foundTrack);

          if(foundTrack.length == 0) callback(new Error('not search track with data :', tracks[i].title, tracks[i].artist));
          callback(null, foundTrack[0]);
        });
      });

      searchReq.end(input);
    },
    // 추천인디 서버 DB에 추천된 곡의 정보가 musicinfos 컬렉션에 존재하는지 확인한다. 
    // 컬렉션 내에 추천된 곡의 정보가 존재하면 DB내의 정보를 client에 response 해준다.
    // 존재하지 않으면 추천된 곡의 정보를 가지고 컬렉션에 새로 저장하고 곡의 정보를 client에 response 해준다.
    function (foundTrack, callback) {
      var videoId = foundTrack.url.split('v=')[1];
      MusicInfo.find({ track_id: foundTrack.track_id }, function (err, track) {
        if (err) return callback(err);
        else if (track.length > 0) {
          var resMessage = JSON.stringify({ track_id: track[0].track_id, url: vidoeId, artist: track[0].artist, title: track[0].title, like: track[0].like, unlike: track[0].unlike});
          res.end(resMessage);
          return callback(null, foundTrack.track_id));
        }
        MusicInfo.create({ track_id: foundTrack.track_id, artist: foundTrack.artist, title: foundTrack.title, like: 0, unlike: 0, count: 0 }, function (err) {
          if (err) return callback(err);
          var resMessage = JSON.stringify({ track_id: foundTrack.track_id, url: videoId, artist: foundTrack.artist, title: foundTrack.title, like: 0, unlike: 0 });
          res.end(resMessage);
          callback(null, foundTrack.track_id);
        }); 
      });
    },
    // 어느 유저가 어느 track_id의 음악을 추천 받았는지 로그를 남긴다.
    function (track_id, callback) {
      Log.create({ user_id: info.user_id, request: info.request, log: track_id }, function(err) {
        if (err) return callback(err);
        callback(null, 'recommendation complete');
      });
    }
  ],
  // 실행 결과 콘솔에 출력. 
  function (err, result) {
    if (err) console.log(err);
    else console.log(result);
  });
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
