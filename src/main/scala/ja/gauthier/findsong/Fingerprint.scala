package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import breeze.numerics._
import breeze.numerics.constants._
import breeze.signal._
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry._
import scala.collection.JavaConverters._
import Types._

object Fingerprint {
    val settings = Settings.settings

    def constellationMapToPeakPairs(constellationMap: ConstellationMap): PeakPairs = {
        val peakEntries = constellationMap
            .entries
            .toBlocking()
            .toIterable()
            .asScala
        val peakPairs = peakEntries
            .flatMap(peakEntry =>
                constellationMap
                    .search(
                        Geometries.rectangle(
                            peakEntry.geometry.x() + settings.ConstellationMap.windowDeltaTi,
                            peakEntry.geometry.y() - settings.ConstellationMap.windowDeltaF,
                            peakEntry.geometry.x() + settings.ConstellationMap.windowDeltaT,
                            peakEntry.geometry.y() + settings.ConstellationMap.windowDeltaF
                        ))
                        .take(settings.ConstellationMap.fanout)
                        .toBlocking()
                        .toIterable()
                        .asScala
                        .map(otherPeakEntry => (peakEntry.value(), otherPeakEntry.value()))
                )
            .toSeq
        peakPairs
    }

    def hannFunction(signals: DenseMatrix[Double]): DenseMatrix[Double] = {
        signals :* (0.5 * cos(2.0 * Pi * signals / (signals.cols - 1.0)))
    }

    def peakPairsToSongIndex(peakPairs: PeakPairs, song: Song): SongIndex = {
        peakPairs
            .foldLeft(Map.empty[SongIndexKey, Song])((songIndex, peakPair) =>
                    songIndex +
                        (SongIndexKey(
                            peakPair._1.frequency,
                            peakPair._2.frequency,
                            peakPair._2.time - peakPair._1.time)
                        -> song))
    }

    def signalToSongIndex(signal: Signal, song: Song): SongIndex =  {
        val spectrogram = signalToSpectrogram(signal)
        val constellationMap = spectrogramToConstellationMap(spectrogram)
        val peakPairs = constellationMapToPeakPairs(constellationMap)
        val songIndex = peakPairsToSongIndex(peakPairs, song)
        songIndex
    }

    def signalToSpectrogram(signal: Signal): Spectrogram = {
        val windowedSignal: DenseMatrix[Double] = hannFunction(
                DenseMatrix(
                    signal
                    .map((n: Byte) => n.toDouble)
                    .sliding(
                        settings.Spectrogram.bytesPerChunk,
                        settings.Spectrogram.bytesPerChunkStep)
                    .toList
                    .map(_.toArray)
                    :_*)
        )
        val spectrogram = windowedSignal(*,::).map(row => {
            val frequencies = fourierTr(row)
            frequencies(0 until row.size / 2).map(_.abs.toInt)
        })
        spectrogram
    }

    def spectrogramToConstellationMap(spectrogram: Spectrogram): ConstellationMap = {
        val peaks = spectrogram
            .pairs
            .iterator
            .toList
            .flatMap((rowColumnToAmplitude: ((Int, Int), Int)) => {
                val peak = Peak(
                    rowColumnToAmplitude._2,
                    rowColumnToAmplitude._1._2,
                    rowColumnToAmplitude._1._1)
                val peakDeltaF = settings.ConstellationMap.peakDeltaF
                val rangeCols = scala.math.max(0, peak.frequency - peakDeltaF)
                    .to(scala.math.min(spectrogram.cols, peak.frequency + peakDeltaF))
                val peakDeltaT = settings.ConstellationMap.peakDeltaT
                val rangeRows = scala.math.max(0, peak.time - peakDeltaT)
                    .to(scala.math.min(spectrogram.rows, peak.time + peakDeltaT))

                if (peak.amplitude == spectrogram(rangeRows, rangeCols).max)
                    Some(peak)
                else
                    None
        })
        val peaksAboveThreshold = peaks
            .sortBy(-_.amplitude)
            .slice(0, settings.ConstellationMap.peaksPerChunk)
        val constellationMap = peaksAboveThreshold
            .foldLeft(RTree.create[Peak, Point])((tree, peak) =>
                    tree.add(peak, Geometries.point(peak.time, peak.frequency)))
        constellationMap
    }
}
