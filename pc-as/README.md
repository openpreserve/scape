# SCAPE PC Action Services (PC-AS)

This part of the SCAPE project is related to the WP10 - PC.WP.2 Action Services Components.

Here can be found several files and information related to the Action Services description and deployment.

## Action Services deployed
Please feel free to try them at http://scape.keep.pt

## Try it out locally (tested on Ubuntu 10.04 LTS)
Add our debian repository and gpg key

* sudo -E wget --output-document=/etc/apt/sources.list.d/scape.keep.pt.list http://scape.keep.pt/apt/stable.list && sudo wget --output-document=/tmp/scape.keep.pt.key http://scape.keep.pt/apt/rep.key && sudo apt-key add /tmp/scape.keep.pt.key && sudo apt-get --quiet update

Install our TIFF to JP2K meta-package

* sudo aptitude install scape-as-tiff2jp2-demo

Read the manual

* man scape-as-tiff2jp2-demo
