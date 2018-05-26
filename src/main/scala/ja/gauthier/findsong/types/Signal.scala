package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._

/**
 *  Contains the class / type definitions for signals.
 */
package object signal {
    implicit class DebuggableSignal(signal: Signal)(implicit settings: Settings) extends Debuggable {
        /**
         *  Dumps the contents of the signal to a file.
         *
         *  @param filename the name of the file where the signal will be dumped into
         */
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

    /**
     *  A signal is an array of shorts
     */
    type Signal = Array[Short]
}
