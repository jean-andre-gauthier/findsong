package ja.gauthier.findsong

import breeze.linalg._
import breeze.linalg.operators._
import breeze.linalg.support._
import breeze.util._
import ja.gauthier.findsong.types._
import ja.gauthier.findsong.types.songIndex._
import javax.sound.sampled._
import java.lang.Runtime
import java.nio.file.Paths
import java.util.concurrent._
import org.apache.commons.io._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.io.StdIn
import scala.util.Failure
import scala.util.Success

/*
 *  The app's entry point.
 *
 *  Usage: [options]
 *
 *  --bytesPerCapture <value>
 *                           Size of a microphone recording in bytes (default = 80000)
 *  --bytesPerChunk <value>  Size of a fingerprinting chunk in bytes (default = 16)
 *  --bytesPerChunkStep <value>
 *                           Size of the stride between two fingerprinting chunks in bytes (default = 8)
 *  --debug <value>          Create intermediate dump files during song fingerprinting and matching (default = false)
 *  --fanout <value>         Maximal number of peaks that can be paired with any given peak (default = 3)
 *  -i, --inputDirectory <directory>
 *                           Directory containing the song files to index
 *  -f, --inputFormat <format>
 *                           Format of the song files to index
 *  --peakDeltaF <value>     Frequency range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 1)
 *  --peakDeltaT <value>     Time range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 1)
 *  --peaksPerChunk <value>  Maximal number of peaks in any given fingerprinting chunk (default = 2)
 *  --sampleRate <value>     Fingerprinting / recording sample rate (default = 8000)
 *  --windowDeltaF <value>   Frequency range in which neighbouring peaks can be paired up (default = 1)
 *  --windowDeltaT <value>   Time range in which neighbouring peaks can be paired up (default = 4)
 *  --windowDeltaTi <value>  Minimal time difference for neighbouring peaks to be paired up (default = 2)
 */
object FindSong extends App {
    Settings.settings(args) match {
        case Some(settings) =>
            findSong(settings)
        case None =>
    }

    /**
     *  Indexes the songs in the input directory, and then waits on user input to record and match a clip.
     *
     *  @param settings a Settings object containing the options for the app
     */
    private def findSong(implicit settings: Settings): Unit = {
        implicit val executionContext = ExecutionContext.fromExecutor(
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors))

        Indexer.indexSongs
            .andThen {
                case Success(songIndex) =>
                    println("Songs indexed")
                    recordLoop(songIndex)
                case Failure(exception) =>
                    println("Failed to index songs: ")
                    exception.printStackTrace()
            }

    }

    /**
     *  Records and matches a clip through the built-in microphone everytime <ENTER> is pressed.
     *
     *  @param songIndex the index containing the song fingerprints
     *  @param settings a Settings object containing the options for the app
     */
    private def recordLoop(songIndex: SongIndex)(implicit settings: Settings): Unit = {
        val startRecordingMessage = "Press <Enter> to start recording"
        println(startRecordingMessage)

        Iterator
            .continually(StdIn.readLine())
            .foreach((_) => {
                println("Recording...")
                val signal = Microphone.extractMicrophoneSignal
                println("Recording complete")
                val matches = Matcher.signalToMatches(signal, songIndex)
                if (matches.size > 0) {
                    val matchesTable = matches.foldLeft("")((table, songMatch) =>
                            table + (songMatch.confidence * 100)
                                + "% - " + songMatch.song.title
                                + " - " + songMatch.song.artist + "\n")
                    println("Found the following matching songs:\n" + matchesTable)
                } else {
                    println("No matching song found")
                }
                println(startRecordingMessage)
            })
    }
}
