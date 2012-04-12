#!/bin/bash
if [ $# -eq 2 ]; then
	tifftopnm $1 | pnmtopng > $2
	exit $?
else
	exit 1
fi
