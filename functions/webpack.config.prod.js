const {webProd} = require("@fun-stack/fun-pack");

// https://github.com/fun-stack/fun-pack
module.exports = webProd({
  assetsDir: "assets",
});

module.exports.output = {
  filename: "app.js",
  library: "app",
  libraryTarget: "commonjs2",
}
