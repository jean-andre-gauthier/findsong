package ja.gauthier.findsong.types

import java.io.File
import java.nio.charset.StandardCharsets
import org.apache.commons.io.FileUtils

package object debuggable {
    trait Debuggable {
        def toFile(filename: String): Unit

        def writeStringToFile(filename: String, fileContent: String): Unit = {
            FileUtils.writeStringToFile(
                new File("debug", filename + ".txt"),
                fileContent,
                StandardCharsets.UTF_8)
        }
    }
}
