package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import breeze.signal._
import ja.gauthier.findsong.types._
import java.io._
import org.scalatest._

class AudioFileSpec extends FunSpec with Matchers {
  val testFile = "./src/test/resources/test.m4a"
  implicit val s = Settings.settings(Array("--inputDirectory", ".", "--inputFormat", "m4a")).get

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
    it("should extract the song's audio signal") {
      val signal = AudioFile.extractFileSignal(testFile)
      signal.length shouldBe 112845
    }
  }
}
