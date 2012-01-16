#!/bin/bash

for i in $(ls scape-as-*)
do 
	./build-service.sh $i
done

for i in $(ls scape-as-*)
do 
	WEB_SERVICE_NAME=`echo $i | sed -e 's/^scape-as-//' -e 's/\.xml$//' -e 's/-//g'`
	./generateDebianPackage.sh services/*$WEB_SERVICE_NAME*.war $i
done

