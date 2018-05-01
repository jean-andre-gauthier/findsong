package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.settings._
import ja.gauthier.findsong.types.song._

package object songConfidence {
    implicit class DebuggableSongConfidence(songConfidence: SongConfidence) extends Debuggable {
        val settings = Settings.settings

        def toFile(filename: String): Unit = {
            if (settings.General.debug) {
                val fileContent = songConfidence
                    .foldLeft(new StringBuilder("song confidence\n"))(
                        (sb: StringBuilder, songSongConfidence: (Song, Double)) => sb
                            .append(songSongConfidence._1.title)
                            .append(" ")
                            .append(songSongConfidence._2)
                            .append("\n"))
                    .toString
                super.writeStringToFile(filename, fileContent)
            }
        }
    }

    type SongConfidence = Map[Song, Double]

    val SongConfidence = () => Map[Song, Double]()
}
