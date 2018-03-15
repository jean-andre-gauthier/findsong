import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "ja.gauthier",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "findsong",
    libraryDependencies ++= Seq(
      commonsIo,
      ffmpeg,
      scalaArm,
      scalatest % Test,
      typesafeConfig
    )
  )
