package ja.gauthier.findsong

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class Settings(config: Config) {
    config.checkValid(ConfigFactory.defaultReference(), "findsong")
    val findsong = config.getConfig("findsong")

    val audioCodec = findsong.getString("audioCodec")
    val audioSampleRate = findsong.getInt("audioSampleRate")
    val ffmpegPath = findsong.getString("ffmpegPath")
    val ffprobePath = findsong.getString("ffprobePath")
}
