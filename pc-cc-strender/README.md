Strender
========

System Traced Rendering

Overview
--------
In short, the idea is to monitor resource usage during rendering, either by manual access or via simulation of access. As all I/O must pass through the kernel, standard systems monitoring methods can be used to analyse what is going in. In this way, possible run-time 'representation information' can be determined. This is particularly important for 'transcluded' resources, e.g. fonts or other external resources, that are not embedded in the primary object bitstream, but are embedded in the performance of that bitstream.

As well as fonts, examples include
* Keys, licences, 
* Preferences, caches, 
* Fonts, colour spaces, Schema? Icons. Sounds/effects?

PDF, Doc, declared fonts v used.

Embedded ok, linked media for doc too.

Linked files, and missing links?
Trace dependent media in PowerPoint presentation, including missing media?
SVG as a good example?
Science example?
http://highered.mcgraw-hill.com/sites/dl/free/0073106941/443736/DSPAudioExamples.ppt

http://stackoverflow.com/questions/1439586/best-way-to-watch-process-and-sub-processes-for-file-system-read-i-o
http://sysadvent.blogspot.com/2008/12/sysadmin-advent-day-1.html

Extend using tcpdump, SystemTap, dtruss, etc... 

Visualisation
-------------

### Graphviz Plotters ###

#### HTML ####
http://www.graphviz.org/content/attrs#dtooltip
http://www.graphviz.org/content/output-formats#dcmapx
or
http://code.google.com/p/canviz/

#### Applet ####
http://zvtm.sourceforge.net/zgrviewer.html



