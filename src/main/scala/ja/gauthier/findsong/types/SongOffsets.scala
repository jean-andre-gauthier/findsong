package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.song._

/**
 *  Contains the class / type definitions for song offsets.
 */
package object songOffsets {
    implicit class DebuggableSongOffsets(songOffsets: SongOffsets)(implicit settings: Settings) extends Debuggable {
        /**
         *  Dumps the contents of the song offset map to a file.
         *
         *  @param filename the name of the file where the song offset map will be dumped into
         */
        def toFile(filename: String): Unit = {
            if (settings.General.debug) {
                val fileContent = songOffsets
                    .foldLeft(new StringBuilder("song offset\n"))(
                        (sb: StringBuilder, songSongOffsets: (Song, Seq[Int])) => songSongOffsets._2
                            .foldLeft(sb)
                                ((sb, offset) => sb
                                    .append(songSongOffsets._1.title)
                                    .append(" ")
                                    .append(offset)
                                    .append("\n")))
                    .toString
                super.writeStringToFile(filename, fileContent)
            }
        }
    }

    /**
     *  Song offsets are a map of songs to their respective offsets
     */
    type SongOffsets = Map[Song, Seq[Int]]

    /**
     * Creates a new song offsets map.
     *
     * @return an empty song offsets map
     */
    val SongOffsets = () => Map[Song, Seq[Int]]()
}
