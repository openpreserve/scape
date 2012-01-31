#!/bin/bash

cd `dirname $0`
if [ $# -eq 2 ]; then
	# create necessary folders and files
	TEMP_DIR=".debianGeneration`date +%s`"
	mkdir $TEMP_DIR
	cp $1 $TEMP_DIR 
	XML=`readlink -m $2`
	XML_NAME_WO_EXT=`basename $2 | sed 's/\.xml//'`
	cp workflow_rest_template.t2flow "$TEMP_DIR/$XML_NAME_WO_EXT"_rest.t2flow
	cp workflow_soap_template.t2flow "$TEMP_DIR/$XML_NAME_WO_EXT"_soap.t2flow
	cd $TEMP_DIR
	mkdir "debian"
	echo "This is a dummy README" > README
	WAR_NAME=`basename $1`
	# generate the .deb
	../generateDebianPackageFiles.pl $XML_NAME_WO_EXT $WAR_NAME $XML
	equivs-build $XML_NAME_WO_EXT
	mv *.deb ..
	cd ..
	rm -rf $TEMP_DIR
else
	echo "[ERROR] Must provide: the .war location (web application archive) and .xml location (toolspeec description, where the xml filename is also used to generate the debian package name)"
fi
