#
# Measures the performance on a series of songs and clips
#

import argparse
import numpy as np
import os
import re
import subprocess
import sys
from glob import glob


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--findsongpath",
        help="path to the FindSong binary (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--indexglob", help="songs to index (glob)", required=True, type=str)
    parser.add_argument(
        "--indexsizes",
        help="number of songs to index (list)",
        nargs="+",
        required=True)
    parser.add_argument(
        "--matchesglob", help="clips to match (glob)", required=True, type=str)
    parser.add_argument(
        "--matchessize",
        help="number of clips to match (int)",
        required=True,
        type=int)
    parser.add_argument(
        "--outputfilepath",
        help="path for the output file (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    matchesClips = glob(args.matchesglob)
    nMacthesGlobClips = len(matchesClips)
    matchesSize = int(args.matchessize)
    indexSongs = glob(args.indexglob)
    nIndexGlobClips = len(indexSongs)
    indexSizes = list(map(lambda indexSize: int(indexSize), args.indexsizes))
    maxIndexSize = max(indexSizes)

    if matchesSize > nMacthesGlobClips:
        print(
            f"--nclips={matchesSize} but inputclips matched only {nMacthesGlobClips} clips"
        )
        sys.exit(1)

    if maxIndexSize > nIndexGlobClips:
        print(
            f"--nsongs={indexSizes} but inputsongs matched only {nIndexGlobClips} songs"
        )
        sys.exit(1)

    findSong = args.findsongpath
    findSongClipsGlob = "{" + ",".join(matchesClips[0:matchesSize]) + "}"
    indexerRegex = r"Indexing completed in (\d+) ms. Index contains (\d+) fingerprints"
    matcherRegex = r"^Matching for .+ completed in (\d+) ms$"

    for indexSize in indexSizes:
        findSongIndexGlob = "{" + ",".join(indexSongs[0:indexSize]) + "}"
        process = subprocess.Popen(
            [
                "java", "-Xmx2G", "-jar", findSong, "-i", findSongIndexGlob,
                "-m", findSongClipsGlob
            ],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE)
        output, errors = list(
            map(lambda s: s.decode("utf-8"), process.communicate()))

        indexerMatch = re.search(indexerRegex, output)
        indexerDuration, indexFingerprintsSize = indexerMatch.group(
            1), indexerMatch.group(2)
        matcherMatches = re.findall(matcherRegex, output, flags=re.MULTILINE)
        matcherDurations = list(
            map(lambda matcherMatch: int(matcherMatch), matcherMatches))

        with open(args.outputfilepath, "a") as outputFile:
            outputFile.write(" ".join([
                str(indexSize), indexFingerprintsSize, indexerDuration,
                str(np.mean(matcherDurations))
            ]) + "\n")


if __name__ == "__main__":
    main()
