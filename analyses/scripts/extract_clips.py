#
# Trims audio files to a certain length at a given offset
#

import argparse
import ffmpeg
import os
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

    os.mkdir(args.outputfolderpath)
    currentLimitFiles = 0
    for inputFilename in glob(args.inputfilesglob):
      currentLimitFiles += 1
      if args.limitfiles == -1 or currentLimitFiles > args.limitfiles:
        break;

      outputBasename = os.path.basename(inputFilename)
      filename, extension = os.path.splitext(outputBasename)
      outputFilename = os.path.join(args.outputfolderpath, filename + "_clip" +
        extension)

      ffmpeg.input(inputFilename, t=args.length, ss=args.offset).filter_(
        "dynaudnorm").output(outputFilename).run()

if __name__ == '__main__':
    main()