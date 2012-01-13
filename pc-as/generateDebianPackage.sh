#!/bin/bash

cd `dirname $0`
if [ $# -eq 3 ]; then
	# create necessary folders and files
	TEMP_DIR=".debianGeneration`date +%s`"
	mkdir $TEMP_DIR
	cp $2 $TEMP_DIR 
	XML=`readlink -m $3`
	cd $TEMP_DIR
	mkdir "debian"
#	echo "Package: $1 
#Version: __VERSION__ 
#Maintainer: Hélder Silva <hsilva@keep.pt>
#Extra-Files: README
#Files: __FILES__
#Depends: __DEPENDENCIES__" > "$1"
	echo "This is a dummy README" > README
	WAR_NAME=`basename $2`
#	echo "Package: $1 
#Version: 1.0 
#Maintainer: Hélder Silva <hsilva@keep.pt>
#Extra-Files: README
#Files: $WAR_NAME /var/lib/tomcat6/webapps/$WAR_NAME
#Depends: tomcat6" > "$1"
	# generate the .deb
	../generateDebianPackageFiles.pl $1 $WAR_NAME $XML
	equivs-build $1
	mv *.deb ..
	cd ..
	rm -rf $TEMP_DIR
else
	echo "[ERROR] Must provide: service/package name, the .war location (web application archive) and .xml location (toolspeec description)"
fi
