@ECHO OFF

REM *** Redirect compare service output to the passed parameter 3 

ECHO Argument 1: %1
ECHO Argument 2: %2
ECHO Argument 3: %3

IF EXIST %1 ECHO 1 is OK
IF EXIST %2 ECHO 2 is OK


REM *** Kick the tool
compare.exe %1 %2 > %3
ECHO AAAA: %ERRORLEVEL%
	IF ERRORLEVEL 1 GOTO EXCEPTION

EXIT %ERRORLEVEL%

REM -----------------------------------------------------------------------------------------------
:EXCEPTION
ECHO !!!! EXCEPTION !!!!
EXIT %ERRORLEVEL%


