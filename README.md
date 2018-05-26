[![Build Status](https://travis-ci.org/jean-andre-gauthier/findsong.svg?branch=master)](https://travis-ci.org/jean-andre-gauthier/findsong)
[![codecov](https://codecov.io/gh/jean-andre-gauthier/findsong/branch/master/graph/badge.svg)](https://codecov.io/gh/jean-andre-gauthier/findsong)

# findsong

A proof of concept for a song search engine. Once the engine has indexed the audio files in the input directory, it can record a clip with the built-in microphone and recognise which song is being played.

## Basic usage

Microphone mode:
```
java -jar findsong_1_0_1.jar -i <glob for files to index>
```

Use audio clips instead of microphone:
```
java -jar findsong_1_0_1.jar -i <glob for files to index> -m <glob for recordings>
```

## Complete list of options

```
Usage: [options]

  --debug                  Create intermediate dump files during song fingerprinting and matching (default = false)
  --fanout <value>         Maximal number of peaks that can be paired with any given peak (default = 3)
  --greenLevel <value>     Threshold for a match score to be displayed in green (default = 25)
  -i, --indexerGlob <glob>
                           Glob for the song files to index
  -m, --matcherGlob <glob>
                           If present, the clip files matching the glob are used instead of the microphone
  --maxMatches <value>     Maximal number of matches returned by the search engine (default = 5)
  --peakDeltaF <value>     Frequency range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 1)
  --peakDeltaT <value>     Time range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 1)
  --peaksPerChunk <value>  Maximal number of peaks in any given fingerprinting chunk (default = 2)
  --samplesPerCapture <value>
                           Size of a microphone recording in samples (default = 80000)
  --samplesPerChunk <value>
                           Size of a fingerprinting chunk in samples (default = 16)
  --samplesPerChunkStep <value>
                           Size of the stride between two fingerprinting chunks in samples (default = 8)
  --sampleRate <value>     Fingerprinting / recording sample rate (default = 8000)
  --scoreCoefficient <value>
                           Coefficient that is used in the match scoring function (default = 30)
  --windowDeltaF <value>   Frequency range in which neighbouring peaks can be paired up (default = 1)
  --windowDeltaT <value>   Time range in which neighbouring peaks can be paired up (default = 4)
  --windowDeltaTi <value>  Minimal time difference for neighbouring peaks to be paired up (default = 2)
  --yellowLevel <value>    Threshold for a match score to be displayed in yellow (default = 10)
```

## Contact

* Follow me on [![Github](http://i.imgur.com/9I6NRUm.png "Github")][https://github.com/jean-andre-gauthier]
* Follow me on [![Twitter](http://i.imgur.com/wWzX9uB.png "Twitter")][https://twitter.com/jandre_gauthier]
* Visit [jean-andre-gauthier.com][https://jean-andre-gauthier.com]
