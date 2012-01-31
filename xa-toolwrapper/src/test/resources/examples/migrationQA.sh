#!/bin/sh

file1=$1
file2=$2
output=$3

/home/scape/working/migrationQA_v1.00 $file1 $file2 2>&1 | tee $output
