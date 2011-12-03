#!/bin/sh

droid_jar=/Users/perdalum/Statsbiblioteket/Projekter/SCAPE/Programmer/DROID/droid-command-line-6.0.jar

TMPFILE=`mktemp -q /tmp/profile.droid.XXXXXX`
if [ $? -ne 0 ]; then
   echo "$0: Can't create temp file, exiting..."
   exit 1
fi

java -jar $droid_jar -a $1 -p $TMPFILE
java -jar $droid_jar -p $TMPFILE -e $2
rm $TMPFILE
