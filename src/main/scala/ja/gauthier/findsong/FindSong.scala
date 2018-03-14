package ja.gauthier.findsong

import com.typesafe.config.ConfigFactory
import java.io.File
import java.io.FileInputStream
import javax.sound.sampled.AudioSystem
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.FFmpegExecutor
import org.apache.commons.io.IOUtils

object FindSong extends App {
    val settings = new Settings(ConfigFactory.load())

    val in = args(0)
    val out = args(1)
    val outFile = new File(out)
    val ffmpeg = new FFmpeg(settings.ffmpegPath)
    val ffprobe = new FFprobe(settings.ffprobePath)
    val builder = new FFmpegBuilder()
        .setInput(in)
        .addOutput(out)
        .setAudioChannels(1)
        .setAudioCodec(settings.audioCodec)
        .setAudioSampleRate(settings.audioSampleRate)
        .disableSubtitle()
        .disableVideo()
        .done()
    val executor = new FFmpegExecutor(ffmpeg, ffprobe)
    executor.createJob(builder).run()
    val timeDomainSignal = IOUtils.toByteArray(AudioSystem.getAudioInputStream(outFile))
    println(timeDomainSignal(0))
}
