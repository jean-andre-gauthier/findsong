package ja.gauthier.findsong.types

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
 *  This object contains helper methods for retrieving configuration values from config files or from the command line.
 */
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

        object Matching {
            val matching = findsong.getConfig("matching")
            val greenLevel = matching.getInt("green-level")
            val maxMatches = matching.getInt("max-matches")
            val scoreCoefficient = matching.getInt("score-coefficient")
            val yellowLevel = matching.getInt("yellow-level")
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
            val bitsPerSample = 16
            val channels = 1
            val codec = "pcm_s16le"
            val intermediateFormat = "wav"
            val sampleRate = preprocessing.getInt("sample-rate")
            val signed = true
        }

        object Recording {
            val recording = findsong.getConfig("recording")
            val samplesPerCapture = recording.getInt("samples-per-capture")
        }

        object Spectrogram {
            val spectrogram = findsong.getConfig("spectrogram")
            val samplesPerChunk = spectrogram.getInt("samples-per-chunk")
            val samplesPerChunkStep = spectrogram.getInt("samples-per-chunk-step")
        }
    }

    case class CliArguments(
        debug: Boolean = ApplicationConfigArguments.General.debug,
        fanout: Int = ApplicationConfigArguments.PeakPairs.fanout,
        greenLevel: Int = ApplicationConfigArguments.Matching.greenLevel,
        inputDirectory: String = "",
        inputFormat: String = ApplicationConfigArguments.General.inputFormat,
        maxMatches: Int = ApplicationConfigArguments.Matching.maxMatches,
        peakDeltaF: Int = ApplicationConfigArguments.ConstellationMap.peakDeltaF,
        peakDeltaT: Int = ApplicationConfigArguments.ConstellationMap.peakDeltaT,
        peaksPerChunk: Int = ApplicationConfigArguments.ConstellationMap.peaksPerChunk,
        samplesPerCapture: Int = ApplicationConfigArguments.Recording.samplesPerCapture,
        samplesPerChunk: Int = ApplicationConfigArguments.Spectrogram.samplesPerChunk,
        samplesPerChunkStep: Int = ApplicationConfigArguments.Spectrogram.samplesPerChunkStep,
        sampleRate: Int = ApplicationConfigArguments.Preprocessing.sampleRate,
        scoreCoefficient: Int = ApplicationConfigArguments.Matching.scoreCoefficient,
        windowDeltaF: Int = ApplicationConfigArguments.PeakPairs.windowDeltaF,
        windowDeltaT: Int = ApplicationConfigArguments.PeakPairs.windowDeltaT,
        windowDeltaTi: Int = ApplicationConfigArguments.PeakPairs.windowDeltaTi,
        yellowLevel: Int = ApplicationConfigArguments.Matching.yellowLevel
    )

    val argumentsParser = new scopt.OptionParser[CliArguments]("") {
        head("findsong", "1.0.1")

        opt[Unit]("debug")
            .action((debug, cliArguments) =>
                cliArguments.copy(debug = true))
                    .text(s"Create intermediate dump files during song fingerprinting and matching (default = ${ApplicationConfigArguments.General.debug})")

        opt[Int]("fanout")
            .action((fanout, cliArguments) =>
                cliArguments.copy(fanout = fanout))
                    .text(s"Maximal number of peaks that can be paired with any given peak (default = ${ApplicationConfigArguments.PeakPairs.fanout})")

        opt[Int]("greenLevel")
            .action((greenLevel, cliArguments) =>
                cliArguments.copy(greenLevel = greenLevel))
                    .text(s"Threshold for a match score to be displayed in green (default = ${ApplicationConfigArguments.Matching.greenLevel})")

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

        opt[Int]("maxMatches")
            .action((maxMatches, cliArguments) =>
                cliArguments.copy(maxMatches = maxMatches))
                    .text(s"Maximal number of matches returned by the search engine (default = ${ApplicationConfigArguments.Matching.maxMatches})")

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

        opt[Int]("samplesPerCapture")
            .action((samplesPerCapture, cliArguments) =>
                cliArguments.copy(samplesPerCapture = samplesPerCapture))
            .text(s"Size of a microphone recording in samples (default = ${ApplicationConfigArguments.Recording.samplesPerCapture})")

        opt[Int]("samplesPerChunk")
            .action((samplesPerChunk, cliArguments) =>
                cliArguments.copy(samplesPerChunk = samplesPerChunk))
                    .text(s"Size of a fingerprinting chunk in samples (default = ${ApplicationConfigArguments.Spectrogram.samplesPerChunk})")

        opt[Int]("samplesPerChunkStep")
            .action((samplesPerChunkStep, cliArguments) =>
                cliArguments.copy(samplesPerChunkStep = samplesPerChunkStep))
                    .text(s"Size of the stride between two fingerprinting chunks in samples (default = ${ApplicationConfigArguments.Spectrogram.samplesPerChunkStep})")

        opt[Int]("sampleRate")
            .action((sampleRate, cliArguments) =>
                cliArguments.copy(sampleRate = sampleRate))
                    .text(s"Fingerprinting / recording sample rate (default = ${ApplicationConfigArguments.Preprocessing.sampleRate})")

        opt[Int]("scoreCoefficient")
            .action((scoreCoefficient, cliArguments) =>
                cliArguments.copy(scoreCoefficient = scoreCoefficient))
                    .text(s"Coefficient that is used in the match scoring function (default = ${ApplicationConfigArguments.Matching.scoreCoefficient})")

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

        opt[Int]("yellowLevel")
            .action((yellowLevel, cliArguments) =>
                cliArguments.copy(yellowLevel = yellowLevel))
                    .text(s"Threshold for a match score to be displayed in yellow (default = ${ApplicationConfigArguments.Matching.yellowLevel})")
    }

    /**
     *  Parses the CLI arguments, using default values if options are missing.
     *
     *  @param args the CLI arguments
     *  @return An option with the setting values, None if the parsing failed
     */
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

    object Matching {
        val greenLevel = arguments.greenLevel
        val maxMatches = arguments.maxMatches
        val scoreCoefficient = arguments.scoreCoefficient
        val yellowLevel = arguments.yellowLevel
    }

    object PeakPairs {
        val fanout = arguments.fanout
        val windowDeltaF = arguments.windowDeltaF
        val windowDeltaT = arguments.windowDeltaT
        val windowDeltaTi = arguments.windowDeltaTi
    }

    object Preprocessing {
        val bigEndian = false
        val bitsPerSample = 16
        val channels = 1
        val codec = "pcm_s16le"
        val intermediateFormat = "wav"
        val sampleRate = arguments.sampleRate
        val signed = true
    }

    object Recording {
        val samplesPerCapture = arguments.samplesPerCapture
    }

    object Spectrogram {
        val samplesPerChunk = arguments.samplesPerChunk
        val samplesPerChunkStep = arguments.samplesPerChunkStep
    }
}
