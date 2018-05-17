package ja.gauthier.findsong

import ja.gauthier.findsong.types.Settings
import ja.gauthier.findsong.types.signal._
import java.io.File
import java.nio._
import javax.sound.sampled._

object Microphone {
    def extractMicrophoneSignal(nBytes: Int)(implicit settings: Settings): Signal = {
        val format = AudioFile.getAudioFormat
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
