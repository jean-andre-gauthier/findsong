package ja.gauthier.findsong.types

import java.nio.file.Paths
import java.nio.charset.StandardCharsets
import org.apache.commons.io.FileUtils

package object debuggable {
    /**
     *  Defines helper methods for dumping objects to files.
     */
    trait Debuggable {
        /**
         *  Dumps the contents of the object to a file.
         *
         *  @param filename the name of the file where the constellation map will be dumped into
         */
        def toFile(filename: String): Unit

        /**
         *  Writes the contents of a file to the debug folder.
         *
         *  @param filename the name of the file where the file content will be dumped into
         *  @param fileContent the content of the dump
         */
        def writeStringToFile(filename: String, fileContent: String): Unit = {
            FileUtils.writeStringToFile(
                Paths.get("analyses", "debug", filename + ".txt").toFile,
                fileContent,
                StandardCharsets.UTF_8)
        }
    }
}
