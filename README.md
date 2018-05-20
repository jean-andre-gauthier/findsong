[![Build Status](https://travis-ci.org/jean-andre-gauthier/findsong.svg?branch=master)](https://travis-ci.org/jean-andre-gauthier/findsong)
[![codecov](https://codecov.io/gh/jean-andre-gauthier/findsong/branch/master/graph/badge.svg)](https://codecov.io/gh/jean-andre-gauthier/findsong)

# findsong

A proof of concept for a song search engine. Once the engine has indexed the audio files in the input directory, it can record a clip with the built-in microphone and recognise which song is being played.

## Basic usage

```
java -jar findsong_1_0_1.jar -i <directory with audio files> -f <audio format (e.g. m4a)>
```

## Complete list of options

```
Usage: [options]

  --bytesPerCapture <value>
                           Size of a microphone recording in bytes (default = 80000)
  --bytesPerChunk <value>  Size of a fingerprinting chunk in bytes (default = 16)
  --bytesPerChunkStep <value>
                           Size of the stride between two fingerprinting chunks in bytes (default = 8)
  --debug <value>          Create intermediate dump files during song fingerprinting and matching (default = false)
  --fanout <value>         Maximal number of peaks that can be paired with any given peak (default = 3)
  -i, --inputDirectory <directory>
                           Directory containing the song files to index
  -f, --inputFormat <format>
                           Format of the song files to index
  --maxMatches <value>     Maximal number of matches returned by the search engine (default = 5)
  --peakDeltaF <value>     Frequency range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 1)
  --peakDeltaT <value>     Time range in which a spectrogram cell has to be a local maximum to be considered a peak (default = 1)
  --peaksPerChunk <value>  Maximal number of peaks in any given fingerprinting chunk (default = 2)
  --sampleRate <value>     Fingerprinting / recording sample rate (default = 8000)
  --windowDeltaF <value>   Frequency range in which neighbouring peaks can be paired up (default = 1)
  --windowDeltaT <value>   Time range in which neighbouring peaks can be paired up (default = 4)
  --windowDeltaTi <value>  Minimal time difference for neighbouring peaks to be paired up (default = 2)
```

## Contact

* Follow me on [![Github](http://i.imgur.com/9I6NRUm.png "Github")][https://github.com/jean-andre-gauthier]
* Follow me on [![Twitter](http://i.imgur.com/wWzX9uB.png "Twitter")][https://twitter.com/jandre_gauthier]
* Visit [jean-andre-gauthier.com][https://jean-andre-gauthier.com]
