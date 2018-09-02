"""
Generates a plot for parameter analyses
WARNING: contains hardcoded values (taken from analyses/data/findsong_parameters/recognition_rate_for_matches)
"""

from argparse import ArgumentParser
import matplotlib
matplotlib.use("Agg")
from itertools import groupby
import matplotlib.pyplot as plt
import numpy as np
from os import path


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--parametersplotpath",
        help="path to the audio filter output plot (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if path.exists(args.parametersplotpath):
        print(f"Error: {args.parametersplotpath} already exists")
        exit(1)

    create_parameters_plot(args.parametersplotpath)


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


def create_parameters_plot(parameters_plot_path):
    plt.figure(0, figsize=(7.5, 15))
    axes = [
        plt.subplot2grid((4, 2), (0, 0)),
        plt.subplot2grid((4, 2), (0, 1)),
        plt.subplot2grid((4, 2), (1, 0)),
        plt.subplot2grid((4, 2), (1, 1)),
        plt.subplot2grid((4, 2), (2, 0)),
        plt.subplot2grid((4, 2), (2, 1)),
        plt.subplot2grid((4, 2), (3, 0))
    ]
    plt.suptitle(
        "Matcher peformance with varying indexer parameters",
        fontsize=12,
        y=0.02)
    plt.tight_layout(pad=6.0, w_pad=6.0, h_pad=6.0)

    xs_fanout = [1, 2, 4, 8, 16]
    ys1_fanout = [97, 97, 97, 97, 97]
    ys2_fanout = [80.66, 81.85, 97.82, 99.54, 99.68]
    create_plot(xs_fanout, "--fanout", ys1_fanout, "Recognition rate in %",
                ys2_fanout, "Average match score", axes[0])

    xs_peakDeltaF = np.array([1, 5, 10, 20, 50, 100])
    ys1_peakDeltaF = np.array([97, 97, 97, 97, 96, 92])
    ys2_peakDeltaF = np.array([96.54, 95.38, 92.84, 86.95, 71.5, 56.1])
    create_plot(xs_peakDeltaF, "--peakDeltaF", ys1_peakDeltaF,
                "Recognition rate in %", ys2_peakDeltaF, "Average match score",
                axes[1])

    xs_peakDeltaT = np.array([1, 5, 10, 20, 50, 100])
    ys1_peakDeltaT = np.array([97, 97, 97, 97, 93, 66])
    ys2_peakDeltaT = np.array([98.56, 94.89, 90.47, 80.63, 49.66, 24.24])
    create_plot(xs_peakDeltaT, "--peakDeltaT", ys1_peakDeltaT,
                "Recognition rate in %", ys2_peakDeltaT, "Average match score",
                axes[2])

    xs_peaksPerChunk = np.array([1, 2, 4, 8, 16, 32])
    ys1_peaksPerChunk = np.array([97, 97, 97, 97, 97, 97])
    ys2_peaksPerChunk = np.array([86.73, 95.86, 99.39, 99.88, 99.96, 99.97])
    create_plot(xs_peaksPerChunk, "--peaksPerChunk", ys1_peaksPerChunk,
                "Recognition rate in %", ys2_peaksPerChunk,
                "Average match score", axes[3])

    xs_windowDeltaF = np.array([1, 5, 10, 20, 50, 100])
    ys1_windowDeltaF = np.array([95, 97, 97, 97, 97, 97])
    ys2_windowDeltaF = np.array([85.71, 92.08, 94.84, 95.71, 96.14, 96.35])
    create_plot(xs_windowDeltaF, "--windowDeltaF", ys1_windowDeltaF,
                "Recognition rate in %", ys2_windowDeltaF,
                "Average match score", axes[4])

    xs_windowDeltaT = np.array([20, 50, 100, 200, 500])
    ys1_windowDeltaT = np.array([97, 97, 97, 97, 91])
    ys2_windowDeltaT = np.array([95.54, 96.14, 94.56, 86.24, 72.09])
    create_plot(xs_windowDeltaT, "--windowDeltaT", ys1_windowDeltaT,
                "Recognition rate in %", ys2_windowDeltaT,
                "Average match score", axes[5])

    xs_windowDeltaTi = np.array([1, 5, 10, 15, 20, 25, 30])
    ys1_windowDeltaTi = np.array([97, 97, 97, 97, 97, 97, 97])
    ys2_windowDeltaTi = np.array(
        [96.18, 95.86, 95.75, 95.24, 94.89, 93.72, 88.74])
    create_plot(xs_windowDeltaTi, "--windowDeltaTi", ys1_windowDeltaTi,
                "Recognition rate in %", ys2_windowDeltaTi,
                "Average match score", axes[6])
    plt.savefig(parameters_plot_path, transparent=True)


def create_plot(xs, xs_label, ys1, ys_label1, ys2, ys_label2, axis):
    axis.set_xlabel(xs_label)
    axis.set_ylabel(ys_label1, color="red")
    axis.tick_params(axis='y', labelcolor="red")
    handle1, = axis.plot(xs, ys1, "r--", label=ys_label1)
    ticks = [tick for tick in plt.gca().get_yticks() if tick >= 0]
    plt.gca().set_yticks(ticks)

    axis2 = axis.twinx()
    axis2.set_ylabel(ys_label2, color="blue")
    axis2.tick_params(axis='y', labelcolor="blue")
    handle2, = axis2.plot(xs, ys2, "b--", label=ys_label2)


if __name__ == "__main__":
    main()
