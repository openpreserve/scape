#!/bin/sh

if test -z "$1"
then
	echo "ERROR: Service definition file not specified"
	echo "USAGE: $0 service-definition-xml"
	exit 1
fi

scriptdir=`dirname "$0"`

if [ ! -d $scriptdir/wars ]; then
	mkdir $scriptdir/wars
fi

# Get absolute path for $scriptdir
scriptdir=`readlink -m $scriptdir`

# Get absolute path for input file
xc=`readlink -m $1`

cd $scriptdir/../../xa-toolwrapper

# delete all previous generated services
rm -rf generated/*

# generate the new service(s) project(s)
java -jar target/xa-toolwrapper-0.3-SNAPSHOT-jar-with-dependencies.jar -pc toolwrapper.properties -xc $xc

cd generated
for file in *; do
	if [ -d $file ]; then
		cd $file
		mvn package
		cp target/*local.war "$scriptdir/wars/scapeservices#`ls target/*local.war | sed 's/^target\/\([^\-]\+-[^\-]\+-[^\-]\+\).*/\1/'`.war"
		cd ..
	fi
done

cd $scriptdir

