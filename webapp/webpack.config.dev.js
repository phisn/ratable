const {webDev} = require("@fun-stack/fun-pack");

// https://github.com/fun-stack/fun-pack
const config = webDev({
  indexHtml: "src/main/html/index.html",
  assetsDir: "assets",
  extraStaticDirs: [
    "src" // for source maps
  ]
});

// Enforcing resource finding from root directory to implement path based routing
// https://github.com/fun-stack/fun-pack/blob/master/src/webpack.config.web.dev.js
// https://webpack.js.org/plugins/html-webpack-plugin/
// https://webpack.js.org/guides/public-path/
// https://stackoverflow.com/questions/34620628/htmlwebpackplugin-injects-relative-path-files-which-breaks-when-loading-non-root
config.output.publicPath = "/";

module.exports = config;
