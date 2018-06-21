"""
Adds audio filters to a clip
"""

from argparse import ArgumentParser
import subprocess
from glob import glob
from os import makedirs, path


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--audiofilternames",
        help="audio filter names (str list)",
        nargs='+',
        required=True)
    parser.add_argument(
        "--audiofilterparameters",
        help="audio filter parameters (str list)",
        nargs='+',
        required=True)
    parser.add_argument(
        "--inputfilesglob",
        help="clips to which to add effects (glob)",
        required=True,
        type=str)
    parser.add_argument(
        "--outputdirectorypath",
        help="directory where to save the processed clips (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    audio_filter_names = args.audiofilternames
    audio_filter_parameters = args.audiofilterparameters
    if len(audio_filter_names) != len(audio_filter_parameters):
        print(f"Error: mismatching length for --audiofilternames" +
              f" {len(audio_filter_names)} and --audiofilterparameters" +
              f" {len(audio_filter_parameters)}")
        exit(1)

    input_filenames = glob(args.inputfilesglob)
    for filter_index, filter_name in enumerate(audio_filter_names):
        audio_filter_parameter = audio_filter_parameters[filter_index]
        output_directory = path.join(args.outputdirectorypath, filter_name,
                                     str(filter_index))
        if path.exists(output_directory):
            print(f"Error: {output_directory} already exists")
            exit(1)
        makedirs(output_directory, exist_ok=True)

        for input_filename in input_filenames:
            input_file_basename = path.basename(input_filename)
            output_filename = path.join(output_directory, input_file_basename)

            if filter_name == "rubberband":
                input_filename_without_extension, _ = path.splitext(
                    input_file_basename)
                intermediary_filename_in = path.join(
                    output_directory,
                    input_filename_without_extension + ".in.wav")
                ffmpeg_command_in = [
                    "ffmpeg", "-i", input_filename, intermediary_filename_in
                ]
                ffmpeg_process_in = subprocess.run(
                    ffmpeg_command_in, stdout=subprocess.PIPE)
                if ffmpeg_process_in.returncode != 0:
                    print(f"Error: ffmpeg returned" +
                          f" {ffmpeg_process_in.returncode} for" +
                          f" {ffmpeg_command_in}")
                    exit(1)

                intermediary_filename_out = path.join(
                    output_directory,
                    input_filename_without_extension + ".out.wav")
                rubberband_command = [
                    "rubberband", audio_filter_parameter,
                    intermediary_filename_in, intermediary_filename_out
                ]
                rubberband_process = subprocess.run(
                    rubberband_command, stdout=subprocess.PIPE)
                if rubberband_process.returncode != 0:
                    print(f"Error: ffmpeg returned" +
                          f" {rubberband_process.returncode} for" +
                          f" {rubberband_command}")
                    exit(1)

                ffmpeg_command_out = [
                    "ffmpeg", "-i", intermediary_filename_out, output_filename
                ]
                ffmpeg_process_out = subprocess.run(
                    ffmpeg_command_out, stdout=subprocess.PIPE)
                if ffmpeg_process_out.returncode != 0:
                    print(f"Error: ffmpeg returned" +
                          f" {ffmpeg_process_out.returncode} for" +
                          f" {ffmpeg_command_out}")
                    exit(1)

            else:
                ffmpeg_command = [
                    "ffmpeg", "-i", input_filename, "-af",
                    f"{filter_name}={audio_filter_parameter}", output_filename
                ]
                ffmpeg_process = subprocess.run(
                    ffmpeg_command, stdout=subprocess.PIPE)
                if ffmpeg_process.returncode != 0:
                    print(f"Error: ffmpeg returned {ffmpeg_process.returncode}"
                          + f" for {ffmpeg_process}")
                    exit(1)


if __name__ == "__main__":
    main()
