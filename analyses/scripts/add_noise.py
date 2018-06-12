"""
Adds noise to a clip
"""

import argparse
import subprocess
from glob import glob
from os import makedirs, path
from shlex import quote


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--inputfilesglob",
        help="clips to which to add noise (glob)",
        required=True,
        type=str)
    parser.add_argument(
        "--noisefilepath",
        help="noise file to use (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--noiseweight",
        help="weight of the noise (float)",
        required=True,
        type=float)
    parser.add_argument(
        "--outputdirectorypath",
        help="directory where to save the processed clips (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if not path.exists(args.noisefilepath):
        print(f"Error: noise file {args.noisefilepath} does not exist")
        exit(1)

    if path.exists(args.outputdirectorypath):
        print(f"Error: {args.outputdirectorypath} already exists")
        exit(1)

    noise_weight = float(args.noiseweight)
    if noise_weight < 0.0:
        print("Error: noise weight must be larger or equal to 0.0 but is " +
              noise_weight)
        exit(1)

    makedirs(args.outputdirectorypath, exist_ok=True)

    for input_filename in glob(args.inputfilesglob):
        input_file_basename = path.basename(input_filename)
        output_filename = path.join(args.outputdirectorypath,
                                    input_file_basename)
        ffmpeg_command = (
            f"ffmpeg -i {quote(input_filename)} -i {args.noisefilepath} " +
            " -filter_complex '[0]volume=1.0[a];" +
            f"[1]volume={noise_weight}[b];[a][b]amix=duration=shortest' " +
            f"-ac 2 -c:a libmp3lame -q:a 4 {quote(output_filename)}")
        subprocess.run(ffmpeg_command, check=True, shell=True)


if __name__ == "__main__":
    main()
