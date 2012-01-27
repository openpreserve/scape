#!/bin/bash

if [ $# -eq 1 ]; then
	SERVICE_PATTERN="$1"
else
	SERVICE_PATTERN="scape-as-*"
fi

for i in $(ls $SERVICE_PATTERN)
do 
	./build-service.sh $i
done

for i in $(ls $SERVICE_PATTERN)
do 
	WEB_SERVICE_NAME=`echo $i | sed -e 's/^scape-as-//' -e 's/\.xml$//' -e 's/-//g'`
	./generateDebianPackage.sh services/*$WEB_SERVICE_NAME*.war $i
done

