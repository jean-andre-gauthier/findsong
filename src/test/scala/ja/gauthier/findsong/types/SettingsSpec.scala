package ja.gauthier.findsong.types

import org.scalatest._

class SettingsSpec extends FunSpec with Matchers {
  describe("settings") {
    describe("when no arguments are specified") {
      it("should throw an error") {
        val s = Settings.settings(Array("--indexerGlob", "*.mp4")).get
      }
    }

    describe("when only the input directory and format are specified") {
      it("should use default values for the other parameters") {}
    }

    describe("when all arguments are specified") {
      it("should use the CLI arguments") {}
    }
  }
}
