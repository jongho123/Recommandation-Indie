var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var MusicInfoSchema = new Schema({
  track_id:String,
  title:String,
  artist:String,
  like:Number,
  unlike:Number,
  count:Number
});

mongoose.model('musicinfo', MusicInfoSchema);
