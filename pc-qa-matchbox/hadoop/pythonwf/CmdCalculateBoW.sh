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

# clean up bow directory
hdfs dfs -rm -r $hdfshome/bow
hdfs dfs -rm $hdfshome/bowinput

# create input file for calculate BoW step. This file should contain the path 
# to the bow.xml file in HDFS
rm bowinput
echo "hdfs://$hdfshome/bow/bow.xml" > bowinput

# assign rights to the generated bow file
chmod 777 bowinput

# copy bowinput file to hdfs
hdfs dfs -copyFromLocal bowinput .

# pass input file list and extact SIFT features using Hadoop streaming API
printf "+++ pass input file list and calculate Bag of Words dictionary using Hadoop streaming API"

export HADOOP_HOME=/usr/lib/hadoop

hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-*streaming*.jar -D mapred.reduce.tasks=0 -input bowinput -inputformat org.apache.hadoop.mapred.lib.NLineInputFormat -file $localhome/CalculateBoW.sh -mapper $localhome/CalculateBoW.sh -output bow

