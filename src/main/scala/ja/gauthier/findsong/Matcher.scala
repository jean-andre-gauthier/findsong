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

/**
 * This object contains helper methods to match a signal against a song index
 */
object Matcher {
    /**
     *  Iterates through the peak pairs and looks up the song index, in order to retrieve the matching songs and the offsets at which the matches occurred.
     *
     *  @param peakPairs the peak pairs for which matches have to be computed
     *  @param songIndex the index containing the song fingerprints
     *  @return a map of matching songs and the offsets at which the matches occurred
     */
    private def peakPairsToSongOffsets(peakPairs: PeakPairs, songIndex: SongIndex): SongOffsets = {
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

    /**
     *  Computes the fingerprints for a signal and retrieves the matching songs from the song index.
     *
     *  @param signal the signal to be matched against the song index
     *  @param songIndex the index containing the song fingerprints
     *  @param settings a Settings object containing the options for the app
     *  @return a list of matching songs and their score
     */
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

    /**
     *  Returns a sorted list of match scores
     *
     *  @param songConfidence a list of unsorted match scores
     *  @param settings a Settings object containing the options for the app
     *  @return a list of sorted match scores
     */
    private def songConfidenceToMatches(songConfidence: SongConfidence)(implicit settings: Settings): Matches = {
        val matches = songConfidence.toSeq.map((songConfidence) => Match(songConfidence._1, songConfidence._2)).sorted.take(settings.General.maxMatches)
        matches
    }

    /**
     *  Returns a list of match scores for a song offset map. A song's match score is the mode of its offset divided by the sum of all modes.
     *
     *  @param songOffsets the song offset map
     *  @return a list of match scores
     */
    private def songOffsetsToSongConfidence(songOffsets: SongOffsets): SongConfidence = {
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
