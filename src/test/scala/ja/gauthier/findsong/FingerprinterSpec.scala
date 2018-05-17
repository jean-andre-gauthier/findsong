package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import breeze.signal._
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry._
import org.scalatest._
import scala.collection.JavaConverters._
import ja.gauthier.findsong.types.constellationMap._
import ja.gauthier.findsong.types.peak._
import ja.gauthier.findsong.types.peakPairs._
import ja.gauthier.findsong.types.song._
import ja.gauthier.findsong.types._
import ja.gauthier.findsong.types.songIndex._

class FingerprinterSpec extends FunSpec with Matchers {
  implicit val s = Settings.settings(Array("--inputDirectory", ".", "--inputFormat", "m4a")).get

  describe("constellationMapToPeakPairs") {
    describe("when the window contains no peaks") {
      it("should not return any peak pair") {
        val constellationMap = RTree.create[Peak, Point]()
        val peakPairs = Fingerprinter.constellationMapToPeakPairs(constellationMap)
        peakPairs should be (empty)
      }
    }

    describe("when the window contains more than fanout peaks") {
      it("should return fanout peak pairs") {
        val entries = Seq(
          (0, 2), (1, 3), (1, 4), (2, 1), (2, 2), (2, 3), (2, 4), (3, 0),
          (3, 1), (3, 2), (3, 3), (4, 0), (4, 1))
        val constellationMap = entries.foldLeft(RTree.create[Peak, Point]())((tree, indices) =>
            tree.add(Peak(1, indices._2, indices._1), Geometries.point(indices._1, indices._2)))
        val peakPairs = Fingerprinter.constellationMapToPeakPairs(constellationMap)
        val sortedPeakPairs = peakPairsToSortedPeakPairs(peakPairs)
        sortedPeakPairs should contain inOrderOnly (
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
        val entries = Seq((0, 0), (3, 0), (3, 2), (6, 0), (7, 1))
        val constellationMap = entries.foldLeft(RTree.create[Peak, Point]())((tree, indices) =>
            tree.add(Peak(1, indices._2, indices._1), Geometries.point(indices._1, indices._2)))
        val peakPairs = Fingerprinter.constellationMapToPeakPairs(constellationMap)
        val sortedPeakPairs = peakPairsToSortedPeakPairs(peakPairs)
        sortedPeakPairs.toArray should contain inOrderOnly (
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
        val peakPairs = Seq[(Peak, Peak)]()
        val song = Song(
            "album",
            "artist",
            "disc",
            "genre",
            "title",
            "track"
          )
        val songIndex = Fingerprinter.peakPairsToSongIndex(peakPairs, song)
        songIndex should be (empty)
      }
    }

    describe("when the peak pairs list is not empty") {
      it("should return all the peak pairs in the song index") {
        val peakPairs = Seq(
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
        val songIndex = Fingerprinter.peakPairsToSongIndex(peakPairs, song)
        songIndex should contain theSameElementsAs Seq(
            SongIndexKey(2, 0, 1) -> Seq(SongIndexValue(0, song)),
            SongIndexKey(2, 3, 1) -> Seq(SongIndexValue(0, song)),
            SongIndexKey(0, 1, 3) -> Seq(SongIndexValue(1, song)),
            SongIndexKey(3, 0, 2) -> Seq(SongIndexValue(1, song))
          )
      }
    }
  }

  describe("signalToSongIndex") {
    it("should return a map of SongIndexKey -> Song for the given signal") {
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
      val song = Song(
          "album",
          "artist",
          "disc",
          "genre",
          "title",
          "track"
        )
      val songIndex = Fingerprinter.signalToSongIndex(signal, song)
      songIndex should contain theSameElementsAs Seq(
          SongIndexKey(3, 3, 2) -> Seq(SongIndexValue(0, song), SongIndexValue(1, song)),
          SongIndexKey(3, 3, 3) -> Seq(SongIndexValue(0, song)),
          SongIndexKey(3, 4, 4) -> Seq(SongIndexValue(3, song))
        )
    }
  }

  describe("signalToSpectrogram") {

    val sine_6_length_16 = Array[Short](
      0 ,1, 0, -1, 0, 1, 0, -1,
      0 ,1, 0, -1, 0, 1, 0, -1
    )

    val sine_6_length_17 = Array[Short](
      0 ,1, 0, -1, 0, 1, 0, -1,
      0 ,1, 0, -1, 0, 1, 0, -1,
      0
    )

    val sine_2_4_length_16 = Array[Short](
      0, 17, 10, -3, 0, 3, -10, -17,
      0, 17, 10, -3, 0, 3, -10, -17
    )

    val sine_2_4_length_16_sine_6_length_16 = Array[Short](
      0, 17, 10, -3, 0, 3, -10, -17,
      0, 17, 10, -3, 0, 3, -10, -17,
      0, 1, 0, -1, 0, 1, 0, -1,
      0, 1, 0, -1, 0, 1, 0, -1
    )

    it("should return the spectrogram for a single wave") {
      val spectrogram = Fingerprinter.signalToSpectrogram(sine_6_length_16)
      spectrogram.rows should be (1)
      spectrogram.cols should be (8)
      spectrogram(0, ::).t.toArray should contain theSameElementsInOrderAs Seq(0, 0, 0, 2, 4, 2, 0, 0)
    }


    it("should return the spectrogram for a composite wave") {
      val spectrogram = Fingerprinter.signalToSpectrogram(sine_2_4_length_16)
      spectrogram.rows should be (1)
      spectrogram.cols should be (8)
      spectrogram(0, ::).t.toArray should contain theSameElementsInOrderAs Seq(1, 20, 38, 40, 38, 20, 1, 1)
    }

    it("should return multiple spectrograms when the number of samples exceeds the window limit") {
      val spectrogram = Fingerprinter.signalToSpectrogram(sine_2_4_length_16_sine_6_length_16)
      spectrogram(0, ::).t.toArray should contain theSameElementsInOrderAs Seq( 1, 20, 38, 40, 38, 20,  1, 1)
      spectrogram(1, ::).t.toArray should contain theSameElementsInOrderAs Seq(22, 24, 24, 22, 24, 20, 11, 7)
      spectrogram(2, ::).t.toArray should contain theSameElementsInOrderAs Seq( 0,  0,  0,  2,  4,  2,  0, 0)
    }

    it("should discard data at the end of the signal if its size is not a multiple of bytes-per-chunk") {
      val spectrogram = Fingerprinter.signalToSpectrogram(sine_6_length_17)
      spectrogram.rows should be (1)
      spectrogram.cols should be (8)
      spectrogram(0, ::).t.toArray should contain theSameElementsInOrderAs Seq(0, 0, 0, 2, 4, 2, 0, 0)
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
        val constellationMap = Fingerprinter.spectrogramToConstellationMap(spectrogram)
        val entries = constellationMapToSortedEntries(constellationMap)
        entries should contain theSameElementsInOrderAs Seq((1, 1), (1, 6), (2, 4), (5, 0), (5, 7))
      }
    }

    describe("when the spectrogram is flat") {
      it("should return all points") {
        val spectrogram = DenseMatrix(
            (1, 1, 1, 1),
            (1, 1, 1, 1)
          )
        val constellationMap = Fingerprinter.spectrogramToConstellationMap(spectrogram)
        val entries = constellationMapToSortedEntries(constellationMap)
        entries should contain theSameElementsInOrderAs Seq((0, 0), (0, 1), (1, 0), (1, 1))
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
        val constellationMap = Fingerprinter.spectrogramToConstellationMap(spectrogram)
        val entries = constellationMapToSortedEntries(constellationMap)
        entries should contain theSameElementsInOrderAs Seq(
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
        val constellationMap = Fingerprinter.spectrogramToConstellationMap(spectrogram)
        val entries = constellationMapToSortedEntries(constellationMap)
        entries should contain theSameElementsInOrderAs Seq((0, 0), (0, 1), (3, 0), (3, 1), (6, 0), (6, 1))
      }
    }

    def constellationMapToSortedEntries(constellationMap: ConstellationMap): Seq[(Int, Int)] = {
        constellationMap
          .entries
          .toBlocking
          .toIterable
          .asScala
          .toSeq
          .sortBy(e => (e.geometry.x.toInt, e.geometry.y.toInt))
          .map(e => (e.geometry.x.toInt, e.geometry.y.toInt))
    }
  }
}
