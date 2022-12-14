const {webDev} = require("@fun-stack/fun-pack");
const {InjectManifest} = require('workbox-webpack-plugin');

// https://github.com/fun-stack/fun-pack
module.exports = webDev({
  // Custom html without workbox call (pwa)
  indexHtml: "src/main/html/index.dev.html",
  assetsDir: "assets",
  extraWatchDirs: [

  ],
  extraStaticDirs: [
    "src" // for source maps
  ]
});

// Enforcing resource finding from root directory to implement path based routing
// https://github.com/fun-stack/fun-pack/blob/master/src/webpack.config.web.dev.js
// https://webpack.js.org/plugins/html-webpack-plugin/
// https://webpack.js.org/guides/public-path/
// https://stackoverflow.com/questions/34620628/htmlwebpackplugin-injects-relative-path-files-which-breaks-when-loading-non-root
module.exports.output.publicPath = "/";

// Disable performance warnings in dev mode to remove warning about large bundle size
// https://stackoverflow.com/questions/49348365/webpack-4-size-exceeds-the-recommended-limit-244-kib
module.exports.performance = {
  hints: false,
  maxEntrypointSize: 512000,
  maxAssetSize: 512000
}

// Dont use service worker in dev mode
/*
module.exports.plugins = module.exports.plugins.concat([
  new InjectManifest({
    swSrc: './service-worker.js',
    swDest: 'sw.js',
    maximumFileSizeToCacheInBytes: 100000000, // 100MB
  })
])
*/

module.exports.mode = "production";
