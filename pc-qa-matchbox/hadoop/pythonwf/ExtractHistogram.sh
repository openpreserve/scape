#!/usr/bin/env bash


# NLineInputFormat gives a single line: key is offset, value is URI
read offset fileuri

echo "extract visual histogram $fileuri" >&2

echo "copy file from HDFS to local" >&2
mkdir matchbox
hdfs dfs -get $fileuri matchbox
#hdfs dfs -get $fileuri .

# get basename
target=`basename $fileuri`
echo "basename: $target" >&2

echo "copy related files from HDFS to local" >&2
# copy related files to the local working directory
resgz="$target.SIFTComparison.feat.xml.gz"
echo "resgz: $resgz" >&2
hdfs dfs -get /user/training/matchbox/$resgz matchbox

resdesc="$target.SIFTComparison.descriptors.dat"
echo "resdesc: $resdesc" >&2
hdfs dfs -get /user/training/matchbox/$resdesc matchbox

reskeyp="$target.SIFTComparison.keypoints.dat"
echo "reskeyp: $reskeyp" >&2
hdfs dfs -get /user/training/matchbox/$reskeyp matchbox

# copy also BoW file
hdfs dfs -get /user/training/bow/bow.xml matchbox

# start script
#/usr/local/bin/extractfeatures -o BOWHistogram -d matchbox --bow "matchbox/bow.xml" matchbox/$target 
/usr/local/bin/extractfeatures -o BOWHistogram -d matchbox --bow "matchbox/bow.xml" matchbox/$target --binary

# copy resulting files back to the HDFS working directory
echo "copy visual histogram from local to HDFS working directory" >&2
reshist="$target.BOWHistogram.feat.xml.gz"
echo "reshist: $reshist" >&2
hdfs dfs -put matchbox/$reshist /user/training/histogram

echo "end of the calculate BoW mapper step" >&2


