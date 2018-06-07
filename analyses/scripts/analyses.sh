#!/bin/bash

#
# Runs all the performed analyses
#

# Extract clips
python workspace/scala/findsong/analyses/scripts/extract_clips.py --inputfilesglob "media/music/**/*.mp3" --length 10 --limitfiles 1000 --offset 0 --outputfolderpath workspace/scala/findsong/analyses/data/clips_noiseless

# Metadata
python workspace/scala/findsong/analyses/scripts/extract_metadata.py --metadatakey artist --pathsfilepath workspace/scala/findsong/analyses/data/clips_noiseless/paths --outputfilepath workspace/scala/findsong/analyses/data/metadata/artists
ython workspace/scala/findsong/analyses/scripts/extract_metadata.py --metadatakey genre  --pathsfilepath workspace/scala/findsong/analyses/data/clips_noiseless/paths --outputfilepath workspace/scala/findsong/analyses/data/metadata/genres
python workspace/scala/findsong/analyses/scripts/extract_metadata.py --metadatakey TLEN  --pathsfilepath workspace/scala/findsong/analyses/data/clips_noiseless/paths --outputfilepath workspace/scala/findsong/analyses/data/metadata/tlens
