"""
Trims audio files to a certain length at a given offset
"""

import argparse
import os
import random
from glob import glob
import ffmpeg


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--inputfilesglob",
        help="files to trim (glob)",
        required=True,
        type=str)
    parser.add_argument(
        "--length",
        help="trim length in seconds (int)",
        required=True,
        type=int)
    parser.add_argument(
        "--limitfiles",
        help="maximal number of input files to process (int)",
        required=True,
        type=int)
    parser.add_argument(
        "--offset",
        help="trim offset in seconds (int)",
        required=True,
        type=int)
    parser.add_argument(
        "--outputfolderpath",
        help="folder in which to save the output clips (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if os.path.exists(args.outputfolderpath):
        print(f"Error: {args.outputfolderpath} already exists")
        exit(1)

    os.mkdir(args.outputfolderpath)

    current_limit_files = 1
    paths_filename = os.path.join(args.outputfolderpath, "paths")

    with open(paths_filename, "w") as paths_file:
        input_files = glob(args.inputfilesglob, recursive=True)
        random.shuffle(input_files)
        for input_filename in input_files:
            absolute_input_filename = os.path.abspath(input_filename)

            if args.limitfiles == -1 or current_limit_files <= args.limitfiles:
                output_basename = os.path.basename(input_filename)
                filename, extension = os.path.splitext(output_basename)
                output_filename = os.path.join(args.outputfolderpath,
                                               filename + extension)

                if not os.path.isfile(output_filename):
                    try:
                        ffmpeg.input(
                            input_filename, t=args.length,
                            ss=args.offset).filter_("dynaudnorm").output(
                                output_filename).run()

                        paths_file.write(absolute_input_filename + "\n")
                        current_limit_files += 1
                    except KeyboardInterrupt:
                        break
                    except:
                        pass

        print(f"{current_limit_files-1} clips exported")


if __name__ == "__main__":
    main()
