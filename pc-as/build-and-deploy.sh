#!/bin/sh

scriptdir=`dirname "$0"`
scriptdir=`readlink -m $scriptdir`

# Get absolute path for input file
xc=`readlink -m $1`

cd $scriptdir/../xa-toolwrapper
java -jar toolwrapper.jar -pc toolwrapper.properties -xc $xc

cd generated
for file in *; do
	if [ -d $file ]; then
		cd $file
		mvn tomcat:redeploy
		echo "***"
		echo "cp ../../apache-tomcat-6.0.29/webapps/scapeservices#scape-$file-service.war $scriptdir"
		echo "***"
		cp ../../apache-tomcat-6.0.29/webapps/scapeservices#scape-$file-service.war $scriptdir
		cd ..
	fi
done

cd $scriptdir

