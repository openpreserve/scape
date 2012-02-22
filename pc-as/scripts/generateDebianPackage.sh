#!/bin/bash

cd `dirname $0`
if [ $# -eq 2 ]; then
	# inputs
	WAR=$1
	XML=`readlink -m $2`

	# info needed
	WAR_NAME=`basename $WAR`
	XML_NAME_WO_EXT=`basename $XML | sed 's/\.xml$//'`
	CUSTOM_POSTINST="`dirname $XML`/$XML_NAME_WO_EXT.postinst"
	CUSTOM_SH="`dirname $XML`/$XML_NAME_WO_EXT.sh"
	
	TEMP_DIR=".debianGeneration`date +%s`"

	# create necessary folders and files
	mkdir $TEMP_DIR
	cp $WAR $TEMP_DIR 
	cp workflow_rest_template.t2flow "$TEMP_DIR/$XML_NAME_WO_EXT"_rest.t2flow
	cp workflow_soap_template.t2flow "$TEMP_DIR/$XML_NAME_WO_EXT"_soap.t2flow
	if [ ! -d ../workflows ]; then
		mkdir ../workflows
	fi
	cp -r debian_template $TEMP_DIR/debian/
	cd $TEMP_DIR
	# determine if there is any custom postinst script
   if [ -f $CUSTOM_POSTINST ]; then
		cp $CUSTOM_POSTINST debian/postinst
	fi
	# determine if there is any custom script
	if [ -f $CUSTOM_SH ]; then
		cp $CUSTOM_SH $XML_NAME_WO_EXT
		echo "$XML_NAME_WO_EXT usr/bin/" >> debian/install
	fi
	mv debian/MAN.manpages debian/$XML_NAME_WO_EXT.manpages
	mv debian/MAN.pod debian/$XML_NAME_WO_EXT.pod
	perl ../generateDebianPackageInformation.pl $XML_NAME_WO_EXT $WAR_NAME $XML
	cp "$XML_NAME_WO_EXT"_rest.t2flow ../../workflows/
	cp "$XML_NAME_WO_EXT"_soap.t2flow ../../workflows/
	
	# generate the .deb
	debuild -us -uc -b
	cd ..
	mv $XML_NAME_WO_EXT*.deb $XML_NAME_WO_EXT*.build $XML_NAME_WO_EXT*.changes debs/
	rm -rf $TEMP_DIR
else
	echo -e "[ERROR] Must provide: the .war location (web application archive) and .xml location (toolspeec description, where the xml filename is also used to generate the debian package name)\n\tusage:\t $0 WAR_FILE XML_FILE"
fi


























#cd `dirname $0`
#if [ $# -eq 2 ]; then
#	# create necessary folders and files
#	TEMP_DIR=".debianGeneration`date +%s`"
#	mkdir $TEMP_DIR
#	cp $1 $TEMP_DIR 
#	XML=`readlink -m $2`
#	XML_NAME_WO_EXT=`basename $2 | sed 's/\.xml//'`
#	cp workflow_rest_template.t2flow "$TEMP_DIR/$XML_NAME_WO_EXT"_rest.t2flow
#	cp workflow_soap_template.t2flow "$TEMP_DIR/$XML_NAME_WO_EXT"_soap.t2flow
#	if [ ! -d ../workflows ]; then
#		mkdir ../workflows
#	fi
#	cd $TEMP_DIR
#	mkdir "debian"
#	echo "This is a dummy README" > README
#	WAR_NAME=`basename $1`
#	# generate the .deb
#	../generateDebianPackageFiles.pl $XML_NAME_WO_EXT $WAR_NAME $XML
#	cp "$XML_NAME_WO_EXT"_rest.t2flow ../../workflows/
#	cp "$XML_NAME_WO_EXT"_soap.t2flow ../../workflows/
#	equivs-build $XML_NAME_WO_EXT
#	mv *.deb ../debs/
#	cd ..
#	rm -rf $TEMP_DIR
#else
#	echo "[ERROR] Must provide: the .war location (web application archive) and .xml location (toolspeec description, where the xml filename is also used to generate the debian package name)"
#fi
