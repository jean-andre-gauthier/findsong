package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.matchPackage._

/**
 *  Contains the class / type definitions for song match lists.
 */
package object matches {
    implicit class DebuggableMatches(matches: Matches)(implicit settings: Settings) extends Debuggable {
        /**
         *  Dumps the contents of the matches list to a file.
         *
         *  @param filename the name of the file where the matches list will be dumped into
         */
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

    /**
     *  Matches are a sequence of matches
     */
    type Matches = Seq[Match]
}
