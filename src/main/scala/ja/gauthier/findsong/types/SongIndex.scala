package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.song._

/**
  *  Contains the class / type definitions for song indexes.
  */
package object songIndex {
  case class SongIndexKey(f1: Int, f2: Int, deltaT: Int)

  case class SongIndexValue(t1: Int, song: Song)

  implicit class DebuggableSongIndex(songIndex: SongIndex)(
      implicit settings: Settings)
      extends Debuggable {

    /**
      *  Dumps the contents of the song index to a file.
      *
      *  @param filename the name of the file where the song index will be dumped into
      */
    def toFile(filename: String)(implicit settings: Settings): Unit = {
      if (settings.General.debug) {
        val fileContent = songIndex
          .foldLeft(new StringBuilder("f1 t1 f2 t2 song\n"))(
            (sb: StringBuilder,
             songIndexKeyValue: (SongIndexKey, Seq[SongIndexValue])) =>
              songIndexKeyValue._2
                .foldLeft(sb)(
                  (sb, songIndexValue) =>
                    sb.append(songIndexKeyValue._1.f1)
                      .append(" ")
                      .append(songIndexValue.t1)
                      .append(" ")
                      .append(songIndexKeyValue._1.f2)
                      .append(" ")
                      .append(songIndexKeyValue._1.deltaT + songIndexValue.t1)
                      .append(" ")
                      .append(songIndexValue.song.title)
                      .append("\n")))
          .toString
        super.writeStringToFile(filename, fileContent)
      }
    }
  }

  /**
    *  A song index maps keys (frequency of both peaks and time difference) to a sequence of values (time of the first peak and song metadata).
    */
  type SongIndex = Map[SongIndexKey, Seq[SongIndexValue]]

  /**
    *  Creates a song index.
    *
    *  @return an empty song index
    */
  val SongIndex = () => Map[SongIndexKey, Seq[SongIndexValue]]()
}
