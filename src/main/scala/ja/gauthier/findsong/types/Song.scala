package ja.gauthier.findsong.types

/**
  *  Contains the class / type definitions for songs.
  */
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
