"""
Generates miscellaneous plots used in the blog post
"""
from argparse import ArgumentParser
from os import path
import matplotlib
matplotlib.use('TkAgg')
from matplotlib import collections as mc, colors, pyplot as plt, rc
import matplotlib.pyplot as plt
import numpy as np


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--digitizationplotfilename",
        help="Digitisation plot (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--fftplotfilename", help="FFT plot (path)", required=True, type=str)
    parser.add_argument(
        "--indexerwalkthroughplotfilename",
        help="Indexer walkthrough plot (path)",
        required=True,
        type=str)
    parser.add_argument(
        "--matcherwalkthroughplotfilename",
        help="Matcher walkthrough plot (path)",
        required=True,
        type=str)
    args = parser.parse_args()

    create_digitization_plot(args.digitizationplotfilename)
    create_fft_plot(args.fftplotfilename)
    create_indexer_walkthrough_plot(args.indexerwalkthroughplotfilename)
    create_matcher_walkthrough_plot(args.matcherwalkthroughplotfilename)


def create_constellation_map_plot(axis, peaks_x, peaks_y, min_peaks_x,
                                  max_peaks_x, min_peaks_y, max_peaks_y):
    axis.set_title(
        "Constellation Map (Δf: 1, Δt: 2, peaks/chunk: 2)", fontsize=10)
    axis.set_xlabel("Frequency", fontsize=8)
    axis.set_ylabel("Time", fontsize=8)
    axis.axis([-1, max_peaks_x + 1, max_peaks_y + 1, -1])
    axis.set_xticks(np.arange(min_peaks_x, max_peaks_x + 1, 1.0))
    axis.set_yticks(np.arange(min_peaks_y, max_peaks_y + 1, 1.0))
    axis.scatter(peaks_x, peaks_y)


def create_digitization_plot(digitization_plot_filename):
    sample_count_continuous = 512
    sampling_rate_continuous = 128
    sample_time_continuous = 1.0 / sampling_rate_continuous
    time_continuous = np.arange(
        sample_count_continuous) * sample_time_continuous
    signal_continuous = np.sin(2 * np.pi * time_continuous)

    sample_count_sampled = 128
    sampling_rate_sampled = 32
    sample_time_sampled = 1.0 / sampling_rate_sampled
    time_sampled = np.arange(sample_count_sampled) * sample_time_sampled
    signal_sampled = np.sin(2 * np.pi * time_sampled)

    sample_count_quantized = 128
    sampling_rate_quantized = 32
    sample_time_quantized = 1.0 / sampling_rate_quantized
    time_quantized = np.arange(sample_count_quantized) * sample_time_quantized
    signal_quantized = (np.digitize(
        signal_sampled, [-1, -0.75, -0.5, -0.25, 0, 0.25, 0.5, 0.75, 1]) -
                        1) / 4.0 - 1

    figure, axes = plt.subplots(3, 1)
    figure.suptitle("Digitisation", fontsize=12, y=0.05)
    figure.tight_layout(pad=3.0, w_pad=3.0, h_pad=3.0)

    # Continuous signal
    axes[0].set_title("Continuous Signal", fontsize=10)
    axes[0].plot(time_continuous, signal_continuous, "r")

    # Sampled signal
    axes[1].set_title("Sampled Signal", fontsize=10)
    axes[1].plot(time_sampled, signal_sampled, "r.")

    # Quantised signal
    axes[2].set_title("Quantised Signal", fontsize=10)
    axes[2].plot(time_quantized, signal_quantized, "r.")

    plt.savefig(digitization_plot_filename, transparent=True)


def create_fft_plot(fft_plot_filename):
    sample_count = 192
    sampling_rate = 48
    sample_time = 1.0 / sampling_rate
    time = np.arange(sample_count) * sample_time
    frequency = np.arange(sample_count / 2) * sampling_rate / sample_count

    signal = (np.sin(2 * np.pi * time) + np.sin(2 * np.pi * 2 * time) + np.sin(
        2 * np.pi * 3 * time) + np.sin(2 * np.pi * 4 * time))
    fft = [i / (sample_count / 2) for i in np.fft.fft(signal)]
    fft_half = fft[:int(sample_count / 2)]
    fft_abs = np.abs(fft_half)
    fft_abs_zero = fft_abs < 0.001
    fft_angle = np.angle(fft_half)
    fft_angle[fft_abs_zero] = 0
    figure, axes = plt.subplots(3, 1)
    figure.suptitle(
        "Signal: sin(2πt) + sin(2π2t) + sin(2π3t) + sin(2π4t)",
        fontsize=12,
        y=0.05)
    figure.tight_layout(pad=3.0, w_pad=3.0, h_pad=3.0)

    # Plot signal
    axes[0].set_title("Signal (time domain)", fontsize=10)
    axes[0].stem(time, signal, "r", basefmt=" ", markerfmt=" ")

    # Plot FFT abs
    axes[1].set_title("FFT (frequency domain - abs)", fontsize=10)
    axes[1].stem(frequency, fft_abs, "r", basefmt=" ", markerfmt=" ")

    # Plot FFT angle
    axes[2].set_title("FFT (frequency domain - angle)", fontsize=10)
    axes[2].stem(frequency, fft_angle, "r", basefmt=" ", markerfmt=" ")

    plt.savefig(fft_plot_filename, transparent=True)


def create_indexer_walkthrough_plot(indexer_walkthrough_filename):
    plt.figure(0, figsize=(10, 20))
    axes = [
        plt.subplot2grid((9, 1), (0, 0)),
        plt.subplot2grid((9, 1), (1, 0), rowspan=3),
        plt.subplot2grid((9, 1), (4, 0), rowspan=2),
        plt.subplot2grid((9, 1), (6, 0), rowspan=2),
        plt.subplot2grid((9, 1), (8, 0))
    ]
    plt.suptitle("Indexer Walkthrough", fontsize=18, y=0.02)
    plt.tight_layout(pad=3.0, w_pad=3.0, h_pad=4.0)

    # Plot signal
    time = np.arange(80)
    signal = np.array([
        0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17,
        10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3,
        0, 3, -10, -17, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0,
        1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0,
        -1
    ])
    create_signal_plot(axes[0], signal, time)

    # Plot spectrogram
    spectrogram = np.array(
        [[1, 20, 38, 40, 38, 20, 1, 1], [1, 20, 38, 40, 38, 20, 1, 1],
         [1, 20, 38, 40, 38, 20, 1, 1], [1, 20, 38, 40, 38, 20, 1,
                                         1], [22, 24, 24, 22, 24, 20, 11, 7],
         [0, 0, 0, 2, 4, 2, 0, 0], [0, 0, 0, 2, 4, 2, 0,
                                    0], [0, 0, 0, 2, 4, 2, 0,
                                         0], [0, 0, 0, 2, 4, 2, 0, 0]])
    create_spectrogram_plot(axes[1], spectrogram)

    # Plot constellation map
    peaks = np.array([[7.0, 0.0], [3.0, 0.0], [7.0, 1.0], [3.0, 1.0],
                      [3.0, 2.0], [3.0, 3.0], [0.0, 7.0], [4.0, 7.0],
                      [4.0, 8.0], [0.0, 8.0]])
    peaks_x, peaks_y = zip(*peaks)
    (min_peaks_x, max_peaks_x, min_peaks_y, max_peaks_y) = (min(peaks_x),
                                                            max(peaks_x),
                                                            min(peaks_y),
                                                            max(peaks_y))
    create_constellation_map_plot(axes[2], peaks_x, peaks_y, min_peaks_x,
                                  max_peaks_x, min_peaks_y, max_peaks_y)

    # Plot peak pairs
    peak_pairs = np.array([[(2.95, 0), (2.95, 2)], [(3.0, 0), (3.0, 3)],
                           [(3.05, 1), (3.05, 3)], [(3.0, 3), (4, 7)]])
    peak_pairs_x = np.array([2.95, 2.95, 3.0, 3.0, 3.05, 3.05, 3.0, 4])
    peak_pairs_y = np.array([0, 2, 0, 3, 1, 3, 3, 7])
    create_peak_pairs_plot(axes[3], peak_pairs, peak_pairs_x, peak_pairs_y,
                           min_peaks_x, max_peaks_x, min_peaks_y, max_peaks_y)

    # Plot song index
    colLabels = np.array(["f1", "f2", "t2-t1", "[(t1, Song), ...]"])
    cellText = np.array([[3, 3, 2, "[(0, Song 1), (1, Song 1)]"],
                         [3, 3, 3,
                          "[(1, Song 1)]"], [3, 4, 4, "[(3, Song 1)]"]])
    create_table_plot(axes[4], cellText, colLabels, "Song Index", 2)

    plt.savefig(indexer_walkthrough_filename, transparent=True)


def create_matcher_walkthrough_plot(matcher_walkthrough_filename):
    plt.figure(0, figsize=(10, 20))
    axes = [
        plt.subplot2grid((10, 1), (0, 0)),
        plt.subplot2grid((10, 1), (1, 0)),
        plt.subplot2grid((10, 1), (2, 0), rowspan=2),
        plt.subplot2grid((10, 1), (4, 0), rowspan=2),
        plt.subplot2grid((10, 1), (6, 0), rowspan=2),
        plt.subplot2grid((10, 1), (8, 0)),
        plt.subplot2grid((10, 1), (9, 0))
    ]
    plt.suptitle("Matcher Walkthrough", fontsize=18, y=0.02)
    plt.tight_layout(pad=3.0, w_pad=3.0, h_pad=4.0)

    # Plot song index
    colLabels = np.array(["f1", "f2", "t2-t1", "[(t1, Song), ...]"])
    cellText = np.array([[3, 3, 2, "[(0, Song 1), (0, Song 2), (1, Song 1)]"],
                         [3, 3, 3, "[(1, Song 1)]"],
                         [3, 4, 4, "[(2, Song 2), (3, Song 1)]"]])
    create_table_plot(axes[0], cellText, colLabels, "Song Index", 1.5)

    # Plot signal
    time = np.arange(40)
    signal = np.array([
        0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17,
        10, -3, 0, 3, -10, -17, 0, 17, 10, -3, 0, 3, -10, -17, 0, 17, 10, -3,
        0, 3, -10, -17
    ])
    create_signal_plot(axes[1], signal, time)

    # Plot spectrogram
    spectrogram = np.array(
        [[1, 20, 38, 40, 38, 20, 1, 1], [1, 20, 38, 40, 38, 20, 1, 1],
         [1, 20, 38, 40, 38, 20, 1, 1], [1, 20, 38, 40, 38, 20, 1, 1]])
    create_spectrogram_plot(axes[2], spectrogram)

    # Plot constellation map
    peaks = np.array([[7.0, 0.0], [3.0, 0.0], [7.0, 1.0], [3.0, 1.0],
                      [3.0, 2.0], [3.0, 3.0], [7.0, 3.0], [7.0, 2.0]])
    peaks_x, peaks_y = zip(*peaks)
    (min_peaks_x, max_peaks_x, min_peaks_y, max_peaks_y) = (min(peaks_x),
                                                            max(peaks_x),
                                                            min(peaks_y),
                                                            max(peaks_y))
    create_constellation_map_plot(axes[3], peaks_x, peaks_y, min_peaks_x,
                                  max_peaks_x, min_peaks_y, max_peaks_y)

    # Plot peak pairs
    peak_pairs = np.array([[(2.95, 0), (2.95, 2)], [(3, 0), (3, 3)],
                           [(3.05, 1), (3.05, 3)], [(6.9, 0), (6.9, 2)],
                           [(6.95, 0), (6.95, 3)], [(7, 1), (7, 3)]])
    peak_pairs_x = np.array(
        [2.95, 2.95, 3, 3, 3.05, 3.05, 6.9, 6.9, 6.95, 6.95, 7, 7])
    peak_pairs_y = np.array([0, 2, 0, 3, 1, 3, 0, 2, 0, 3, 1, 3])
    create_peak_pairs_plot(axes[4], peak_pairs, peak_pairs_x, peak_pairs_y,
                           min_peaks_x, max_peaks_x, min_peaks_y, max_peaks_y)

    # Plot song offsets
    colLabels = np.array(["Song", "[Offset, ...]"])
    cellText = np.array([["Song 1", "[0, 1, 0, 0]"], ["Song 2", "0"]])
    create_table_plot(axes[5], cellText, colLabels, "Song Offsets", 1.75)

    # Plot song matches
    colLabels = np.array(["Song", "Confidence"])
    cellText = np.array([["Song 1", "9.967"], ["Song 2", "3.332"]])
    create_table_plot(axes[6], cellText, colLabels, "Song Confidence", 1.75)

    plt.savefig(matcher_walkthrough_filename, transparent=True)


def create_peak_pairs_plot(axis, peak_pairs, peak_pairs_x, peak_pairs_y,
                           min_peaks_x, max_peaks_x, min_peaks_y, max_peaks_y):
    peak_pairs_lines = mc.LineCollection(peak_pairs)
    axis.add_collection(peak_pairs_lines)
    axis.scatter(peak_pairs_x, peak_pairs_y)
    axis.axis([-1, max_peaks_x + 1, max_peaks_y + 1, -1])
    axis.set_xticks(np.arange(min_peaks_x, max_peaks_x + 1, 1.0))
    axis.set_yticks(np.arange(min_peaks_y, max_peaks_y + 1, 1.0))
    axis.set_title("Peak Pairs (f: 3, Δf: 1, Δt: 4, Δti: 2)", fontsize=10)
    axis.set_xlabel("Frequency", fontsize=8)
    axis.set_ylabel("Time", fontsize=8)


def create_signal_plot(axis, signal, time):
    axis.set_title("Signal", fontsize=10)
    axis.set_xlabel("Time", fontsize=8)
    axis.set_ylabel("Amplitude", fontsize=8)
    axis.stem(time, signal, "r", basefmt=" ", markerfmt=" ")


def create_spectrogram_plot(axis, spectrogram):
    normalize = colors.Normalize(0, max(map(max, spectrogram)))
    axis.set_title("Spectrogram (chunk size: 16, chunk step: 8)", fontsize=10)
    axis.set_xlabel("Frequency", fontsize=8)
    axis.set_ylabel("Time", fontsize=8)
    for (j, i), value in np.ndenumerate(spectrogram):
        color = "white" if value > 25 else "black"
        axis.text(i, j, value, color=color, ha="center", va="center")
    axis.imshow(
        spectrogram,
        aspect="auto",
        cmap="Reds",
        interpolation="nearest",
        norm=normalize)


def create_table_plot(axis, cellText, colLabels, title, vscale):
    axis.xaxis.set_visible(False)
    axis.yaxis.set_visible(False)
    axis.set_title(title, fontsize=10)
    table = axis.table(
        cellText=cellText,
        colLabels=colLabels,
        alpha=0.0,
        bbox=None,
        colLoc="center",
        cellLoc="center",
        loc="center",
        rasterized=False,
        rowLoc="center")
    table.scale(1, vscale)

    for (line, col), cell in table.get_celld().items():
        if line == 0:
            cell._text.set_weight("bold")
        cell.set_linewidth(0)
        cell.set_fill(False)


if __name__ == "__main__":
    main()
