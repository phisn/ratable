Global / onChangedBuildSource := IgnoreSourceChanges // not working well with webpack devserver

name                     := "Localrating"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.1.2"

val versions = new {
  val outwatch  = "1.0.0-RC8"
  val funPack   = "0.2.0"
  val scalaTest = "3.2.13"
  val rescala = "0.31.0"
}

lazy val scalaJsMacrotaskExecutor = Seq(
  // https://github.com/scala-js/scala-js-macrotask-executor
  libraryDependencies       += "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0",
  Compile / npmDependencies += "setimmediate"  -> "1.0.5", // polyfill
)

lazy val webapp = (project in file("webapp"))
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
  )
  .settings(scalaJsMacrotaskExecutor)
  .settings(
    name := "webapp",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies          ++= Seq(
      "org.ekrich" %%% "sconfig" % "1.4.9",
      
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % "2.17.0",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.17.0",

      "io.github.outwatch"                    %%% "outwatch"  % versions.outwatch,
      "org.scalatest"                         %%% "scalatest" % versions.scalaTest % Test,
      // rescala snapshot with rdt support. needs to be replaced with a release version > 0.31.0
      "com.github.rescala-lang.rescala"       %%% "rescala" % "6d9019e946",
     ("com.github.rescala-lang.rescala"       %%% "kofre" % "6d9019e946").cross(CrossVersion.for2_13Use3),
      // same for loci as above with rescala?
      "com.github.scala-loci.scala-loci"      %%% "scala-loci-communicator-webrtc" % "609b4c1b58",
      // do I need this?
      "com.github.scala-loci.scala-loci"      %%% "scala-loci-serializer-jsoniter-scala" % "609b4c1b58",
    ),
    Compile / npmDevDependencies ++= Seq(
      "@fun-stack/fun-pack" -> versions.funPack, // sane defaults for webpack development and production, see webpack.config.*.js
      "postcss" -> "^8.4.16",
      "postcss-loader" -> "^4.0.2",
      "tailwindcss" -> "^3.1.8",
      "autoprefixer" -> "^10.4.8",
      "daisyui" -> "^2.31.0",
    ),
    scalacOptions --= Seq(
      "-Xfatal-warnings",
    ), // overwrite option from https://github.com/DavidGregory084/sbt-tpolecat
    scalacOptions ++= Seq(
      "-scalajs",
      "-Dtest=test"
    ),
    useYarn := true, // Makes scalajs-bundler use yarn instead of npm
    scalaJSLinkerConfig ~= (_.withModuleKind(
      ModuleKind.CommonJSModule,
    )), // configure Scala.js to emit a JavaScript module instead of a top-level script
    scalaJSUseMainModuleInitializer   := true, // On Startup, call the main function
    webpackDevServerPort              := 12345,
    webpack / version                 := "4.46.0",
    startWebpackDevServer / version   := "3.11.3",
    webpackDevServerExtraArgs         := Seq("--color"),
    fullOptJS / webpackEmitSourceMaps := true,
    fastOptJS / webpackBundlingMode   := BundlingMode
      .LibraryOnly(), // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
    fastOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack.config.dev.js"),
    fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack.config.prod.js"),
    Test / requireJsDomEnv        := true,
  )

// https://github.com/olivergrabinski/scala-azure-fn
lazy val functionsBackend = (project in file("functions/backend"))
  .settings(
    name := "functions-register",
    libraryDependencies ++= Seq(
      "com.microsoft.azure.functions" % "azure-functions-java-library" % "2.0.1" % "provided",
      "com.azure" % "azure-messaging-webpubsub" % "1.1.6" % "provided"
    ),
    assembly / assemblyOutputPath := file(".") / "functions" / "deploy" / "scala-az-backend.jar",
  )

lazy val functionRun = taskKey[Unit]("Run Azure Function locally")
functionRun := {
  import scala.sys.process._
  sys.process.Process(Seq("func","start", "--java"), new java.io.File("./functions/deploy")).!
}

addCommandAlias("prod", "fullOptJS/webpack")
addCommandAlias("dev", "devInit; devWatchAll; devDestroy")
addCommandAlias("function", "functionBuild; functionRun")

addCommandAlias("functionBuild", "functionsBackend/assembly")

addCommandAlias("devInit", "; webapp/fastOptJS/startWebpackDevServer")
addCommandAlias("devWatchAll", "~; webapp/fastOptJS/webpack")
addCommandAlias("devDestroy", "webapp/fastOptJS/stopWebpackDevServer")
