"""
Generates a plot for performance analyses
"""

from argparse import ArgumentParser
from os import path
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--inputfilepath",
        help="path to the input file (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--outputplotindexerpath",
        help="path to the indexer output plot (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--outputplotmatchespath",
        help="path to the matches output plot (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if not path.exists(args.inputfilepath):
        print(f"Error: {args.inputfilepath} does not exist")
        exit(1)

    if path.exists(args.outputplotindexerpath):
        print(f"Error: {args.outputplotindexerpath} already exists")
        exit(1)

    if path.exists(args.outputplotmatchespath):
        print(f"Error: {args.outputplotmatchespath} already exists")
        exit(1)

    with open(args.inputfilepath) as input_file:
        input_file_contents = list(
            map(lambda line: line.strip().split(" "), input_file.readlines()))
        plt_index_sizes = list(map(lambda line: line[0], input_file_contents))
        plt_index_fingerprints = list(
            map(lambda line: line[1], input_file_contents))
        plt_indexer_durations = list(
            map(lambda line: line[2], input_file_contents))
        plt_matcher_durations_averages = list(
            map(lambda line: np.average(np.array(line[3:]).astype(np.int)),
                input_file_contents))

        make_plot(plt_index_sizes, "Index size (# songs)",
                  plt_index_fingerprints, "# Fingerprints",
                  plt_indexer_durations, "Indexer duration (ms)",
                  "Indexer Performance", args.outputplotindexerpath)
        make_plot(plt_index_sizes, "Index size (# songs)",
                  plt_index_fingerprints, "# Fingerprints",
                  plt_matcher_durations_averages,
                  "Matcher average duration (ms)", "Matcher Performance",
                  args.outputplotmatchespath)


def align_yaxis(ax1, v1, ax2, v2):
    _, y1 = ax1.transData.transform((0, v1))
    _, y2 = ax2.transData.transform((0, v2))
    adjust_yaxis(ax2, (y1 - y2) / 2, v2)
    adjust_yaxis(ax1, (y2 - y1) / 2, v1)


def adjust_yaxis(ax, ydif, v):
    inv = ax.transData.inverted()
    _, dy = inv.transform((0, 0)) - inv.transform((0, ydif))
    miny, maxy = ax.get_ylim()
    miny, maxy = miny - v, maxy - v
    if -miny > maxy or (-miny == maxy and dy > 0):
        nminy = miny
        nmaxy = miny * (maxy + dy) / (miny + dy)
    else:
        nmaxy = maxy
        nminy = maxy * (miny + dy) / (maxy + dy)
    ax.set_ylim(nminy + v, nmaxy + v)


def make_plot(xs, xs_label, ys1, ys_label1, ys2, ys_label2, title, file_name):
    figure, axis1 = plt.subplots()
    axis1.set_xlabel(xs_label)
    axis1.set_ylabel(ys_label1, color="red")
    axis1.tick_params(axis='y', labelcolor="red")
    handle1, = plt.plot(xs, ys1, "r--", label=ys_label1)
    ticks = [tick for tick in plt.gca().get_yticks() if tick >= 0]
    plt.gca().set_yticks(ticks)

    axis2 = axis1.twinx()
    axis2.set_ylabel(ys_label2, color="blue")
    axis2.tick_params(axis='y', labelcolor="blue")
    handle2, = plt.plot(xs, ys2, "b--", label=ys_label2)

    align_yaxis(axis1, 2700, axis2, 5000)

    figure.tight_layout(pad=3.0, w_pad=3.0, h_pad=3.0)
    figure.suptitle(title, fontsize=12, y=0.05)
    plt.legend(handles=[handle1, handle2], loc=1)

    plt.savefig(file_name, transparent=True)


if __name__ == "__main__":
    main()
