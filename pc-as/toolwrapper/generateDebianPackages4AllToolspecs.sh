#!/bin/bash

cd `dirname $0`

usage(){
	echo -e "Usage: $0 TOOLSPECS_DIR|TOOLSPEC TOOLWRAPPER_BASE_DIR DEBIAN_OUTPUT_DIRECTORY MAINTAINER_EMAIL
\twhere:
\t  TOOLSPECS_DIR|TOOLSPEC            > path to the directory where the toolspecs are
\t                                      or absolute path to a specific toolspec
\t  TOOLWRAPPER_BASE_DIR              > path to the toolwrapper base directory
\t  DEBIAN_OUTPUT_DIRECTORY           > path to the directory where the Debian packages are going to be copied
\t  MAINTAINER_EMAIL                  > e-mail of the Debian package maintainer"
}

echo -e "[WARN] This script only aplies to toolspecs with 1 operation (as the others need extra parameters for Debian package generation.\nDo you want to procede? [yN]"
read anwser
case $anwser in
	[yY])
		;;
	*)
		exit 1
		;;
esac

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


TOOLWRAPPER_BASE_DIR=$(readlink -m $2)
DEBIAN_OUTPUT_DIRECTORY=$(readlink -m $3)
MAINTAINER_EMAIL=$4

for i in $(ls $LS_EXPRESSION);
do
	TOOLSPEC_PATH=$i
	TOOLSPEC_SCRIPT_PATH=`echo $TOOLSPEC_PATH | sed 's#\.[a-zA-Z]\+#.sh#'`
	CHANGELOG_PATH=`echo $TOOLSPEC_PATH | sed 's#\.[a-zA-Z]\+#.changelog#'`
	TEMP_FOLDER=$(mktemp -d)
	$TOOLWRAPPER_BASE_DIR/bash-generator/bin/generate.sh -t $TOOLSPEC_PATH -o $TEMP_FOLDER
	if [ -e $TOOLSPEC_SCRIPT_PATH ]; then
		cp $TOOLSPEC_SCRIPT_PATH "$TEMP_FOLDER/install/"
	fi
	$TOOLWRAPPER_BASE_DIR/bash-debian-generator/bin/generate.sh -t $TOOLSPEC_PATH -i $TEMP_FOLDER -o $TEMP_FOLDER -e $MAINTAINER_EMAIL -ch $CHANGELOG_PATH
	if [ $? -eq 0 ]; then
		for i in $(find $TEMP_FOLDER -name *.deb);
		do
			cp $i $DEBIAN_OUTPUT_DIRECTORY
		done
	fi
	rm -rf $TEMP_FOLDER
done

exit 0
