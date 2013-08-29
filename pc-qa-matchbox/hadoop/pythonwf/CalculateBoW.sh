#!/usr/bin/env bash


# NLineInputFormat gives a single line: key is offset, value is URI
read offset fileuri

echo "mapper calcualate BoW $fileuri" >&2

# get basename
target=`basename $fileuri`
echo "basename: $target" >&2

echo "copy related files from HDFS to local" >&2
# copy related files to the local working directory
resgz="*.SIFTComparison.feat.xml.gz"
echo "resgz: $resgz" >&2
mkdir matchbox
hdfs dfs -get /user/training/matchbox/$resgz matchbox
#hdfs dfs -get /user/training/matchbox/$resgz .

resdesc="*.SIFTComparison.descriptors.dat"
echo "resdesc: $resdesc" >&2
hdfs dfs -get /user/training/matchbox/$resdesc matchbox

reskeyp="*.SIFTComparison.keypoints.dat"
echo "reskeyp: $reskeyp" >&2
hdfs dfs -get /user/training/matchbox/$reskeyp matchbox

# start script
echo "start calculate BoW reducer" >&2
#/usr/local/bin/train -o matchbox/$target matchbox --bowsize 100
/usr/local/bin/train -o matchbox/$target matchbox --binary --bowsize 100

# copy resulting files back to the HDFS working directory
echo "copy bow.xml from local to HDFS working directory" >&2
hdfs dfs -put matchbox/bow.xml /user/training/bow
#hdfs dfs -put bow.xml /user/training/matchbox

echo "end of the calculate BoW mapper step" >&2


