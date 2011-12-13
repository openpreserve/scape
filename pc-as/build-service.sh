#!/bin/sh

if test -z "$1"
then
	echo "ERROR: Service definition file not specified"
	echo "USAGE: $0 service-definition-xml"
	exit 1
fi

scriptdir=`dirname "$0"`
# Get absolute path for $scriptdir
scriptdir=`readlink -m $scriptdir`

# Get absolute path for input file
xc=`readlink -m $1`

cd $scriptdir/../xa-toolwrapper

# delete all previous generated services
rm -rf generated/*

# generate the new service(s) project(s)
java -jar toolwrapper.jar -pc toolwrapper.properties -xc $xc

cd generated
for file in *; do
	if [ -d $file ]; then
		cd $file
		mvn tomcat:redeploy
		echo "***"
		echo "cp ../../apache-tomcat-6.0.29/webapps/scapeservices#scape-$file-service.war $scriptdir/services"
		echo "***"
		cp ../../apache-tomcat-6.0.29/webapps/scapeservices#scape-$file-service.war $scriptdir/services
		cd ..
	fi
done

cd $scriptdir

