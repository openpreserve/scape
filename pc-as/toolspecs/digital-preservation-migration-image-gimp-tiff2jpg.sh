#!/bin/bash
if [ $# -eq 2 ]; then
	echo "(define (convert-tiff-to-jpeg filename outfile)(let* ((image (car (gimp-file-load RUN-NONINTERACTIVE filename filename)))  (drawable (car (gimp-image-merge-visible-layers image CLIP-TO-IMAGE)))  )    (file-jpeg-save RUN-NONINTERACTIVE image drawable outfile outfile .9 0 0 0 \" \" 0 1 0 1)(gimp-image-delete image)))(convert-tiff-to-jpeg \"$1\" \"$2\")(gimp-quit 0)" | gimp -i -b -
	exit 0
else
	exit -1
fi
