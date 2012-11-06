# SCAPE PC Action Services (PC-AS)

This part of the SCAPE project is related to the WP10 - PC.WP.2 Action Services Components.

Here can be found several files and information related to the Action Services description and deployment.

## How to create a Debian package from a toolspec

1) Install all the tools needed. Do:
* sudo apt-get install build-essential dh-make devscripts debhelper lintian maven 

2) Clone SCAPE github so you are able to compile the toolwrapper. Do: 
* git clone https://github.com/openplanets/scape.git

3) Now compile the toolwrapper. From the root of the SCAPE repository, do:
* cd pc-as/toolwrapper/ && mvn package

4) Execute the toolwrapper and generate the Debian package. Do:

* ./generateDebianPackages4AllToolspecs.sh TOOLSPECS_DIR|TOOLSPEC TOOLWRAPPER_JAR_WITH_DEPENDENCIES DEBIAN_OUTPUT_DIRECTORY MAINTAINER_EMAIL

Example:

* ./generateDebianPackages4AllToolspecs.sh /home/hsilva/Git/scape/pc-as/toolspecs/digital-preservation-migration-office-pdfbox-pdf2txt.xml /home/hsilva/Git/scape/pc-as/toolwrapper/bash-generator/target/bash-generator-0.0.1-SNAPSHOT-jar-with-dependencies.jar /home/hsilva/Git/scape/pc-as/toolwrapper/outdir/ hsilva@keep.pt

or

* ./generateDebianPackages4AllToolspecs.sh /home/hsilva/Git/scape/pc-as/toolspecs/ /home/hsilva/Git/scape/pc-as/toolwrapper/bash-generator/target/bash-generator-0.0.1-SNAPSHOT-jar-with-dependencies.jar /home/hsilva/Git/scape/pc-as/toolwrapper/outdir/ hsilva@keep.pt

## Try it out locally (tested on Debian 6.0.5)

Add KEEPS debian package repository (final version of the tools will be added also to the OPF debian repository):

* sudo -E wget --output-document=/etc/apt/sources.list.d/scape.keep.pt.list http://scape.keep.pt/apt/stable.list && wget -q http://scape.keep.pt/apt/rep.key -O- | sudo apt-key add - 

Add debian-multimedia repository (needed to install handbrake-cli):

* echo "deb http://www.deb-multimedia.org squeeze main non-free" | sudo tee /etc/apt/sources.list.d/deb-multimedia.list

Update the list of packages known by apt (to add the packages from the recently added repositories):

* sudo apt-get --quiet 2 update

Install all migration tools (using a metapackage):

* sudo apt-get install digital-preservation-tools-migration


For more information visit: http://wiki.opf-labs.org/display/SP/MS48+-+Prototype+of+tools+adapted+for+large-scale+application+and+integrated+into+SCAPE+platform
