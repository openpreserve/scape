#!/bin/bash
if [ $# -eq 3 ]; then
	tifftopnm $2 | $1 > $3
	exit 0
else
	exit -1
fi
