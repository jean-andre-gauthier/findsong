"""
Measures the performance on a series of songs and clips
"""

from argparse import ArgumentParser
import re
import subprocess
import sys
from glob import glob
import numpy as np


def main():
    parser = ArgumentParser()
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

    matches_clips = glob(args.matchesglob)
    n_matches_glob_clips = len(matches_clips)
    matches_size = int(args.matchessize)
    index_songs = glob(args.indexglob)
    n_index_glob_clips = len(index_songs)
    index_sizes = list(
        map(lambda index_size: int(index_size), args.indexsizes))
    max_index_size = max(index_sizes)

    if matches_size > n_matches_glob_clips:
        print(f"--nclips={matches_size} but inputclips matched only " +
              f"{n_matches_glob_clips} clips")
        sys.exit(1)

    if max_index_size > n_index_glob_clips:
        print(f"--nsongs={index_sizes} but inputsongs matched only " +
              f"{n_index_glob_clips} songs")
        sys.exit(1)

    find_song = args.findsongpath
    find_song_clips_glob = "{" + ",".join(matches_clips[0:matches_size]) + "}"
    indexer_regex = (r"Indexing completed in (\d+) ms. Index contains " +
                     r"(\d+) fingerprints")
    matcher_regex = r"^Matching for .+ completed in (\d+) ms$"

    for index_size in index_sizes:
        find_song_index_glob = "{" + ",".join(index_songs[0:index_size]) + "}"
        process = subprocess.Popen(
            [
                "java", "-Xmx2G", "-jar", find_song, "-i",
                find_song_index_glob, "-m", find_song_clips_glob
            ],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE)
        output, = list(map(lambda s: s.decode("utf-8"), process.communicate()))

        indexer_match = re.search(indexer_regex, output)
        indexer_duration, index_fingerprints_size = indexer_match.group(
            1), indexer_match.group(2)
        matcher_matches = re.findall(matcher_regex, output, flags=re.MULTILINE)
        matcher_durations = list(
            map(lambda matcherMatch: int(matcherMatch), matcher_matches))

        with open(args.outputfilepath, "a") as output_file:
            output_file.write(" ".join([
                str(index_size), index_fingerprints_size, indexer_duration,
                str(np.mean(matcher_durations))
            ]) + "\n")


if __name__ == "__main__":
    main()
