const { webProd } = require("@fun-stack/fun-pack");
const { InjectManifest } = require('workbox-webpack-plugin');

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
    exclude: [
      // Exclude routes.json (used by azure static webapp) from precaching
      // because it is inaccessable and results in worker service crash -> offline mode not working
      /routes[.]js/ 
    ],
    swSrc: './service-worker.js',
    swDest: 'sw.js',
  })
])

module.exports = config;
