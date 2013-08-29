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

# clean up compare directory
hdfs dfs -rm -r $hdfshome/compare

# create input file for compare step. This file should include the result of
# the duplicate search in given collection and contain the path to the summary file in HDFS
echo "hdfs://$hdfshome/matchbox/summary.csv" > summary

# assign rights to the generated summary file
chmod 777 summary

# copy summary file to hdfs
hdfs dfs -copyFromLocal summary .

# compare visual histograms with following SSIM comparison for three nearest duplicate candidates using Hadoop streaming API
printf "+++ compare visual histograms with following SSIM comparison for three nearest duplicate candidates using Hadoop streaming API"

export HADOOP_HOME=/usr/lib/hadoop

#hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-*streaming*.jar -D mapred.reduce.tasks=0 -input summary -file /$localhome/Compare.sh -mapper $localhome/Compare.sh -output compare

hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-*streaming*.jar -D mapred.reduce.tasks=0 -input summary -inputformat org.apache.hadoop.mapred.lib.NLineInputFormat -file /$localhome/Compare.sh -mapper $localhome/Compare.sh -output compare
