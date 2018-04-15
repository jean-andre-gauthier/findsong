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
                    val songIndexEntries = Fingerprinter.signalToSongIndex(signal, song)
                    songIndexEntries
                }
            }))(Seq[SongIndex]())((songIndex, songIndexEntries) => songIndex :+ songIndexEntries)
                .map((songIndexes) => combineSongIndexes(songIndexes))
    }

    def combineSongIndexes(songIndexes: Seq[SongIndex]): SongIndex = {
        songIndexes.flatMap(_.toSeq).groupBy(_._1).mapValues(_.flatMap(_._2))
    }
}
