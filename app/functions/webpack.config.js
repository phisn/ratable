const webpack = require('webpack');
const path = require("path");

module.exports = require('./scalajs.webpack.config');

module.exports.output = {
  globalObject: "this",
  // Write lib.js directly into /functions/app/lib.js for azure function debugging
  path: path.resolve(__dirname, "../../../../app"),

  filename: "lib.js",

  library: "lib",
  libraryTarget: "commonjs2",
}

// Tell webpack to use nodejs libraries. Else we get "window is not defined" errors
module.exports.target = "node";
