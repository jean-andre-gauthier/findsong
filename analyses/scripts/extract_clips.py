#
# Trims audio files to a certain length at a given offset
#

import argparse
import ffmpeg
import os
import random
import string
from glob import glob

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--inputfilesglob",
      help="files to trim (glob)", required=True, type=str)
    parser.add_argument("--length", help="trim length in seconds (int)",
      required=True, type=int)
    parser.add_argument("--limitfiles",
      help="maximal number of input files to process (int)",
      required=True, type=int)
    parser.add_argument("--offset", help="trim offset in seconds (int)",
      required=True, type=int)
    parser.add_argument("--outputfolderpath",
      help="folder in which to save the output clips (path)", required=True,
      type=str)
    args = parser.parse_args()

    if os.path.exists(args.outputfolderpath):
        print(f"Error: {args.outputfolderpath} already exists")
        exit(1)

    os.mkdir(args.outputfolderpath)

    currentLimitFiles = 1
    pathsFilename = os.path.join(args.outputfolderpath, "paths")
    printable = set(string.printable)

    with open(pathsFilename, "w") as pathsFile:
      inputFiles = glob(args.inputfilesglob, recursive=True)
      random.shuffle(inputFiles)
      for inputFilename in inputFiles:
        absoluteInputFilename = os.path.abspath(inputFilename)

        if args.limitfiles == -1 or currentLimitFiles <= args.limitfiles:
          outputBasename = os.path.basename(inputFilename)
          filename, extension = os.path.splitext(outputBasename)
          outputFilename = os.path.join(args.outputfolderpath, filename
            + extension)

          if not os.path.isfile(outputFilename):
            try:
              ffmpeg.input(inputFilename, t=args.length, ss=args.offset).filter_(
                "dynaudnorm").output(outputFilename).run()

              pathsFile.write(absoluteInputFilename + "\n")
              currentLimitFiles += 1
            except KeyboardInterrupt:
              break
            except:
              pass

      print(f"{currentLimitFiles-1} clips exported")

if __name__ == "__main__":
    main()
