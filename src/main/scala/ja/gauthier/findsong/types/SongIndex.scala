package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.song._

package object songIndex {
    case class SongIndexKey(f1: Int, f2: Int, deltaT: Int)

    case class SongIndexValue(t1: Int, song: Song)

    implicit class DebuggableSongIndex(songIndex: SongIndex)(implicit settings: Settings) extends Debuggable {
        def toFile(filename: String): Unit = {
            if (settings.General.debug) {
                val fileContent = songIndex
                    .foldLeft(new StringBuilder("f1 t1 f2 t2 song\n"))(
                        (sb: StringBuilder, songIndexKeyValue: (SongIndexKey, Seq[SongIndexValue])) =>
                            songIndexKeyValue._2
                            .foldLeft(sb)
                                ((sb, songIndexValue) => sb
                                    .append(songIndexKeyValue._1.f1)
                                    .append(" ")
                                    .append(songIndexValue.t1)
                                    .append(" ")
                                    .append(songIndexKeyValue._1.f2)
                                    .append(" ")
                                    .append(songIndexKeyValue._1.deltaT - songIndexValue.t1)
                                    .append(" ")
                                    .append(songIndexValue.song.title)
                                    .append("\n")))
                    .toString
                super.writeStringToFile(filename, fileContent)
            }
        }
    }

    type SongIndex = Map[SongIndexKey, Seq[SongIndexValue]]

    val SongIndex = () => Map[SongIndexKey, Seq[SongIndexValue]]()
}

