var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var LogSchema = new Schema({
  user_id:String,
  request:String,
  log:String,
  createdAt:{ type:Date, default:Date.now() }
});

mongoose.model('log', LogSchema);
