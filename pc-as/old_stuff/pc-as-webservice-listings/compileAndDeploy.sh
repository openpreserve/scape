#!/bin/bash

mvn package
cd `dirname $0`
TOMCAT_DIR="/var/lib/tomcat6/webapps/"
sudo cp target/*.war $TOMCAT_DIR/ROOT.war
