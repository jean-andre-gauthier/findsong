"""
Runs findsong on the given globs
"""

from argparse import ArgumentParser
from os import makedirs, path
import re
import subprocess
import sys


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--clipsglob",
        help="clips to use (list of globs)",
        required=True,
        type=str)
    parser.add_argument(
        "--clipssamplespercapture",
        help="values to use for the findsong --samplesPerCapture flag " +
        "(list of ints)",
        nargs="+",
        required=True)
    parser.add_argument(
        "--findsongjarpath",
        help="findsong jar file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--outputfolderpath",
        help="folder where to save the processed clips (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--songsglob", help="songs to index (glob)", required=True)
    args = parser.parse_args()

    if path.exists(args.outputfolderpath):
        print(f"Error: {args.outputfolderpath} already exists")
        sys.exit(1)
    makedirs(args.outputfolderpath)
    matches_for_glob_filename = path.join(args.outputfolderpath,
                                          "matches_for_glob")

    completed_findsong_process = subprocess.run(
        [
            "java", "-Xmx2G", "-jar", args.findsongjarpath, "-i",
            args.songsglob, "-m", args.clipsglob
        ],
        stdout=subprocess.PIPE)

    if completed_findsong_process.returncode == 0:
        findsong_output = completed_findsong_process.stdout.decode("ascii")
        findsong_output_regex = (
            r"Matching for (.+) completed in (\d+) ms\n" +
            r"(\d*) / (\d*) - (.*)\n" + r"(\d*) / (\d*) - (.*)\n" +
            r"(\d*) / (\d*) - (.*)\n")
        matches_clips = re.findall(
            findsong_output_regex, findsong_output, flags=re.MULTILINE)

        with open(matches_for_glob_filename, "w") as matches_for_glob_file:
            for matches_clip in matches_clips:
                matches_for_glob = ";".join(matches_clip)
                print(matches_for_glob, file=matches_for_glob_file)
    else:
        print(
            f"Error: findsong returned {completed_findsong_process.returncode}"
        )


if __name__ == "__main__":
    main()
