package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry.Point
import scala.math._
import scala.collection.immutable.HashMap

object Types {
    type ConstellationMap = RTree[Peak, Point]

    case class Match(song: Song, confidence: Double) extends Ordered[Match] {
        def compare(that: Match): Int =
            Ordering.Tuple2[Double, String]
                .compare(
                    (-this.confidence, this.song.title),
                    (-that.confidence, that.song.title)
                )
    }

    type Matches = Seq[Match]

    case class Peak(amplitude: Int, frequency: Int, time: Int) extends Ordered[Peak] {
        def compare(that: Peak): Int =
            Ordering.Tuple3[Int, Int, Int]
                .compare(
                    (-this.amplitude, this.time, this.frequency),
                    (-that.amplitude, that.time, that.frequency)
                )
    }

    type PeakPair = (Peak, Peak);

    type PeakPairs = Seq[PeakPair]

    type Signal = Array[Byte]

    case class Song(
        album: String,
        artist: String,
        disc: String,
        genre: String,
        title: String,
        track: String
    )

    type SongHash = Int

    type Spectrogram = DenseMatrix[Int]

    type SongConfidence = Map[Song, Double]
    val SongConfidence = () => Map[Song, Double]()

    type SongOffsets = Map[Song, Seq[Int]]
    val SongOffsets = () => Map[Song, Seq[Int]]()

    type SongIndex = Map[SongIndexKey, Seq[SongIndexValue]]
    val SongIndex = () => Map[SongIndexKey, Seq[SongIndexValue]]()

    case class SongIndexKey(f1: Int, f2: Int, diffT: Int)

    case class SongIndexValue(t1: Int, song: Song)
}
