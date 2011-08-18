
From http://kohei.us/2010/06/25/strace-equivalent-for-windows/comment-page-1/#comment-10554

set PM=C:\sysint\procmon.exe
start %PM% /quiet /minimized /backingfile C:\temp\soffice.pml
%PM% /waitforidle
soffice.exe
%PM% /terminate


Seems to be deprecated functionality. ProcExp 15.01 does not support it. There is also a program called Handle but this does not appear to have a monitoring mode, just snapshot.

Running manually doesn't seem to help. Difficult to see all files as sampling is unclear.
