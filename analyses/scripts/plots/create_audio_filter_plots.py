"""
Generates a plot for audio filter analyses

WARNING: contains hardcoded values (taken from analyses/data/audio_filters/recognition_rate_for_matches)
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
        "--audiofilterplotpath",
        help="path to the audio filter output plot (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--pitchplotpath",
        help="path to the pitch output plot (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--tempoplotpath",
        help="path to the tempo output plot (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    if path.exists(args.audiofilterplotpath):
        print(f"Error: {args.audiofilterplotpath} already exists")
        exit(1)

    if path.exists(args.pitchplotpath):
        print(f"Error: {args.pitchplotpath} already exists")
        exit(1)

    if path.exists(args.tempoplotpath):
        print(f"Error: {args.tempoplotpath} already exists")
        exit(1)

    create_audio_filter_plot(args.audiofilterplotpath)
    create_pitch_plot(args.pitchplotpath)
    create_tempo_plot(args.tempoplotpath)


def create_audio_filter_plot(audio_filter_plot_path):
    plt.figure(0, figsize=(5, 7.5))
    axes = [plt.subplot2grid((2, 1), (0, 0)), plt.subplot2grid((2, 1), (1, 0))]
    plt.suptitle(
        "Matcher peformance with distorted audio", fontsize=12, y=0.05)
    plt.tight_layout(pad=4.0, w_pad=4.0, h_pad=4.0)

    indices = np.arange(1, 8)
    labels = np.array([
        "aecho", "aphaser", "chorus", "clean", "flanger", "highpass", "lowpass"
    ])
    values = np.array([97.55, 97.91, 98.05, 99.36, 97.81, 97.88, 99.21])
    aecho, aphaser, chorus, clean, flanger, highpass, lowpass = axes[0].bar(
        indices, values)
    axes[0].set_xticks(indices)
    axes[0].set_xticklabels(labels, rotation=45)
    axes[0].set_ylim([95, 100])
    axes[0].set_ylabel("Recognition rate in %")

    cell_text = np.array([["aecho", "0.8:0.8:1000:0.8"], [
        "aphaser", "delay=5.0:speed=2.0"
    ], ["chorus", "0.7:0.9:55:0.4:0.25:2"], ["clean", "-"],
                          ["flanger", "delay=20:depth=5:regen=10:speed=2"],
                          ["highpass", "f=440"], ["lowpass", "f=440"]])
    col_labels = np.array(["filter name", "filter parameter"])
    axes[1].xaxis.set_visible(False)
    axes[1].yaxis.set_visible(False)
    table = axes[1].table(
        cellText=cell_text,
        colLabels=col_labels,
        alpha=0.0,
        bbox=None,
        colLoc="center",
        cellLoc="center",
        loc="center",
        rasterized=False,
        rowLoc="center")
    table.auto_set_font_size(False)
    table.set_fontsize(6)
    table.scale(1, 1.75)

    for (line, col), cell in table.get_celld().items():
        if line == 0:
            cell._text.set_weight("bold")
        cell.set_linewidth(0)
        cell.set_fill(False)

    plt.savefig(audio_filter_plot_path, transparent=True)


def create_pitch_plot(pitch_plot_path):
    xs = np.arange(1, 7)
    ys1 = np.array([41, 12, 5, 2, 10, 1])
    ys2 = np.array([38.29, 24.33, 20.4, 15, 16.3, 13])
    create_plot(xs, "Pitch shift (halftones)", ys1, "Recognition rate in %",
                ys2, "Average match score",
                "Matcher performance with pitch shift", pitch_plot_path)


def create_tempo_plot(tempo_plot_path):
    xs = np.array([2.5, 5, 7.5, 10, 12.5, 15])
    ys1 = np.array([97, 95, 73, 54, 49, 36])
    ys2 = np.array([76.26, 39.14, 26.93, 23.74, 21.24, 20.28])
    create_plot(xs, "Tempo increase (percent)", ys1, "Recognition rate in %",
                ys2, "Average match score",
                "Matcher performance with tempo increase", tempo_plot_path)


def create_plot(xs, xs_label, ys1, ys_label1, ys2, ys_label2, title,
                file_name):
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

    figure.tight_layout(pad=3.0, w_pad=3.0, h_pad=3.0)
    figure.suptitle(title, fontsize=12, y=0.05)
    plt.legend(handles=[handle1, handle2], loc=1)

    plt.savefig(file_name, transparent=True)


if __name__ == "__main__":
    main()
