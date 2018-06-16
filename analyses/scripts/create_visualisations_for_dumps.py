"""
Helper script that creates visualisations for findsong's debug info
"""
from argparse import ArgumentParser
from itertools import groupby, islice
from os import listdir
from os.path import isfile, join
import re
from matplotlib import collections as mc, colors, pyplot as plt, rc
import numpy as np
from scipy.io.wavfile import write


def main():
    parser = ArgumentParser()
    parser.add_argument(
        "--signalwav",
        action="store_true",
        help="generate wav file for signal")
    parser.add_argument(
        "--signalplot", action="store_true", help="generate plots for signal")
    parser.add_argument(
        "--spectrogramplot",
        action="store_true",
        help="generate plots for spectrogram")
    parser.add_argument(
        "--constellationmapplot",
        action="store_true",
        help="generate plots for constellation map")
    parser.add_argument(
        "--peakpairsplot",
        action="store_true",
        help="generate plots for peak pairs")
    parser.add_argument(
        "--songoffsetsplot",
        action="store_true",
        help="generate plots for song offsets")
    args = parser.parse_args()

    rc("font", family="Arial")

    folder = "."
    max_time = 80
    sample_rate = 8000
    width, height = 90, 10

    if args.signalwav:
        n_output_samples = 80000
        signal_pattern = (r"(signal-to-peak-pairs-signal.*|" +
                          r"signal-to-matches-signal-microphone)\.txt$")
        signal_filenames = filenames(folder, signal_pattern)
        write_signal_wav(signal_filenames, n_output_samples, sample_rate)

    if args.signalplot:
        n_shown_samples = 8000
        signal_pattern = (r"(signal-to-peak-pairs-signal.*|" +
                          r"signal-to-matches-signal-microphone)\.txt$")
        signal_filenames = filenames(folder, signal_pattern)
        write_signal_plot(signal_filenames, n_shown_samples, sample_rate,
                          width, height)

    if args.spectrogramplot:
        spectrogram_pattern = (
            r"(signal-to-peak-pairs-spectrogram.*|" +
            r"signal-to-matches-spectrogram-microphone)\.txt$")
        spectrogram_filenames = filenames(folder, spectrogram_pattern)
        width, height = 10, 60
        write_spectrogram_plot(spectrogram_filenames, max_time, width, height)

    if args.constellationmapplot:
        constellation_map_pattern = (
            r"(signal-to-peak-pairs-constellation-map.*|" +
            r"signal-to-matches-constellation-map-microphone)\.txt$")
        constellation_map_filenames = filenames(folder,
                                                constellation_map_pattern)
        write_constellation_map_plot(constellation_map_filenames, max_time,
                                     width, height)

    if args.peakpairsplot:
        peak_pairs_pattern = (
            r"(signal-to-peak-pairs-peak-pairs.*|" +
            r"signal-to-peak-pairs-peak-pairs-microphone)\.txt$")
        peak_pairs_filenames = filenames(folder, peak_pairs_pattern)
        write_peak_pairs_plot(peak_pairs_filenames, max_time, width, height)

    if args.songoffsetsplot:
        song_offsets_pattern = (
            r"signal-to-matches-song-offsets-" + r"microphone.txt$")
        song_offsets_filenames = filenames(folder, song_offsets_pattern)
        write_song_offsets_plot(song_offsets_filenames, width, height)


def filenames(folder, pattern):
    """
    Returns a list of filenames that correspond to the given pattern
    """
    return [
        f for f in listdir(folder)
        if isfile(join(folder, f)) and re.match(pattern, f)
    ]


def write_signal_wav(input_filenames, n_output_samples, sample_rate):
    """
    Converts sample files to wav files
    """
    for signal_filename in input_filenames:
        with open(signal_filename) as signal_file:
            signal = list(map(int, islice(signal_file, n_output_samples)))
            scaled_signal = np.int8(
                signal / np.max(np.abs(signal)).astype(float) * 65535)
            scaled_signal.byteswap(True)
            write(signal_filename + ".wav", sample_rate, scaled_signal)


def write_signal_plot(input_filenames, n_shown_samples, sample_rate, width,
                      height):
    """
    Visualises sample files with a signal plot
    """
    for signal_filename in input_filenames:
        with open(signal_filename) as signal_file:
            signal = list(map(int, islice(signal_file, n_shown_samples)))
            time = np.linspace(0, len(signal) / sample_rate, num=len(signal))
            plt.figure(1, figsize=(width, height))
            plt.title("Signal (sample rate 8000Hz)", fontsize=36)
            plt.xlabel("Time (seconds)", fontsize=18)
            plt.ylabel("Amplitude (8 bits unsigned)", fontsize=18)
            plt.plot(time, signal, color="red")
            plt.savefig(signal_filename + ".png")


def write_spectrogram_plot(spectrogram_filenames, max_time, width, height):
    """
    Creates a heatmap for spectrogram files
    """
    for spectrogram_filename in spectrogram_filenames:
        spectrogram = np.loadtxt(spectrogram_filename)[
            0:max_time, :].transpose()
        normalize = colors.Normalize(0, max(map(max, spectrogram)))
        plt.figure(1, figsize=(width, height))
        plt.title("Spectrogram", fontsize=36)
        plt.xlabel("Time (chunk index)", fontsize=18)
        plt.ylabel("Frequency (range)", fontsize=18)
        plt.imshow(
            spectrogram,
            aspect="auto",
            cmap="Reds",
            interpolation="nearest",
            norm=normalize)
        plt.savefig(spectrogram_filename + ".png")


def write_constellation_map_plot(constellation_map_filenames, max_time, width,
                                 height):
    """
    Creates a scatter plot for constellation map files
    """
    for constellation_map_filename in constellation_map_filenames:
        with open(constellation_map_filename) as constellation_map_file:
            peaks = list(
                filter(
                    lambda ft: ft[1] <= max_time,
                    map(lambda tf: [float(n) for n in reversed(tf.split(" "))],
                        islice(constellation_map_file, 1, None))))
            plt.figure(1, figsize=(width, height))
            plt.title("Constellation map", fontsize=36)
            plt.xlabel("Time (chunk index)", fontsize=18)
            plt.ylabel("Frequency (range)", fontsize=18)
            x, y = zip(*peaks)
            plt.scatter(x, y)
            plt.savefig(constellation_map_filename + ".png")


def write_peak_pairs_plot(peak_pairs_filenames, max_time, width, height):
    """
    Visualises peak pairs in a line plot
    """
    for peak_pairs_filename in peak_pairs_filenames:
        with open(peak_pairs_filename) as peak_pairs_file:

            def max_time_filter(f1t1f2t2):
                return int(f1t1f2t2[0][1]) <= max_time and int(
                    f1t1f2t2[1][1]) <= max_time,

            def frequency_time_mapper(f1t1f2t2):
                return [(f1t1f2t2[0], f1t1f2t2[1]), (f1t1f2t2[2], f1t1f2t2[3])]

            peak_pairs = list(
                filter(
                    max_time_filter,
                    map(frequency_time_mapper, [
                        l.split(" ") for l in islice(peak_pairs_file, 1, None)
                    ])))
            peak_pairs_lines = mc.LineCollection(peak_pairs)
            fig, ax = plt.subplots()
            fig.set_size_inches(width, height)
            ax.add_collection(peak_pairs_lines)
            ax.autoscale()
            plt.title("Peak pairs", fontsize=36)
            plt.xlabel("Time (chunk index)", fontsize=18)
            plt.ylabel("Frequency (Hz)", fontsize=18)
            plt.savefig(peak_pairs_filename + ".png")


def write_song_offsets_plot(song_offsets_filenames, width, height):
    """
    Creates histograms for song offset files
    """
    for song_offsets_filename in song_offsets_filenames:
        with open(song_offsets_filename) as song_offsets_file:
            songname_offsets = groupby(
                [l.rsplit(" ", 1) for l in islice(song_offsets_file, 1, None)],
                lambda songnameOffset: songnameOffset[0])
            plt.figure(1, figsize=(width, height))
            plt.title("Song offsets histogram", fontsize=36)
            songname_offsets_dict = {}
            for songname, offsets in songname_offsets:
                songname_offsets_dict[songname] = list(
                    map(lambda songnameOffset: int(songnameOffset[1]),
                        list(offsets)))
            subplot_index = 1
            for songname, offsets in songname_offsets_dict.items():
                plt.subplot(1, len(songname_offsets_dict), subplot_index)
                offsets_list = [int(o) for o in list(offsets)]
                plt.hist(offsets_list, max(offsets_list))
                plt.title(songname)
                plt.xlabel("Offset (chunk index)", fontsize=18)
                plt.ylabel("# occurrences", fontsize=18)
                subplot_index += 1
            plt.savefig(song_offsets_filename + ".png")


if __name__ == '__main__':
    main()
