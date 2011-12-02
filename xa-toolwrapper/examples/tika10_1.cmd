@ECHO OFF

REM *** Set variables depending on your TIKA environment installation
set toolCMDjar="C:\Programme\_ram_Programs\tika_1.0\tika-app-1.0.jar"

ECHO Argument 1: %1
ECHO Argument 2: %2


REM *** Kick the tool
java -jar %toolCMDjar% -x %1 > %2
	IF ERRORLEVEL 1 GOTO EXCEPTION

EXIT %ERRORLEVEL%

REM -----------------------------------------------------------------------------------------------
:EXCEPTION
ECHO !!!! EXCEPTION !!!!
EXIT %ERRORLEVEL%


