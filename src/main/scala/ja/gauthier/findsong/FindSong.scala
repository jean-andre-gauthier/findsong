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

object FindSong extends App {
    Settings.settings(args) match {
        case Some(settings) =>
            findSong(settings)
        case None =>
    }

    def findSong(implicit settings: Settings): Unit = {
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

    def recordLoop(songIndex: SongIndex)(implicit settings: Settings): Unit = {
        val startRecordingMessage = "Press <Enter> to start recording"
        println(startRecordingMessage)

        Iterator
            .continually(StdIn.readLine())
            .foreach((_) => {
                println("Recording...")
                val signal = Microphone.extractMicrophoneSignal(settings.Recording.bytesPerCapture)
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
