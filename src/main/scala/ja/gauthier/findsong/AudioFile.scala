package ja.gauthier.findsong

import ja.gauthier.findsong.types.settings._
import ja.gauthier.findsong.types.signal._
import ja.gauthier.findsong.types.song._
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.nio._
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import javax.sound.sampled._
import net.bramp.ffmpeg._
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.apache.commons.io._
import scala.io.Source

object AudioFile {
    val settings = Settings.settings

    def byteArrayToShortArray(bytes: Array[Byte], byteOrder: Option[ByteOrder]): Array[Short] = {
        bytes.map((byte: Byte) => (byte & 0xFF).toShort)
    }

    def extractFileSignal(file: String): Signal = {
        val pathOut = getReplacedExtension(file, settings.Preprocessing.intermediateFormat)
        val fileOut = new File(pathOut)
        val ffmpeg = new FFmpeg()
        val ffprobe = new FFprobe()
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
        CompletableFuture.runAsync(ffmpegExecutor.createJob(ffmpegBuilder)).join()
        val signalBytes = IOUtils
            .toByteArray(AudioSystem.getAudioInputStream(fileOut))
        val signalShorts = byteArrayToShortArray(signalBytes, Some(ByteOrder.LITTLE_ENDIAN))
        signalShorts
    }

    def extractSongMetadata(file: String): Song = {
        val pathOut = getReplacedExtension(file, "txt")
        val fileOut = new File(pathOut)
        val ffmpeg = new FFmpeg()
        val ffprobe = new FFprobe()
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

    def getAudioFormat(): AudioFormat = {
        new AudioFormat(
            settings.Preprocessing.sampleRate,
            settings.Preprocessing.bitsPerSample,
            settings.Preprocessing.channels,
            settings.Preprocessing.signed,
            settings.Preprocessing.bigEndian
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
