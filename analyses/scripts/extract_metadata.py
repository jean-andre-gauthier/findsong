#
# Extracts metadata from audio files with ffmpeg
#

import argparse
import os
import re
import subprocess

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--metadatakey", help="ffprobe metadata key",
      required=True, type=str)
    parser.add_argument("--pathsfilepath",
      help="file containing paths to the files to process (path)",
      required=True, type=str)
    parser.add_argument("--outputfilepath",
      help="file where the metadata is going to be stored (path)",
      required=True, type=str)
    args = parser.parse_args()

    if not os.path.exists(args.pathsfilepath):
        print(f"Error: {args.pathsfilepath} does not exist\n")
        exit(1)

    if os.path.exists(args.outputfilepath):
        print(f"Error: {args.outputfilepath} exists\n")
        exit(1)

    formatRegex = r"^format\|tag:\w+=(.*)$"
    inputFilename = args.pathsfilepath
    metadataDict = {}
    with open(inputFilename) as inputFile:
        for path in inputFile.readlines():
            strippedPath = path.strip()
            if not os.path.exists(strippedPath):
                print(f"Error: {path} does not exist")
                exit(1)

            metadata = subprocess.check_output(["ffprobe", "-show_entries",
                "format_tags=" + args.metadatakey, "-of", "compact",
                strippedPath])
            decodedMetadata = metadata.decode("utf-8")
            metadataKeyMatches = re.search(formatRegex, decodedMetadata)
            if metadataKeyMatches:
                metadataKeyMatch = metadataKeyMatches.group(1)
                metadataDict[metadataKeyMatch] = metadataDict.get(metadataKeyMatch, 0) + 1
            else:
                metadataDict["None"] = metadataDict.get("None", 0) + 1

    outputFilename = args.outputfilepath
    os.makedirs(os.path.dirname(outputFilename), exist_ok=True)
    with open(outputFilename, "w") as outputFile:
        for key, value in metadataDict.items():
            print(key.encode("ascii", "ignore").decode("ascii") + " " + str(value), file=outputFile)

if __name__ == "__main__":
    main()
