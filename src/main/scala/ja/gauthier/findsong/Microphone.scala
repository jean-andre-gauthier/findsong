package ja.gauthier.findsong

import javax.sound.sampled._
import resource.managed
import Types._

object Microphone extends AudioUtils {
    def extractMicrophoneSignal(nBytes: Int): Signal = {
        val format = getAudioFormat()
        val info = new DataLine.Info(classOf[TargetDataLine], format)
        val signal = new Signal(settings.Preprocessing.bytesPerCapture)
        for (microphone <- managed(AudioSystem.getLine(info).asInstanceOf[TargetDataLine])) {
            microphone.open(format)
            microphone.start()
            microphone.read(signal, 0, settings.Preprocessing.bytesPerCapture)
        }
        signal
    }
}
