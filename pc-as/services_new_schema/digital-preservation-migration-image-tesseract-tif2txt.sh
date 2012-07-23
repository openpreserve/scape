#!/bin/bash
if [ $# -eq 2 ]; then
   tesseract $1 $2 && mv -f $2.txt $2
   exit $?
else
   exit 1
fi
