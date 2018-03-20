package ja.gauthier.findsong

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object Settings {
    val settings = new Settings(ConfigFactory.load())
}

class Settings(config: Config) {
    config.checkValid(ConfigFactory.defaultReference(), "findsong")
    val findsong = config.getConfig("findsong")

    object ConstellationMap {
        val constellationMap = findsong.getConfig("constellation-map")
        val fanout = constellationMap.getInt("fanout")
        val peakDeltaF = constellationMap.getInt("peak-delta-f")
        val peakDeltaT = constellationMap.getInt("peak-delta-t")
        val peaksPerChunk = constellationMap.getInt("peak-per-chunk")
        val windowDeltaF = constellationMap.getInt("window-delta-f")
        val windowDeltaT = constellationMap.getInt("window-delta-t")
        val windowDeltaTi = constellationMap.getInt("window-delta-ti")
    }

    object Preprocessing {
        val preprocessing = findsong.getConfig("preprocessing")
        val bigEndian = preprocessing.getBoolean("big-endian")
        val bitsPerSample = preprocessing.getInt("bits-per-sample")
        val bytesPerCapture = preprocessing.getInt("bytes-per-capture")
        val channels = preprocessing.getInt("channels")
        val codec = preprocessing.getString("codec")
        val inputFormat = preprocessing.getString("input-format")
        val intermediateFormat = preprocessing.getString("intermediate-format")
        val sampleRate = preprocessing.getInt("sample-rate")
        val signed = preprocessing.getBoolean("signed")
    }

    object FFmpeg {
        val ffmpeg = findsong.getConfig("ffmpeg")
        val ffmpegPath = ffmpeg.getString("ffmpeg-path")
        val ffprobePath = ffmpeg.getString("ffprobe-path")
    }

    object Spectrogram {
        val spectrogram = findsong.getConfig("spectrogram")
        val bytesPerChunk = spectrogram.getInt("bytes-per-chunk")
        val bytesPerChunkStep = spectrogram.getInt("bytes-per-chunk-step")
    }
}
