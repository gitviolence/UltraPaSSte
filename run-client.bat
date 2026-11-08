@echo off
setlocal
cd /d "%~dp0"

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto java_ready
set "JAVA_HOME="
for /d %%J in ("%ProgramFiles%\Java\jdk-21*" "%ProgramFiles%\Eclipse Adoptium\jdk-21*" "%ProgramFiles%\Microsoft\jdk-21*" "%ProgramFiles%\Amazon Corretto\jdk21*") do if exist "%%~fJ\bin\javac.exe" set "JAVA_HOME=%%~fJ"
if not defined JAVA_HOME goto missing_java
if not exist "%JAVA_HOME%\bin\javac.exe" goto missing_java
:java_ready
set "PATH=%JAVA_HOME%\bin;%PATH%"

call "%~dp0gradlew.bat" runClient --console=plain
set "CLIENT_EXIT=%ERRORLEVEL%"
if "%CLIENT_EXIT%"=="0" exit /b 0
echo.
echo Client launch failed. See the messages above or run-client.log.
if /i not "%~1"=="--background" pause
exit /b %CLIENT_EXIT%

:missing_java
echo JDK 21 is required. Install it and set JAVA_HOME to its installation folder.
if /i not "%~1"=="--background" pause
exit /b 1
