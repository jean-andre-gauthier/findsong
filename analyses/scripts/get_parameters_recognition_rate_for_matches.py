"""
Iterates over match info files produced by get_matches_for_globs.py, and
extracts the respective recognition rates
"""
from argparse import ArgumentParser
from os import listdir, path
import re
import subprocess
import sys


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--matchesdirectorypath",
        help="matches input directory (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--recognitionrateformatchesfilepath",
        help="recrate output file (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if not path.exists(args.matchesdirectorypath):
        print(f"Error: {args.matchesdirectorypath} does not exist")
        sys.exit(1)

    if path.exists(args.recognitionrateformatchesfilepath):
        print(
            f"Error: {args.recognitionrateformatchesfilepath} already exists")
        sys.exit(1)

    metadata_artist_title_regex = (
        r"^format\|tag:artist=(.*)\|" + r"tag:title=(.*)$")
    metadata_title_artist_regex = (
        r"^format\|tag:title=(.*)\|" + "tag:artist=(.*)$")
    n_matches_by_param_by_val = {}
    score_by_param_by_val = {}
    with open(args.recognitionrateformatchesfilepath,
              "w") as recognition_rate_for_matches_file:
        for param in listdir(args.matchesdirectorypath):
            param_directory = path.join(args.matchesdirectorypath, param)
            if path.isdir(param_directory):
                for val in listdir(param_directory):
                    val_directory = path.join(param_directory, val)
                    if path.isdir(val_directory):
                        matches_filename = path.join(
                            val_directory, "matched_clips_for_parameter")
                        with open(matches_filename) as matches_file:
                            for match_info in matches_file.readlines():
                                match_info_list = match_info.split(";")
                                if len(match_info_list) != 12:
                                    print("Error: unexpected match_info_list "
                                          + f"for {param}, {val}: " +
                                          f"{match_info_list}")
                                    sys.exit(1)

                                (clip_filename, _, clip_score1, _,
                                 clip_title_artist1, _, _, _, _, _, _,
                                 _) = (match_info_list)

                                metadata = subprocess.run(
                                    [
                                        "ffprobe", "-show_entries",
                                        "format_tags=artist,title", "-of",
                                        "compact", clip_filename
                                    ],
                                    stdout=subprocess.PIPE)
                                decoded_metadata = metadata.stdout.decode(
                                    "utf-8")

                                metadata_artist_title_match = re.search(
                                    metadata_artist_title_regex,
                                    decoded_metadata,
                                    flags=re.MULTILINE)
                                metadata_title_artist_match = re.search(
                                    metadata_title_artist_regex,
                                    decoded_metadata,
                                    flags=re.MULTILINE)

                                if (metadata_artist_title_match
                                        or metadata_title_artist_match):
                                    (metadata_artist, metadata_title) = (
                                        metadata_artist_title_match.group(1),
                                        metadata_artist_title_match.group(2)
                                    ) if metadata_artist_title_match else (
                                        metadata_title_artist_match.group(2),
                                        metadata_title_artist_match.group(1))

                                if (metadata_title + " - " +
                                        metadata_artist == clip_title_artist1):
                                    n_matches_by_param_by_val[param] = (
                                        n_matches_by_param_by_val.get(
                                            param, {}))
                                    n_matches_by_param_by_val[param][val] = (
                                        n_matches_by_param_by_val[param].get(
                                            val, 0) + 1)
                                    score_by_param_by_val[param] = (
                                        score_by_param_by_val.get(param, {}))
                                    score_by_param_by_val[param][val] = (
                                        score_by_param_by_val[param].get(
                                            val, 0) + int(clip_score1))

        param_val_n_matches_score_list = []
        for (param, n_matches_by_val) in n_matches_by_param_by_val.items():
            for (val, n_matches) in n_matches_by_val.items():
                score = score_by_param_by_val[param][val]
                param_val_n_matches_score_list.append(
                    [param, val, n_matches, score])
        param_val_n_matches_score_list.sort()

        for param_val_n_matches_score in param_val_n_matches_score_list:
            print(
                " ".join(str(entry) for entry in param_val_n_matches_score),
                file=recognition_rate_for_matches_file)


if __name__ == "__main__":
    main()
