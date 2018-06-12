"""
Generates a plot for performance analyses
"""

from argparse import ArgumentParser
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt


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

    with open(args.inputfilepath) as input_file:
        input_file_contents = list(
            map(lambda line: line.strip().split(" "), input_file.readlines()))
        plt_index_sizes = list(map(lambda line: line[0], input_file_contents))
        plt_index_fingerprints = list(
            map(lambda line: line[1], input_file_contents))
        plt_indexer_durations = list(
            map(lambda line: line[2], input_file_contents))
        plt_matcher_durations_averages = list(
            map(lambda line: round(float(line[3])), input_file_contents))

        make_plot(plt_index_sizes, "Index size (# songs)",
                  plt_index_fingerprints, "# Fingerprints",
                  plt_indexer_durations, "Indexer duration (ms)",
                  args.outputplotindexerpath)
        make_plot(plt_index_sizes, "Index size (# songs)",
                  plt_index_fingerprints, "# Fingerprints",
                  plt_matcher_durations_averages,
                  "Matcher average duration (ms)", args.outputplotmatchespath)


def make_plot(xs, xs_label, ys1, ys_label1, ys2, ys_label2, file_name):
    figure, axis1 = plt.subplots()
    axis1.set_xlabel(xs_label)
    axis1.set_ylabel(ys_label1, color="red")
    axis1.tick_params(axis='y', labelcolor="red")
    handle1, = plt.plot(xs, ys1, "r--", label=ys_label1)

    axis2 = axis1.twinx()
    axis2.set_ylabel(ys_label2, color="blue")
    axis2.tick_params(axis='y', labelcolor="blue")
    handle2, = plt.plot(xs, ys2, "b--", label=ys_label2)

    figure.tight_layout()
    plt.legend(handles=[handle1, handle2], loc=1)

    plt.savefig(file_name)


if __name__ == "__main__":
    main()
