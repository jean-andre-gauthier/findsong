package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.peak._
import ja.gauthier.findsong.types.peakPair._

package object peakPairs {
    implicit class DebuggablePeakPairs(peakPairs: PeakPairs)(implicit settings: Settings) extends Debuggable {
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

    type PeakPairs = Seq[(Peak, Peak)]
}
