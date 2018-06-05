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
      help="files that should be trimmed (glob)", required=True, type=str)
    parser.add_argument("--length", help="trim length in seconds (int)",
      required=True, type=int)
    parser.add_argument("--limitfiles",
      help="maximal number of input files that will be processed (int)",
      required=True, type=int)
    parser.add_argument("--offset", help="trim offset in seconds (int)",
      required=True, type=int)
    parser.add_argument("--outputfolderpath",
      help="folder in which the clips will be saved (path)", required=True,
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
      shuffledGlob = glob(args.inputfilesglob, recursive=True)
      for inputFilename in shuffledGlob:
        absoluteInputFilename = os.path.abspath(inputFilename)

        if args.limitfiles == -1 or currentLimitFiles <= args.limitfiles:
          outputBasename = os.path.basename(inputFilename)
          filename, extension = os.path.splitext(outputBasename)
          outputFilename = os.path.join(args.outputfolderpath, filename
            + "_clip_noiseless" + extension)

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
