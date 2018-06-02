#
# Trims audio files to a certain length at a given offset
#

import argparse
import ffmpeg
import os
from glob import glob

def appe(filename, newExtension):

  return filename + newExtension

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("inputfiles", type=str,
      help="files that should be trimmed")
    parser.add_argument("--limitfiles", type=int, default=-1,
      help="maximal number of input files that will be processed")
    parser.add_argument("--length", type=int, default=0,
      help="trim length (in seconds)")
    parser.add_argument("--offset", type=int, help="trim offset (in seconds)")
    parser.add_argument("--outputfolder", type=str, default=".",
      help="folder in which the clips will be saved")
    args = parser.parse_args()

    currentLimitFiles = 0
    for inputFilename in glob(args.inputfiles):
      currentLimitFiles += 1
      if args.limitfiles != -1 and currentLimitFiles <= args.limitfiles:
        break;

      outputBasename = os.path.basename(inputFilename)
      filename, extension = os.path.splitext(outputBasename)
      outputFilename = os.path.join(args.outputfolder, filename + "_clip" +
        extension)

      ffmpeg.input(inputFilename, t=args.length, ss=args.offset).filter_(
        "dynaudnorm").output(outputFilename).run()

if __name__ == '__main__':
    main()