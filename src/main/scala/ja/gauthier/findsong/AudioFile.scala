package ja.gauthier.findsong

import ja.gauthier.findsong.types.signal._
import ja.gauthier.findsong.types.Settings
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

/**
 *  This object contains helper methods for ffmpeg and the java sound API.
 */
object AudioFile {
    /**
     *  Transforms an array of Bytes to an array of Shorts.
     *
     *  @param bytes the Byte array
     *  @return an array of Shorts
     */
    def byteArrayToShortArray(bytes: Array[Byte]): Array[Short] = {
        val shorts = new Array[Short](bytes.length / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        shorts
    }

    /**
     *  Uses ffmpeg to retrieve the signal contained in the input file (without DC bias).
     *
     *  @param file the input file
     *  @param settings a Settings object containing the options for the app
     *  @return the signal extracted from the input file
     */
    def extractFileSignal(file: String)(implicit settings: Settings): Signal = {
        val pathOut = getReplacedExtension(file, settings.Preprocessing.intermediateFormat)
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
            .setFormat(settings.Preprocessing.intermediateFormat)
            .disableSubtitle()
            .disableVideo()
            .done()
        val ffmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe)
        CompletableFuture.runAsync(ffmpegExecutor.createJob(ffmpegBuilder)).join()
        val fileOut = new File(pathOut)
        val signalBytes = IOUtils
            .toByteArray(new FileInputStream(fileOut))
        val signalShorts = byteArrayToShortArray(signalBytes)
        signalShorts
    }

    /**
     *  Uses ffmpeg to extract the metadata from an audio file.
     *
     *  @param file the input file
     *  @param settings a Settings object containing the options for the app
     *  @return the song metadata
     */
    def extractSongMetadata(file: String)(implicit settings: Settings): Song = {
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

    /**
     *  Creates an AudioFormat instance.
     *
     *  @param settings a Settings object containing the options for the app
     *  @return a new AudioFormat instance
     */
    def getAudioFormat(implicit settings: Settings): AudioFormat = {
        new AudioFormat(
            settings.Preprocessing.sampleRate,
            settings.Preprocessing.bitsPerSample,
            settings.Preprocessing.channels,
            settings.Preprocessing.signed,
            settings.Preprocessing.bigEndian
        )
    }

    /**
     *  Replaces the extension in the input file name by the provided extension.
     *
     *  @param file the input file name
     *  @param extension the new extension
     *  @return the input file name with the new extension
     */
    private def getReplacedExtension(file: String, extension: String): String = {
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
