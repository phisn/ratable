var lib = require("../lib.js")

module.exports = function (context, req, connection) {
  lib.analytics(context)
  context.res = { body: connection }
  
  // context.done() is called inside lib.analytics
}
