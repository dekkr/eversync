name := "EverSyncScala"

version := "1.0"

scalaVersion := "2.11.8"

// https://mvnrepository.com/artifact/com.evernote/evernote-api
libraryDependencies += "com.evernote" % "evernote-api" % "1.25.1"


scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation, Opts.compile.unchecked) ++
  Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint", "-feature")

