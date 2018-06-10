package ja.gauthier.findsong

import ja.gauthier.findsong.types._
import java.io._
import org.scalatest._

class GlobSpec extends FunSpec with Matchers {
  implicit val s = Settings.settings(Array("--indexerGlob", "*.mp4")).get

  describe("getMatchingFiles") {
    describe("when the glob does not correspond to any existing file") {
      it("should return an empty list") {
        val glob = "src/test/resources/foo.bar"
        val matchingFiles = Glob.getMatchingFiles(glob)
        matchingFiles should have length 0
      }
    }

    describe("when the glob corresponds to an existing file") {
      it("should return the matching file only") {
        val glob = "src/test/resources/clip_1.mp4"
        val matchingFiles = Glob.getMatchingFiles(glob)
        matchingFiles should have length 1
        matchingFiles(0).getName() should be("clip_1.mp4")
      }
    }

    describe("when the glob contains a wildcard") {
      it("should return all matching files") {
        val glob = "src/test/resources/clip_?.mp4"
        val matchingFiles = Glob.getMatchingFiles(glob)
        val matchingFilenames = matchingFiles.map(_.getName)
        matchingFilenames should contain theSameElementsAs Seq(
          "clip_1.mp4",
          "clip_2.mp4",
          "clip_3.mp4"
        )
      }
    }
  }
}
