
Works for convert, not for Adobe Reader

pdffonts ~/Downloads/ANSI_NISO_Z39.87-2006.pdf

strace -o AcroRead.log /cygdrive/c/Program\ Files/Adobe/Reader\ 9.0/Reader/AcroRd32.exe ~/Downloads/ANSI_NISO_Z39.87-2006.pdf 

strace -o trace.log /usr/bin/convert.exe ~/Downloads/ANSI_NISO_Z39.87-2006.pdf image.jpg
grep " = open" trace.log | grep -v "\-1 = open" | grep -v " 0 = open" | tee opens.log
#Edited in VIM to make opened-files.txt#
uniq opened-files.txt > opened-files-uniq.txt

grep "spawn_guts" trace.log > spawns.txt

cygcheck -l ghostscript-fonts-std
cygcheck -f /usr/share/ghostscript/fonts/n019004l.pfb

 -f, --find-package   find the package to which FILE belongs
  -l, --list-package   list contents of PACKAGE (or all packages if none given)

