import play.Project._
import sbt.Keys._

name := "pfe-semantic-news"

version := "1.0-SNAPSHOT"


resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.anormcypher" %% "anormcypher" % "0.4.4",
  "jdom" % "jdom" % "1.0",
  "rome" % "rome" % "1.0",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "joda-time" % "joda-time" % "2.3",
  "com.sun.jersey" % "jersey-bundle" % "1.10",
  "org.json" % "json" % "20090211",
  "jp.t2v" %% "play2-auth"      % "0.11.0",
  "jp.t2v" %% "play2-auth-test" % "0.11.0" % "test",
  "org.apache.httpcomponents" % "httpclient" % "4.2"
)

play.Project.playScalaSettings