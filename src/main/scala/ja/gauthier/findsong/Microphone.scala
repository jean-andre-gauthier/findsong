package ja.gauthier.findsong

import ja.gauthier.findsong.types.settings._
import ja.gauthier.findsong.types.signal._
import java.io.File
import java.nio._
import javax.sound.sampled._

object Microphone {
    val settings = Settings.settings

    def extractMicrophoneSignal(nBytes: Int): Signal = {
        val format = AudioFile.getAudioFormat()
        val info = new DataLine.Info(classOf[TargetDataLine], format)
        val microphone = AudioSystem.getLine(info).asInstanceOf[TargetDataLine]
        try {
            val signalBytes = new Array[Byte](settings.Recording.bytesPerCapture)
            microphone.open(format)
            microphone.start()
            microphone.read(signalBytes, 0, settings.Recording.bytesPerCapture)
            val signalShorts = AudioFile.byteArrayToShortArray(signalBytes, Some(ByteOrder.BIG_ENDIAN))
            signalShorts
        } finally {
            microphone.close()
        }
    }
}
