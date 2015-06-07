var exec = require('child_process').exec,
    child;

process.on('message', function(m) {
  child = exec('./controller/feature/extract ' + m.input + ' ' + m.output, function (err, stdout, stderr) {
    process.exit(0);
  });
});
