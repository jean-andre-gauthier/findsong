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
        help="matches input file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--recrateformatchesfilepath",
        help="recrate output file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--wrongresultsformatchesfilepath",
        help="wrong matches output file (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if path.exists(args.recrateformatchesfilepath):
        print(f"Error: {args.recrateformatchesfilepath} already exists")
        sys.exit(1)

    if path.exists(args.wrongresultsformatchesfilepath):
        print(f"Error: {args.wrongresultsformatchesfilepath} already exists")
        sys.exit(1)

    with open(args.matchesfilepath) as matches_file, open(
            args.recrateformatchesfilepath,
            "w") as recrate_for_matches_file, open(
                args.wrongresultsformatchesfilepath,
                "w") as wrong_results_for_matches_file:
        length_noise_regex = r"/noise/(\d*)/length/(\d*)/"
        metadata_artist_title_regex = (
            r"^format\|tag:artist=(.*)\|" + r"tag:title=(.*)$")
        metadata_title_artist_regex = (
            r"^format\|tag:title=(.*)\|" + "tag:artist=(.*)$")
        n_matches_by_length_by_noise = {}
        wrong_results_by_length_by_noise = {}

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
            (clip_noise, clip_length) = (int(clip_noise_length_match.group(1)),
                                         int(clip_noise_length_match.group(2)))

            metadata = subprocess.run(
                [
                    "ffprobe", "-show_entries", "format_tags=artist,title",
                    "-of", "compact", clip_filename
                ],
                stdout=subprocess.PIPE)
            decoded_metadata = metadata.stdout.decode("utf-8")

            metadata_artist_title_match = re.search(
                metadata_artist_title_regex,
                decoded_metadata,
                flags=re.MULTILINE)
            metadata_title_artist_match = re.search(
                metadata_title_artist_regex,
                decoded_metadata,
                flags=re.MULTILINE)

            if metadata_artist_title_match or metadata_title_artist_match:
                (metadata_artist,
                 metadata_title) = (metadata_artist_title_match.group(1),
                                    metadata_artist_title_match.group(2)
                                    ) if metadata_artist_title_match else (
                                        metadata_title_artist_match.group(2),
                                        metadata_title_artist_match.group(1))

                if (metadata_title + " - " +
                        metadata_artist == clip_title_artist1):
                    n_matches_by_length_by_noise[
                        clip_length] = n_matches_by_length_by_noise.get(
                            clip_length, {})
                    n_matches_by_length_by_noise[clip_length][clip_noise] = (
                        n_matches_by_length_by_noise[clip_length].get(
                            clip_noise, 0) + 1)
                else:
                    wrong_results_by_length_by_noise[
                        clip_length] = wrong_results_by_length_by_noise.get(
                            clip_length, {})
                    wrong_results_by_length_by_noise[clip_length][
                        clip_noise] = (
                            wrong_results_by_length_by_noise[clip_length].get(
                                clip_noise, []) +
                            [metadata_title + " - " + metadata_artist])

        length_noise_n_matches_list = []
        for (length,
             n_matches_by_noise) in n_matches_by_length_by_noise.items():
            for (noise, n_matches) in n_matches_by_noise.items():
                length_noise_n_matches_list.append([length, noise, n_matches])
        length_noise_n_matches_list.sort()

        for length_noise_n_matches in length_noise_n_matches_list:
            print(
                " ".join(str(entry) for entry in length_noise_n_matches),
                file=recrate_for_matches_file)

        length_noise_wrong_results_list = []
        for (length, wrong_results_by_noise
             ) in wrong_results_by_length_by_noise.items():
            for (noise, wrong_results) in wrong_results_by_noise.items():
                wrong_results.sort()
                length_noise_wrong_results_list.append(
                    [length, noise, ";".join(wrong_results)])
        length_noise_wrong_results_list.sort()

        for length_noise_wrong_results in length_noise_wrong_results_list:
            print(
                " ".join(str(entry) for entry in length_noise_wrong_results),
                file=wrong_results_for_matches_file)


if __name__ == "__main__":
    main()
