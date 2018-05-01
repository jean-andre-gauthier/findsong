package ja.gauthier.findsong.types

package object song {
    case class Song(
        album: String,
        artist: String,
        disc: String,
        genre: String,
        title: String,
        track: String
    )
}
