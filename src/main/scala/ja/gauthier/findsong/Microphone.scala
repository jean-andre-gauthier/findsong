package ja.gauthier.findsong

import ja.gauthier.findsong.types.Settings
import ja.gauthier.findsong.types.signal._
import java.io.File
import java.nio._
import javax.sound.sampled._

/**
 * This object accesses the built-in microphone.
 */
object Microphone {
    /**
     * Uses the java sound API to make an audio recording with the built-in microphone. The method blocks until settings.Recording.samplesPerCapture are read.
     *
     *  @param settings a Settings object containing the options for the app
     *  @return the recorded signal
     */
    def extractMicrophoneSignal(implicit settings: Settings): Signal = {
        val format = AudioFile.getAudioFormat
        val info = new DataLine.Info(classOf[TargetDataLine], format)
        val microphone = AudioSystem.getLine(info).asInstanceOf[TargetDataLine]
        try {
            val signalBytes = new Array[Byte](settings.Recording.samplesPerCapture * 2)
            microphone.open(format)
            microphone.start()
            microphone.read(signalBytes, 0, settings.Recording.samplesPerCapture * 2)
            val signalShorts = AudioFile.byteArrayToShortArray(signalBytes)
            signalShorts
        } finally {
            microphone.close()
        }
    }
}
