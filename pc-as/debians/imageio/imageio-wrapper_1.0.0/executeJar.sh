#!/bin/sh

LINK=`readlink -m $0`
DIR=`dirname $LINK`
java -jar $DIR/imageio-wrapper*.jar "$@"
