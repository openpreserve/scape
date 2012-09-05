#!/bin/bash

if [ $# -ne 3 ]; then
	echo -e "Usage: $0 TOOLSPECS_DIR TOOLWRAPPER_JAR DEBIAN_OUTPUT_DIRECTORY
\twhere:
\t  TOOLSPECS_DIR           > absolute path to the directory where the toolspecs are
\t  TOOLWRAPPER_JAR         > absolute path to the toolwrapper jar
\t  DEBIAN_OUTPUT_DIRECTORY > absolute path to the directory where the Debian packages are going to be copied"
	exit 1
fi

TOOLSPECS_DIR=$1
TOOLWRAPPER_JAR=$2
DEBIAN_OUTPUT_DIRECTORY=$3

for i in $(ls $TOOLSPECS_DIR/*.xml);
do
	TOOLSPEC_PATH=$i
	TOOLSPEC_SCRIPT_PATH=`echo $TOOLSPEC_PATH | sed 's#\.[a-zA-Z]\+#.sh#'`
	if [ -e $TOOLSPEC_SCRIPT_PATH ]; then
		JAVA_OUTPUT=`java -jar $TOOLWRAPPER_JAR -d -e hsilva@keep.pt -o /tmp -t $TOOLSPEC_PATH -sh $TOOLSPEC_SCRIPT_PATH `
	else
		JAVA_OUTPUT=`java -jar $TOOLWRAPPER_JAR -d -e hsilva@keep.pt -o /tmp -t $TOOLSPEC_PATH`
	fi
	if [ $? -eq 0 ]; then
		cd `echo $JAVA_OUTPUT | sed 's#.*in the directory "\([^"]\+\)"#\1#'`
		/usr/bin/debuild -us -uc -b
		if [ $? -eq 0 ]; then
			cp ../*.deb $DEBIAN_OUTPUT_DIRECTORY 
		fi
	fi
done
