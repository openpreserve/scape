#!/bin/sh

#echo "deb http://scape.keep.pt/apt stable main" > /etc/apt/sources.list.d/scape.keep.pt.list
#wget -q http://scape.keep.pt/apt/rep.key -O- | apt-key add -
#aptitude update
#aptitude install hello-world
#echo "execute \"hello-world\" in the command-line"
#echo "now, if you want, search for SCAPE related packages: \"sudo aptitude search scape\""

## setup
sudo -E wget --output-document=/etc/apt/sources.list.d/scape.keep.pt.list http://scape.keep.pt/apt/stable.list && sudo wget --output-document=/tmp/scape.keep.pt.key http://scape.keep.pt/apt/rep.key && sudo apt-key add /tmp/scape.keep.pt.key && sudo apt-get --quiet update

sudo aptitude install taverna-commandline scape-as-execute-workflow-with-webservice scape-as-imagemagick-image2jp2

## get data
wget http://scape.keep.pt/scape/testdata/scape-logo.png

cp /usr/share/doc/scape-as-imagemagick-image2jp2/scape-as-imagemagick-image2jp2_rest.t2flow.gz .

gunzip scape-as-imagemagick-image2jp2_rest.t2flow.gz

## run it
scape-as-execute-workflow-with-webservice scape-logo.png scape-as-imagemagick-image2jp2_rest.t2flow
