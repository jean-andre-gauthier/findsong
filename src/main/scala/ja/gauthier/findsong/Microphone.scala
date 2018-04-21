package ja.gauthier.findsong

import ja.gauthier.findsong.types.settings._
import ja.gauthier.findsong.types.signal._
import java.io.File
import javax.sound.sampled._

object Microphone {
    val settings = Settings.settings

    def extractMicrophoneSignal(nBytes: Int): Signal = {
        val format = AudioFile.getAudioFormat()
        val info = new DataLine.Info(classOf[TargetDataLine], format)
        val signal = new Signal(settings.Recording.bytesPerCapture)
        val microphone = AudioSystem.getLine(info).asInstanceOf[TargetDataLine]
        try {
            microphone.open(format)
            microphone.start()
            microphone.read(signal, 0, settings.Recording.bytesPerCapture)
        } finally {
            microphone.close()
        }
        val tempFile = File.createTempFile("findsong", null)
        AudioFile.exportSignal(signal, tempFile.getAbsolutePath())
        val signalWithoutDcBias = AudioFile.extractFileSignal(tempFile.getAbsolutePath())
        signalWithoutDcBias
    }
}
