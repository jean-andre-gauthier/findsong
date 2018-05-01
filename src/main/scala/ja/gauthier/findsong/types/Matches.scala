package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.matchPackage._
import ja.gauthier.findsong.types.settings._

package object matches {
    implicit class DebuggableMatches(matches: Matches) extends Debuggable {
        val settings = Settings.settings

        def toFile(filename: String): Unit = {
            if (settings.General.debug) {
                val fileContent = matches
                    .foldLeft(new StringBuilder("song confidence\n"))(
                        (sb: StringBuilder, songMatch: Match) => sb
                            .append(songMatch.song.title)
                            .append(" ")
                            .append(songMatch.confidence)
                            .append("\n"))
                    .toString
                super.writeStringToFile(filename, fileContent)
            }
        }
    }

    type Matches = Seq[Match]
}
