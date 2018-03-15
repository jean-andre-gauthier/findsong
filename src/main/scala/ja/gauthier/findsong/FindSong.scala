package ja.gauthier.findsong

import com.typesafe.config.ConfigFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.Runtime
import java.nio.file.Paths
import java.util.concurrent.Executors
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.FFmpegExecutor
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import resource.managed
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.blocking
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Failure
import scala.util.Success

object FindSong extends App {
    implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors))
    val settings = new Settings(ConfigFactory.load())

    indexSongs(settings.audioInputDirectory)
        .andThen {
            case Success(songs) =>
                println("Songs indexed")
                println(songs(0)(0))
                println("Press any key to start recording")

                Iterator
                    .continually(StdIn.readChar())
                    .zipWithIndex
                    .takeWhile(_._1 != 0x1B)
                    .foreach((charIndex: (Char, Int)) => {
                        println("Recording...")
                        val index = charIndex._2
                        val microphoneSignalData = extractMicrophoneSignal(settings.audioBytesPerCapture)
                        val outFile = Paths.get(settings.audioInputDirectory, s"microphone$index.${settings.audioIntermediateFormat}").toString
                        exportSignal(microphoneSignalData, outFile)
                        println("Recording complete")
                        println(microphoneSignalData.length)
                        println("Press any key to start recording")
                    })
            case Failure(exception) =>
                println("Failed to index songs: ")
                exception.printStackTrace()
        }

    def exportSignal(data: Array[Byte], name: String): Unit = {
        val outFile = new File(name)
        val format = new AudioFormat(settings.audioSampleRate, settings.audioBitsPerSample, settings.audioChannels, settings.audioSigned, settings.audioBigEndian)
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(data), format, data.length), AudioFileFormat.Type.WAVE, outFile)
    }

    def extractFileSignal(file: String): Array[Byte] = {
        val pathOut = getReplacedExtension(file, settings.audioIntermediateFormat)
        val fileOut = new File(pathOut)
        val ffmpeg = new FFmpeg(settings.ffmpegPath)
        val ffprobe = new FFprobe(settings.ffprobePath)
        val ffmpegBuilder = new FFmpegBuilder()
            .setInput(file)
            .setAudioFilter("dynaudnorm=c")
            .overrideOutputFiles(false)
            .addOutput(pathOut)
            .setAudioChannels(settings.audioChannels)
            .setAudioCodec(settings.audioCodec)
            .setAudioSampleRate(settings.audioSampleRate)
            .disableSubtitle()
            .disableVideo()
            .done()
        val ffmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe)
        ffmpegExecutor.createJob(ffmpegBuilder).run()
        IOUtils.toByteArray(AudioSystem.getAudioInputStream(fileOut))
    }

    def extractMicrophoneSignal(nBytes: Int): Array[Byte] = {
        val format = new AudioFormat(settings.audioSampleRate, settings.audioBitsPerSample, settings.audioChannels, settings.audioSigned, settings.audioBigEndian)
        val info = new DataLine.Info(classOf[TargetDataLine], format)
        val signal = new Array[Byte](settings.audioBytesPerCapture)
        for (microphone <- managed(AudioSystem.getLine(info).asInstanceOf[TargetDataLine])) {
            microphone.open(format)
            microphone.start()
            microphone.read(signal, 0, settings.audioBytesPerCapture)
        }
        signal
    }

    def getReplacedExtension(file: String, extension: String): String = {
        val fileNameIn = FilenameUtils.getBaseName(file)
        val pathIn = Paths.get(file)
        val parentPath = pathIn.getParent()
        val pathOut = parentPath.resolve(fileNameIn + "." + settings.audioIntermediateFormat)
        pathOut.toString()
    }


    def indexSongs(directory: String): Future[Seq[Array[Byte]]] = {
        val directoryFile = new File(directory)
        val songFiles = FileUtils.listFiles(directoryFile, Array(settings.audioInputFormat), true).asScala.toSeq
        Future.sequence(songFiles.map((songFile: File) => Future {
                blocking {
                    extractFileSignal(songFile.getCanonicalPath())
                }
            }))
    }
}
