import sbt.*
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  lazy val scalaVersion = "3.2.0"

  lazy val versions = new {
    val scalajs   = "1.1.0"
    val jsoniter  = "2.17.6"
    val rescala   = "085d4cdbe8"
    val sttp      = "3.8.3"
    val colibri   = "0.7.1"
    val outwatch  = "1.0.0-RC10"
    val scalatest = "3.2.14"
  }

  lazy val npmDevVersions = new {
    val funpack              = "^0.2.6"
    val postcss              = "^8.4.16"
    val postcssloader        = "^4.0.2"
    val tailwindcss          = "^3.1.8"
    val autoprefixer         = "^10.4.8"
    val daisyui              = "^2.31.0"
    val workboxwebpackplugin = "^6.5.4"
  }

  lazy val npmVersions = new {
    val snabbdom = "github:outwatch/snabbdom.git#semver:0.7.5"
  }

  lazy val webpackDevServerVersion = "3.11.3"
  lazy val webpackVersion          = "4.46.0"
  lazy val webpackCliVersion       = "4.10.0"

  val coreDependencies = Seq(Keys.libraryDependencies ++= Seq(
    "org.scala-js"                          %%% "scala-js-macrotask-executor" % versions.scalajs,
    
    "com.thesamet.scalapb"                  %%% "scalapb-runtime"             % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb"                  %%% "scalapb-runtime"             % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"         % versions.jsoniter,
    "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros"       % versions.jsoniter,

    // Rescala snapshot with rdt support. needs to be replaced with a release version > 0.31.0
    "com.github.rescala-lang.rescala"       %%% "rescala"                     % versions.rescala,
   ("com.github.rescala-lang.rescala"       %%% "kofre"                       % versions.rescala).cross(CrossVersion.for2_13Use3),
  ))

  val webappDependencies = coreDependencies ++ Seq(Keys.libraryDependencies ++= Seq(
    "com.softwaremill.sttp.client3" %%% "core"           % versions.sttp,
    "com.softwaremill.sttp.client3" %%% "jsoniter"       % versions.sttp,
    
    "io.github.outwatch"            %%% "outwatch"       % versions.outwatch,
    "com.github.cornerman"          %%% "colibri-router" % versions.colibri,

    "org.scalatest"                 %%% "scalatest"      % versions.scalatest % Test
  ))

  val webappNpmDevDependencies = Seq(
    // Sane defaults for webpack development and production, see webpack.config.*.js
    "@fun-stack/fun-pack"    -> npmDevVersions.funpack,
    
    // UI libraries
    "postcss"                -> npmDevVersions.postcss,
    "postcss-loader"         -> npmDevVersions.postcssloader,
    "tailwindcss"            -> npmDevVersions.tailwindcss,
    "autoprefixer"           -> npmDevVersions.autoprefixer,
    "daisyui"                -> npmDevVersions.daisyui,

    // Pwa support
    "workbox-webpack-plugin" -> npmDevVersions.workboxwebpackplugin
  )

  val webappNpmDependencies = Seq(
    // Required by outwatch
    "snabbdom" -> npmVersions.snabbdom
  )

  val functionsNpmDependencies = Seq(
    "@types/node"   -> "latest",
    "@azure/cosmos" -> "latest",
  )

  val functionsDependencies = coreDependencies
}
