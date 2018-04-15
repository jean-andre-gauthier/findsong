package ja.gauthier.findsong

import breeze.linalg._
import breeze.linalg.operators._
import breeze.linalg.support._
import breeze.util._
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
import Types._

object FindSong extends App {
    val settings = Settings.settings
    val inputDirectory = args(0)

    implicit val executionContext = ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors
        ))

    Indexer.indexSongs(inputDirectory)
        .andThen {
            case Success(songIndex) =>
                println("Songs indexed")
                recordLoop(songIndex)
            case Failure(exception) =>
                println("Failed to index songs: ")
                exception.printStackTrace()
        }

        def recordLoop(songIndex: SongIndex): Unit = {
            val startRecordingMessage = "Press <Enter> to start recording"
            println(startRecordingMessage)

            Iterator
                .continually(StdIn.readChar())
                .zipWithIndex
                .foreach((_: (Char, Int)) => {
                    println("Recording...")
                    val signal = Microphone.extractMicrophoneSignal(settings.Recording.bytesPerCapture)
                    println("Recording complete")
                    val matches = Matcher.signalToMatches(signal, songIndex)
                    val matchesTable = matches.foldLeft("")((table, songMatch) =>
                            table + (songMatch.confidence * 100)
                                + "% - " + songMatch.song.title
                                + " - " + songMatch.song.artist)
                    println(matchesTable)
                    println(startRecordingMessage)
                })
    }
}
