// Not working well with webpack devserver
Global / onChangedBuildSource := IgnoreSourceChanges

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := Dependencies.scalaVersion

lazy val commonSettings = Seq(
  resolvers += "jitpack" at "https://jitpack.io",

  // Manually add because sbt-tpolecat overrides -scalajs option
  // https://github.com/typelevel/sbt-tpolecat/issues/102
  scalacOptions += "-scalajs",

  // Configure Scala.js to emit a JavaScript module instead of a top-level script
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  
  // Enable protobuf compilation
  // https://scalapb.github.io/docs/installation
  Compile / PB.targets := Seq(
    scalapb.gen(grpc = false) -> (Compile / sourceManaged).value
  ),
)

lazy val bundlerSettings = Seq(
  webpack / version                 := Dependencies.webpackVersion,

  // Makes scalajs-bundler use yarn instead of npm
  useYarn := true,

  // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
  fastOptJS / webpackBundlingMode   := BundlingMode.LibraryOnly(),
  
  fullOptJS / webpackEmitSourceMaps := false,
)

lazy val core = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings, Dependencies.coreDependencies)

lazy val webapp = project
  .dependsOn(core)
  .enablePlugins(ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)
  .settings(commonSettings, bundlerSettings, Dependencies.webappDependencies)
  .settings(
    scalaJSUseMainModuleInitializer   := true,

    Compile / npmDevDependencies     ++= Dependencies.webappNpmDevDependencies,
    Compile / npmDependencies        ++= Dependencies.webappNpmDependencies,
    
    startWebpackDevServer / version   := Dependencies.webpackDevServerVersion,
    
    // For Webpack v5
    // webpackCliVersion              := Dependencies.webpackCliVersion,

    webpackDevServerPort              := 12345,
    webpackDevServerExtraArgs         := Seq("--color"),

    fastOptJS / webpackConfigFile     := Some(baseDirectory.value / "webpack.config.dev.js"),
    fullOptJS / webpackConfigFile     := Some(baseDirectory.value / "webpack.config.prod.js"),
  )

/*
lazy val functions = project
  .dependsOn(core)
  .enablePlugins(ScalaJSBundlerPlugin) //, ScalablyTypedConverterPlugin)
  .settings(commonSettings, bundlerSettings, Dependencies.functionsDependencies)
  .settings(
    Compile / npmDependencies ++= Dependencies.functionsNpmDependencies,

    webpackConfigFile          := Some(baseDirectory.value / "webpack.config.js"),
  )

*/

addCommandAlias("prod", "webapp/fullOptJS/webpack; functions/fullOptJS/webpack")
addCommandAlias("dev", "devInit; devWatchAll; devDestroy")
addCommandAlias("devtest", "devInit; devTestWatchAll; devDestroy")
addCommandAlias("functions", "~; functions/fullOptJS/webpack")
addCommandAlias("test", "webapp/test")

addCommandAlias("devInit", "; webapp/fastOptJS/startWebpackDevServer")
addCommandAlias("devWatchAll", "~; webapp/fastOptJS/webpack")
addCommandAlias("devTestWatchAll", "~; webapp/fastOptJS/webpack; webapp/test")
addCommandAlias("devDestroy", "webapp/fastOptJS/stopWebpackDevServer")
