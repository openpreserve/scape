#!/bin/sh

mp3=$1
name=`basename $mp3`
wav=$2

ffmpeg -y -i $mp3 $wav  2>&1 | tee $wav.log
