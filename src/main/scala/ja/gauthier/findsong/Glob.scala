package ja.gauthier.findsong

import java.io.File
import java.nio.file._
import scala.collection.JavaConverters._

/**
 * This object contains helper methods for glob-like file search
 */
object Glob {
  /**
   * Returns all file whose filename match the given glob
   *
   * @param glob a glob (without 'glob:' prefix)
   * @return a sequence of files with matching filename
   */
  def getMatchingFiles(glob: String): Seq[File] = {
    val currentDirectory = Paths.get("")
    val pattern = FileSystems.getDefault().getPathMatcher("glob:" + glob)
    Files
      .walk(currentDirectory)
      .filter((path: Path) => pattern.matches(path) && path.toFile().isFile())
      .iterator()
      .asScala
      .toSeq
      .map((path: Path) => path.toFile())
  }
}
