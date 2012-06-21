#!/bin/sh

LINK=`readlink -m $0`
DIR=`dirname $LINK`
java -jar $DIR/pdfbox-app-1.7.0.jar "$@"
