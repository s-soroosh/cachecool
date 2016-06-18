fork in Test := true

val scalacacheVersion = "0.7.5"

lazy val root = (project in file(".")).
  settings(
    name := "cachecool",
    // Execute tests in the current project serially
    parallelExecution in Test := false,
    scalaVersion := "2.11.8",

    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    libraryDependencies += "com.typesafe" % "config" % "1.3.0",
    libraryDependencies += "com.google.inject" % "guice" % "4.0",
    libraryDependencies ++= Seq(
      "com.github.cb372" %% "scalacache-core" % scalacacheVersion,
      "com.github.cb372" %% "scalacache-redis" % scalacacheVersion,
      "com.github.cb372" %% "scalacache-guava" % scalacacheVersion
    )
  )