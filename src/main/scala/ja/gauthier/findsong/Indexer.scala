package ja.gauthier.findsong

import ja.gauthier.findsong.types.Settings
import ja.gauthier.findsong.types.songIndex._
import java.io.File
import javax.sound.sampled._
import org.apache.commons.io._
import scala.concurrent._
import scala.collection.JavaConverters._

object Indexer {
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

    def combineSongIndexes(songIndexes: Seq[SongIndex]): SongIndex = {
        songIndexes.flatMap(_.toSeq).groupBy(_._1).mapValues(_.flatMap(_._2))
    }
}
