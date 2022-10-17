const {webProd} = require("@fun-stack/fun-pack");
const {InjectManifest} = require('workbox-webpack-plugin');

// https://github.com/fun-stack/fun-pack
const config = webProd({
  indexHtml: "src/main/html/index.html",
  assetsDir: "assets",
});

// See webpack.config.dev.js for explanation
config.output.publicPath = "/";

// https://developer.chrome.com/docs/workbox/modules/workbox-webpack-plugin/#injectmanifest-plugin
// example: https://gist.github.com/jeffposnick/fc761c06856fa10dbf93e62ce7c4bd57
config.plugins = config.plugins.concat([
  new InjectManifest({
    exclude: /routes[.]js/,
    swSrc: './service-worker.js',
    swDest: 'sw.js',
  })
])

module.exports = config;
