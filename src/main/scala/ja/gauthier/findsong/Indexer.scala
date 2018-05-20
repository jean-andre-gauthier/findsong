package ja.gauthier.findsong

import ja.gauthier.findsong.types.Settings
import ja.gauthier.findsong.types.songIndex._
import java.io.File
import javax.sound.sampled._
import org.apache.commons.io._
import scala.concurrent._
import scala.collection.JavaConverters._

/**
 *  This object contains helper methods for creating a song fingerprint index.
 */
object Indexer {
    /**
     *  Creates a song index for the files with format settings.General.inputFormat in the directory settings.General.inputDirectory.
     *
     *  @param executionContext an executionContext for the indexing operations
     *  @param settings a Settings object containing the options for the app
     *  @return a future that completes once all songs are indexed
     */
    def indexSongs(implicit executionContext: ExecutionContext, settings: Settings): Future[SongIndex] = {
        val directoryFile = new File(settings.General.inputDirectory)
        val songFiles = FileUtils
            .listFiles(directoryFile, Array(settings.General.inputFormat), true)
            .asScala
            .toSeq
            .to[collection.immutable.Seq]
        Future
            .foldLeft(songFiles.map((songFile: File) => Future {
                val signal = AudioFile.extractFileSignal(songFile.getCanonicalPath())
                val song = AudioFile.extractSongMetadata(songFile.getCanonicalPath())
                val songIndexEntries = Fingerprinter.signalToSongIndex(signal, song)
                songIndexEntries
            }))(Seq[SongIndex]())((songIndex, songIndexEntries) => songIndex :+ songIndexEntries)
                .map((songIndexes) => combineSongIndexes(songIndexes))
    }

    /**
     *  Creates a song index with the contents from the list of song indexes.
     *
     *  @return a song index with the combined fingerprints from the list of song indexes
     */
    private def combineSongIndexes(songIndexes: Seq[SongIndex]): SongIndex = {
        songIndexes.flatMap(_.toSeq).groupBy(_._1).mapValues(_.flatMap(_._2))
    }
}
