const {webDev} = require("@fun-stack/fun-pack");

// https://github.com/fun-stack/fun-pack
module.exports = webDev({
  indexHtml: "src/main/html/index.html",
  extraWatchDirs: [
    "assets/dev"
  ],
  extraStaticDirs: [
    "src" // for source maps
  ]
});
