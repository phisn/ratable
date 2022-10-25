// not working well with webpack devserver
Global / onChangedBuildSource := IgnoreSourceChanges

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.0"

val versions = new {
  val outwatch  = "1.0.0-RC10"
  val colibri   = "0.7.1"
  val rescala   = "6d9019e946"
  val jsoniter  = "2.17.0"
}

lazy val commonSettings = Seq(
  resolvers += "jitpack" at "https://jitpack.io",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.0",
    
    "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"   % "2.17.6",
    "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros" % "2.17.6",

    // rescala snapshot with rdt support. needs to be replaced with a release version > 0.31.0
    "com.github.rescala-lang.rescala"       %%% "rescala" % versions.rescala,
    ("com.github.rescala-lang.rescala"      %%% "kofre"   % versions.rescala).cross(CrossVersion.for2_13Use3),
  ),

  scalacOptions += "-scalajs",

  // configure Scala.js to emit a JavaScript module instead of a top-level script
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },

  webpack / version                 := "4.46.0",
  // Makes scalajs-bundler use yarn instead of npm
  useYarn := true,

  // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
  fastOptJS / webpackBundlingMode   := BundlingMode.LibraryOnly(),
  
  fastOptJS / webpackEmitSourceMaps := false,
  fullOptJS / webpackEmitSourceMaps := false,
 
  // We do not use code sharing by project, because this causes linking issues with scalajs and is difficult
  // integrating with scala jvm. Instead we have a shared source directory `core`.
  Compile / unmanagedSourceDirectories += (root / baseDirectory).value / "core"
)

lazy val root = project in file(".")

lazy val webapp = project
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer   := true,

    libraryDependencies          ++= Seq(
      "com.softwaremill.sttp.client3" %%% "core"           % "3.8.3",
      "com.softwaremill.sttp.client3" %%% "jsoniter"       % "3.8.3", 
      
      "io.github.outwatch"            %%% "outwatch"       % versions.outwatch,
      "com.github.cornerman"          %%% "colibri-router" % versions.colibri,

     ("org.scalamock"                 %%  "scalamock"      % "5.2.0"  % Test).cross(CrossVersion.for3Use2_13),
      "org.scalatest"                 %%% "scalatest"      % "3.2.14" % Test
    ),

    Compile / npmDevDependencies ++= Seq(
      // sane defaults for webpack development and production, see webpack.config.*.js
      "@fun-stack/fun-pack"    -> "0.2.6",
      // "@fun-stack/fun-pack"    -> "file:C:/Users/Phisn/Repos/fun-pack/fun-stack-fun-pack-0.2.7.tgz",
      
      // ui libraries
      "postcss"                -> "^8.4.16",
      "postcss-loader"         -> "^4.0.2",
      "tailwindcss"            -> "^3.1.8",
      "autoprefixer"           -> "^10.4.8",
      "daisyui"                -> "^2.31.0",
      
      // pwa support
      "workbox-webpack-plugin" -> "^6.5.4",
    ),

    startWebpackDevServer / version   := "3.11.3",
    // For Webpack v5
    // webpackCliVersion                 := "4.10.0",

    webpackDevServerPort              := 12345,
    webpackDevServerExtraArgs         := Seq("--color"),

    fastOptJS / webpackConfigFile     := Some(baseDirectory.value / "webpack.config.dev.js"),
    fullOptJS / webpackConfigFile     := Some(baseDirectory.value / "webpack.config.prod.js"),
  )

lazy val functions = project
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .settings(
    webpackConfigFile := Some(baseDirectory.value / "webpack.config.js"),
  )

addCommandAlias("prod", "webapp/fullOptJS/webpack")
addCommandAlias("dev", "devInit; devWatchAll; devDestroy")
addCommandAlias("test", "webapp/test")
addCommandAlias("devtest", "~; webapp/test")

addCommandAlias("devInit", "; webapp/fastOptJS/startWebpackDevServer")
addCommandAlias("devWatchAll", "~; webapp/fastOptJS/webpack;")
addCommandAlias("devDestroy", "webapp/fastOptJS/stopWebpackDevServer")
