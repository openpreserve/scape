#!/usr/bin/env bash


# NLineInputFormat gives a single line: key is offset, value is URI
read offset fileuri

echo "extract features $fileuri" >&2

echo "copy file from HDFS to local" >&2
mkdir matchbox
hdfs dfs -get $fileuri matchbox
#hdfs dfs -get $fileuri .

# get basename
target=`basename $fileuri`
echo "basename: $target" >&2

# start script
#/usr/local/bin/extractfeatures matchbox/$target 
/usr/local/bin/extractfeatures matchbox/$target --binary --only SIFTComparison

# copy resulting files back to the HDFS working directory
resgz="$target.SIFTComparison.feat.xml.gz"
echo "resgz: $resgz" >&2
hdfs dfs -put matchbox/$resgz /user/training/matchbox
#hdfs dfs -put $resgz /user/training/matchbox

resdesc="$target.SIFTComparison.descriptors.dat"
echo "resdesc: $resdesc" >&2
hdfs dfs -put matchbox/$resdesc /user/training/matchbox

reskeyp="$target.SIFTComparison.keypoints.dat"
echo "reskeyp: $reskeyp" >&2
hdfs dfs -put matchbox/$reskeyp /user/training/matchbox

echo "end of the feature extraction step" >&2


