package ja.gauthier.findsong

import breeze.linalg._
import breeze.linalg.operators._
import breeze.linalg.support._
import breeze.util._
import ja.gauthier.findsong.types._
import ja.gauthier.findsong.types.signal._
import ja.gauthier.findsong.types.songIndex._
import javax.sound.sampled._
import java.io.File
import java.lang.Runtime
import java.nio.file.Paths
import java.util.concurrent._
import org.apache.commons.io._
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.io.StdIn
import scala.util.Failure
import scala.util.Success

/*
 *  The app's entry point.
 *
 *  Usage: [options]
 *
 * --debug                  Create intermediate dump files during song fingerprinting and matching (default = false)
 * --fanout <value>         Maximal number of peaks that can be paired with any given peak (default = 3)
 * --greenLevel <value>     Threshold for a match score to be displayed in green (default = 25)
 * -i, --indexerGlob <glob>
 *                          Glob for the song files to index
 * -m, --matcherGlob <glob>
 *                          If present, the clip files matching the glob are used instead of the microphone
 * --maxMatches <value>     Maximal number of matches returned by the search engine (default = 5)
 * --peakDeltaF <value>     Frequency range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 1)
 * --peakDeltaT <value>     Time range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 1)
 * --peaksPerChunk <value>  Maximal number of peaks in any given fingerprinting chunk (default = 2)
 * --samplesPerCapture <value>
 *                          Size of a microphone recording in samples (default = 80000)
 * --samplesPerChunk <value>
 *                          Size of a fingerprinting chunk in samples (default = 16)
 * --samplesPerChunkStep <value>
 *                          Size of the stride between two fingerprinting chunks in samples (default = 8)
 * --sampleRate <value>     Fingerprinting / recording sample rate (default = 8000)
 * --scoreCoefficient <value>
 *                          Coefficient that is used in the match scoring function (default = 30)
 * --windowDeltaF <value>   Frequency range in which neighbouring peaks can be paired up (default = 1)
 * --windowDeltaT <value>   Time range in which neighbouring peaks can be paired up (default = 4)
 * --windowDeltaTi <value>  Minimal time difference for neighbouring peaks to be paired up (default = 2)
 * --yellowLevel <value>    Threshold for a match score to be displayed in yellow (default = 10)
 *
 */
object FindSong extends App {
  println("findsong 1.0.3")
  Settings.settings(args) match {
    case Some(settings) =>
      implicit val executionContext = ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors))

      findSong(executionContext, settings) andThen {
        case Success(status) =>
          System.exit(status)
        case Failure(_) =>
          System.exit(1)
      }
    case None =>
      System.exit(1)
  }

  /**
    *  Indexes the songs in the input directory, and then waits on user input to record and match a clip.
    *
    *  @param settings a Settings object containing the options for the app
    */
  private def findSong(implicit executionContext: ExecutionContext,
                       settings: Settings): scala.concurrent.Future[Int] = {
    val indexerStart = System.nanoTime()

    Indexer.indexSongs
      .transform {
        case Success(songIndex) =>
          val indexerEnd = System.nanoTime()
          val indexerDuration =
            TimeUnit.NANOSECONDS.toMillis(indexerEnd - indexerStart)
          val indexSize = songIndex.foldLeft(0)(
            (acc: Int, keyValue: (SongIndexKey, Seq[SongIndexValue])) =>
              acc + keyValue._2.size)
          println(
            s"Indexing completed in ${indexerDuration} ms. Index contains ${indexSize} fingerprints")
          recordLoop(songIndex)
          Success(0)
        case Failure(exception) =>
          println("Failed to index songs: ")
          exception.printStackTrace()
          Success(1)
      }
  }

  /**
    *  Records and matches a clip through the built-in microphone everytime <ENTER> is pressed.
    *
    *  @param songIndex the index containing the song fingerprints
    *  @param settings a Settings object containing the options for the app
    */
  private def recordLoop(songIndex: SongIndex)(
      implicit settings: Settings): Unit = {
    val clipNamesClipSignals = settings.General.matcherGlob match {
      case Some(glob) =>
        Glob
          .getMatchingFiles(glob)
          .map(
            (file: File) =>
              (file.getCanonicalPath(),
               AudioFile.extractFileSignal(file.getCanonicalPath())))
          .toIterator
      case None =>
        Iterator
          .continually({
            println("Press <Enter> to start recording")
            StdIn.readLine()
          })
          .map((_) => {
            println("Recording...")
            val signal = Microphone.extractMicrophoneSignal
            println("Recording complete")
            ("microphone recording", signal)
          })
    }

    clipNamesClipSignals.foreach((clipNameClipSignal: (String, Signal)) => {
      val (clipName, clipSignal) = clipNameClipSignal
      val matcherStart = System.nanoTime()
      val matches = Matcher.signalToMatches(clipSignal, songIndex)
      val matcherEnd = System.nanoTime()
      val matcherDuration =
        TimeUnit.NANOSECONDS.toMillis(matcherEnd - matcherStart)
      val matchesSummaryOption = Matcher.getMatchesStatistics(matches)

      println(s"Matching for ${clipName} completed in ${matcherDuration} ms")
      matchesSummaryOption match {
        case Some(matchesSummary) =>
          println(matchesSummary)
        case None =>
          println("No matching song found")
      }
    })
  }
}
