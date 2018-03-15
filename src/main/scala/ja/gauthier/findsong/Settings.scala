package ja.gauthier.findsong

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class Settings(config: Config) {
    config.checkValid(ConfigFactory.defaultReference(), "findsong")
    val findsong = config.getConfig("findsong")

    val audio = findsong.getConfig("audio")
    val audioBigEndian = audio.getBoolean("big-endian")
    val audioBitsPerSample = audio.getInt("bits-per-sample")
    val audioBytesPerCapture = audio.getInt("bytes-per-capture")
    val audioChannels = audio.getInt("channels")
    val audioCodec = audio.getString("codec")
    val audioInputDirectory = audio.getString("input-directory")
    val audioInputFormat = audio.getString("input-format")
    val audioIntermediateFormat = audio.getString("intermediate-format")
    val audioSampleRate = audio.getInt("sample-rate")
    val audioSigned = audio.getBoolean("signed")

    val ffmpegPath = findsong.getString("ffmpeg-path")
    val ffprobePath = findsong.getString("ffprobe-path")
}
