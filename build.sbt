name := "merge-request-monitor"

version := "0.1"

scalaVersion := "2.13.11"

(run / fork) := true

libraryDependencies += "org.dispatchhttp" %% "dispatch-core" % "1.2.0"

val circeVersion = "0.14.5"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
).map(_ % circeVersion)

libraryDependencies += "com.typesafe" % "config" % "1.4.2"

// Test
libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "4.20.1" % "test")
libraryDependencies ++= Seq("org.specs2" %% "specs2-mock" % "4.20.1" % "test")
libraryDependencies += "com.github.tomakehurst" % "wiremock-jre8" % "2.35.0" % Test

(Test / scalacOptions) ++= Seq("-Yrangepos")
