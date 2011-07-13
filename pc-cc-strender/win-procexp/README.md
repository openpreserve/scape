
From http://kohei.us/2010/06/25/strace-equivalent-for-windows/comment-page-1/#comment-10554

set PM=C:\sysint\procmon.exe
start %PM% /quiet /minimized /backingfile C:\temp\soffice.pml
%PM% /waitforidle
soffice.exe
%PM% /terminate

