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
        "--recognitionrateformatchesfilepath",
        help="recrate output file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--wrongresultsformatchesfilepath",
        help="wrong matches output file (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if not path.exists(args.matchesfilepath):
        print(f"Error: {args.matchesfilepath} does not exist")
        sys.exit(1)

    if path.exists(args.recognitionrateformatchesfilepath):
        print(
            f"Error: {args.recognitionrateformatchesfilepath} already exists")
        sys.exit(1)

    if path.exists(args.wrongresultsformatchesfilepath):
        print(f"Error: {args.wrongresultsformatchesfilepath} already exists")
        sys.exit(1)

    with open(args.matchesfilepath) as matches_file, open(
            args.recognitionrateformatchesfilepath,
            "w") as recrate_for_matches_file, open(
                args.wrongresultsformatchesfilepath,
                "w") as wrong_results_for_matches_file:
        path_regex = (r"workspace/scala/findsong/analyses/data/clips/" +
                      r"audio_filters/(.*)/(.*)")
        metadata_artist_title_regex = (
            r"^format\|tag:artist=(.*)\|" + r"tag:title=(.*)$")
        metadata_title_artist_regex = (
            r"^format\|tag:title=(.*)\|" + "tag:artist=(.*)$")
        n_matches_by_path_suffix = {}
        wrong_results_by_path_suffix = {}

        for match_info in matches_file.readlines():
            match_info_list = match_info.split(";")
            if len(match_info_list) != 11:
                print(f"Error: unexpected match_info_list {match_info_list}")
                sys.exit(1)

            (clip_filename, clip_matcher_duration, clip_score1, _,
             clip_title_artist1, clip_score2, _, clip_title_artist2,
             clip_score3, _, clip_title_artist3) = (match_info_list)
            clip_path_match = re.search(path_regex, clip_filename)
            path_suffix = clip_path_match.group(1)
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
                    n_matches_by_path_suffix[path_suffix] = (
                        n_matches_by_path_suffix.get(path_suffix, 0) + 1)
                else:
                    wrong_results_by_path_suffix[path_suffix] = (
                        wrong_results_by_path_suffix.get(path_suffix, []) +
                        [metadata_title + " - " + metadata_artist])

        for (path_suffix, n_matches) in n_matches_by_path_suffix.items():
            print(
                " ".join([path_suffix, str(n_matches)]),
                file=recrate_for_matches_file)

        path_suffix_wrong_results_list = []
        for (path_suffix,
             wrong_results) in wrong_results_by_path_suffix.items():
            wrong_results.sort()
            path_suffix_wrong_results_list.append(
                [path_suffix, ";".join(wrong_results)])
        path_suffix_wrong_results_list.sort()

        for path_suffix_wrong_results in path_suffix_wrong_results_list:
            print(
                " ".join(str(entry) for entry in path_suffix_wrong_results),
                file=wrong_results_for_matches_file)


if __name__ == "__main__":
    main()
