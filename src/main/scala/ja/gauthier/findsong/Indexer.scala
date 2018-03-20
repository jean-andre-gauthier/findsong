package ja.gauthier.findsong

import java.io.File
import javax.sound.sampled._
import org.apache.commons.io._
import scala.concurrent._
import scala.collection.JavaConverters._
import Types._

object Indexer {
    val settings = Settings.settings

    def indexSongs(directory: String)(implicit executionContext: ExecutionContext): Future[SongIndex] = {
        val directoryFile = new File(directory)
        val songFiles = FileUtils
            .listFiles(directoryFile, Array(settings.Preprocessing.inputFormat), true)
            .asScala
            .toSeq
        Future
            .fold(songFiles.map((songFile: File) => Future {
                blocking {
                    val signal = AudioFile.extractFileSignal(songFile.getCanonicalPath())
                    val song = AudioFile.extractSongMetadata(songFile.getCanonicalPath())
                    val songIndexEntries = Fingerprint.signalToSongIndex(signal, song)
                    songIndexEntries
                }
            }))(Map.empty[SongIndexKey, Song])((songIndex, songIndexEntries) => songIndex ++ songIndexEntries)
    }
}
