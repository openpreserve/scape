http://developer.apple.com/mac/library/documentation/Darwin/Reference/ManPages/man1/dtruss.1m.html 

sudo dtruss -f /Applications/Preview.app/Contents/MacOS/Preview ~/Documents/MCFlow-Parallel.pdf > filed.txt 2>&1

With Adobe Reader, on the first run, it touched all the font files, but on the second run it only laoded the ones needed by the file.

