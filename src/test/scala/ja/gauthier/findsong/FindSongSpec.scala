package ja.gauthier.findsong

import ja.gauthier.findsong.types._
import java.lang.Runtime
import org.scalatest._
import java.util.concurrent.Executors
import scala.concurrent._

class FindSongSpec extends AsyncFunSpec with Matchers with PrivateMethodTester {
  val findSong = PrivateMethod[Future[Int]]('findSong)
  implicit val s = Settings
    .settings(
      Array(
        "--indexerGlob",
        "src/test/resources/song_?.mp4",
        "--matcherGlob",
        "src/test/resources/clip_?.mp4"
      ))
    .get
  implicit val ec = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors))

  describe("FindSong") {
    it(
      "should exit with a status code of 0 once the songs have been indexed and the clips matched") {
      val future = FindSong invokePrivate findSong(ec, s)
      future map { status =>
        status should be(0)
      }
    }
  }
}
