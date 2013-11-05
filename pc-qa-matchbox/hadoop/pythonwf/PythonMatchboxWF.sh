#!/bin/bash

# this script runs Matchbox workflow for duplicate search in image
# collection on Hadoop

# home directory on local machine - default is "/home/training/pythonwf"
localhome="$1"
# home directory on HDFS - default is "/user/training" 
hdfshome="$2"
# input directory on HDFS - default is "collection"
inputdir="$3"
# bow size for calculation of visual dictionary - default is "1000"
bowsize="$4"


# use either default or passed parameter for home directory on local machine
if [[ -z "$localhome" ]]; then
   localhome="/home/training/pythonwf"
   echo "default path: $localhome"
else
   echo "passed path: $localhome"
fi

# use either default or passed parameter for home directory on HDFS
if [[ -z "$hdfshome" ]]; then
   hdfshome="/user/training"
   echo "default path: $hdfshome"
else
   echo "passed path: $hdfshome"
fi

# use either default or passed parameter for input directory on HDFS
if [[ -z "$inputdir" ]]; then
   inputdir="collection"
   echo "default path: $inputdir"
else
   echo "passed path: $inputdir"
fi

# use either default or passed parameter for bow size
if [[ -z "$bowsize" ]]; then
   bowsize="1000"
   echo "default path: $bowsize"
else
   echo "passed path: $bowsize"
fi

./CreateInputFiles.sh $inputdir $hdfshome
./CmdExtractSift.sh $localhome $hdfshome
./CmdCalculateBoW.sh $localhome $hdfshome $bowsize
./CmdExtractHistogram.sh $localhome $hdfshome
./CmdCompare.sh $localhome $hdfshome

# print out the workflow summary
hdfs dfs -cat compare/benchmark_result_list.csv

