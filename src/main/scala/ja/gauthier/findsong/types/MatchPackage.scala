package ja.gauthier.findsong.types

import ja.gauthier.findsong.types.song._

package object matchPackage {
    case class Match(song: Song, confidence: Double) extends Ordered[Match] {
        def compare(that: Match): Int =
            Ordering.Tuple2[Double, String]
                .compare(
                    (-this.confidence, this.song.title),
                    (-that.confidence, that.song.title)
                )
    }
}
