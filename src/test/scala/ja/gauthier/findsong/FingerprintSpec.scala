package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import breeze.signal._
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry._
import org.scalatest._
import scala.collection.JavaConverters._
import Types._

class FingerprintSpec extends FunSpec with Matchers {

  describe("constellationMapToPeakPairs") {
    describe("when the window contains no peaks") {
      it("should not return any peak pair") {
        val constellationMap = RTree.create[Peak, Point]()
        val peakPairs = Fingerprint.constellationMapToPeakPairs(constellationMap)
        peakPairs.length shouldEqual 0
      }
    }

    describe("when the window contains more than fanout peaks") {
      it("should return fanout peak pairs") {
        val entries = Array(
          (0, 2), (1, 3), (1, 4), (2, 1), (2, 2), (2, 3), (2, 4), (3, 0),
          (3, 1), (3, 2), (3, 3), (4, 0), (4, 1))
        val constellationMap = entries.foldLeft(RTree.create[Peak, Point]())((tree, indices) =>
            tree.add(Peak(1, indices._2, indices._1), Geometries.point(indices._1, indices._2)))
        val peakPairs = Fingerprint.constellationMapToPeakPairs(constellationMap)
        val sortedPeakPairs = peakPairsToSortedPeakPairs(peakPairs)
        sortedPeakPairs.length shouldEqual 9
        sortedPeakPairs.toArray shouldEqual Array(
            (Peak(1, 2, 0), Peak(1, 1, 2)),
            (Peak(1, 2, 0), Peak(1, 2, 2)),
            (Peak(1, 2, 0), Peak(1, 3, 2)),
            (Peak(1, 3, 1), Peak(1, 2, 3)),
            (Peak(1, 3, 1), Peak(1, 3, 3)),
            (Peak(1, 4, 1), Peak(1, 3, 3)),
            (Peak(1, 1, 2), Peak(1, 0, 4)),
            (Peak(1, 1, 2), Peak(1, 1, 4)),
            (Peak(1, 2, 2), Peak(1, 1, 4)),
          )
      }
    }

    describe("when the window contains less than fanout peaks") {
      it("should return the n peak pairs") {
        val entries = Array((0, 0), (3, 0), (3, 2), (6, 0), (7, 1))
        val constellationMap = entries.foldLeft(RTree.create[Peak, Point]())((tree, indices) =>
            tree.add(Peak(1, indices._2, indices._1), Geometries.point(indices._1, indices._2)))
        val peakPairs = Fingerprint.constellationMapToPeakPairs(constellationMap)
        val sortedPeakPairs = peakPairsToSortedPeakPairs(peakPairs)
        sortedPeakPairs.length shouldEqual 4
        sortedPeakPairs.toArray shouldEqual Array(
            (Peak(1, 0, 0), Peak(1, 0, 3)),
            (Peak(1, 0, 3), Peak(1, 0, 6)),
            (Peak(1, 0, 3), Peak(1, 1, 7)),
            (Peak(1, 2, 3), Peak(1, 1, 7)),
          )
      }
    }

    def peakPairsToSortedPeakPairs(peakPairs: PeakPairs): PeakPairs = {
      peakPairs.sortBy(peakPair => (peakPair._1.time, peakPair._1.frequency,
        peakPair._2.time, peakPair._2.frequency))
    }
  }

  describe("peakPairsToSongIndex") {
    describe("when the peak pairs list is empty") {
      it("should return an empty song index") {
        val peakPairs = Array[(Peak, Peak)]()
        val song = Song(
            "album",
            "artist",
            "disc",
            "genre",
            "title",
            "track"
          )
        val songIndex = Fingerprint.peakPairsToSongIndex(peakPairs, song)
        songIndex.size shouldEqual 0
      }
    }

    describe("when the peak pairs list is not empty") {
      it("should return all the peak pairs in the song index") {
        val peakPairs = Array(
          (Peak(1, 2, 0), Peak(1, 3, 1)),
          (Peak(1, 2, 0), Peak(1, 0, 1)),
          (Peak(1, 0, 1), Peak(1, 1, 4)),
          (Peak(1, 3, 1), Peak(1, 0, 3))
        )
        val song = Song(
            "album",
            "artist",
            "disc",
            "genre",
            "title",
            "track"
          )
        val songIndex = Fingerprint.peakPairsToSongIndex(peakPairs, song)
        songIndex.size shouldEqual 4
        songIndex shouldEqual Map(
            SongIndexKey(2, 0, 1) -> song,
            SongIndexKey(2, 3, 1) -> song,
            SongIndexKey(0, 1, 3) -> song,
            SongIndexKey(3, 0, 2) -> song
          )
      }
    }
  }

  describe("signalToSongIndex") {
    it("should return a map of SongIndexKey -> Song for the given signal") {
      val signal = Array[Byte](
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
      val song = Song(
          "album",
          "artist",
          "disc",
          "genre",
          "title",
          "track"
        )
      val songIndex = Fingerprint.signalToSongIndex(signal, song)
      println(songIndex)
      songIndex.size shouldEqual 3
      songIndex shouldEqual Map(
          SongIndexKey(3, 3, 2) -> song,
          SongIndexKey(3, 3, 3) -> song,
          SongIndexKey(3, 4, 4) -> song
        )
    }
  }

  describe("signalToSpectrogram") {

    val sine_6_length_16 = Array[Byte](
      0 ,1, 0, -1, 0, 1, 0, -1,
      0 ,1, 0, -1, 0, 1, 0, -1
    )

    val sine_2_4_length_16 = Array[Byte](
      0, 17, 10, -3, 0, 3, -10, -17,
      0, 17, 10, -3, 0, 3, -10, -17
    )

    val sine_2_4_length_16_sine_6_length_16 = Array[Byte](
      0, 17, 10, -3, 0, 3, -10, -17,
      0, 17, 10, -3, 0, 3, -10, -17,
      0, 1, 0, -1, 0, 1, 0, -1,
      0, 1, 0, -1, 0, 1, 0, -1
    )

    it("should return the spectrogram for a single wave") {
      val spectrogram = Fingerprint.signalToSpectrogram(sine_6_length_16)
      spectrogram.rows shouldEqual 1
      spectrogram.cols shouldEqual 8
      spectrogram(0, ::).t.toArray shouldEqual Array(0, 0, 0, 2, 4, 2, 0, 0)
    }


    it("should return the spectrogram for a composite wave") {
      val spectrogram = Fingerprint.signalToSpectrogram(sine_2_4_length_16)
      spectrogram.rows shouldEqual 1
      spectrogram.cols shouldEqual 8
      spectrogram(0, ::).t.toArray shouldEqual Array(1, 20, 38, 40, 38, 20, 1, 1)
    }

    it("should return multiple spectrograms when the number of samples exceeds the window limit") {
      val spectrogram = Fingerprint.signalToSpectrogram(sine_2_4_length_16_sine_6_length_16)
      spectrogram(0, ::).t.toArray shouldEqual Array( 1, 20, 38, 40, 38, 20,  1, 1)
      spectrogram(1, ::).t.toArray shouldEqual Array(22, 24, 24, 22, 24, 20, 11, 7)
      spectrogram(2, ::).t.toArray shouldEqual Array( 0,  0,  0,  2,  4,  2,  0, 0)
    }
  }

  describe("spectrogramToConstellationMap") {
    describe("when the spectrogram is composed of several peaks") {
      it("should return at most peaks-per-chunk peaks for each chunk") {
        val spectrogram = DenseMatrix(
            (1, 2, 5, 3, 2, 4, 0, 2),
            (2, 9, 4, 2, 2, 1, 5, 3),
            (0, 3, 4, 1, 7, 2, 2, 3),
            (1, 1, 3, 2, 2, 5, 1, 2),
            (0, 3, 5, 6, 2, 3, 2, 2),
            (7, 4, 3, 2, 1, 0, 1, 8))
        val constellationMap = Fingerprint.spectrogramToConstellationMap(spectrogram)
        val entries = constellationMapToSortedEntries(constellationMap)
        entries.length shouldEqual 5
        entries shouldEqual Array((1, 1), (1, 6), (2, 4), (5, 0), (5, 7))
      }
    }

    describe("when the spectrogram is flat") {
      it("should return all points") {
        val spectrogram = DenseMatrix(
            (1, 1, 1, 1),
            (1, 1, 1, 1)
          )
        val constellationMap = Fingerprint.spectrogramToConstellationMap(spectrogram)
        val entries = constellationMapToSortedEntries(constellationMap)
        entries.length shouldEqual 4
        entries shouldEqual Array((0, 0), (0, 1), (1, 0), (1, 1))
      }
    }

    describe("when the spectrogram is a trough") {
      it("should return the points on the border of the spectrogram") {
        val spectrogram = DenseMatrix(
            (2, 2, 2, 2, 2, 2, 2, 2),
            (2, 1, 1, 1, 1, 1, 1, 2),
            (2, 1, 1, 1, 1, 1, 1, 2),
            (2, 1, 1, 1, 1, 1, 1, 2),
            (2, 1, 1, 1, 1, 1, 1, 2),
            (2, 1, 1, 1, 1, 1, 1, 2),
            (2, 2, 2, 2, 2, 2, 2, 2),
          )
        val constellationMap = Fingerprint.spectrogramToConstellationMap(spectrogram)
        val entries = constellationMapToSortedEntries(constellationMap)
        entries.length shouldEqual 14
        entries shouldEqual Array(
          (0, 0), (0, 1), (1, 0), (1, 7), (2, 0), (2, 7), (3, 0), (3, 7),
          (4, 0), (4, 7), (5, 0), (5, 7), (6, 0), (6, 1))
      }
    }

    describe("when the spectrogram is a ridge") {
      it("should return the points on the ridge") {
        val spectrogram = DenseMatrix(
            (1, 1, 1, 1, 1, 1, 1, 1),
            (1, 1, 1, 1, 1, 1, 1, 1),
            (1, 1, 1, 1, 1, 1, 1, 1),
            (2, 2, 2, 2, 2, 2, 2, 2),
            (1, 1, 1, 1, 1, 1, 1, 1),
            (1, 1, 1, 1, 1, 1, 1, 1),
            (1, 1, 1, 1, 1, 1, 1, 1),
          )
        val constellationMap = Fingerprint.spectrogramToConstellationMap(spectrogram)
        val entries = constellationMapToSortedEntries(constellationMap)
        entries.length shouldEqual 6
        entries shouldEqual Array((0, 0), (0, 1), (3, 0), (3, 1), (6, 0), (6, 1))
      }
    }

    def constellationMapToSortedEntries(constellationMap: ConstellationMap): Array[(Int, Int)] = {
        constellationMap
          .entries
          .toBlocking
          .toIterable
          .asScala
          .toArray
          .sortBy(e => (e.geometry.x.toInt, e.geometry.y.toInt))
          .map(e => (e.geometry.x.toInt, e.geometry.y.toInt))
    }
  }


}
