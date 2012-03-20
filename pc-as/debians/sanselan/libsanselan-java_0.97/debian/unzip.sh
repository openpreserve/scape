#!/bin/sh -e

# called with '--upstream-version' <version> <file>
VERSION=$2
FILE_DOWNLOADED=$3

DPKG_FILES_VERSION=`dpkg-parsechangelog | sed -n 's/^Version: //p'`
if [ "$DPKG_FILES_VERSION" != "${VERSION}" ]; then
	echo "Version has changed (changelog=$DPKG_FILES_VERSION VS http=$VERSION)! Verify if dpkg files are according to the newer version (i.e., changelog, etc)!"
	echo "---------------------------------"
	egrep "$DPKG_FILES_VERSION" debian/*
	echo "---------------------------------"
fi
UNZIP_OUT=`unzip $FILE_DOWNLOADED`
DIR_CREATED=`echo -n "$UNZIP_OUT" | egrep "creating:" | sed 's/^\s*creating: //'`
mv $DIR_CREATED/sanselan-$VERSION-incubator.jar .
rm -rf $DIR_CREATED
rm -f $FILE_DOWNLOADED
