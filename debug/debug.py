import argparse
from itertools import groupby, islice
from matplotlib import collections as mc, colors, pyplot as plt, rc
import numpy as np
from os import listdir
from os.path import isfile, join
import pylab as pl
import re
from scipy.io.wavfile import write
import sys

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--signalwav", action="store_true", help="generate wav file for signal")
    parser.add_argument("--signalplot", action="store_true", help="generate plots for signal")
    parser.add_argument("--spectrogramplot", action="store_true", help="generate plots for spectrogram")
    parser.add_argument("--constellationmapplot", action="store_true", help="generate plots for constellation map")
    parser.add_argument("--peakpairsplot", action="store_true", help="generate plots for peak pairs")
    parser.add_argument("--songoffsetsplot", action="store_true", help="generate plots for song offsets")
    args = parser.parse_args()

    rc("font", family="Arial")

    folder = "."
    maxTime = 80
    sampleRate = 8000
    width, height = 90, 10

    if args.signalwav:
        nOutputSamples = 80000
        signalPattern = "(signal-to-peak-pairs-signal.*|signal-to-matches-signal-microphone)\.txt$"
        signalFilenames = filenames(folder, signalPattern)
        writeSignalWav(signalFilenames, nOutputSamples, sampleRate)

    if args.signalplot:
        nShownSamples = 8000
        signalPattern = "(signal-to-peak-pairs-signal.*|signal-to-matches-signal-microphone)\.txt$"
        signalFilenames = filenames(folder, signalPattern)
        writeSignalPlot(signalFilenames, nShownSamples, sampleRate, width, height)

    if args.spectrogramplot:
        spectrogramPattern = "(signal-to-peak-pairs-spectrogram.*|signal-to-matches-spectrogram-microphone)\.txt$"
        spectrogramFilenames = filenames(folder, spectrogramPattern)
        width, height = 10, 60
        writeSpectrogramPlot(spectrogramFilenames, maxTime, width, height)

    if args.constellationmapplot:
        constellationMapPattern = "(signal-to-peak-pairs-constellation-map.*|signal-to-matches-constellation-map-microphone)\.txt$"
        constellationMapFilenames = filenames(folder, constellationMapPattern)
        writeConstellationMapPlot(constellationMapFilenames, maxTime, width, height)

    if args.peakpairsplot:
        peakPairsPattern = "(signal-to-peak-pairs-peak-pairs.*|signal-to-peak-pairs-peak-pairs-microphone)\.txt$"
        peakPairsFilenames = filenames(folder, peakPairsPattern)
        writePeakPairsPlot(peakPairsFilenames, maxTime, width, height)

    if args.songoffsetsplot:
        songOffsetsPattern = "signal-to-matches-song-offsets-microphone.txt$"
        songOffsetsFilenames = filenames(folder, songOffsetsPattern)
        writeSongOffsetsPlot(songOffsetsFilenames, width, height)

def filenames(folder, pattern):
     return [f for f in listdir(folder) if isfile(join(folder, f)) and re.match(pattern, f)]

def writeSignalWav(inputFilenames, nOutputSamples, sampleRate):
    for signalFilename in inputFilenames:
        with open(signalFilename) as signalFile:
            signal = list(map(int, islice(signalFile, nOutputSamples)))
            scaledSignal = np.int8(signal/np.max(np.abs(signal)) * 257)
            scaledSignal.byteswap(True)
            write(signalFilename + ".wav", sampleRate, scaledSignal)

def writeSignalPlot(inputFilenames, nShownSamples, sampleRate, width, height):
    for signalFilename in inputFilenames:
        with open(signalFilename) as signalFile:
            signal = list(map(int, islice(signalFile, nShownSamples)))
            time = np.linspace(0, len(signal)/sampleRate, num=len(signal))
            plt.figure(1, figsize=(width, height))
            plt.title("Signal (sample rate 8000Hz)", fontsize=36)
            plt.xlabel("Time (seconds)", fontsize=18)
            plt.ylabel("Amplitude (8 bits unsigned)", fontsize=18)
            plt.plot(time, signal, color="red")
            plt.savefig(signalFilename + ".png")

def writeSpectrogramPlot(spectrogramFilenames, maxTime, width, height):
    for spectrogramFilename in spectrogramFilenames:
        spectrogram = np.loadtxt(spectrogramFilename)[0:maxTime,:].transpose()
        normalize = colors.Normalize(0, max(map(max, spectrogram)))
        plt.figure(1, figsize=(width, height))
        plt.title("Spectrogram", fontsize=36)
        plt.xlabel("Time (chunk index)", fontsize=18)
        plt.ylabel("Frequency (range)", fontsize=18)
        plt.imshow(spectrogram, aspect = "auto", cmap = "Reds", interpolation = "nearest", norm = normalize)
        plt.savefig(spectrogramFilename + ".png")

def writeConstellationMapPlot(constellationMapFilenames, maxTime, width, height):
    for constellationMapFilename in constellationMapFilenames:
        with open(constellationMapFilename) as constellationMapFile:
            peaks = list(
                filter(
                    lambda ft: ft[1] <= maxTime,
                    map(
                        lambda tf: [float(n) for n in reversed(tf.split(" "))],
                        islice(constellationMapFile, 1, None))))
            plt.figure(1, figsize=(width, height))
            plt.title("Constellation map", fontsize=36)
            plt.xlabel("Time (chunk index)", fontsize=18)
            plt.ylabel("Frequency (range)", fontsize=18)
            x, y = zip(*peaks)
            plt.scatter(x, y)
            plt.savefig(constellationMapFilename + ".png")

def writePeakPairsPlot(peakPairsFilenames, maxTime, width, height):
    for peakPairsFilename in peakPairsFilenames:
        with open(peakPairsFilename) as peakPairsFile:
            peakPairs = list(
                filter(
                    lambda f1t1f2t2: int(f1t1f2t2[0][1]) <= maxTime and int(f1t1f2t2[1][1]) <= maxTime,
                    map(
                        lambda f1t1f2t2: [(f1t1f2t2[0], f1t1f2t2[1]), (f1t1f2t2[2], f1t1f2t2[3])],
                        [l.split(" ") for l in islice(peakPairsFile, 1, None)])))
            peakPairsLines = mc.LineCollection(peakPairs)
            fig, ax = plt.subplots()
            fig.set_size_inches(width, height)
            ax.add_collection(peakPairsLines)
            ax.autoscale()
            plt.title("Peak pairs", fontsize=36)
            plt.xlabel("Time (chunk index)", fontsize=18)
            plt.ylabel("Frequency (Hz)", fontsize=18)
            plt.savefig(peakPairsFilename + ".png")

def writeSongOffsetsPlot(songOffsetsFilenames, width, height):
    for songOffsetsFilename in songOffsetsFilenames:
        with open(songOffsetsFilename) as songOffsetsFile:
            songnameOffsets = groupby(
                    [l.rsplit(" ", 1) for l in islice(songOffsetsFile, 1, None)],
                    lambda songnameOffset: songnameOffset[0]
                )
            plt.figure(1, figsize=(width, height))
            plt.title("Song offsets histogram", fontsize=36)
            songnameOffsetsDict = {}
            for songname, offsets in songnameOffsets:
                songnameOffsetsDict[songname] = list(map(
                        lambda songnameOffset: int(songnameOffset[1]),
                        list(offsets)))
            subplotIndex = 1
            for songname, offsets in songnameOffsetsDict.items():
                plt.subplot(1, len(songnameOffsetsDict), subplotIndex)
                offsetsList = [int(o) for o in list(offsets)]
                plt.hist(offsetsList, max(offsetsList))
                plt.title(songname)
                plt.xlabel("Offset (chunk index)", fontsize=18)
                plt.ylabel("# occurrences", fontsize=18)
                subplotIndex += 1
            plt.savefig(songOffsetsFilename + ".png")

if __name__ == '__main__':
    main()
