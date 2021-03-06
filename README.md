[![Build Status](https://travis-ci.org/jean-andre-gauthier/findsong.svg?branch=master)](https://travis-ci.org/jean-andre-gauthier/findsong)
[![codecov](https://codecov.io/gh/jean-andre-gauthier/findsong/branch/master/graph/badge.svg)](https://codecov.io/gh/jean-andre-gauthier/findsong)

# findsong

A proof of concept for a song search engine. Once the engine has indexed the audio files in the input directory, it can record a clip with the built-in microphone and recognise which song is being played.

## Installation

1. Install [ffmpeg](https://ffmpeg.org/download.html) (minimal required version: ffmpeg 2.8.14)
2. Download the findsong [binary](https://github.com/jean-andre-gauthier/findsong/raw/master/bin/findsong-assembly-1.0.7.jar)
3. Run the binary with java (minimal required version: Java SE 8)

## Basic usage

Microphone mode:
```
java -Xmx2G -jar findsong-assembly-1.0.6.jar -i "<glob for files to index>"
```

Use audio clips instead of microphone:
```
java -Xmx2G -jar findsong-assembly-1.0.6.jar -i "<glob for files to index>" -m "<glob for recordings>"
```

## Complete list of options

```
Usage: [options]

  --debug                  Create intermediate dump files during song fingerprinting and matching (default = false)
  --debugDirectory         Directory for the dump files that are created when using --debug (default = analyses/debug)
  --fanout <value>         Maximal number of peaks that can be paired with any given peak (default = 3)
  --greenLevel <value>     Threshold for a match score to be displayed in green (default = 25)
  -i, --indexerGlob <glob>
                           Glob for the song files to index
  -m, --matcherGlob <glob>
                           If present, the clip files matching the glob are used instead of the microphone
  --maxMatches <value>     Maximal number of matches returned by the search engine (default = 3)
  --peakDeltaF <value>     Frequency range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 4)
  --peakDeltaT <value>     Time range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 4)
  --peaksPerChunk <value>  Maximal number of peaks in any given fingerprinting chunk (default = 2)
  --samplesPerCapture <value>
                           Size of a microphone recording in samples (default = 80000)
  --samplesPerChunk <value>
                           Size of a fingerprinting chunk in samples (default = 1024)
  --samplesPerChunkStep <value>
                           Size of the stride between two fingerprinting chunks in samples (default = 512)
  --sampleRate <value>     Fingerprinting / recording sample rate (default = 8000)
  --scoreCoefficient <value>
                           Coefficient that is used in the match scoring function (default = 30)
  --windowDeltaF <value>   Frequency range in which neighbouring peaks can be paired up (default = 30)
  --windowDeltaT <value>   Time range in which neighbouring peaks can be paired up (default = 35)
  --windowDeltaTi <value>  Minimal time difference for neighbouring peaks to be paired up (default = 5)
  --yellowLevel <value>    Threshold for a match score to be displayed in yellow (default = 10)
```

## Contact

* Follow me on [![Github][1.1]][1]
* Follow me on [![Twitter][2.1]][2]
* Visit [jean-andre-gauthier.com](https://jean-andre-gauthier.com)

[1.1]: http://i.imgur.com/9I6NRUm.png
[2.1]: http://i.imgur.com/wWzX9uB.png

[1]: https://github.com/jean-andre-gauthier
[2]: https://twitter.com/_jagauthier
