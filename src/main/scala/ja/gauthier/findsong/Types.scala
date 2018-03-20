package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry.Point

object Types {
    type ConstellationMap = RTree[Peak, Point]
    type Signal = Array[Byte]
    type SongHash = Int
    type Spectrogram = DenseMatrix[Int]

    case class Peak(amplitude: Double, frequency: Int, time: Int)
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
