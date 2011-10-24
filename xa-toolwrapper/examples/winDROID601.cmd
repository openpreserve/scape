@ECHO OFF

REM *** Set variables depending on your DROID environment installation
set droidCMDjar="C:\Programme\_ram_Programs\droid-6.01\droid-command-line-6.0.jar"

REM *** Set TMP filename
set fileTMP="%TMP%\tmp.droid"

ECHO Argument 1: %1
ECHO Argument 2: %2
ECHO TMP file:   %TMP%

REM *** Kick the tool
java -jar %droidCMDjar% -R -a %1 -p %fileTMP%
	IF ERRORLEVEL 1 GOTO EXCEPTION
java -jar %droidCMDjar% -p %fileTMP% -e %2
	IF ERRORLEVEL 1 GOTO EXCEPTION
del /Q %fileTMP%

EXIT %ERRORLEVEL%

REM -----------------------------------------------------------------------------------------------
:EXCEPTION
ECHO !!!! EXCEPTION !!!!
EXIT %ERRORLEVEL%


