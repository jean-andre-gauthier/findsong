"""
Generates miscellaneous plots used in the blog post
"""
from argparse import ArgumentParser
from os import path
import matplotlib
matplotlib.use('TkAgg')
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
    args = parser.parse_args()

    create_digitization_plot(args.digitizationplotfilename)
    create_fft_plot(args.fftplotfilename)


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

    plt.savefig(digitization_plot_filename, dpi=300)


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

    plt.savefig(fft_plot_filename, dpi=300)


if __name__ == "__main__":
    main()
