#!/bin/sh

file=$1
output=$2

ffprobe $file 2>&1 | tee $output
