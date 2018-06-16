"""
Measures the performance on a series of songs and clips
"""

from argparse import ArgumentParser
from os import path
import re
import subprocess


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--clipsglobs",
        help="clips to use for the matching process (list of globs)",
        nargs="+",
        required=True)
    parser.add_argument(
        "--findsongjarpath",
        help="path to the findsong jar (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--indexsizes",
        help="number of songs to index (list)",
        nargs="+",
        required=True)
    parser.add_argument(
        "--performancefilepath",
        help="path for the output file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--songsglobs",
        help="songs to index (list of globs)",
        nargs="+",
        required=True)
    args = parser.parse_args()

    if path.exists(args.performancefilepath):
        print(f"Error: {args.performancefilepath} already exists")
        exit(1)

    clips_globs_length = len(args.clipsglobs)
    index_sizes_length = len(args.indexsizes)
    songs_globs_length = len(args.songsglobs)
    if (clips_globs_length != index_sizes_length
            or clips_globs_length != songs_globs_length
            or index_sizes_length != songs_globs_length):
        print(f"Error: inconsistent length for --clipsglobs " +
              f"({clips_globs_length}), --indexsizes ({index_sizes_length}), "
              + f"--songsglobs ({songs_globs_length})")
        exit(1)

    clips_globs_by_index_size = {}
    songs_globs_by_index_size = {}
    for (i, index_size) in enumerate(args.indexsizes):
        clips_globs_by_index_size[index_size] = args.clipsglobs[i]
        songs_globs_by_index_size[index_size] = args.songsglobs[i]

    indexer_regex = (r"Indexing completed in (\d+) ms. Index contains " +
                     r"(\d+) fingerprints\n")
    matcher_regex = r"Matching for .+ completed in (\d+) ms\n"

    for index_size in args.indexsizes:
        completed_findsong_process = subprocess.run(
            [
                "java", "-Xmx2G", "-jar", args.findsongjarpath, "-i",
                clips_globs_by_index_size[index_size], "-m",
                songs_globs_by_index_size[index_size]
            ],
            stdout=subprocess.PIPE)

        if completed_findsong_process.returncode == 0:
            findsong_output = completed_findsong_process.stdout.decode("ascii")
            indexer_match = re.search(indexer_regex, findsong_output)
            indexer_duration, indexer_fingerprints = indexer_match.group(
                1), indexer_match.group(2)
            matcher_durations = re.findall(
                matcher_regex, findsong_output, flags=re.MULTILINE)

            with open(args.performancefilepath, "a") as performance_file:
                print(
                    " ".join(
                        [index_size, indexer_duration, indexer_fingerprints] +
                        matcher_durations),
                    file=performance_file)
        else:
            print(f"Error: findsong returned " +
                  f"{completed_findsong_process.returncode}")


if __name__ == "__main__":
    main()
