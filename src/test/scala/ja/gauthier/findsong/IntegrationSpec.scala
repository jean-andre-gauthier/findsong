package ja.gauthier.findsong

import org.scalactic._
import org.scalatest._
import scala.collection.JavaConverters._
import ja.gauthier.findsong.types.song._
import ja.gauthier.findsong.types._

class IntegrationSpec extends FunSpec with Matchers {
  implicit val s = Settings.settings(Array("--inputDirectory", ".", "--inputFormat", "m4a")).get

  val EPS = 0.001

  val album = "album"
  val artist = "artist"
  val disc = "1"
  val genre = "latin"
  val track = "1"

  val song1 = Song(album, artist, disc, genre, "song 1", track)

  val signalSong1 = Array[Short](
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 1, 0, -1, 0, 1, 0, -1,
    0, 1, 0, -1, 0, 1, 0, -1,
    0, 1, 0, -1, 0, 1, 0, -1,
    0, 1, 0, -1, 0, 1, 0, -1,
    0, 1, 0, -1, 0, 1, 0, -1
  )

  val song2 = Song(album, artist, disc, genre, "song 2", track)

  val signalSong2 = Array[Short](
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 1, 0, -1, 0, 1, 0, -1,
    0, 1, 0, -1, 0, 1, 0, -1,
    0, 1, 0, -1, 0, 1, 0, -1,
    0, 1, 0, -1, 0, 1, 0, -1
  )

  val songIndex = Indexer.combineSongIndexes(Seq(
    Fingerprinter.signalToSongIndex(signalSong1, song1),
    Fingerprinter.signalToSongIndex(signalSong2, song2)
  ))

  describe("FindSong") {
    describe("when the signal is empty") {
      it("should return no results") {
        val signal = Array[Short]()
        val matches = Matcher.signalToMatches(signal, songIndex)
        matches should have length 0
      }
    }

    describe("when the signal mostly corresponds to song 1") {
      it("should match song 1 with the highest confidence") {
        val signal = Array[Short](
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17
        )
        val matches = Matcher.signalToMatches(signal, songIndex)
        matches should have length 2
        matches(0).song should be (song1)
        matches(0).confidence should be (0.75 +- EPS)
        matches(1).song should be (song2)
        matches(1).confidence should be (0.25 +- EPS)
      }
    }

    describe("when the signal is identical to song 1") {
      it("should match song 1 with the highest confidence") {
        val signal = Array[Short](
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 1, 0, -1, 0, 1, 0, -1,
          0, 1, 0, -1, 0, 1, 0, -1,
          0, 1, 0, -1, 0, 1, 0, -1,
          0, 1, 0, -1, 0, 1, 0, -1,
          0, 1, 0, -1, 0, 1, 0, -1
        )
        val matches = Matcher.signalToMatches(signal, songIndex)
        matches should have length 2
        matches(0).song should be (song1)
        matches(0).confidence should be (0.8 +- EPS)
        matches(1).song should be (song2)
        matches(1).confidence should be (0.2 +- EPS)
      }
    }

    describe("when the signal may correspond to song 1 or song 2") {
      it("should match song 1 and song 2 with equal confidence") {
        val signal1 = Array[Short](
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17
        )
        val matches1 = Matcher.signalToMatches(signal1, songIndex)
        matches1 should have length 2
        matches1(0).song should be (song1)
        matches1(0).confidence should be (0.5 +- EPS)
        matches1(1).song should be (song2)
        matches1(1).confidence should be (0.5 +- EPS)

        val signal2 = Array[Short](
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 17, 10, -3, 0, 3, -10, -17,
          0, 1, 0, -1, 0, 1, 0, -1,
          0, 1, 0, -1, 0, 1, 0, -1,
          0, 1, 0, -1, 0, 1, 0, -1,
          0, 1, 0, -1, 0, 1, 0, -1
        )
        val matches2 = Matcher.signalToMatches(signal2, songIndex)
        matches2 should have length 2
        matches2(0).song should be (song1)
        matches2(0).confidence should be (0.5 +- EPS)
        matches2(1).song should be (song2)
        matches2(1).confidence should be (0.5 +- EPS)
      }
    }
  }
}
