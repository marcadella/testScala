name := "testScala"

organization := "no.uit.metapipe"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.4"

logBuffered in Test := false

resolvers ++= {
  Seq(
    Some(
      "Artifactory" at "https://artifactory.metapipe.uit.no/artifactory/sbt-release-local/"),
    if (version.value.endsWith("-SNAPSHOT"))
      Some(
        "Artifactory-dev" at "https://artifactory.metapipe.uit.no/artifactory/sbt-dev-local/")
    else
      None
  ).flatten
}

val scalaUtilsVer = "0.2.1-SNAPSHOT"

libraryDependencies ++= Seq(
 // "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2", //for scala 2.11
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.joda" % "joda-convert" % "2.1.2",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.json4s" %% "json4s-jackson" % "3.6.2",
  "com.typesafe.play" %% "play-json" % "2.6.11",
  "com.typesafe.akka" %% "akka-actor" % "2.5.18",
  "net.liftweb" %% "lift-json" % "3.3.0",
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "commons-io" % "commons-io" % "2.6",
  "com.lihaoyi" %% "scalarx" % "0.4.0",
  "io.minio" % "minio" % "5.0.6",
  "no.uit.sfb" %% "scala-utils-yaml" % scalaUtilsVer,
  "no.uit.sfb" %% "scala-utils-json" % scalaUtilsVer,
)