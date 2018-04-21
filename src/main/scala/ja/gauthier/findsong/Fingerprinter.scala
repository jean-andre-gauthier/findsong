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
import ja.gauthier.findsong.types.settings._
import ja.gauthier.findsong.types.signal._
import ja.gauthier.findsong.types.song._
import ja.gauthier.findsong.types.songIndex._
import ja.gauthier.findsong.types.spectrogram._
import scala.collection.JavaConverters._

object Fingerprinter {
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
                        .toBlocking()
                        .toIterable()
                        .asScala
                        .toSeq
                        .map(_.value())
                        .sorted
                        .take(settings.ConstellationMap.fanout)
                        .map(otherPeak =>
                                if (peakEntry.value().time <= otherPeak.time)
                                    (peakEntry.value(), otherPeak)
                                else
                                    (otherPeak, peakEntry.value()))
                )
            .toSeq
        peakPairs
    }

    def hannFunction(signals: DenseMatrix[Double]): DenseMatrix[Double] = {
        signals :* (0.5 * (1.0 - cos(2.0 * Pi * signals.mapPairs((rowColumn, _) => rowColumn._2.toDouble) / (signals.cols - 1.0))))
    }

    def peakPairsToSongIndex(peakPairs: PeakPairs, song: Song): SongIndex = {
        val songIndexUnsortedValues = peakPairs
            .foldLeft(SongIndex())((songIndex, peakPair) => {
                val key = SongIndexKey(
                    peakPair._1.frequency,
                    peakPair._2.frequency,
                    peakPair._2.time - peakPair._1.time)
                val values = songIndex.getOrElse(key, Seq.empty[SongIndexValue]) :+ SongIndexValue(peakPair._1.time, song)
                songIndex + (key -> values)
            })
        val songIndexSortedValues = songIndexUnsortedValues.mapValues(_.sortBy(_.t1))
        songIndexSortedValues.toFile("peak-pairs-to-song-index-song-index-" + song.title)
        songIndexUnsortedValues
    }

    def signalToPeakPairs(signal: Signal, song: Song): PeakPairs = {
        signal.toFile("signal-to-peak-pairs-signal-" + song.title)
        val spectrogram = signalToSpectrogram(signal)
        spectrogram.toFile("signal-to-peak-pairs-spectrogram-" + song.title)
        val constellationMap = spectrogramToConstellationMap(spectrogram)
        constellationMap.toFile("signal-to-peak-pairs-constellation-map-" + song.title)
        val peakPairs = constellationMapToPeakPairs(constellationMap)
        peakPairs.toFile("signal-to-peak-pairs-peak-pairs-" + song.title)
        peakPairs
    }

    def signalToSongIndex(signal: Signal, song: Song): SongIndex =  {
        val peakPairs = signalToPeakPairs(signal, song)
        val songIndex = peakPairsToSongIndex(peakPairs, song)
        songIndex
    }

    def signalToSpectrogram(signal: Signal): Spectrogram = {
        val raggedChunks = signal
            .map(_.toDouble)
            .sliding(
                settings.Spectrogram.bytesPerChunk,
                settings.Spectrogram.bytesPerChunkStep)
            .toArray
        val chunks = if (
            raggedChunks.size > 0
            && raggedChunks.last.size != settings.Spectrogram.bytesPerChunk) {
            raggedChunks.init
        } else {
            raggedChunks
        }
        val chunksMatrix = if (chunks.size > 0) {
            JavaArrayOps.array2DToDm(chunks)
        } else {
            new DenseMatrix[Double](0,0)
        }
        val windowedSignal = hannFunction(chunksMatrix)
        val spectrogram = windowedSignal(*,::).map(row => {
            val frequencies = fourierTr(row)
            frequencies(0 until row.size / 2).map(_.abs.round.toInt)
        })
        spectrogram
    }

    def spectrogramToConstellationMap(spectrogram: Spectrogram): ConstellationMap = {
        val indices = spectrogram
            .mapPairs((rowColumn, _) => rowColumn)
        val peaks = indices(*, ::)
            .map(_
                    .toArray
                    .flatMap((rowColumn) => {
                        val peak = Peak(
                            spectrogram(rowColumn._1, rowColumn._2),
                            rowColumn._2,
                            rowColumn._1)
                        val peakDeltaF = settings.ConstellationMap.peakDeltaF
                        val rangeCols = scala.math.max(0, peak.frequency - peakDeltaF)
                            .to(scala.math.min(spectrogram.cols-1, peak.frequency + peakDeltaF))
                        val peakDeltaT = settings.ConstellationMap.peakDeltaT
                        val rangeRows = scala.math.max(0, peak.time - peakDeltaT)
                            .to(scala.math.min(spectrogram.rows-1, peak.time + peakDeltaT))

                        if (peak.amplitude == spectrogram(rangeRows, rangeCols).max)
                            Some(peak)
                        else
                            None
                    })
                )
            .toArray
        val peaksAboveThreshold = peaks
            .map(_.sorted.take(settings.ConstellationMap.peaksPerChunk))
        val constellationMap = peaksAboveThreshold
            .flatten
            .toArray
            .foldLeft(RTree.create[Peak, Point])((tree, peak) =>
                    tree.add(peak, Geometries.point(peak.time, peak.frequency)))
        constellationMap
    }
}
