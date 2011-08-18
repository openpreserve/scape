#!/bin/bash
sudo dtruss -f /Applications/Preview.app/Contents/MacOS/Preview > dtruss-preview.log 2>&1 &
sleep 5s
echo OPENING-FILE-NOW >> dtruss-preview.log
open -a "Preview" $1

