#!/bin/bash

cd `dirname $0`
RELATIVE_PATH="../services_new_schema/"

if [ $# -eq 1 ]; then
	SERVICE_PATTERN="$1"
else
	SERVICE_PATTERN="${RELATIVE_PATH}digital-preservation-migration-*.xml"
fi

for i in $(ls $SERVICE_PATTERN)
do 
	WRAPPER=`echo $i | sed 's#.xml$##'`
	bash build-wrapper.sh $i $RELATIVE_PATH
	bash generateDebianPackage.sh $i $WRAPPER
done

