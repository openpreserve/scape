#!/bin/bash

cd `dirname $0`

if [ $# -ne 2 ]; then
	echo "Must provide 2 arguments: xml file and output directory"
	exit 1
fi

if [ ! -f ../toolwrapper/toolwrapper_core/target/toolwrapper_core-0.0.1-SNAPSHOT-jar-with-dependencies.jar ]; then
	echo "Cannot find jar to generate the bash wrapper! take a look: `dirname $0`/../toolwrapper/toolwrapper_core/target/"
   exit 2
fi
java -jar ../toolwrapper/toolwrapper_core/target/toolwrapper_core-0.0.1-SNAPSHOT-jar-with-dependencies.jar -t $1 -o $2
