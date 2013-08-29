#!/usr/bin/env bash


# NLineInputFormat gives a single line: key is offset, value is URI
read offset fileuri

echo "mapper compare $fileuri" >&2

# get basename
target=`basename $fileuri`
echo "basename: $target" >&2

echo "copy related files from HDFS to local" >&2
# copy related files to the local working directory
hdfs dfs -get /user/training/matchbox .

orig="*.jp2"
echo "orig: $orig" >&2
hdfs dfs -get /user/training/collection/$orig matchbox

echo "remove old gz files" >&2
rm matchbox/part*
rm -rf matchbox/_*
#rm matchbox/*.dat

reshist="*.BOWHistogram.feat.xml.gz"
echo "reshist: $reshist" >&2
hdfs dfs -get /user/training/histogram/$reshist matchbox

# copy bow file to local machine
hdfs dfs -get /user/training/bow/bow.xml matchbox

# change permissions
chmod 777 -R matchbox

# show contents
ls -al . >&2
echo "show matchbox directory" >&2
ls -al matchbox >&2
pwd >&2

# start script
echo "start comparison" >&2
#/usr/bin/FindDuplicates.py --benchmark matchbox compare
#/usr/bin/FindDuplicates.py --binary --benchmark matchbox compare
/usr/bin/FindDuplicates.py --binary --benchmark matchbox compare

ls -al . >&2

# copy resulting files back to the HDFS working directory
echo "copy resulting files from local to HDFS working directory" >&2
hdfs dfs -put matchbox/*.csv /user/training/compare
hdfs dfs -put matchbox/*.npz /user/training/compare
hdfs dfs -put matchbox/benchmark_result_list.csv /user/training/matchbox/summary.csv

echo "end of the comparison step" >&2


