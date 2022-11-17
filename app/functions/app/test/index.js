module.exports = function (context) {
//  lib.http(context)
  context.log(JSON.stringify(context))
  context.done()
}
