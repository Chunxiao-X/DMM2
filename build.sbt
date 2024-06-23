ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "project",
    libraryDependencies ++= Seq(

      "com.softwaremill.sttp.client3" %% "core" % "3.5.1",
      "com.softwaremill.sttp.client3" %% "circe" % "3.5.1",
      "com.softwaremill.sttp.client3" %% "play-json" % "3.5.1",

      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1",
      
      "com.typesafe.play" %% "play-json" % "2.9.2",

      "org.jsoup" % "jsoup" % "1.13.1",

      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",

      "org.jfree" % "jfreechart" % "1.5.3"
    ),
    resolvers += Resolver.mavenCentral
  )