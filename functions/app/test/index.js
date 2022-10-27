var lib = require('../lib.js')

module.exports = function (context, req, connection) {
  context.log(`Connection ID: ${JSON.stringify(lib)}`)
  context.log(`Connection ID: ${lib.distribute}`)
  context.done()
}