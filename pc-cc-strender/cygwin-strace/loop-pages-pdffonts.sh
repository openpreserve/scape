#!/bin/bash
for i in `seq 1 121`;
do
  echo $i
  pdffonts -f $i -l $i ~/Downloads/ANSI_NISO_Z39.87-2006.pdf
done  
