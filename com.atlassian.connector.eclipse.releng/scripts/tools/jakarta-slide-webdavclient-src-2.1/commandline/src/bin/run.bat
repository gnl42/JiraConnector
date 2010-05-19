@echo off
rem ---------------------------------------------------------------------------
rem run.bat - Start Script for the Slide Client
rem
rem $Id: run.bat,v 1.3 2004/03/31 09:42:00 ozeigermann Exp $
rem ---------------------------------------------------------------------------

set _SLIDE_CLIENT_HOME=%SLIDE_CLIENT_HOME%
if "%_SLIDE_CLIENT_HOME%" == "" set _SLIDE_CLIENT_HOME=..

rem add all jars from lib to classpath
set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("%_SLIDE_CLIENT_HOME%\lib\*.jar") do call "%_SLIDE_CLIENT_HOME%\bin\lcp.bat" %%i

set MAINCLASS=org.apache.webdav.cmd.Slide
java -classpath "%LOCALCLASSPATH%" %MAINCLASS% %1 %2 %3 %4 %5 %6 %7 %8 %9

set _SLIDE_CLIENT_HOME=
set MAINCLASS=
set LOCALCLASSPATH=
