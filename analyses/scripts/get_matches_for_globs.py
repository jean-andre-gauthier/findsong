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
        "--outputdirectorypath",
        help="directory where to save the processed clips (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--songsglob", help="songs to index (glob)", required=True)
    args = parser.parse_args()

    if path.exists(args.outputdirectorypath):
        print(f"Error: {args.outputdirectorypath} already exists")
        sys.exit(1)
    makedirs(args.outputdirectorypath)
    matched_clips_for_glob_filename = path.join(args.outputdirectorypath,
                                                "matched_clips_for_glob")
    unmatched_clips_for_glob_filename = path.join(args.outputdirectorypath,
                                                  "unmatched_clips_for_glob")

    completed_findsong_process = subprocess.run(
        [
            "java", "-Xmx2G", "-jar", args.findsongjarpath, "-i",
            args.songsglob, "-m", args.clipsglob
        ],
        stdout=subprocess.PIPE)

    if completed_findsong_process.returncode == 0:
        findsong_output = completed_findsong_process.stdout.decode("ascii")
        matched_clips_regex = (
            r"Matching for (.+) completed in (\d+) ms\n" +
            r"(\d*) / (\d*) - (.*)\n" + r"(\d*) / (\d*) - (.*)\n" +
            r"(\d*) / (\d*) - (.*)\n")
        matched_clips = re.findall(
            matched_clips_regex, findsong_output, flags=re.MULTILINE)
        unmatched_clips_regex = (r"Matching for (.+) completed in (\d+) ms\n" +
                                 r"No matching song found\n")
        unmatched_clips = re.findall(
            unmatched_clips_regex, findsong_output, flags=re.MULTILINE)

        with open(matched_clips_for_glob_filename,
                  "w") as matched_clips_for_glob_file:
            for matched_clip in matched_clips:
                matched_clip_for_glob = ";".join(matched_clip)
                print(matched_clip_for_glob, file=matched_clips_for_glob_file)

        with open(unmatched_clips_for_glob_filename,
                  "w") as unmatched_clips_for_glob_file:
            for unmatched_clip in unmatched_clips:
                unmatched_clip_for_glob = ";".join(unmatched_clip)
                print(
                    unmatched_clip_for_glob,
                    file=unmatched_clips_for_glob_file)

    else:
        print(
            f"Error: findsong returned {completed_findsong_process.returncode}"
        )


if __name__ == "__main__":
    main()
