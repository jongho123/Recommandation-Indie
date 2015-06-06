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
var FileStreamRotator = require('file-stream-rotator');
fs.existsSync(logDirectory) || fs.mkdirSync(logDirectory);

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
  res.end('안녕하세요. 추천인디 사이트입니다.');
});

app.get('/recommendation', controller.recommendation);
app.get('/music', controller.list);

// register music test... 
app.post('/register', controller.register);
app.post('/play', controller.play);

app.listen(port, function(err) {
  if(err) return console.log(err);
  console.log('listening on %s', port);
});
