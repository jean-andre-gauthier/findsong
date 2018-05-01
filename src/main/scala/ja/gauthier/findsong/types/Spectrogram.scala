package ja.gauthier.findsong.types

import breeze.linalg._
import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.settings._

package object spectrogram {
    implicit class DebuggableSpectrogram(spectrogram: Spectrogram) extends Debuggable {
        val settings = Settings.settings

        def toFile(filename: String): Unit = {
            if (settings.General.debug) {
                val fileContent = spectrogram
                    .iterator
                    .toSeq
                    .foldLeft(new StringBuilder())(
                        (sb: StringBuilder, rowColumnToValue: ((Int, Int), Int)) =>
                            if (rowColumnToValue._1._2 == spectrogram.cols-1) {
                                sb.append(rowColumnToValue._2).append("\n")
                            } else {
                                sb.append(rowColumnToValue._2).append(" ")
                            })
                    .toString
                super.writeStringToFile(filename, fileContent)
            }
        }
    }

    type Spectrogram = DenseMatrix[Int]
}
