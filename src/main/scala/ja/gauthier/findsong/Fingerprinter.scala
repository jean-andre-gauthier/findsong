package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import breeze.numerics._
import breeze.numerics.constants._
import breeze.signal._
import breeze.util._
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry._
import ja.gauthier.findsong.types.constellationMap._
import ja.gauthier.findsong.types.peak._
import ja.gauthier.findsong.types.peakPair._
import ja.gauthier.findsong.types.peakPairs._
import ja.gauthier.findsong.types.Settings
import ja.gauthier.findsong.types.signal._
import ja.gauthier.findsong.types.song._
import ja.gauthier.findsong.types.songIndex._
import ja.gauthier.findsong.types.spectrogram._
import scala.collection.JavaConverters._

/**
  *  This object contains helper methods for creating a signal fingerprint.
  */
object Fingerprinter {

  /**
    *  Creates a list of peak pairs by iterating through a constellation map and pairing up all peaks that are in the vicinity of each other. The rectangular window in which neighbouring pairs are scanned is [f + settings.PeakPairs.windowDeltaF, f - settings.PeakPairs.windowDeltaF], [t + settings.PeakPairs.windowDeltaTi, t + settings.PeakPairs.windowDeltaTi + settings.PeakPairs.windowDeltaT].
    *
    *  @param constellationMap a map that contains the peaks to pair up
    *  @param settings a Settings object containing the options for the app
    *  @return the peak pairs in the constallation map
    */
  private def constellationMapToPeakPairs(constellationMap: ConstellationMap)(
      implicit settings: Settings): PeakPairs = {
    val peakEntries = constellationMap.entries
      .toBlocking()
      .toIterable()
      .asScala
    val peakPairs = peakEntries
      .flatMap(
        peakEntry =>
          constellationMap
            .search(Geometries.rectangle(
              peakEntry.geometry.x() + settings.PeakPairs.windowDeltaTi,
              peakEntry.geometry.y() - settings.PeakPairs.windowDeltaF,
              peakEntry.geometry.x() + settings.PeakPairs.windowDeltaT,
              peakEntry.geometry.y() + settings.PeakPairs.windowDeltaF
            ))
            .toBlocking()
            .toIterable()
            .asScala
            .toSeq
            .map(_.value())
            .sorted
            .take(settings.PeakPairs.fanout)
            .map(otherPeak =>
              if (peakEntry.value().time <= otherPeak.time)
                (peakEntry.value(), otherPeak)
              else
                (otherPeak, peakEntry.value())))
      .toSeq
    peakPairs
  }

  /**
    *  Applies the Hann function to an array of signals
    *
    *  @param signals the array of signals
    *  @return an array where the original signals have been hanned
    */
  private def hannFunction(
      signals: DenseMatrix[Double]): DenseMatrix[Double] = {
    signals :* (0.5 * (1.0 - cos(2.0 * Pi * signals.mapPairs((rowColumn, _) =>
      rowColumn._2.toDouble) / (signals.cols - 1.0))))
  }

  /**
    *  Creates an index containing the fingerprints from the song.
    *
    *  @param peakPairs the peak pairs that have to be inserted in the song index
    *  @param song the song the peak pairs have been extracted from
    *  @param settings a Settings object containing the options for the app
    *  @return an index with the song's fingerprints
    */
  private def peakPairsToSongIndex(peakPairs: PeakPairs, song: Song)(
      implicit settings: Settings): SongIndex = {
    val songIndexUnsortedValues = peakPairs
      .foldLeft(SongIndex())((songIndex, peakPair) => {
        val key = SongIndexKey(peakPair._1.frequency,
                               peakPair._2.frequency,
                               peakPair._2.time - peakPair._1.time)
        val values = songIndex
          .getOrElse(key, Seq.empty[SongIndexValue]) :+ SongIndexValue(
          peakPair._1.time,
          song)
        songIndex + (key -> values)
      })
    val songIndexSortedValues =
      songIndexUnsortedValues.mapValues(_.sortBy(_.t1))
    songIndexSortedValues.toFile(
      "peak-pairs-to-song-index-song-index-" + song.title)
    songIndexSortedValues
  }

  /**
    *  Extracts peak pairs from a signal.
    *
    *  @param signal the signal peak pairs have to be extracted from
    *  @param song the song metadata for the signal
    *  @param settings a Settings object containing the options for the app
    *  @return the peak pairs detected in the signal
    */
  def signalToPeakPairs(signal: Signal, song: Song)(
      implicit settings: Settings): PeakPairs = {
    signal.toFile("signal-to-peak-pairs-signal-" + song.title)
    val spectrogram = signalToSpectrogram(signal)
    spectrogram.toFile("signal-to-peak-pairs-spectrogram-" + song.title)
    val constellationMap = spectrogramToConstellationMap(spectrogram)
    constellationMap.toFile(
      "signal-to-peak-pairs-constellation-map-" + song.title)
    val peakPairs = constellationMapToPeakPairs(constellationMap)
    peakPairs.toFile("signal-to-peak-pairs-peak-pairs-" + song.title)
    peakPairs
  }

  /**
    *  Creates an index containing the fingerprints from the input signal.
    *
    *  @param signal the signal from which fingerprints are extracted
    *  @param song the song associated to the signal
    *  @param settings a Settings object containing the options for the app
    *  @return an index containing the fingerprints from the input signal
    */
  def signalToSongIndex(signal: Signal, song: Song)(
      implicit settings: Settings): SongIndex = {
    val peakPairs = signalToPeakPairs(signal, song)
    val songIndex = peakPairsToSongIndex(peakPairs, song)
    songIndex
  }

  /**
    *  Converts a signal from the time domain to the frequency domain.
    *
    *  @param signal the input signal
    *  @param settings a Settings object containing the options for the app
    *  @return the representation of the input signal in the frequency domain
    */
  private def signalToSpectrogram(signal: Signal)(
      implicit settings: Settings): Spectrogram = {
    val raggedChunks = signal
      .map(_.toDouble)
      .sliding(settings.Spectrogram.samplesPerChunk,
               settings.Spectrogram.samplesPerChunkStep)
      .toArray
    val chunks =
      if (raggedChunks.size > 0
          && raggedChunks.last.size != settings.Spectrogram.samplesPerChunk) {
        raggedChunks.init
      } else {
        raggedChunks
      }
    val chunksMatrix = if (chunks.size > 0) {
      JavaArrayOps.array2DToDm(chunks)
    } else {
      new DenseMatrix[Double](0, 0)
    }
    val windowedSignal = hannFunction(chunksMatrix)
    val spectrogram = windowedSignal(*, ::).map(row => {
      val frequencies = fourierTr(row)
      frequencies(0 until row.size / 2).map(_.abs.round.toInt)
    })
    spectrogram
  }

  /**
    *  Computes a constellation map for the given spectrogram. A point is considered as a peak if no other point has a higher amplitude in the window [f - settings.ConstellationMap.peakDeltaF, f + settings.ConstellationMap.peakDeltaF], [t - settings.ConstellationMap.peakDeltaT, t + settings.ConstellationMap.peakDeltaT]
    *
    *  @param spectrogram the spectogram based which the constellation map is computed
    *  @param settings a Settings object containing the options for the app
    *  @return the constellation map extracted from the spectrogram
    */
  private def spectrogramToConstellationMap(spectrogram: Spectrogram)(
      implicit settings: Settings): ConstellationMap = {
    val indices = spectrogram
      .mapPairs((rowColumn, _) => rowColumn)
    val peaks = indices(*, ::)
      .map(_.toArray
        .flatMap((rowColumn) => {
          val peak = Peak(spectrogram(rowColumn._1, rowColumn._2),
                          rowColumn._2,
                          rowColumn._1)
          val peakDeltaF = settings.ConstellationMap.peakDeltaF
          val rangeCols = scala.math
            .max(0, peak.frequency - peakDeltaF)
            .to(
              scala.math.min(spectrogram.cols - 1, peak.frequency + peakDeltaF))
          val peakDeltaT = settings.ConstellationMap.peakDeltaT
          val rangeRows = scala.math
            .max(0, peak.time - peakDeltaT)
            .to(scala.math.min(spectrogram.rows - 1, peak.time + peakDeltaT))

          if (peak.amplitude == spectrogram(rangeRows, rangeCols).max)
            Some(peak)
          else
            None
        }))
      .toArray
    val peaksAboveThreshold = peaks
      .map(_.sorted.take(settings.ConstellationMap.peaksPerChunk))
    val constellationMap = peaksAboveThreshold.flatten.toArray
      .foldLeft(RTree.create[Peak, Point])((tree, peak) =>
        tree.add(peak, Geometries.point(peak.time, peak.frequency)))
    constellationMap
  }
}
