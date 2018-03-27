package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry.Point
import scala.math._

object Types {
    type ConstellationMap = RTree[Peak, Point]
    type Signal = Array[Byte]
    type SongHash = Int
    type Spectrogram = DenseMatrix[Int]

    case class Peak(amplitude: Int, frequency: Int, time: Int) extends Ordered[Peak] {
        def compare(that: Peak): Int =
            Ordering.Tuple3[Int, Int, Int]
                .compare(
                    (-this.amplitude, this.time, this.frequency),
                    (-that.amplitude, that.time, that.frequency)
                )
    }

    type PeakPairs = Seq[(Peak, Peak)]

    case class Song(
        album: String,
        artist: String,
        disc: String,
        genre: String,
        title: String,
        track: String
    )
    case class SongIndexKey(f1: Int, f2: Int, diffT: Int)
    type SongIndex = Map[SongIndexKey, Song]
}
