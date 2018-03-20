package ja.gauthier.findsong

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import javax.sound.sampled._
import net.bramp.ffmpeg._
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.apache.commons.io._
import scala.io.Source
import Types._

object AudioFile extends AudioUtils {
    def exportSignal(data: Signal, name: String): Unit = {
        val outFile = new File(name)
        val format = getAudioFormat()
        AudioSystem.write(
            new AudioInputStream(new ByteArrayInputStream(data), format, data.length),
            AudioFileFormat.Type.WAVE,
            outFile
        )
    }

    def extractFileSignal(file: String): Signal = {
        val pathOut = getReplacedExtension(file, settings.Preprocessing.intermediateFormat)
        val fileOut = new File(pathOut)
        val ffmpeg = new FFmpeg(settings.FFmpeg.ffmpegPath)
        val ffprobe = new FFprobe(settings.FFmpeg.ffprobePath)
        val ffmpegBuilder = new FFmpegBuilder()
            .setInput(file)
            .setAudioFilter("dynaudnorm=c")
            .overrideOutputFiles(true)
            .addOutput(pathOut)
            .setAudioChannels(settings.Preprocessing.channels)
            .setAudioCodec(settings.Preprocessing.codec)
            .setAudioSampleRate(settings.Preprocessing.sampleRate)
            .disableSubtitle()
            .disableVideo()
            .done()
        val ffmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe)
        ffmpegExecutor.createJob(ffmpegBuilder).run()
        IOUtils.toByteArray(AudioSystem.getAudioInputStream(fileOut))
    }

    def extractSongMetadata(file: String): Song = {
        val pathOut = getReplacedExtension(file, "txt")
        val fileOut = new File(pathOut)
        val ffmpeg = new FFmpeg(settings.FFmpeg.ffmpegPath)
        val ffprobe = new FFprobe(settings.FFmpeg.ffprobePath)
        val ffmpegBuilder = new FFmpegBuilder()
            .setInput(file)
            .overrideOutputFiles(true)
            .addOutput(pathOut)
            .setFormat("ffmetadata")
            .done()
        val ffmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe)
        ffmpegExecutor.createJob(ffmpegBuilder).run()
        val metadataSource = Source.fromFile(pathOut)
        val metadata = metadataSource
            .getLines()
            .foldLeft(Map.empty[String, String])((map, line) => {
                line.split("=") match {
                    case Array(key, value, _*) => map + (key -> value)
                    case _ => map
                }
            })
        Song(
            metadata("album"),
            metadata("artist"),
            metadata("disc"),
            metadata("genre"),
            metadata("title"),
            metadata("track")
        )
    }

    def getReplacedExtension(file: String, extension: String): String = {
        val fileNameIn = FilenameUtils.getBaseName(file)
        val pathIn = Paths.get(file)
        val parentPath = pathIn.getParent()
        val pathOut = parentPath.resolve(
            fileNameIn
            + "."
            + extension)
        pathOut.toString()
    }
}
