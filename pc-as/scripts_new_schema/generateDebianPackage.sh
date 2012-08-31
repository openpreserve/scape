#!/bin/bash

cd `dirname $0`
if [ $# -eq 2 ]; then
	# inputs
	XML=`readlink -m $1`
	WRAPPER=`readlink -m $2`
	WRAPPER_NAME=`basename $WRAPPER`

	# info needed
	XML_NAME_WO_EXT=`basename $XML | sed 's/\.xml$//'`
	CUSTOM_POSTINST="`dirname $XML`/$XML_NAME_WO_EXT.postinst"
	CUSTOM_SH="`dirname $XML`/$XML_NAME_WO_EXT.sh"
	
	TEMP_DIR=".debianGeneration`date +%s`"

	# create necessary folders and files
	mkdir $TEMP_DIR
	cp workflow_bash_template.t2flow "$TEMP_DIR/$XML_NAME_WO_EXT"_bash.t2flow
	if [ ! -d ../workflows ]; then
		mkdir ../workflows
	fi

	cp $WRAPPER $TEMP_DIR
	chmod 755 "$TEMP_DIR/$WRAPPER_NAME"

	cp -r debian_template $TEMP_DIR/debian/
	cd $TEMP_DIR
	# determine if there is any custom postinst script
   if [ -f $CUSTOM_POSTINST ]; then
		cp $CUSTOM_POSTINST debian/postinst
	fi
	# determine if there is any custom script
	if [ -f $CUSTOM_SH ]; then
		cp $CUSTOM_SH "$XML_NAME_WO_EXT.sh"
		chmod 755 "$XML_NAME_WO_EXT.sh"
		echo "$XML_NAME_WO_EXT.sh usr/share/$XML_NAME_WO_EXT/" >> debian/install
	fi
	mv debian/MAN.manpages debian/$XML_NAME_WO_EXT.manpages
	mv debian/MAN.pod debian/$XML_NAME_WO_EXT.pod
	perl ../generateDebianPackageInformation.pl $XML_NAME_WO_EXT $XML
	cp "$XML_NAME_WO_EXT"_bash.t2flow ../../workflows/
	
	# generate the .deb
	debuild -us -uc -b
	cd ..
	mv $XML_NAME_WO_EXT*.deb $XML_NAME_WO_EXT*.build $XML_NAME_WO_EXT*.changes debs/
	rm -rf $TEMP_DIR
else
	echo -e "[ERROR] Must provide: .xml location (toolspeec description, where the xml filename is also used to generate the debian package name) and the wrapped bash script location\n\tusage:\t $0 XML_FILE WRAPPED_BASH"
fi
