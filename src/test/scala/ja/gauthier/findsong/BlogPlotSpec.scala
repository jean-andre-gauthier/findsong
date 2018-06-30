package ja.gauthier.findsong

import org.scalactic._
import org.scalatest._
import scala.collection.JavaConverters._
import ja.gauthier.findsong.types.song._
import ja.gauthier.findsong.types.songIndex._
import ja.gauthier.findsong.types._
import ja.gauthier.findsong.types.constellationMap._
import ja.gauthier.findsong.types.spectrogram._
import ja.gauthier.findsong.types.peakPairs._
import ja.gauthier.findsong.types.signal._
import ja.gauthier.findsong.types.songConfidence._
import ja.gauthier.findsong.types.songOffsets._
import ja.gauthier.findsong.types.matches._

@Ignore
class BlogPlotSpec
    extends FunSpec
    with Matchers
    with PrivateMethodTester {
  implicit val s = Settings.settings(Array("--debug", "--indexerGlob", "*.mp4")).get

  val EPS = 0.001

  val album = "album"
  val artist = "artist"
  val disc = "1"
  val genre = "latin"
  val track = "1"

  val song1 = Song(album, artist, disc, genre, "song 1", track)

  val signalSong1 = Array[Short](
    0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3,
    0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10,
    -17, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1,
    0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1
  )

  val song2 = Song(album, artist, disc, genre, "song 2", track)

  val signalSong2 = Array[Short](
    0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3,
    0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 1, 0, -1, 0, 1, 0, -1, 0,
    1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1
  )

  val signalClip = Array[Short](
    0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17,
    10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3,
    0, 3, -10, -17
  )

  val combineSongIndexes = PrivateMethod[SongIndex]('combineSongIndexes)
  val songIndex = Indexer invokePrivate combineSongIndexes(
    Seq(
      Fingerprinter.signalToSongIndex(signalSong1, song1),
      Fingerprinter.signalToSongIndex(signalSong2, song2)
    ))


  ignore("fingerprinter") {
    signalSong1.toFile("signal-song-1")
    val signalToSpectrogram = PrivateMethod[Spectrogram]('signalToSpectrogram)
    val spectrogram = Fingerprinter invokePrivate signalToSpectrogram(signalSong1, s)
    spectrogram.toFile("spectrogram")
    val spectrogramToConstellationMap = PrivateMethod[ConstellationMap]('spectrogramToConstellationMap)
    val constellationMap = Fingerprinter invokePrivate spectrogramToConstellationMap(spectrogram, s)
    constellationMap.toFile("constellation-map")
    val constellationMapToPeakPairs = PrivateMethod[PeakPairs]('constellationMapToPeakPairs)
    val peakPairs = Fingerprinter invokePrivate constellationMapToPeakPairs(constellationMap, s)
    peakPairs.toFile("peak-airs-fingerprinter")
    val peakPairsToSongIndex = PrivateMethod[SongIndex]('peakPairsToSongIndex)
    val songIndex = Fingerprinter invokePrivate peakPairsToSongIndex(peakPairs, song1, s)
    songIndex.toFile("song-index-fingerprinter")
  }

  ignore("matcher") {
    songIndex.toFile("song-index-matcher")
    signalClip.toFile("signal-clip")
    val peakPairs = Fingerprinter.signalToPeakPairs(signalClip, Song("", "", "", "", "clip", ""))(s)
    peakPairs.toFile("peak-pairs-matcher")
    val peakPairsToSongOffsets = PrivateMethod[SongOffsets]('peakPairsToSongOffsets)
    val songOffsets = Matcher invokePrivate peakPairsToSongOffsets(peakPairs, songIndex)
    songOffsets.toFile("song-offsets")
    val songOffsetsToSongConfidence = PrivateMethod[SongConfidence]('songOffsetsToSongConfidence)
    val songConfidence = Matcher invokePrivate songOffsetsToSongConfidence(songOffsets, s)
    songConfidence.toFile("song-confidence")
    val songConfidenceToMatches = PrivateMethod[Matches]('songConfidenceToMatches)
    val matches = Matcher invokePrivate songConfidenceToMatches(songConfidence, s)
    matches.toFile("matches")
  }
}
