name := "merge-request-monitor"

version := "0.1"

scalaVersion := "2.12.17"

(run / fork) := true

libraryDependencies += "org.dispatchhttp" %% "dispatch-core" % "1.0.3"

val circeVersion = "0.11.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
).map(_ % circeVersion)

libraryDependencies += "com.typesafe" % "config" % "1.4.0"

// Test
libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "4.3.6" % "test")
libraryDependencies ++= Seq("org.specs2" %% "specs2-mock" % "4.3.6" % "test")
libraryDependencies += "com.github.tomakehurst" % "wiremock-jre8" % "2.24.1" % Test

(Test / scalacOptions) ++= Seq("-Yrangepos")
