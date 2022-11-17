var lib = require("../lib.js")

module.exports = function (context) {
//  lib.http(context)
  context.log(JSON.stringify(process.env))
  context.done()
}
