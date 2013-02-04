#!/bin/bash

cd `dirname $0`

usage(){
	echo -e "Usage: $0 TOOLSPECS_DIR|TOOLSPEC TOOLWRAPPER_JAR_WITH_DEPENDENCIES DEBIAN_OUTPUT_DIRECTORY MAINTAINER_EMAIL
\twhere:
\t  TOOLSPECS_DIR|TOOLSPEC            > absolute path to the directory where the toolspecs are
\t                                      or absolute path to a specific toolspec
\t  TOOLWRAPPER_JAR_WITH_DEPENDENCIES > absolute path to the toolwrapper jar
\t  DEBIAN_OUTPUT_DIRECTORY           > absolute path to the directory where the Debian packages are going to be copied
\t  MAINTAINER_EMAIL                  > e-mail of the Debian package maintainer"
}

if [ $# -ne 4 ]; then
	usage
	exit 1
fi

if [ -d $1 ]; then
	LS_EXPRESSION="$1/*.xml"
else
	if [ -f $1 ]; then
		LS_EXPRESSION="$1"
	else
		usage
		exit 2
	fi
fi

TOOLWRAPPER_JAR=$2
DEBIAN_OUTPUT_DIRECTORY=$3
MAINTAINER_EMAIL=$4

for i in $(ls $LS_EXPRESSION);
do
	TOOLSPEC_PATH=$i
	TOOLSPEC_SCRIPT_PATH=`echo $TOOLSPEC_PATH | sed 's#\.[a-zA-Z]\+#.sh#'`
	if [ -e $TOOLSPEC_SCRIPT_PATH ]; then
		JAVA_OUTPUT=`java -jar $TOOLWRAPPER_JAR -d -e $MAINTAINER_EMAIL -o $DEBIAN_OUTPUT_DIRECTORY -t $TOOLSPEC_PATH -sh $TOOLSPEC_SCRIPT_PATH `
	else
		JAVA_OUTPUT=`java -jar $TOOLWRAPPER_JAR -d -e $MAINTAINER_EMAIL -o $DEBIAN_OUTPUT_DIRECTORY -t $TOOLSPEC_PATH`
	fi
	if [ $? -eq 0 ]; then
		cd `echo $JAVA_OUTPUT | sed 's#.*in the directory "\([^"]\+\)"#\1#'`
		/usr/bin/debuild -us -uc -b
		if [ $? -eq 0 ]; then
			cp ../*.deb $DEBIAN_OUTPUT_DIRECTORY 
		fi
		cd -
	fi
done

exit 0
