#!/bin/bash

droidHomeDir=/usr/local/scape/droid6.1
droidScript=$droidHomeDir/droid.sh

#CHECK for the DROID tool
if [ ! -f $droidScript ]; then
	echo "************************************************************"
	echo "*** The required script '$droidScript' does not exist!"
	echo "*** Please make sure, DROID 6.1 is installed at: $droidHomeDir"
	echo "************************************************************"
	exit 1
else
	echo "INPUT:  $1"
	echo "OUTPUT: $2"
fi

#CREATE the TMP profile
droidTMPFile=`mktemp -q /tmp/droid.profile.XXXXXXXX`
if [ ! -f $droidTMPFile ]; then
	echo "ERROR: Failed creating TMP file! => EXIT"
	exit 1
fi

#CHANGE DIRECTORY (because we use the recommended droid.sh instead of calling the jar directly)
cd $droidHomeDir

#CREATE PROFILE
echo "Starting DROID now..."
$droidScript -R -a $1 -p $droidTMPFile

#EXPORT
$droidScript -p $droidTMPFile -e $2

#REMOVE TMP profile
if [ -f $droidTMPFile ]; then
	rm $droidTMPFile
fi

#PRINT AN ERROR if the output has not been created
if [ -f $2 ]; then
	echo "SUCCESS: Expected OUTPUT: '$2' has been generated!"	
	exit 0
else
	echo "ERROR: Expected OUTPUT: '$2' does not exist!"
	exit 1
fi
