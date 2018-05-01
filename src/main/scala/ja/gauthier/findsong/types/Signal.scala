package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.settings._

object signal {
    implicit class DebuggableSignal(signal: Signal) extends Debuggable {
        val settings = Settings.settings

        def toFile(filename: String): Unit = {
            if (settings.General.debug) {
                val fileContent = signal
                    .foldLeft(new StringBuilder(""))(
                        (sb: StringBuilder, amplitude: Short) => sb
                            .append(amplitude)
                            .append("\n"))
                    .toString
                super.writeStringToFile(filename, fileContent)
            }
        }
    }

    type Signal = Array[Short]
}
