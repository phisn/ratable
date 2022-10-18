Global / onChangedBuildSource := IgnoreSourceChanges // not working well with webpack devserver

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.1.2"

val versions = new {
  val outwatch  = "1.0.0-RC8"
  val scalaTest = "3.2.13"
  val rescala   = "6d9019e946"
  val jsoniter  = "2.17.0"
}

val npmDependencies = Seq(
  "@fun-stack/fun-pack"    -> "^0.2.0", // sane defaults for webpack development and production, see webpack.config.*.js
  // ui libraries
  "postcss"                -> "^8.4.16",
  "postcss-loader"         -> "^4.0.2",
  "tailwindcss"            -> "^3.1.8",
  "autoprefixer"           -> "^10.4.8",
  "daisyui"                -> "^2.31.0",
  // pwa support
  "workbox-webpack-plugin" -> "^6.5.4",
)

lazy val scalaJsMacrotaskExecutor = Seq(
  // https://github.com/scala-js/scala-js-macrotask-executor
  libraryDependencies       += "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0",
)

// We do not use code sharing by project, because this causes linking issues with scalajs and is difficult
// integrating with scala jvm. Instead we have a shared source directory `core`.
lazy val coreProjectDependencySettings = Seq(
  Compile / unmanagedSourceDirectories += (root / baseDirectory).value / "core"
)

lazy val root = project in file(".")

lazy val webapp = project
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
  )
  .settings(coreProjectDependencySettings)
  .settings(scalaJsMacrotaskExecutor)
  .settings(
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies          ++= Seq(
      "com.softwaremill.sttp.client3" %%% "core"    % "3.7.6",
      
      "io.github.outwatch"                    %%% "outwatch"       % versions.outwatch,
      "com.github.cornerman"                  %%% "colibri-router" % "0.5.0",
      "org.scalatest"                         %%% "scalatest"      % versions.scalaTest % Test,

      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"   % "2.17.0",
      "com.github.plokhotnyuk.jsoniter-scala" %%  "jsoniter-scala-macros" % "2.17.0",

      // rescala snapshot with rdt support. needs to be replaced with a release version > 0.31.0
      "com.github.rescala-lang.rescala"       %%% "rescala" % versions.rescala,
     ("com.github.rescala-lang.rescala"       %%% "kofre"   % versions.rescala).cross(CrossVersion.for2_13Use3)
    ),

    Compile / npmDevDependencies ++= npmDependencies,

    // webpack, js and bundling

    scalacOptions += "-scalajs",
    
    useYarn := true, // Makes scalajs-bundler use yarn instead of npm
    webpack / version                 := "4.46.0",

    scalaJSLinkerConfig ~= (_.withModuleKind(
      ModuleKind.CommonJSModule,
    )), // configure Scala.js to emit a JavaScript module instead of a top-level script
    
    scalaJSUseMainModuleInitializer   := true, // On Startup, call the main function
    webpackDevServerPort              := 12345,
    startWebpackDevServer / version   := "3.11.3",
    webpackDevServerExtraArgs         := Seq("--color"),

    // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
    fastOptJS / webpackBundlingMode   := BundlingMode.LibraryOnly(),
    fastOptJS / webpackConfigFile     := Some(baseDirectory.value / "webpack.config.dev.js"),
    fullOptJS / webpackConfigFile     := Some(baseDirectory.value / "webpack.config.prod.js"),

    Test / requireJsDomEnv            := true,
  )

// https://github.com/olivergrabinski/scala-azure-fn
lazy val functionsBackend = (project in file("functions/backend"))
  .settings(coreProjectDependencySettings)
  .settings(
    resolvers += "jitpack" at "https://jitpack.io",
    name := "functions-register",
    libraryDependencies ++= Seq(
      "com.microsoft.azure.functions"         %  "azure-functions-java-library" % "2.0.1"  % "provided",
      "com.azure"                             %  "azure-messaging-webpubsub"    % "1.1.6"  % "provided",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"          % versions.jsoniter % "provided",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"        % versions.jsoniter % "compile-internal",
      "com.github.rescala-lang.rescala"       %% "rescala"                      % versions.rescala,
     ("com.github.rescala-lang.rescala"       %% "kofre"                        % versions.rescala).cross(CrossVersion.for2_13Use3)
    ),
    assembly / assemblyOutputPath := file(".") / "functions" / "deploy" / "scala-az-backend.jar",
  )

addCommandAlias("prod", "webapp/fullOptJS/webpack")
addCommandAlias("dev", "devInit; devWatchAll; devDestroy")
addCommandAlias("function", "functionBuild; functionRun")

addCommandAlias("devInit", "; webapp/fastOptJS/startWebpackDevServer")
addCommandAlias("devWatchAll", "~; webapp/fastOptJS/webpack; functionsBackend/assembly")
addCommandAlias("devDestroy", "webapp/fastOptJS/stopWebpackDevServer")
