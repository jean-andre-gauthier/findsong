"""
Trims audio files to a certain length (at a random offset)
"""

from argparse import ArgumentParser
from os import makedirs, path
import random
from glob import glob
import ffmpeg


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--inputfilesglob",
        help="files to trim (glob)",
        required=True,
        type=str)
    parser.add_argument(
        "--cliplength",
        help="trim length in seconds (int)",
        required=True,
        type=int)
    parser.add_argument(
        "--limitfiles",
        help="maximal number of input files to process (int)",
        required=True,
        type=int)
    parser.add_argument(
        "--outputdirectorypath",
        help="directory in which to save the output clips (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if path.exists(args.outputdirectorypath):
        print(f"Error: {args.outputdirectorypath} already exists")
        exit(1)

    makedirs(args.outputdirectorypath)

    current_limit_files = 1
    paths_filename = path.join(args.outputdirectorypath, "paths")

    with open(paths_filename, "w") as paths_file:
        input_files = glob(args.inputfilesglob, recursive=True)
        random.shuffle(input_files)
        for input_filename in input_files:
            absolute_input_filename = path.abspath(input_filename)

            if args.limitfiles == -1 or current_limit_files <= args.limitfiles:
                output_basename = path.basename(input_filename)
                filename, extension = path.splitext(output_basename)
                output_filename = path.join(args.outputdirectorypath,
                                            filename + extension)

                if not path.isfile(output_filename):
                    try:
                        clip_length = int(args.cliplength)
                        duration = int(
                            float(
                                ffmpeg.probe(input_filename)["format"][
                                    "duration"]))
                        if duration >= clip_length:
                            offset = random.randint(0, duration - clip_length)
                        else:
                            offset = 0

                        ffmpeg.input(
                            input_filename, t=clip_length, ss=offset).filter_(
                                "dynaudnorm").output(output_filename).run()

                        paths_file.write(absolute_input_filename + "\n")
                        current_limit_files += 1
                    except KeyboardInterrupt:
                        break
                    except:
                        pass

        print(f"{current_limit_files-1} clips exported")


if __name__ == "__main__":
    main()
