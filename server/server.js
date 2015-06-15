var express = require('express'),
    port = process.env.PORT | 19918,
    app = express(),
    bodyParser = require('body-parser');

var logger = require('morgan'),
    multer = require('multer');

var fs = require('fs');

var myLog = ':remote-addr :remote-user :method :url HTTP/:http-version :status :res[content-length] - :response-time ms ":user-agent"';

var mongoose = require('mongoose');
require('./models/track'),
require('./models/musicInfo'),
require('./models/log');

mongoose.connect('mongodb://localhost/recommendationIndie');

var controller = require('./controller/controller');

var logDirectory = __dirname + '/log';
var musicDirectory = __dirname + '/music';

var FileStreamRotator = require('file-stream-rotator');
fs.existsSync(logDirectory) || fs.mkdirSync(logDirectory);
fs.existsSync(musicDirectory) || fs.mkdirSync(musicDirectory);

var accessLogStream = FileStreamRotator.getStream({
  filename: logDirectory + '/access-%DATE%.log',
  frequency: 'daily',
  verbose: false
});

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended : true }));
app.use(multer());
app.use(logger(myLog, {stream: accessLogStream}));

app.get('/', function(req, res) {
  console.log('comin');
  res.setHeader("Content-Type", "text/html; charset=utf-8");
  res.end('안녕하세요. 추천인디 사이트입니다.');
});

app.get('/like/:trackId/:userId', controller.like); 
app.get('/unlike/:trackId/:userId', controller.unlike);

app.get('/music', controller.list);
app.get('/streaming/:videoId/:trackId', controller.streaming);
app.get('/musicinfo/:videoId/:trackId/:userId', controller.musicinfo);
app.post('/recommendation', controller.recommendation);

// register music test... 
app.post('/register', controller.register);
app.post('/analysis', controller.analysis);

app.listen(port, function(err) {
  if(err) return console.log(err);
  console.log('listening on %s', port);
});
