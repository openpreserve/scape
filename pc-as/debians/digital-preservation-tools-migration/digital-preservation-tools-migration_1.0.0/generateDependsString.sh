#!/bin/bash

if [ $# -ne 1 ]; then
	echo "Must provide the directory containing the dependencies for the meta-package"
	exit 1
fi

STRING=""
cd $1
for i in $(ls *.xml);
do
	DEP_NAME=`echo -n $i | sed 's#.xml$##'`
	if [ "$STRING" == "" ]; then
		STRING=$DEP_NAME
	else
		STRING="$DEP_NAME,$STRING"
	fi
done
echo $STRING
