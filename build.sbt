name := "supermonstar2"

version := "1.0"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.scaldi" %% "scaldi-akka" % "0.5.3",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.elasticsearch" % "elasticsearch" % "1.5.0",
  "org" % "jaudiotagger" % "2.0.3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.8",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.8" % "test",
  "io.spray" % "spray-can_2.11" % "1.3.3",
  "io.spray" % "spray-routing_2.11" % "1.3.3",
  "io.spray" %% "spray-testkit" % "1.3.3" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.13"
)

Revolver.settings