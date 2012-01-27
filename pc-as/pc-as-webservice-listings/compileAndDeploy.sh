#!/bin/bash

mvn package
cd `dirname $0`
TOMCAT_DIR="/var/lib/tomcat6/webapps/"
cp target/*.war $TOMCAT_DIR/ROOT.war
