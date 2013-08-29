#!/bin/bash

# this script creates input file list for hadoop job
printf "\n+++ create input file list for hadoop job\n\n"

# input directory on HDFS
inputdir="$1"

# home directory on HDFS
hdfshome="$2"

# use either default or passed parameter for input directory on HDFS
if [[ -z "$inputdir" ]]; then
   inputdir="collection"
   echo "default path: $inputdir"
else
   echo "passed path: $inputdir"
fi

# use either default or passed parameter for home directory on HDFS
if [[ -z "$hdfshome" ]]; then
   hdfshome="/user/training"
   echo "default path: $hdfshome"
else
   echo "passed path: $hdfshome"
fi

# clean up folder for temporary files on HDFS 
hdfs dfs -rm inputfiles 

# read input files from passed directory in HDFS
printf "\n+++ read input files from passed directory in HDFS\n"

hdfs dfs -ls $inputdir > tmp

./CreateInputList.py -d $hdfshome
 
rm tmp

# assign rights to the generated list
chmod 777 inputfiles

# copy input file list to HDFS
hdfs dfs -put inputfiles $hdfshome
