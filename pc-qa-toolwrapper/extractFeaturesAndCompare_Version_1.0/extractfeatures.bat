@ECHO OFF

REM *** Redirect service output to the passed parameter 2 

ECHO Argument 1: %1
ECHO Argument 2: %2

REM *** Kick the tool
extractfeatures.exe %1 > %2
	IF ERRORLEVEL 1 GOTO EXCEPTION

EXIT %ERRORLEVEL%

REM -----------------------------------------------------------------------------------------------
:EXCEPTION
ECHO !!!! EXCEPTION !!!!
EXIT %ERRORLEVEL%


