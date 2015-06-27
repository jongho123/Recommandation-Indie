var exec = require('child_process').exec,
    child;

process.on('message', function(m) {
  child = exec('./controller/feature/extract ' + m.input + ' ' + m.output, function (err, stdout, stderr) {
    console.log(stdout);
    process.exit(0);
  });
});
