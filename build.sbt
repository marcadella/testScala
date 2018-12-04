name := "testScala"

version := "0.1"

scalaVersion := "2.10.6"

logBuffered in Test := false

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.joda" % "joda-convert" % "1.8.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "com.typesafe.akka" %% "akka-actor" % "2.3.14",
  "net.liftweb" %% "lift-json" % "2.6",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "commons-io" % "commons-io" % "2.4",
  "com.lihaoyi" %% "scalarx" % "0.3.2")