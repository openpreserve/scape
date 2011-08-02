

strace, scripts

strace -f -o trace.log /usr/bin/acroread /mnt/hgfs/Shared/Z39-87-2006.pdf

strace -f -o acroread-nofile-trace.log /usr/bin/acroread
more acroread-nofile-trace.log | grep open | grep -v ENOENT | grep -v EACCES > acroread-nofile-files.txt
sort acroread-nofile-files.txt | uniq > acroread-nofile-files.srted.uniq.txt

strace -f -o acroread-file-trace.log /usr/bin/acroread /mnt/hgfs/Shared/Z39-87-2006.pdf
more acroread-file-trace.log | grep open | grep -v ENOENT | grep -v EACCES > acroread-file-files.txt
sort acroread-file-files.txt | uniq > acroread-file-files.srted.uniq.txt

strace -f -o acroread-nofontfile-trace.log /usr/bin/acroread ~/drawing-no-fonts.pdf
more acroread-nofontfile-trace.log | grep open | grep -v ENOENT | grep -v EACCES > acroread-nofontfile-files.txt

export ACRO_DISABLE_FONT_CONFIG=true
strace -f -o acroread-file-adfc-trace.log /usr/bin/acroread /mnt/hgfs/Shared/Z39-87-2006.pdf 

more acroread-nofontfile-trace.log | grep RDO | grep -v ENOTDIR | grep -v ENOENT | grep -v EACCES | grep -v "O_DIRECTORY" | grep -i "PFB"

/usr/bin/acroread /a "page=22,view=Fit" /mnt/hgfs/Shared/Z39-87-2006.pdf 

strace -f -o strace-convert-file.log convert /mnt/hgfs/Shared/Z39-87-2006.pdf image.jpg

 - Use packages strace and lsof to inspect dependencies during rendering. See [http://www.netadmintools.com/art353.html][22]  strace -e trace=open -o strace-file.txt xpdf SimulationPaper.pdf Have to page through, and then Symbol and Times New Roman (standard) crop up: [08:08:18] [anj@li15-203 /home/anj]$ diff strace-file.txt strace-nofile.txt | grep -v tmp 110,158c110,111 &amp;lt; open("SimulationPaper.pdf", O_RDONLY|O_LARGEFILE) = 4 &amp;lt; open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 5 &amp;lt; open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 5 &amp;lt; open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 5 &amp;lt; open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 5 --- &amp;gt; --- SIGINT (Interrupt) @ 0 (0) --- &amp;gt; +++ killed by SIGINT +++

Acroread might be workable too, although it seems to have poked all the fonts when first started up. Mose sensible output the second time, although pop-ups/non-render stuff gets in the way.

See also direct forced font analysis.

[09:14:03] [anj@li15-203 /home/anj]$ strace -e trace=open -o render.txt pdftoppm -r 72 SimulationPaper.pdf pages [09:13:59] [anj@li15-203 /home/anj]$ more render.txt | grep ont open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4

Consistent with pdffonts SimulationPaper.pdf (Times-Roman, Symbol).


* Use packages strace and lsof to inspect dependencies during rendering. See http://www.netadmintools.com/art353.html strace -e trace=open -o strace-file.txt xpdf SimulationPaper.pdf Have to page through, and then Symbol and Times New Roman (standard) crop up: [08:08:18] [anj@li15-203 /home/anj]$ diff strace-file.txt strace-nofile.txt | grep -v tmp 110,158c110,111 &lt; open("SimulationPaper.pdf", O_RDONLY|O_LARGEFILE) = 4 &lt; open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 5 &lt; open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 5 &lt; open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 5 &lt; open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 5 --- &gt; --- SIGINT (Interrupt) @ 0 (0) --- &gt; +++ killed by SIGINT +++

Acroread might be workable too, although it seems to have poked all the fonts when first started up. Mose sensible output the second time, although pop-ups/non-render stuff gets in the way.

See also direct forced font analysis.

[09:14:03] [anj@li15-203 /home/anj]$ strace -e trace=open -o render.txt pdftoppm -r 72 SimulationPaper.pdf pages [09:13:59] [anj@li15-203 /home/anj]$ more render.txt | grep ont open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/n021003l.pfb", O_RDONLY) = 4 open("/usr/share/fonts/type1/gsfonts/s050000l.pfb", O_RDONLY) = 4

Consistent with pdffonts SimulationPaper.pdf (Times-Roman, Symbol).


anj@debian:~/scape/pc-cc-strender/debian-strace$ pdffonts /mnt/hgfs/Shared/Z39-87-2006.pdf 
name                                 type              emb sub uni object ID
------------------------------------ ----------------- --- --- --- ---------
Arial-BoldMT                         TrueType          no  no  no     386  0
ArialMT                              TrueType          no  no  no     387  0
TimesNewRomanPSMT                    TrueType          no  no  no     340  0
Arial-ItalicMT                       TrueType          no  no  no     342  0
BBNPHD+SymbolMT                      CID TrueType      yes yes yes    344  0
Arial-BoldItalicMT                   TrueType          no  no  no     348  0


Link to package
---------------
dpkg -S /path/file

dlocate /path/file (may be faster)

http://collab-maint.alioth.debian.org/debtree/

### To maintainer ###
http://packages.debian.org/squeeze/devscripts

dd-list pretty-prints it
http://manpages.ubuntu.com/manpages/natty/man1/dd-list.1.html

