#!/bin/bash
if [ $# -eq 2 ]; then
	echo "(define (convert-tiff-to-png filename outfile)(let* ((image (car (gimp-file-load RUN-NONINTERACTIVE filename filename)))(drawable (car (gimp-image-merge-visible-layers image CLIP-TO-IMAGE))))(file-png-save-defaults RUN-NONINTERACTIVE image drawable outfile outfile)(gimp-image-delete image)))(convert-tiff-to-png \"$1\" \"$2\")(gimp-quit 0)" | gimp -i -b -
	exit 0
else
	exit -1
fi
