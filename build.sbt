import sbt.Keys._

fork in Test := true

val scalacacheVersion = "0.7.5"
val ScalaVersion = "2.11.8"
val PlayVersion = "2.4.6"


lazy val commonSettings =
  Defaults.coreDefaultSettings ++
    Seq(
      organization := "de.zalando.cachecool",
      scalaVersion := ScalaVersion,
      crossScalaVersions := Seq(ScalaVersion, "2.12.0-M4"),
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
      resolvers += Resolver.typesafeRepo("releases"),
      parallelExecution in Test := false,
      releaseCrossBuild := true,
      libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.9",
      libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
    )

lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "cachecool-core",
    libraryDependencies += "com.typesafe" % "config" % "1.3.0",
    libraryDependencies += "com.google.inject" % "guice" % "4.0",
    libraryDependencies ++= Seq(
      "com.github.cb372" %% "scalacache-core" % scalacacheVersion,
      "com.github.cb372" %% "scalacache-redis" % scalacacheVersion,
      "com.github.cb372" %% "scalacache-guava" % scalacacheVersion
    )
  )

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "cachecool"
  ).aggregate(core, playmodule)

lazy val playmodule = (project in file("playmodule"))
  .settings(commonSettings: _*)
  .dependsOn(core)
  .settings(
    name := "cachecool-play-module",
    libraryDependencies += "com.typesafe.play" %% "play" % "2.4.6",
    libraryDependencies += "org.scalatestplus" %% "play" % "1.4.0" % "test",
    libraryDependencies += "com.typesafe.play" %% "play-cache" % PlayVersion

  )