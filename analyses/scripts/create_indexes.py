"""
Creates directories containing various numbers of songs, in order to index them
later on
"""

from argparse import ArgumentParser
from glob import glob
from os import makedirs, path, remove
from random import shuffle
from shutil import copy2
import sys


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--indexsizes",
        help="sizes of the indexes (list of int)",
        nargs="+",
        required=True)
    parser.add_argument(
        "--inputfilesglob",
        help="glob for the input files (glob)",
        required=True,
        type=str)
    parser.add_argument(
        "--outputdirectory",
        help="directory where to store the index songs (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if path.exists(args.outputdirectory):
        print(f"Error: {args.outputdirectory} already exists")
        sys.exit(1)
    makedirs(args.outputdirectory)

    for index_size in args.indexsizes:
        index_directory = path.join(args.outputdirectory, index_size)
        makedirs(index_directory)
        input_filenames = glob(args.inputfilesglob, recursive=True)
        shuffle(input_filenames)
        paths_filename = path.join(index_directory, "paths")

        with open(paths_filename, "w") as paths_file:
            n_current_files = 0
            while n_current_files < int(index_size):
                input_filename = input_filenames.pop()
                absolute_input_filename = path.abspath(input_filename)
                output_filename = path.join(
                    index_directory, path.basename(absolute_input_filename))

                if not path.exists(output_filename):
                    try:
                        copy2(absolute_input_filename, output_filename)

                        try:
                            print(absolute_input_filename, file=paths_file)
                            n_current_files += 1
                        except KeyboardInterrupt:
                            sys.exit(1)
                        except:
                            remove(output_filename)
                    except KeyboardInterrupt:
                        sys.exit(1)
                    except:
                        pass


if __name__ == "__main__":
    main()
