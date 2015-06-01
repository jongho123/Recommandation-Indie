var express = require('express'),
    port = process.env.PORT | 19918,
    app = express(),
    bodyParser = require('body-parser'),
    controller = require('./controller/controller');

var logger = require('morgan'),
    multer = require('multer');

var myLog = ':remote-addr :remote-user :method :url HTTP/:http-version :status :res[content-length] - :response-time ms ":user-agent"';

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended : true }));
app.use(multer());
app.use(logger(myLog));

app.get('/', function(req, res) {
  res.end('Hello world');
});

app.get('/recommendation', controller.recommendation);
app.get('/music', controller.list);

// register music test... 
app.post('/', controller.register);

app.listen(port, function(err) {
  if(err) return console.log(err);
  console.log('listening on %s', port);
});
