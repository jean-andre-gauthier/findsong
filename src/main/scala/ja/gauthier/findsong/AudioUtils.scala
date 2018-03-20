package ja.gauthier.findsong

import javax.sound.sampled._

trait AudioUtils {
    val settings = Settings.settings

    def getAudioFormat(): AudioFormat = {
        new AudioFormat(
            settings.Preprocessing.sampleRate,
            settings.Preprocessing.bitsPerSample,
            settings.Preprocessing.channels,
            settings.Preprocessing.signed,
            settings.Preprocessing.bigEndian
        )
    }
}
