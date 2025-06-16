name := "merge-request-monitor"

version := "0.1"

scalaVersion := "2.13.16"

(run / fork) := true

libraryDependencies += "org.dispatchhttp" %% "dispatch-core" % "2.0.0"

val circeVersion = "0.14.14"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "com.typesafe" % "config" % "1.4.3"

// Test
libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "4.21.0" % "test")
libraryDependencies ++= Seq("org.specs2" %% "specs2-mock" % "4.21.0" % "test")
libraryDependencies += "com.github.tomakehurst" % "wiremock" % "3.0.1" % Test

(Test / scalacOptions) ++= Seq("-Yrangepos")
