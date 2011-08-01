set PM=C:\AnJackson\Apps\procexp.exe
start %PM% /quiet /minimized /backingfile C:\strender.pml
%PM% /waitforidle
"C:\Program Files\Adobe\Reader 9.0\Reader\AcroRd32.exe" C:\BritishLibrary\Eclipse\workspace-bl\scape\pc-cc-strender\resources\ANSI_NISO_Z39.87-2006.pdf
%PM% /terminate

