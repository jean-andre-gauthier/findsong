"""
Extracts metadata from audio files with ffmpeg
"""

from argparse import ArgumentParser
import os
import re
import subprocess


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--metadatakey", help="ffprobe metadata key", required=True, type=str)
    parser.add_argument(
        "--pathsfilepath",
        help="file containing paths to the files to process (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--outputfilepath",
        help="file where to store the metadata (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if not os.path.exists(args.pathsfilepath):
        print(f"Error: {args.pathsfilepath} does not exist\n")
        exit(1)

    if os.path.exists(args.outputfilepath):
        print(f"Error: {args.outputfilepath} exists\n")
        exit(1)

    format_regex = r"^format\|tag:\w+=(.*)$"
    input_filename = args.pathsfilepath
    metadata_dict = {}
    with open(input_filename) as input_file:
        for path in input_file.readlines():
            stripped_path = path.strip()
            if not os.path.exists(stripped_path):
                print(f"Error: {path} does not exist")
                exit(1)

            metadata = subprocess.check_output([
                "ffprobe", "-show_entries", "format_tags=" + args.metadatakey,
                "-of", "compact", stripped_path
            ])
            decode_metadata = metadata.decode("utf-8")
            metadata_key_matches = re.search(format_regex, decode_metadata)
            if metadata_key_matches:
                metadata_key_match = metadata_key_matches.group(1)
                metadata_dict[metadata_key_match] = metadata_dict.get(
                    metadata_key_match, 0) + 1
            else:
                metadata_dict["None"] = metadata_dict.get("None", 0) + 1

    output_filename = args.outputfilepath
    os.makedirs(os.path.dirname(output_filename), exist_ok=True)
    with open(output_filename, "w") as output_file:
        for key, value in metadata_dict.items():
            print(
                key.encode("ascii", "ignore").decode("ascii") + " " +
                str(value),
                file=output_file)


if __name__ == "__main__":
    main()
