addSbtPlugin("org.scala-js"  % "sbt-scalajs"         % "1.11.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix"        % "0.10.4")
addSbtPlugin("com.eed3si9n"  % "sbt-assembly"        % "1.2.0")
addSbtPlugin("com.thesamet"  % "sbt-protoc"          % "1.0.3")

addSbtPlugin("org.scalameta"             % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.1")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.11"