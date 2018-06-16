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
cp -r workspace/scala/findsong/analyses/data/indexes/performance/size/100 workspace/scala/findsong/analyses/data/indexes/recrate
for length in 1 2 5 10 20; do
	python workspace/scala/findsong/analyses/scripts/extract_clips.py --inputfilesglob "workspace/scala/findsong/analyses/data/indexes/recrate/*.mp3" --cliplength $length --limitfiles 100 --outputdirectorypath workspace/scala/findsong/analyses/data/clips/recrate/noise/0/length/$length
done

for noise in 5 10 15 20; do
		for length in 1 2 5 10 20; do
				python workspace/scala/findsong/analyses/scripts/add_noise.py --inputfilesglob "workspace/scala/findsong/analyses/data/clips/recrate/noise/0/length/$length/*.mp3" --noisefilepath workspace/scala/findsong/analyses/data/noise/bar.wav --noiseweight $noise --outputdirectorypath workspace/scala/findsong/analyses/data/clips/recrate/noise/$noise/length/$length
		done
done

# Recognition rate
python workspace/scala/findsong/analyses/scripts/get_matches_for_globs.py --clipsglob "workspace/scala/findsong/analyses/data/clips/recrate/**/*.mp3" --clipssamplespercapture 8000 16000 40000 80000 160000 --findsongjarpath workspace/scala/findsong/bin/findsong-assembly-1.0.5.jar --outputfolderpath workspace/scala/findsong/analyses/data/recognition_rate --songsglob "workspace/scala/findsong/analyses/data/indexes/recrate/*.mp3"
python workspace/scala/findsong/analyses/scripts/get_recrate_for_matches.py --matchesfilepath workspace/scala/findsong/analyses/data/recognition_rate/matches_for_glob --outputfilepath workspace/scala/findsong/analyses/data/recognition_rate/recrate_for_matches
