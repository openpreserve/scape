#!/bin/sh

input_file=$1
timestampStart=$2
durationTimestamp=$3
output_file=$5
format=$4

ffmpeg -y -ss $timestampStart -i $input_file  -f $format -c:a copy -t $durationTimestamp $output_file