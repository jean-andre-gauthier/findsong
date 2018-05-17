package ja.gauthier.findsong.types

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object Settings {
    val config = ConfigFactory.load()
    config.checkValid(ConfigFactory.defaultReference(), "findsong")

    object ApplicationConfigArguments {
        val findsong = config.getConfig("findsong")

        object ConstellationMap {
            val constellationMap = findsong.getConfig("constellation-map")
            val peakDeltaF = constellationMap.getInt("peak-delta-f")
            val peakDeltaT = constellationMap.getInt("peak-delta-t")
            val peaksPerChunk = constellationMap.getInt("peaks-per-chunk")
        }

        object General {
            val general = findsong.getConfig("general")
            val debug = general.getBoolean("debug")
            val inputFormat = "m4a"
        }

        object PeakPairs {
            val peakPairs = findsong.getConfig("peak-pairs")
            val fanout = peakPairs.getInt("fanout")
            val windowDeltaF = peakPairs.getInt("window-delta-f")
            val windowDeltaT = peakPairs.getInt("window-delta-t")
            val windowDeltaTi = peakPairs.getInt("window-delta-ti")
        }

        object Preprocessing {
            val preprocessing = findsong.getConfig("preprocessing")
            val bigEndian = false
            val bitsPerSample = 8
            val channels = 1
            val codec = "pcm_u8"
            val intermediateFormat = "u8"
            val sampleRate = preprocessing.getInt("sample-rate")
            val signed = true
        }

        object Recording {
            val recording = findsong.getConfig("recording")
            val bytesPerCapture = recording.getInt("bytes-per-capture")
        }

        object Spectrogram {
            val spectrogram = findsong.getConfig("spectrogram")
            val bytesPerChunk = spectrogram.getInt("bytes-per-chunk")
            val bytesPerChunkStep = spectrogram.getInt("bytes-per-chunk-step")
        }
    }

    case class CliArguments(
        bytesPerCapture: Int = ApplicationConfigArguments.Recording.bytesPerCapture,
        bytesPerChunk: Int = ApplicationConfigArguments.Spectrogram.bytesPerChunk,
        bytesPerChunkStep: Int = ApplicationConfigArguments.Spectrogram.bytesPerChunkStep,
        debug: Boolean = ApplicationConfigArguments.General.debug,
        fanout: Int = ApplicationConfigArguments.PeakPairs.fanout,
        inputDirectory: String = "",
        inputFormat: String = ApplicationConfigArguments.General.inputFormat,
        peakDeltaF: Int = ApplicationConfigArguments.ConstellationMap.peakDeltaF,
        peakDeltaT: Int = ApplicationConfigArguments.ConstellationMap.peakDeltaT,
        peaksPerChunk: Int = ApplicationConfigArguments.ConstellationMap.peaksPerChunk,
        sampleRate: Int = ApplicationConfigArguments.Preprocessing.sampleRate,
        windowDeltaF: Int = ApplicationConfigArguments.PeakPairs.windowDeltaF,
        windowDeltaT: Int = ApplicationConfigArguments.PeakPairs.windowDeltaT,
        windowDeltaTi: Int = ApplicationConfigArguments.PeakPairs.windowDeltaTi
    )

    val argumentsParser = new scopt.OptionParser[CliArguments]("scopt") {
        head("scopt", "3.x")

        opt[Int]("bytesPerCapture")
            .action((bytesPerCapture, cliArguments) =>
                cliArguments.copy(bytesPerCapture = bytesPerCapture))
            .text(s"Size of a microphone recording in bytes (default = ${ApplicationConfigArguments.Recording.bytesPerCapture})")

        opt[Int]("bytesPerChunk")
            .action((bytesPerChunk, cliArguments) =>
                cliArguments.copy(bytesPerChunk = bytesPerChunk))
                    .text(s"Size of a fingerprinting chunk in bytes (default = ${ApplicationConfigArguments.Spectrogram.bytesPerChunk})")

        opt[Int]("bytesPerChunkStep")
            .action((bytesPerChunkStep, cliArguments) =>
                cliArguments.copy(bytesPerChunkStep = bytesPerChunkStep))
                    .text(s"Size of the stride between two fingerprinting chunks in bytes (default = ${ApplicationConfigArguments.Spectrogram.bytesPerChunkStep})")

        opt[Boolean]("debug")
            .action((debug, cliArguments) =>
                cliArguments.copy(debug = debug))
                    .text(s"Create intermediate dump files during song fingerprinting and matching (default = ${ApplicationConfigArguments.General.debug})")

        opt[Int]("fanout")
            .action((fanout, cliArguments) =>
                cliArguments.copy(fanout = fanout))
                    .text(s"Maximal number of peaks that can be paired with any given peak (default = ${ApplicationConfigArguments.PeakPairs.fanout})")

        opt[String]('i', "inputDirectory")
            .required()
            .valueName("<directory>")
            .action((inputDirectory, cliArguments) =>
                cliArguments.copy(inputDirectory = inputDirectory))
                    .text("Directory containing the song files to index")

        opt[String]('f', "inputFormat")
            .required()
            .valueName("<format>")
            .action((inputFormat, cliArguments) =>
                cliArguments.copy(inputFormat = inputFormat))
                    .text("Format of the song files to index")

        opt[Int]("peakDeltaF")
            .action((peakDeltaF, cliArguments) =>
                cliArguments.copy(peakDeltaF = peakDeltaF))
                    .text(s"Frequency range in which a spectrogram cell has to be a local maximum to be considered a peak (default = ${ApplicationConfigArguments.ConstellationMap.peakDeltaF})")

        opt[Int]("peakDeltaT")
            .action((peakDeltaT, cliArguments) =>
                cliArguments.copy(peakDeltaT = peakDeltaT))
                    .text(s"Time range in which a spectrogram cell has to be a local maximum to be considered a peak (default = ${ApplicationConfigArguments.ConstellationMap.peakDeltaF})")

        opt[Int]("peaksPerChunk")
            .action((peaksPerChunk, cliArguments) =>
                cliArguments.copy(peaksPerChunk = peaksPerChunk))
                    .text(s"Maximal number of peaks in any given fingerprinting chunk (default = ${ApplicationConfigArguments.ConstellationMap.peaksPerChunk})")

        opt[Int]("sampleRate")
            .action((sampleRate, cliArguments) =>
                cliArguments.copy(sampleRate = sampleRate))
                    .text(s"Fingerprinting / recording sample rate (default = ${ApplicationConfigArguments.Preprocessing.sampleRate})")

        opt[Int]("windowDeltaF")
            .action((windowDeltaF, cliArguments) =>
                cliArguments.copy(windowDeltaF = windowDeltaF))
                    .text(s"Frequency range in which neighbouring peaks can be paired up (default = ${ApplicationConfigArguments.PeakPairs.windowDeltaF})")

        opt[Int]("windowDeltaT")
            .action((windowDeltaT, cliArguments) =>
                cliArguments.copy(windowDeltaT = windowDeltaT))
                    .text(s"Time range in which neighbouring peaks can be paired up (default = ${ApplicationConfigArguments.PeakPairs.windowDeltaT})")

        opt[Int]("windowDeltaTi")
            .action((windowDeltaTi, cliArguments) =>
                cliArguments.copy(windowDeltaTi = windowDeltaTi))
                    .text(s"Minimal time difference for neighbouring peaks to be paired up (default = ${ApplicationConfigArguments.PeakPairs.windowDeltaTi})")
    }

    def settings(args: Array[String]): Option[Settings] = {
        val cliArgumentsOption = argumentsParser.parse(args, CliArguments())
        cliArgumentsOption.flatMap((cliArguments: CliArguments) => Some(new Settings(cliArguments)))
    }
}

class Settings(arguments: Settings.CliArguments) {
    object ConstellationMap {
        val peakDeltaF = arguments.peakDeltaF
        val peakDeltaT = arguments.peakDeltaT
        val peaksPerChunk = arguments.peaksPerChunk
    }

    object General {
        val debug = arguments.debug
        val inputDirectory = arguments.inputDirectory
        val inputFormat = arguments.inputFormat
    }

    object PeakPairs {
        val fanout = arguments.fanout
        val windowDeltaF = arguments.windowDeltaF
        val windowDeltaT = arguments.windowDeltaT
        val windowDeltaTi = arguments.windowDeltaTi
    }

    object Preprocessing {
        val bigEndian = false
        val bitsPerSample = 8
        val channels = 1
        val codec = "pcm_u8"
        val intermediateFormat = "u8"
        val sampleRate = arguments.sampleRate
        val signed = true
    }

    object Recording {
        val bytesPerCapture = arguments.bytesPerCapture
    }

    object Spectrogram {
        val bytesPerChunk = arguments.bytesPerChunk
        val bytesPerChunkStep = arguments.bytesPerChunkStep
    }
}
