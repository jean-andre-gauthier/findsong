"""
Iterates over match info files produced by get_matches_for_globs.py, and
extracts the respective recognition rates
"""
from argparse import ArgumentParser
from os import path
import re
import subprocess
import sys


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--matchesfilepath",
        help="matches file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--outputfilepath", help="output file (path)", required=True, type=str)
    args = parser.parse_args()

    if path.exists(args.outputfilepath):
        print(f"Error: {args.outputfilepath} already exists")
        sys.exit(1)

    with open(args.matchesfilepath) as matches_file, open(
            args.outputfilepath, "w") as output_file:
        metadata_regex = r"^format\|tag:title=(.*)\|tag:artist=(.*)$"
        length_noise_regex = r"/noise/(\d*)/length/(\d*)/"
        n_matches_by_length_by_noise = {}

        for match_info in matches_file.readlines():
            match_info_list = match_info.split(";")
            if len(match_info_list) != 11:
                print(f"Error: unexpected match_info_list {match_info_list}")
                sys.exit(1)

            (clip_filename, clip_matcher_duration, clip_score1, _,
             clip_title_artist1, clip_score2, _, clip_title_artist2,
             clip_score3, _, clip_title_artist3) = (match_info_list)
            clip_noise_length_match = re.search(length_noise_regex,
                                                clip_filename)
            (clip_noise, clip_length) = (clip_noise_length_match.group(1),
                                         clip_noise_length_match.group(2))

            metadata = subprocess.run(
                [
                    "ffprobe", "-show_entries", "format_tags=artist,title",
                    "-of", "compact", clip_filename
                ],
                stdout=subprocess.PIPE)
            decoded_metadata = metadata.stdout.decode("utf-8")

            metadata_title_artist_match = re.search(
                metadata_regex, decoded_metadata, flags=re.MULTILINE)

            if metadata_title_artist_match:
                (metadata_title,
                 metadata_artist) = (metadata_title_artist_match.group(1),
                                     metadata_title_artist_match.group(2))

                if (metadata_title + " - " +
                        metadata_artist == clip_title_artist1):
                    n_matches_by_length_by_noise[
                        clip_length] = n_matches_by_length_by_noise.get(
                            clip_length, {})
                    n_matches_by_length_by_noise[clip_length][clip_noise] = (
                        n_matches_by_length_by_noise[clip_length].get(
                            clip_noise, 0) + 1)

        length_noise_n_matches = []
        for (length,
             n_matches_by_noise) in n_matches_by_length_by_noise.items():
            for (noise, n_matches) in n_matches_by_noise.items():
                length_noise_n_matches.append(
                    [str(length), str(noise),
                     str(n_matches)])
        for entry in length_noise_n_matches:
            print(" ".join(entry), file=output_file)


if __name__ == "__main__":
    main()
