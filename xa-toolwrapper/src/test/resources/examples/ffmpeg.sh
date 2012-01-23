#!/bin/sh

ffmpeg -i $mp3 `basename $mp3`.wav  2>&1 | tee `basename $mp3`.log