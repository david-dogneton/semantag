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
  "rome" % "rome" % "1.0"
)

play.Project.playScalaSettings