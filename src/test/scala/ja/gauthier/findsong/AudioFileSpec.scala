package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import breeze.signal._
import org.scalatest._
import java.io._

class AudioFileSpec extends FunSpec with Matchers {
  val testFile = "./src/test/resources/test.m4a"

  describe("extractSongMetadata") {
    it("should extract the song's metadata") {
      val song = AudioFile.extractSongMetadata(testFile)
      song.album shouldBe "Listen Here!"
      song.artist shouldBe "Eddie Palmieri"
      song.disc shouldBe "1/1"
      song.genre shouldBe "Jazz"
      song.title shouldBe "In Flight"
      song.track shouldBe "1/10"
    }
  }

  describe("extractFileSignal") {
    ignore("should extract the song's audio signal") {
      val signal = AudioFile.extractFileSignal(testFile)
      signal.length shouldBe 112745
    }
  }
}
