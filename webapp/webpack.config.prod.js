const {webProd} = require("@fun-stack/fun-pack");

// https://github.com/fun-stack/fun-pack
const config = webProd({
  indexHtml: "src/main/html/index.html",
  assetsDir: "assets",
});

// See webpack.config.dev.js for explanation
config.output.publicPath = "/";

module.exports = config;
