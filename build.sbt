name := "testScala"

organization := "no.uit.metapipe"

version := "0.1"

scalaVersion := "2.11.12"

logBuffered in Test := false

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.joda" % "joda-convert" % "2.1.2",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.json4s" %% "json4s-jackson" % "3.6.2",
  "com.typesafe.play" %% "play-json" % "2.6.11",
  "com.typesafe.akka" %% "akka-actor" % "2.5.18",
  "net.liftweb" %% "lift-json" % "3.3.0",
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "commons-io" % "commons-io" % "2.6",
  "com.lihaoyi" %% "scalarx" % "0.4.0")