package ja.gauthier.findsong

import ja.gauthier.findsong.types.matchPackage._
import ja.gauthier.findsong.types.matches._
import ja.gauthier.findsong.types.peakPairs._
import ja.gauthier.findsong.types.Settings
import ja.gauthier.findsong.types.signal._
import ja.gauthier.findsong.types.song._
import ja.gauthier.findsong.types.songConfidence._
import ja.gauthier.findsong.types.songIndex._
import ja.gauthier.findsong.types.songOffsets._
import java.time.LocalDateTime

object Matcher {
    def peakPairsToSongOffsets(peakPairs: PeakPairs, songIndex: SongIndex): SongOffsets = {
        val songOffsets = peakPairs.foldLeft(SongOffsets())((songOffsetsMap, peakPair) => {
            val songIndexValues = songIndex.get(SongIndexKey(
                peakPair._1.frequency,
                peakPair._2.frequency,
                peakPair._2.time - peakPair._1.time))
            songIndexValues match {
                case Some(values) =>
                    val songOffsetsEntries = songOffsetsMap.toSeq ++ values.flatMap((value) => {
                        val offset = value.t1 - peakPair._1.time
                        if (offset >= 0)
                            Some(value.song -> Seq(offset))
                        else
                            None
                    })
                    songOffsetsEntries.groupBy(_._1).mapValues(_.flatMap(_._2))
                case None => songOffsetsMap
            }
        })
        songOffsets
    }

    def signalToMatches(signal: Signal, songIndex: SongIndex)(implicit settings: Settings): Matches = {
        signal.toFile("signal-to-matches-signal-microphone")
        val peakPairs = Fingerprinter.signalToPeakPairs(signal, Song("", "", "", "", "microphone", ""))
        peakPairs.toFile("signal-to-matches-peak-pairs-microphone")
        val songOffsets = peakPairsToSongOffsets(peakPairs, songIndex)
        songOffsets.toFile("signal-to-matches-song-offsets-microphone")
        val songConfidence = songOffsetsToSongConfidence(songOffsets)
        songConfidence.toFile("signal-to-matches-song-confidence-microphone")
        val matches = songConfidenceToMatches(songConfidence)
        matches.toFile("signal-to-matches-matches-microphone")
        matches
    }

    def songConfidenceToMatches(songConfidence: SongConfidence): Matches = {
        val matches = songConfidence.toSeq.map((songConfidence) => Match(songConfidence._1, songConfidence._2)).sorted
        matches
    }

    def songOffsetsToSongConfidence(songOffsets: SongOffsets): SongConfidence = {
        val songToMaxOffsetOccurrence = songOffsets
            .foldLeft(Map[Song, Int]())((songToMaxOffsetOccurrenceMap, songOffsetsPair) => {
                songToMaxOffsetOccurrenceMap + (
                    songOffsetsPair._1 -> songOffsetsPair._2.groupBy(identity).mapValues(_.size).maxBy(_._2)._2)
            })
        val totalOccurrences = songToMaxOffsetOccurrence.foldLeft(0)((occurrences, songMaxOffsetOccurrencePair) =>
            occurrences + songMaxOffsetOccurrencePair._2)
        val songConfidence = songToMaxOffsetOccurrence.mapValues(_ / totalOccurrences.toDouble)
        songConfidence
    }
}
