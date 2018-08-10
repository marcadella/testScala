name := "testScala"

version := "0.1"

scalaVersion := "2.10.6"

logBuffered in Test := false

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.joda" % "joda-convert" % "1.8.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "com.typesafe.akka" %% "akka-actor" % "2.3.14")