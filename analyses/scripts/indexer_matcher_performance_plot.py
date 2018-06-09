#
# Generates a plot for performance analyses
#

import argparse
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt


def main():
    parser = argparse.ArgumentParser()
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

    with open(args.inputfilepath) as inputFile:
        inputFileContents = list(
            map(lambda line: line.strip().split(" "), inputFile.readlines()))
        pltIndexSizes = list(map(lambda line: line[0], inputFileContents))
        pltIndexFingerprints = list(
            map(lambda line: line[1], inputFileContents))
        pltIndexerDurations = list(
            map(lambda line: line[2], inputFileContents))
        pltMatcherDurationsAverages = list(
            map(lambda line: round(float(line[3])), inputFileContents))

        makePlot(pltIndexSizes, "Index size (# songs)", pltIndexFingerprints,
                 "# Fingerprints", pltIndexerDurations,
                 "Indexer duration (ms)", args.outputplotindexerpath)
        makePlot(pltIndexSizes, "Index size (# songs)", pltIndexFingerprints,
                 "# Fingerprints", pltMatcherDurationsAverages,
                 "Matcher average duration (ms)", args.outputplotmatchespath)


def makePlot(xs, xsLabel, ys1, ysLabel1, ys2, ysLabel2, fileName):
    figure, axis1 = plt.subplots()
    axis1.set_xlabel(xsLabel)
    axis1.set_ylabel(ysLabel1, color="red")
    axis1.tick_params(axis='y', labelcolor="red")
    handle1, = plt.plot(xs, ys1, "r--", label=ysLabel1)

    axis2 = axis1.twinx()
    axis2.set_ylabel(ysLabel2, color="blue")
    axis2.tick_params(axis='y', labelcolor="blue")
    handle2, = plt.plot(xs, ys2, "b--", label=ysLabel2)

    figure.tight_layout()
    plt.legend(handles=[handle1, handle2], loc=1)

    plt.savefig(fileName)


if __name__ == "__main__":
    main()
