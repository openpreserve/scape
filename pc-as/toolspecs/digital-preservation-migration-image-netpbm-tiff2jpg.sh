#!/bin/bash
if [ $# -eq 2 ]; then
	tifftopnm $1 | pnmtojpeg > $2
	exit $?
else
	exit 1
fi
