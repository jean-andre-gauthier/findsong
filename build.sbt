import Dependencies._

lazy val root = (project in file(".")).settings(
  connectInput in run := true,
  fork in run := true,
  inThisBuild(
    List(
      organization := "ja.gauthier",
      scalaVersion := "2.12.3",
      version := "1.0.6"
    )),
  javaOptions in run ++= Seq("-Xmx2G"),
  name := "findsong",
  libraryDependencies ++= Seq(
    breeze,
    breezeNatives,
    commonsIo,
    ffmpeg,
    logback,
    rtree,
    scalatest % Test,
    scopt,
    typesafeConfig
  )
)
