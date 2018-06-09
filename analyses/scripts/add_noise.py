#
# Adds noise to a clip
#

import argparse
import ffmpeg
import os
import subprocess
from glob import glob
from os import path


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
        "--outputfolderpath",
        help="folder where to save the processed clips (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if not path.exists(args.noisefilepath):
        print(f"Error: noise file {args.noisefilepath} does not exist")
        exit(1)

    if path.exists(args.outputfolderpath):
        print(f"Error: {args.outputfolderpath} already exists")
        exit(1)

    noiseWeight = float(args.noiseweight)
    if noiseWeight < 0.0:
        print(
            f"Error: noise weight must be larger or equal to 0.0 but is {noiseWeight}"
        )
        exit(1)

    os.makedirs(args.outputfolderpath, exist_ok=True)

    for inputFilename in glob(args.inputfilesglob):
        inputFileBasename = path.basename(inputFilename)
        outputFilename = path.join(args.outputfolderpath, inputFileBasename)
        ffmpegCommand = f"ffmpeg -i {inputFilename} -i {args.noisefilepath} -filter_complex '[0]volume=1.0[a];[1]volume={noiseWeight}[b];[a][b]amix=duration=shortest' -ac 2 -c:a libmp3lame -q:a 4 {outputFilename}"
        subprocess.run(ffmpegCommand, check=True, shell=True)


if __name__ == "__main__":
    main()
