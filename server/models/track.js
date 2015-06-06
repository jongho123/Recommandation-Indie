var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var TrackSchema = new Schema({
  title:String,
  artist:String,
  feature:String
});

mongoose.model('track', TrackSchema);
