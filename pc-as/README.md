# SCAPE PC Action Services (PC-AS)

This part of the SCAPE project is related to the WP10 - PC.WP.2 Action Services Components.

Here can be found several files and information related to the Action Services description and deployment.

## How to create a Debian package from a toolspec

See toolwrapper README.md: [link](https://github.com/openplanets/scape-toolwrapper)

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
