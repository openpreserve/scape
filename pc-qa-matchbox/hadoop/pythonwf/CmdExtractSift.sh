#!/usr/bin/env bash

# home directory on local machine - default is "/home/training/pythonwf"
localhome="$1"

# home directory on HDFS
hdfshome="$2"

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

# clean up folder for temporary files on HDFS 
hdfs dfs -rm -r $hdfshome/matchbox 

# pass input file list and extact SIFT features using Hadoop streaming API
printf "+++ pass input file list and extact SIFT features using Hadoop streaming API"

export HADOOP_HOME=/usr/lib/hadoop

hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-*streaming*.jar -D mapred.reduce.tasks=0 -input inputfiles -inputformat org.apache.hadoop.mapred.lib.NLineInputFormat -file $localhome/ExtractSift.sh -mapper $localhome/ExtractSift.sh -output matchbox
