package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.peak._
import ja.gauthier.findsong.types.peakPair._

/**
 *  Contains the class / type definitions for peak pair lists.
 */
package object peakPairs {
    implicit class DebuggablePeakPairs(peakPairs: PeakPairs)(implicit settings: Settings) extends Debuggable {
        /**
         *  Dumps the contents of the peak pair list to a file.
         *
         *  @param filename the name of the file where the peak pair list will be dumped into
         */
        def toFile(filename: String): Unit = {
            if (settings.General.debug) {
                val fileContent = peakPairs
                    .foldLeft(new StringBuilder("f1 t1 f2 t2\n"))(
                        (sb: StringBuilder, peakPair: PeakPair) => sb
                            .append(peakPair._1.frequency)
                            .append(" ")
                            .append(peakPair._1.time)
                            .append(" ")
                            .append(peakPair._2.frequency)
                            .append(" ")
                            .append(peakPair._2.time)
                            .append("\n"))
                    .toString
                super.writeStringToFile(filename, fileContent)
            }
        }
    }

    /**
     *  Peak pairs are a sequence of peak pairs.
     */
    type PeakPairs = Seq[PeakPair]
}
