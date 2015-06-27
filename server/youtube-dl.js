var exec = require('child_process').exec,
    child;

process.on('message', function(m) {
  child = exec('youtube-dl --extract-audio  --prefer-ffmpeg --audio-format mp3 -o ' + m.filename + ' ' + m.url, function (err, stdout, stderr) {
    console.log(stdout);
    process.exit(0);
  });
});
