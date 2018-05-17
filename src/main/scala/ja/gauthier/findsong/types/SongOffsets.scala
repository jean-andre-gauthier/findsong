package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.song._

package object songOffsets {
    implicit class DebuggableSongOffsets(songOffsets: SongOffsets)(implicit settings: Settings) extends Debuggable {
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

    type SongOffsets = Map[Song, Seq[Int]]

    val SongOffsets = () => Map[Song, Seq[Int]]()
}
