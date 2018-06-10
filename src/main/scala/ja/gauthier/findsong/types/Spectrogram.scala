package ja.gauthier.findsong.types

import breeze.linalg._
import ja.gauthier.findsong.types.debuggable._

/**
  *  Contains the class / type definitions for spectrograms.
  */
package object spectrogram {
  implicit class DebuggableSpectrogram(spectrogram: Spectrogram)(
      implicit settings: Settings)
      extends Debuggable {

    /**
      *  Dumps the contents of the spectrogram to a file.
      *
      *  @param filename the name of the file where the spectrogram will be dumped into
      */
    def toFile(filename: String): Unit = {
      if (settings.General.debug) {
        val fileContent = spectrogram.iterator.toSeq
          .foldLeft(new StringBuilder())(
            (sb: StringBuilder, rowColumnToValue: ((Int, Int), Int)) =>
              if (rowColumnToValue._1._2 == spectrogram.cols - 1) {
                sb.append(rowColumnToValue._2).append("\n")
              } else {
                sb.append(rowColumnToValue._2).append(" ")
            })
          .toString
        super.writeStringToFile(filename, fileContent)
      }
    }
  }

  /**
    *  A spectrogram is an array of frequency spectra.
    */
  type Spectrogram = DenseMatrix[Int]
}
