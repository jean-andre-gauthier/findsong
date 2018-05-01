package ja.gauthier.findsong.types

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

package object settings {
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
            val peaksPerChunk = constellationMap.getInt("peaks-per-chunk")
            val windowDeltaF = constellationMap.getInt("window-delta-f")
            val windowDeltaT = constellationMap.getInt("window-delta-t")
            val windowDeltaTi = constellationMap.getInt("window-delta-ti")
        }

        object General {
            val general = findsong.getConfig("general")
            val debug = general.getBoolean("debug")
        }

        object Preprocessing {
            val preprocessing = findsong.getConfig("preprocessing")
            val bigEndian = false
            val bitsPerSample = 8
            val channels = 1
            val codec = "pcm_u8"
            val inputFormat = "m4a"
            val intermediateFormat = "wav"
            val sampleRate = preprocessing.getInt("sample-rate")
            val signed = true
        }

        object Recording {
            val recording = findsong.getConfig("recording")
            val bytesPerCapture = recording.getInt("bytes-per-capture")
        }

        object Spectrogram {
            val spectrogram = findsong.getConfig("spectrogram")
            val bytesPerChunk = spectrogram.getInt("bytes-per-chunk")
            val bytesPerChunkStep = spectrogram.getInt("bytes-per-chunk-step")
        }
    }
}
