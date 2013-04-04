name := "awsns"

version := "1.0"

scalaVersion := "2.10.1"

resolvers += "Akka snapshots" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "org.littleshoot" % "dnsjava" % "2.1.3",
  "com.typesafe.akka" %% "akka-actor" % "2.2-SNAPSHOT"
)
