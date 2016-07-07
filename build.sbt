name := "EverSyncScala"

version := "1.0"

scalaVersion := "2.11.8"

// https://mvnrepository.com/artifact/com.evernote/evernote-api
libraryDependencies ++= Seq(
  "com.evernote" % "evernote-api" % "1.25.1",
  "com.typesafe" % "config" % "1.3.0"
)


scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation, Opts.compile.unchecked) ++
  Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint", "-feature")

