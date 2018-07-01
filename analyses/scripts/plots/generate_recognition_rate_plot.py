"""
Generates a plot for recognition rate analyses
"""

from argparse import ArgumentParser
import matplotlib
matplotlib.use('TkAgg')
from itertools import groupby
import matplotlib.pyplot as plt
import numpy as np
from os import path


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--inputfilepath",
        help="path to the input file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--outputplotpath",
        help="path to the output plot (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if not path.exists(args.inputfilepath):
        print(f"Error: {args.inputfilepath} does not exist")
        exit(1)

    if path.exists(args.outputplotpath):
        print(f"Error: {args.outputplotpath} already exists")
        exit(1)

    with open(args.inputfilepath) as input_file:
        figure, axis = plt.subplots()
        figure.suptitle("Matcher recognition rate", fontsize=12, y=0.05)
        figure.tight_layout(pad=4.0, w_pad=4.0, h_pad=4.0)
        axis.set_xlabel("Relative noise level")
        axis.set_ylabel("Recognition rate in %")

        input_file_contents = list(
            map(lambda line: line.strip().split(" "), input_file.readlines()))
        legends = []
        xs = sorted(set(map(lambda line: int(line[1]), input_file_contents)))
        yss = groupby(input_file_contents, key=lambda line: line[0])
        for (clip_length, ys) in yss:
            legends.append("Clip Length = " + str(clip_length))
            ysi = list(
                map(
                    lambda clip_length_noise_recognition_rate: clip_length_noise_recognition_rate[2],
                    sorted(
                        ys,
                        key=
                        lambda clip_length_noise_recognition_rate: int(clip_length_noise_recognition_rate[1]),
                    )))
            plt.plot(xs, ysi)

        plt.legend(legends, loc="best", fancybox=True, framealpha=0)
        plt.savefig(args.outputplotpath, transparent=True)


if __name__ == "__main__":
    main()
