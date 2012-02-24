#!/bin/bash

if [ $# -ne 2 ]; then
	echo -e "Usage:\n\t$0 [PYINSTALLER_URL as tar.bz2] [JPYLYZER as tar.gz]\n\texample:\n \t\tbash $0 \"https://github.com/downloads/pyinstaller/pyinstaller/pyinstaller-1.5.1.tar.bz2\" \"https://github.com/bitsgalore/jpylyzer/tarball/master\""
	exit 1
fi
cd `dirname $0`

PYINSTALLER_URL=$1
JPYLYZER_URL=$2
TEMP_DIR=".build`date +%s`"
INNER_TEMP_DIR=".all"
PWD=`pwd`

mkdir $TEMP_DIR
cd $TEMP_DIR

wget -O "$PWD/pyinstaller.tar.bz2" "$PYINSTALLER_URL"
wget -O "$PWD/jpylyzer.tar.gz" "$JPYLYZER_URL"
tar -xjf pyinstaller.tar.bz2
tar -xzf jpylyzer.tar.gz
rm pyinstaller.tar.bz2
rm jpylyzer.tar.gz

PYINSTALLER_DIR=`ls | egrep "pyinstaller"`
JPYLYZER_DIR=`ls | egrep "jpylyzer"`
for i in $(ls $JPYLYZER_DIR/*.py)
do 
	$PYINSTALLER_DIR/pyinstaller.py $i
done
cd dist
mkdir $INNER_TEMP_DIR
for i in $(ls | egrep "^\w.*")
do
	cp $i/* $INNER_TEMP_DIR
done

mv $INNER_TEMP_DIR ../../compiled/
cd ../../

rm -rf $TEMP_DIR
