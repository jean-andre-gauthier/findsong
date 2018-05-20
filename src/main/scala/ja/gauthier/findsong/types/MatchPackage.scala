package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.song._

/**
 *  Contains the class / type definitions for song matches.
 */
package object matchPackage {
    case class Match(song: Song, confidence: Double) extends Ordered[Match] {
        /**
         * Compares two matches, according to a descending order for the confidence and an ascending order for the song title.
         *
         * @param that the other match
         * @return the result of the comparison
         **/
        def compare(that: Match): Int =
            Ordering.Tuple2[Double, String]
                .compare(
                    (-this.confidence, this.song.title),
                    (-that.confidence, that.song.title)
                )
    }
}
