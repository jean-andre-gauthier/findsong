package ja.gauthier.findsong

import org.scalactic._
import org.scalatest._
import scala.collection.JavaConverters._
import Types._

class MatcherSpec extends FunSpec with Matchers {

  val EPS = 0.001

  val album = "album"
  val artist = "artist"
  val disc = "1"
  val genre = "latin"
  val track = "1"

  val song0 = Song(album, artist, disc, genre, "song 0", track)
  val song1 = Song(album, artist, disc, genre, "song 1", track)
  val song2 = Song(album, artist, disc, genre, "song 2", track)
  val song3 = Song(album, artist, disc, genre, "song 3", track)
  val song4 = Song(album, artist, disc, genre, "song 4", track)

  describe("peakPairsToSongOffsets") {
    describe("when the song index is empty") {
      val songIndex = SongIndex()

      describe("when the signal doesn't contain any peak pair") {
        it("should return an empty song offset map") {
          val peakPairs = Seq.empty
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should have size 0
        }
      }

      describe("when the signal contains several peak pairs") {
        it("should return an empty song offset map") {
          val peakPairs = Seq(
            (Peak(0, 1, 1), Peak(0, 0, 3)),
            (Peak(0, 5, 7), Peak(0, 6, 8)),
            (Peak(0, 9, 11), Peak(0, 10, 12))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should have size 0
        }
      }
    }

    describe("when the song index contains exactly one peak pair") {
      val songIndex = SongIndex() + (SongIndexKey(1, 0, 1) -> Seq(SongIndexValue(1, song1)))

      describe("when the signal doesn't contain any peak pair") {
        it("should return an empty song offset map") {
          val peakPairs = Seq.empty
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should have size 0
        }
      }

      describe("when the signal contains the peak pair") {
        it("should return a song offset map with an entry for the song") {
          val peakPairs = Seq(
            (Peak(0, 1, 1), Peak(0, 0, 2)),
            (Peak(0, 1, 3), Peak(0, 2, 4))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should contain theSameElementsAs Seq(song1 -> Seq(0))
        }
      }

      describe("when the signal contains the peak pair, offset by 1") {
        it("should return a song offset map with an entry for the song") {
          val peakPairs = Seq(
            (Peak(0, 1, 0), Peak(0, 0, 1)),
            (Peak(0, 1, 3), Peak(0, 2, 4))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should contain theSameElementsAs Seq(song1 -> Seq(1))
        }
      }

      describe("when the signal contains a peak pair with a wrong time delta") {
        it("should return an empty song offset map") {
          val peakPairs = Seq(
            (Peak(0, 1, 1), Peak(0, 0, 3)),
            (Peak(0, 1, 3), Peak(0, 2, 4))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should have size 0
        }
      }

      describe("when the signal contains a peak pair with a wrong first frequency") {
        it("should return an empty song offset map") {
          val peakPairs = Seq(
            (Peak(0, 2, 1), Peak(0, 0, 2)),
            (Peak(0, 1, 3), Peak(0, 2, 4))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should have size 0
        }
      }

      describe("when the signal contains a peak pair with a wrong second frequency") {
        it("should return an empty song offset map") {
          val peakPairs = Seq(
            (Peak(0, 1, 1), Peak(0, 2, 2)),
            (Peak(0, 1, 3), Peak(0, 2, 4))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should have size 0
        }
      }

      describe("when the signal contains the peak pair with a negative offset") {
        it("should return an empty song offset map") {
          val peakPairs = Seq(
            (Peak(0, 1, 2), Peak(0, 0, 3)),
            (Peak(0, 1, 3), Peak(0, 2, 4))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should have size 0
        }
      }
    }

    describe("when the song index contains multiple peak pairs") {
      val songIndex = SongIndex() + (
        SongIndexKey(1, 3, 1) -> Seq(SongIndexValue(6, song4)),
        SongIndexKey(2, 0, 1) -> Seq(SongIndexValue(1, song1), SongIndexValue(2, song2), SongIndexValue(5, song3)),
        SongIndexKey(2, 1, 3) -> Seq(SongIndexValue(4, song1), SongIndexValue(6, song2)),
        SongIndexKey(2, 3, 1) -> Seq(SongIndexValue(10, song1)),
        SongIndexKey(4, 5, 3) -> Seq(SongIndexValue(0, song1), SongIndexValue(1, song2), SongIndexValue(2, song3), SongIndexValue(5, song4)),
        SongIndexKey(6, 0, 1) -> Seq(SongIndexValue(7, song0))
      )

      describe("when the signal doesn't contain any peak pair") {
        it("should return an empty song offset map") {
          val peakPairs = Seq.empty
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should have size 0
        }
      }

      describe("when the signal contains peak pairs from song 0") {
        it("should return a song offset map where song 0 has the largest number of identical offsets") {
          val peakPairs = Seq(
            (Peak(0, 6, 1), Peak(0, 0, 2)),
            (Peak(0, 3, 2), Peak(0, 2, 3))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should contain theSameElementsAs Seq(song0 -> Seq(6))
        }
      }

      describe("when the signal contains peak pairs from song 1") {
        it("should return a song offset map where song 1 has the largest number of identical offsets") {
          val peakPairs = Seq(
            (Peak(0, 4, 0), Peak(0, 5, 3)),
            (Peak(0, 2, 1), Peak(0, 0, 2)),
            (Peak(0, 2, 4), Peak(0, 1, 7)),
            (Peak(0, 3, 8), Peak(0, 1, 9)),
            (Peak(0, 2, 9), Peak(0, 3, 10)),
            (Peak(0, 2, 11), Peak(0, 3, 12))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should contain theSameElementsAs Seq(
            song1 -> Seq(0, 0, 0, 1),
            song2 -> Seq(1, 1, 2),
            song3 -> Seq(2, 4),
            song4 -> Seq(5)
            )
        }
      }

      describe("when the signal contains peak pairs from song 2") {
        it("should return a song offset map where song 2 has the largest number of identical offsets") {
          val peakPairs = Seq(
            (Peak(0, 2, 1), Peak(0, 0, 2)),
            (Peak(0, 2, 5), Peak(0, 1, 8)),
            (Peak(0, 2, 6), Peak(0, 2, 7))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should contain theSameElementsAs Seq(
            song1 -> Seq(0),
            song2 -> Seq(1, 1),
            song3 -> Seq(4)
            )
        }
      }

      describe("when the signal contains peak pairs from song 3") {
        it("should return a song offset map where song 3 has the largest number of identical offsets") {
          val peakPairs = Seq(
            (Peak(0, 4, 0), Peak(0, 5, 3)),
            (Peak(0, 2, 3), Peak(0, 0, 4)),
            (Peak(0, 6, 5), Peak(0, 0, 6))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should contain theSameElementsAs Seq(
            song0 -> Seq(2),
            song1 -> Seq(0),
            song2 -> Seq(1),
            song3 -> Seq(2, 2),
            song4 -> Seq(5)
            )
        }
      }

      describe("when the signal contains peak pairs from song 4") {
        it("should return a song offset map where song 4 has the largest number of identical offsets") {
          val peakPairs = Seq(
            (Peak(0, 2, 0), Peak(0, 1, 3)),
            (Peak(0, 4, 1), Peak(0, 5, 4)),
            (Peak(0, 1, 2), Peak(0, 3, 3)),
            (Peak(0, 6, 3), Peak(0, 0, 4))
          )
          val songOffsets = Matcher.peakPairsToSongOffsets(peakPairs, songIndex)
          songOffsets should contain theSameElementsAs Seq(
            song0 -> Seq(4),
            song1 -> Seq(4),
            song2 -> Seq(6, 0),
            song3 -> Seq(1),
            song4 -> Seq(4, 4)
            )
        }
      }
    }
  }
  
  describe("songConfidenceToMatches") {
    describe("when the confidence map is empty") {
      it("should return an empty match list") {
        val songConfidence = SongConfidence()
        val matches = Matcher.songConfidenceToMatches(songConfidence)
        matches should have size 0
      }
    }

    describe("when song 0 has the largest confidence") {
      it("should return a match list with song 0 as first element") {
        val songConfidence = Map(song0 -> 1.0)
        val matches = Matcher.songConfidenceToMatches(songConfidence)
        matches should contain theSameElementsInOrderAs Seq(Match(song0, 1.0))
      }
    }

    describe("when song 1 has the largest confidence") {
      it("should return a match list with song 1 as first element") {
        val songConfidence = Map(
            song1 -> 0.429,
            song2 -> 0.286,
            song3 -> 0.143,
            song4 -> 0.143
            )
        val matches = Matcher.songConfidenceToMatches(songConfidence)
        matches should contain theSameElementsInOrderAs Seq(
          Match(song1, 0.429),
          Match(song2, 0.286),
          Match(song3, 0.143),
          Match(song4, 0.143)
          )
      }
    }

    describe("when song 2 has the largest confidence") {
      it("should return a match list with song 2 as first element") {
        val songConfidence = Map(
            song1 -> 0.25,
            song2 -> 0.5,
            song3 -> 0.25
            )
        val matches = Matcher.songConfidenceToMatches(songConfidence)
        matches should contain theSameElementsInOrderAs Seq(
          Match(song2, 0.5),
          Match(song1, 0.25),
          Match(song3, 0.25)
          )
      }
    }

    describe("when song 3 has the largest confidence") {
      it("should return a match list with song 3 as first element") {
        val songConfidence = Map(
            song0 -> 0.167,
            song1 -> 0.167,
            song2 -> 0.167,
            song3 -> 0.333,
            song4 -> 0.167
            )
        val matches = Matcher.songConfidenceToMatches(songConfidence)
        matches should contain theSameElementsInOrderAs Seq(
          Match(song3, 0.333),
          Match(song0, 0.167),
          Match(song1, 0.167),
          Match(song2, 0.167),
          Match(song4, 0.167)
          )
      }
    }

    describe("when song 4 has the largest confidence") {
      it("should return a match list with song 4 as first element") {
        val songConfidence = Map(
            song0 -> 0.167,
            song1 -> 0.167,
            song2 -> 0.167,
            song3 -> 0.167,
            song4 -> 0.333
            )
        val matches = Matcher.songConfidenceToMatches(songConfidence)
        matches should contain theSameElementsInOrderAs Seq(
          Match(song4, 0.333),
          Match(song0, 0.167),
          Match(song1, 0.167),
          Match(song2, 0.167),
          Match(song3, 0.167)
          )
      }
    }
  }

  describe("songOffsetsToSongConfidence") {
    describe("when the offset map is empty") {
      it("should return an empty confidence map") {
        val songOffsets = SongOffsets()
        val songConfidence = Matcher.songOffsetsToSongConfidence(songOffsets)
        songConfidence should have size 0
      }
    }

    describe("when song 0 has the largest number of identical offsets") {
      it("return a confidence map where song 0 has the highest confidence") {
        val songOffsets = Map(
            song0 -> Seq(0)
            )
        val songConfidence = Matcher.songOffsetsToSongConfidence(songOffsets)
        songConfidence should have size 1
        songConfidence should contain key (song0)
        songConfidence(song0) should be (1.0 +- EPS)
      }
    }

    describe("when song 1 has the largest number of identical offsets") {
      it("return a confidence map where song 1 has the highest confidence") {
        val songOffsets = Map(
            song1 -> Seq(0, 0, 0, 1),
            song2 -> Seq(1, 1, 2),
            song3 -> Seq(2, 4),
            song4 -> Seq(5)
            )
        val songConfidence = Matcher.songOffsetsToSongConfidence(songOffsets)
        songConfidence should have size 4
        songConfidence should contain key (song1)
        songConfidence(song1) should be (0.429 +- EPS)
        songConfidence should contain key (song2)
        songConfidence(song2) should be (0.286 +- EPS)
        songConfidence should contain key (song3)
        songConfidence(song3) should be (0.143 +- EPS)
        songConfidence should contain key (song4)
        songConfidence(song4) should be (0.143 +- EPS)
      }
    }

    describe("when song 2 has the largest number of identical offsets") {
      it("return a confidence map where song 2 has the highest confidence") {
        val songOffsets = Map(
            song1 -> Seq(0),
            song2 -> Seq(1, 1),
            song3 -> Seq(4)
            )
        val songConfidence = Matcher.songOffsetsToSongConfidence(songOffsets)
        songConfidence should have size 3
        songConfidence should contain key (song1)
        songConfidence(song1) should be (0.25 +- EPS)
        songConfidence should contain key (song2)
        songConfidence(song2) should be (0.5 +- EPS)
        songConfidence should contain key (song3)
        songConfidence(song3) should be (0.25 +- EPS)
      }
    }

    describe("when song 3 has the largest number of identical offsets") {
      it("return a confidence map where song 3 has the highest confidence") {
        val songOffsets = Map(
            song0 -> Seq(2),
            song1 -> Seq(0),
            song2 -> Seq(1),
            song3 -> Seq(2, 2),
            song4 -> Seq(5)
            )
        val songConfidence = Matcher.songOffsetsToSongConfidence(songOffsets)
        songConfidence should have size 5
        songConfidence should contain key (song0)
        songConfidence(song0) should be (0.167 +- EPS)
        songConfidence should contain key (song1)
        songConfidence(song1) should be (0.167 +- EPS)
        songConfidence should contain key (song2)
        songConfidence(song2) should be (0.167 +- EPS)
        songConfidence should contain key (song3)
        songConfidence(song3) should be (0.333 +- EPS)
        songConfidence should contain key (song4)
        songConfidence(song4) should be (0.167 +- EPS)
      }
    }

    describe("when song 4 has the largest number of identical offsets") {
      it("return a confidence map where song 4 has the highest confidence") {
        val songOffsets = Map(
            song0 -> Seq(4),
            song1 -> Seq(4),
            song2 -> Seq(6, 0),
            song3 -> Seq(1),
            song4 -> Seq(4, 4)
            )
        val songConfidence = Matcher.songOffsetsToSongConfidence(songOffsets)
        songConfidence should have size 5
        songConfidence should contain key (song0)
        songConfidence(song0) should be (0.167 +- EPS)
        songConfidence should contain key (song1)
        songConfidence(song1) should be (0.167 +- EPS)
        songConfidence should contain key (song2)
        songConfidence(song2) should be (0.167 +- EPS)
        songConfidence should contain key (song3)
        songConfidence(song3) should be (0.167 +- EPS)
        songConfidence should contain key (song4)
        songConfidence(song4) should be (0.333 +- EPS)
      }
    }
  }
}
