import sbt._

object Dependencies {
  lazy val commonsIo = "commons-io" % "commons-io" % "2.6"
  lazy val ffmpeg = "net.bramp.ffmpeg" % "ffmpeg" % "0.6.2"
  lazy val scalaArm = "com.jsuereth" %% "scala-arm" % "2.0"
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val typesafeConfig = "com.typesafe" % "config" % "1.3.2"
}
