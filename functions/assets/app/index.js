var { str } = require('../app.js')

module.exports = async function (context, req) {
  context.log('JavaScript HTTP trigger function processed a request.');

  return {
    // status: 200, /* Defaults to 200 */
    body: str
  };
}
