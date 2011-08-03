http://developer.apple.com/mac/library/documentation/Darwin/Reference/ManPages/man1/dtruss.1m.html 

sudo dtruss -f /Applications/Preview.app/Contents/MacOS/Preview ~/Documents/MCFlow-Parallel.pdf > filed.txt 2>&1

With Adobe Reader, on the first run, it touched all the font files, but on the second run it only laoded the ones needed by the file. It seems that it does this periodically, so that might be something to note. No, those are not proper reads.

PowerPoint Presentation Gallery
Don't show this when opening PowerPoint ?

Necessity to use sudo means it runs as root, which is rather clumsy and a bit wring.

Avoid first run due to all the setup.

Seem to have to use open -a "Microsoft PowerPoint" instead of running directly, because MPP does not take CLI args. But for some reason 'dtruss -f' does not follow the fork and is decoupled from the render. Maybe I must combine the two. Open the app with tracing, then open the file with the running app? I guess open leave a ticket on some queue somewhere and exits.

Okay, so fire-up, pause, and open file works fine. What about a linked image? ...

Link to File, ticked, Save with Document unticked. Yes, the reach to the file is clear. And, if you remove the file, then you get a lot of getattrlist(file) = -1 Err #2 responses as PowerPoint hunts around for the file.



24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)          = -1 Err#2
24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)          = -1 Err#2
24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)          = -1 Err#2
24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)               = -1 Err#2
24069/0xa4987:  getattrlist("/.vol/234881026/1540209/:Users:andy:Documents:workspace:scape:pc-cc-strender:osx-dtruss:osx-dtruss:cc.png\0", 0xB06C5D84, 0xB06C5CA0)               = -1 Err#2
24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/pc-cc-strender/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)                = -1 Err#2
24069/0xa4987:  getattrlist("/.vol/234881026/1540209/:Users:andy:Documents:workspace:scape:pc-cc-strender:osx-dtruss:pc-cc-strender:osx-dtruss:cc.png\0", 0xB06C5D84, 0xB06C5CA0)                = -1 Err#2
24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/scape/pc-cc-strender/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)          = -1 Err#2
24069/0xa4987:  getattrlist("/.vol/234881026/1540209/:Users:andy:Documents:workspace:scape:pc-cc-strender:osx-dtruss:scape:pc-cc-strender:osx-dtruss:cc.png\0", 0xB06C5D84, 0xB06C5CA0)          = -1 Err#2
24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/workspace/scape/pc-cc-strender/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)                = -1 Err#2
24069/0xa4987:  getattrlist("/.vol/234881026/1540209/:Users:andy:Documents:workspace:scape:pc-cc-strender:osx-dtruss:workspace:scape:pc-cc-strender:osx-dtruss:cc.png\0", 0xB06C5D84, 0xB06C5CA0)                = -1 Err#2
24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/Documents/workspace/scape/pc-cc-strender/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)              = -1 Err#2
24069/0xa4987:  getattrlist("/.vol/234881026/1540209/:Users:andy:Documents:workspace:scape:pc-cc-strender:osx-dtruss:Documents:workspace:scape:pc-cc-strender:osx-dtruss:cc.png\0", 0xB06C5D84, 0xB06C5CA0)              = -1 Err#2
24069/0xa4987:  getattrlist("/Users/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/andy/Documents/workspace/scape/pc-cc-strender/osx-dtruss/cc.png\0", 0xB06C6578, 0xB06C6200)                 = -1 Err#2
24069/0xa4987:  getattrlist("/.vol/234881026/1540209/:Users:andy:Documents:workspace:scape:pc-cc-strender:osx-dtruss:andy:Documents:workspace:scape:pc-cc-strender:osx-dtruss:cc.png\0", 0xB06C5D84, 0xB06C5CA0)                 = -1 Err#2


Okay, running convert based on brew imagemagick with no ghostscript lead to a very ugly result, but it worked with no fonts pulled in!
Brew cannot install GS at present, so switching to MacPorts...
No, the fonts were pulled in but OSX uses posix_spawn, not fork etc, so dtruss was not following all the children.
