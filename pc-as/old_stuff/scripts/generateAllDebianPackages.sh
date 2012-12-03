#!/bin/bash

RELATIVE_PATH="../services/"

if [ $# -eq 1 ]; then
	SERVICE_PATTERN="$1"
else
	SERVICE_PATTERN="${RELATIVE_PATH}scape-pc-as-*.xml"
fi

for i in $(ls $SERVICE_PATTERN)
do 
	bash build-service.sh $i
done

for i in $(ls $SERVICE_PATTERN)
do 
	#WEB_SERVICE_NAME=`echo $i | sed -e "'s/${RELATIVE_PATH}scape-as-//'" -e 's/\.xml$//' -e 's/-//g'`
	WEB_SERVICE_NAME=`echo $i | sed -e 's/..\/services\/scape-pc-as-//' -e 's/\.xml$//' -e 's/-//g'`
	bash generateDebianPackage.sh wars/*$WEB_SERVICE_NAME*.war $i
done

