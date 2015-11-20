name := """osm-mandatory"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.graphhopper" % "graphhopper" % "0.5.0",
  "net.sf.trove4j" % "trove4j" % "3.0.3",
  "org.neo4j" % "neo4j" % "2.3.1",
  "org.neo4j" % "neo4j-slf4j" % "2.3.1"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
