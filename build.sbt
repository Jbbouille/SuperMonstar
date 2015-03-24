name := "supermonstar2"

version := "1.0"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.8",
  "org.scaldi" %% "scaldi-akka" % "0.5.3",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "org.apache.tika" % "tika-core" % "1.7" exclude("org.gagravarr", "vorbis-java-tika") exclude("org.gagravarr", "vorbis-java-core") ,
  "org.apache.tika" % "tika-parsers" % "1.7" exclude("org.gagravarr", "vorbis-java-tika") exclude("org.gagravarr", "vorbis-java-core") ,
  "org.gagravarr" % "vorbis-java-tika" % "0.7" intransitive(),
  "org.gagravarr" % "vorbis-java-core" % "0.7" intransitive()
)

Revolver.settings