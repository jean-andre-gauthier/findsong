package ja.gauthier.findsong

import java.io.File
import java.nio.file._
import scala.collection.JavaConverters._

object Glob {
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
