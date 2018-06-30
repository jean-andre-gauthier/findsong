package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.song._

/**
  *  Contains the class / type definitions for song confidences.
  */
package object songConfidence {
  implicit class DebuggableSongConfidence(songConfidence: SongConfidence)(
      implicit settings: Settings)
      extends Debuggable {

    /**
      *  Dumps the contents of the song score map to a file.
      *
      *  @param filename the name of the file where the song scores will be dumped into
      */
    def toFile(filename: String)(implicit settings: Settings): Unit = {
      if (settings.General.debug) {
        val fileContent = songConfidence
          .foldLeft(new StringBuilder("song confidence\n"))(
            (sb: StringBuilder, songSongConfidence: (Song, Double)) =>
              sb.append(songSongConfidence._1.title)
                .append(" ")
                .append(songSongConfidence._2)
                .append("\n"))
          .toString
        super.writeStringToFile(filename, fileContent)
      }
    }
  }

  /**
    *  A song confidence is a map of songs to a numerical match confidence score
    */
  type SongConfidence = Map[Song, Double]

  /**
    *  Creates a new song confidence map.
    *
    *  @return an empty song confidence map
    */
  val SongConfidence = () => Map[Song, Double]()
}
