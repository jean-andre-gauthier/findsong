#!/bin/bash

#
# Runs all the performed analyses
#

# Indexes
python workspace/scala/findsong/analyses/scripts/create_indexes.py --indexsizes 10 20 50 100 200 500 1000 --inputfilesglob "media/music/**/*.mp3" --outputdirectory workspace/scala/findsong/analyses/data/indexes/performance/size

# Metadata
python workspace/scala/findsong/analyses/scripts/extract_metadata.py --metadatakey artist --pathsfilepath workspace/scala/findsong/analyses/data/indexes/performance/size/1000/paths --outputfilepath workspace/scala/findsong/analyses/data/metadata/artists
python workspace/scala/findsong/analyses/scripts/extract_metadata.py --metadatakey genre  --pathsfilepath workspace/scala/findsong/analyses/data/indexes/performance/size/1000/paths --outputfilepath workspace/scala/findsong/analyses/data/metadata/genres
python workspace/scala/findsong/analyses/scripts/extract_metadata.py --metadatakey TLEN  --pathsfilepath workspace/scala/findsong/analyses/data/indexes/performance/size/1000/paths --outputfilepath workspace/scala/findsong/analyses/data/metadata/tlens

# Extract clips
for size in 10 20 50 100 200 500 1000; do
		python workspace/scala/findsong/analyses/scripts/extract_clips.py --inputfilesglob "workspace/scala/findsong/analyses/data/indexes/performance/size/$size/*.mp3" --cliplength 10 --limitfiles $size --outputdirectorypath workspace/scala/findsong/analyses/data/clips/performance/size/$size
done

# Create clips with noise
cp -r workspace/scala/findsong/analyses/data/indexes/performance/size/100 workspace/scala/findsong/analyses/data/indexes/recognition_rate
for length in 1 2 5 10 20; do
	python workspace/scala/findsong/analyses/scripts/extract_clips.py --inputfilesglob "workspace/scala/findsong/analyses/data/indexes/recognition_rate/*.mp3" --cliplength $length --limitfiles 100 --outputdirectorypath workspace/scala/findsong/analyses/data/clips/recognition_rate/noise/0/length/$length
done

for noise in 5 10 15 20; do
		for length in 1 2 5 10 20; do
				python workspace/scala/findsong/analyses/scripts/add_noise.py --inputfilesglob "workspace/scala/findsong/analyses/data/clips/recognition_rate/noise/0/length/$length/*.mp3" --noisefilepath workspace/scala/findsong/analyses/data/noise/bar.wav --noiseweight $noise --outputdirectorypath workspace/scala/findsong/analyses/data/clips/recognition_rate/noise/$noise/length/$length
		done
done

# Recognition rate
python workspace/scala/findsong/analyses/scripts/get_matches_for_globs.py --clipsglob "workspace/scala/findsong/analyses/data/clips/recognition_rate/**/*.mp3" --clipssamplespercapture 8000 16000 40000 80000 160000 --findsongjarpath workspace/scala/findsong/bin/findsong-assembly-1.0.6.jar --outputdirectorypath workspace/scala/findsong/analyses/data/recognition_rate --songsglob "workspace/scala/findsong/analyses/data/indexes/recognition_rate/*.mp3"
python workspace/scala/findsong/analyses/scripts/get_recognition_rate_for_matches.py --matchesfilepath workspace/scala/findsong/analyses/data/recognition_rate/matched_clips_for_glob --recognitionrateformatchesfilepath workspace/scala/findsong/analyses/data/recognition_rate/recognition_rate_for_matches --wrongresultsformatchesfilepath workspace/scala/findsong/analyses/data/recognition_rate/wrong_results_for_matches

# Performance
mkdir workspace/scala/findsong/analyses/data/performance
clipsglobs=()
indexsizes=()
songsglobs=()
for size in 10 20 50 100 200 500 1000; do
	clipsglobs+=("workspace/scala/findsong/analyses/data/clips/performance/size/$size/*.mp3")
	indexsizes+=("$size")
	songsglobs+=("workspace/scala/findsong/analyses/data/indexes/performance/size/$size/*.mp3")
done
python workspace/scala/findsong/analyses/scripts/calculate_indexer_matcher_performance.py --clipsglobs "${clipsglobs[@]}" --findsongjarpath "workspace/scala/findsong/bin/findsong-assembly-1.0.6.jar" --indexsizes "${indexsizes[@]}" --performancefilepath workspace/scala/findsong/analyses/data/performance/performance_by_index_size --songsglobs "${songsglobs[@]}"

# Audio Filters
mkdir workspace/scala/findsong/analyses/data/clips/audio_filters
cp -r workspace/scala/findsong/analyses/data/clips/recognition_rate/noise/0/length/20 workspace/scala/findsong/analyses/data/clips/audio_filters/clean
cp -r workspace/scala/findsong/analyses/data/indexes/recognition_rate workspace/scala/findsong/analyses/data/indexes/audio_filters
python workspace/scala/findsong/analyses/scripts/add_audio_filters.py --audiofilternames highpass lowpass rubberband rubberband rubberband rubberband rubberband rubberband rubberband rubberband rubberband rubberband rubberband rubberband chorus aecho flanger aphaser --audiofilterparameters "f=440" "f=440" "-p 1" "-p 2" "-p 3" "-p 4" "-p 5" "-p 6" "-t 1.025" "-t 1.05" "-t 1.075" "-t 1.1" "-t 1.125" "-t 1.15" "0.7:0.9:55:0.4:0.25:2" "0.8:0.8:1000:0.8" "delay=20:depth=5:regen=10:speed=2" "delay=5.0:speed=2.0" --inputfilesglob "workspace/scala/findsong/analyses/data/clips/audio_filters/clean/*.mp3" --outputdirectorypath workspace/scala/findsong/analyses/data/clips/audio_filters
python workspace/scala/findsong/analyses/scripts/get_matches_for_globs.py --clipsglob "workspace/scala/findsong/analyses/data/clips/audio_filters/**/*.mp3" --clipssamplespercapture 80000 --findsongjarpath workspace/scala/findsong/bin/findsong-assembly-1.0.6.jar --outputdirectorypath workspace/scala/findsong/analyses/data/audio_filters --songsglob "workspace/scala/findsong/analyses/data/indexes/recognition_rate/*.mp3"
python workspace/scala/findsong/analyses/scripts/get_audio_filters_recognition_rate_for_matches.py --matchesfilepath workspace/scala/findsong/analyses/data/audio_filters/matched_clips_for_glob --recognitionrateformatchesfilepath workspace/scala/findsong/analyses/data/audio_filters/recognition_rate_for_matches --wrongresultsformatchesfilepath workspace/scala/findsong/analyses/data/audio_filters/wrong_results_for_matches

# Parameters
cp -r workspace/scala/findsong/analyses/data/clips/recognition_rate/noise/0/length/10 workspace/scala/findsong/analyses/data/clips/findsong_parameters/
cp -r workspace/scala/findsong/analyses/data/indexes/recognition_rate workspace/scala/findsong/analyses/data/indexes/findsong_parameters
python workspace/scala/findsong/analyses/scripts/get_parameters_matches_for_globs.py --clipsglob "workspace/scala/findsong/analyses/data/clips/findsong_parameters/*.mp3" --findsongjarpath workspace/scala/findsong/bin/findsong-assembly-1.0.6.jar --parameterlist "--fanout=1 2 4 8 16" "--peakDeltaF=1 5 10 20 50 100" "--peakDeltaT=1 5 10 20 50 100" "--peaksPerChunk=1 2 4 8 16 32" "--windowDeltaF=1 5 10 20 50 100" "--windowDeltaT=10 20 50 100 200 500" "--windowDeltaTi=1 5 10 15 20 25 30"  --outputdirectorypath workspace/scala/findsong/analyses/data/findsong_parameters  --songsglob "workspace/scala/findsong/analyses/data/indexes/findsong_parameters/*.mp3"
python workspace/scala/findsong/analyses/scripts/get_parameters_recognition_rate_for_matches.py --matchesdirectorypath workspace/scala/findsong/analyses/data/findsong_parameters/ --recognitionrateformatchesfilepath workspace/scala/findsong/analyses/data/findsong_parameters/recognition_rate_for_matches

# Plots
python analyses/scripts/plots/generate_performance_plot.py --inputfilepath analyses/data/performance/performance_by_index_size --outputplotindexerpath analyses/data/plots/performance_indexer.png --outputplotmatchespath analyses/data/plots/performance_matcher.png
python analyses/scripts/plots/generate_recognition_rate_plot.py --inputfilepath analyses/data/recognition_rate/recognition_rate_for_matches --outputplotpath analyses/data/plots/recognition_rate.png
python analyses/scripts/plots/generate_blog_plots.py --digitizationplotfilename analyses/data/plots/blog_digitisation.png  --fftplotfilename analyses/data/plots/blog_fft.png --indexerwalkthroughplotfilename analyses/data/plots/blog_indexer_walkthrough.png --matcherwalkthroughplotfilename analyses/data/plots/blog_matcher_walkthrough.png
python analyses/scripts/plots/generate_audio_filter_plots.py --audiofilterplotpath analyses/data/plots/audio_filter.png --pitchplotpath analyses/data/plots/audio_filter_pitch.png --tempoplotpath analyses/data/plots/audio_filter_tempo.png
