@echo off
REM =====================================================
REM Fleet Maintenance System - Windows launcher
REM
REM Picks up:
REM   .\config\database.properties  - DB connection
REM   .\config\logging.properties   - log path & rotation
REM Logs are written to whatever logging.file.name points to.
REM =====================================================
setlocal

REM Resolve the directory this script lives in
set "APP_HOME=%~dp0"
cd /d "%APP_HOME%"

REM JVM options (override by setting JAVA_OPTS env var before running)
if "%JAVA_OPTS%"=="" set "JAVA_OPTS=-Xms256m -Xmx1024m -Dfile.encoding=UTF-8"

REM Locate the JAR
for %%f in (fleet-maintenance-system-*.jar) do set "APP_JAR=%%f"
if "%APP_JAR%"=="" (
    echo [ERROR] Could not find fleet-maintenance-system-*.jar in %APP_HOME%
    exit /b 1
)

if not exist logs mkdir logs

echo Starting %APP_JAR% from %APP_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo.
java %JAVA_OPTS% -jar "%APP_JAR%" %*

endlocal
