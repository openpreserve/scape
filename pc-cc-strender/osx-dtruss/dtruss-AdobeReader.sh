#!/bin/bash
sudo dtruss -f -t open "/Applications/Adobe\ Reader.app/Contents/MacOS/AdobeReader" $1 > dtruss-adobereader.log 2>&1

