"""
Runs findsong on the given parameters
"""

from argparse import ArgumentParser
from os import makedirs, path
import re
import subprocess
import sys


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--clipsglob", help="clips to use (glob)", required=True, type=str)
    parser.add_argument(
        "--findsongjarpath",
        help="findsong jar file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--parameterlist",
        help="parameters to use for findsong" + "(list of ints)",
        nargs="+",
        required=True)
    parser.add_argument(
        "--outputdirectorypath",
        help="directory where to save the processed clips (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--songsglob", help="songs to index (glob)", required=True, type=str)
    args = parser.parse_args()

    if path.exists(args.outputdirectorypath):
        print(f"Error: {args.outputdirectorypath} already exists")
        sys.exit(1)
    makedirs(args.outputdirectorypath)

    parameter_list = list(map(lambda pv: pv.split("="), args.parameterlist))

    for (parameter, values) in parameter_list:
        values_list = values.split(" ")
        for value in values_list:
            output_directory_path = path.join(args.outputdirectorypath,
                                              parameter, value)
            makedirs(output_directory_path)

            matched_clips_for_parameter_filename = path.join(
                output_directory_path, "matched_clips_for_parameter")
            unmatched_clips_for_parameter_filename = path.join(
                output_directory_path, "unmatched_clips_for_parameter")

            completed_findsong_process = subprocess.run(
                [
                    "java", "-Xmx2G", "-jar", args.findsongjarpath, "-i",
                    args.songsglob, "-m", args.clipsglob, parameter, value
                ],
                stdout=subprocess.PIPE)

            if completed_findsong_process.returncode == 0:
                findsong_output = completed_findsong_process.stdout.decode(
                    "ascii")
                indexer_regex = (
                    r"Indexing completed in (\d+) ms. Index contains " +
                    r"(\d+) fingerprints\n")
                indexer_match = re.search(indexer_regex, findsong_output)
                indexer_duration = indexer_match.group(1)
                matched_clips_regex = (
                    r"Matching for (.+) completed in (\d+) ms\n" +
                    r"(\d*) / (\d*) - (.*)\n" + r"(\d*) / (\d*) - (.*)\n" +
                    r"(\d*) / (\d*) - (.*)\n")
                matched_clips = re.findall(
                    matched_clips_regex, findsong_output, flags=re.MULTILINE)
                unmatched_clips_regex = (
                    r"Matching for (.+) completed in (\d+) ms\n" +
                    r"No matching song found\n")
                unmatched_clips = re.findall(
                    unmatched_clips_regex, findsong_output, flags=re.MULTILINE)

                with open(matched_clips_for_parameter_filename,
                          "w") as matched_clips_for_parameter_file:
                    for matched_clip in matched_clips:
                        matched_clip_for_parameter = ";".join(
                            matched_clip) + ";" + str(indexer_duration)
                        print(
                            matched_clip_for_parameter,
                            file=matched_clips_for_parameter_file)

                with open(unmatched_clips_for_parameter_filename,
                          "w") as unmatched_clips_for_parameter_file:
                    for unmatched_clip in unmatched_clips:
                        unmatched_clip_for_parameter = ";".join(unmatched_clip)
                        print(
                            unmatched_clip_for_parameter,
                            file=unmatched_clips_for_parameter_file)

            else:
                print("Error: findsong returned " +
                      str(completed_findsong_process.returncode))


if __name__ == "__main__":
    main()
