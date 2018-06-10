package ja.gauthier.findsong.types

import com.github.davidmoten.rtree._
import com.github.davidmoten.rtree.geometry.Point
import ja.gauthier.findsong.types.debuggable._
import ja.gauthier.findsong.types.peak._
import scala.collection.JavaConverters._

/**
  *  Contains the class / type definitions for constellation maps.
  */
package object constellationMap {
  implicit class DebuggableConstellationMap(constellationMap: ConstellationMap)(
      implicit settings: Settings)
      extends Debuggable {

    /**
      *  Dumps the contents of the constellation map to a file.
      *
      *  @param filename the name of the file where the constellation map will be dumped into
      */
    def toFile(filename: String): Unit = {
      if (settings.General.debug) {
        val fileContent = constellationMap.entries
          .toBlocking()
          .toIterable()
          .asScala
          .foldLeft(new StringBuilder("x y\n"))(
            (sb: StringBuilder, treeEntry: Entry[Peak, Point]) =>
              sb.append(treeEntry.geometry.x)
                .append(" ")
                .append(treeEntry.geometry.y)
                .append("\n"))
          .toString
        super.writeStringToFile(filename, fileContent)
      }
    }
  }

  /**
    *  A constellation map is a (point-based) RTree, where peaks are stored as values in the leaves.
    */
  type ConstellationMap = RTree[Peak, Point]
}
