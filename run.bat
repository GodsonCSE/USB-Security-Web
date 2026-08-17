@echo off
REM ============================================================
REM USB Device Security Monitor — Windows Launcher
REM Zero-Configuration: Runs instantly with embedded database.
REM No database setup or password required!
REM ============================================================

setlocal
cd /d "%~dp0"

set JAR=%~dp0backend\target\usb-security-monitor-1.0.0.jar

REM Always rebuild so any code/UI changes are picked up.
REM (Previously this only built the jar if it didn't exist yet, which meant
REM  an old jar could keep serving old HTML/CSS/JS even after files changed.)
echo ============================================================
echo  Building USB Security Monitor backend...
echo ============================================================
cd "%~dp0backend"
call mvn package -DskipTests
cd "%~dp0"
echo.

if not exist "%JAR%" (
  echo Build failed - see the Maven output above for the error.
  pause
  exit /b 1
)

echo ============================================================
echo  Starting USB Device Security Monitor...
echo  Zero Login / Password required!
echo ============================================================
echo.
echo  * Starting server on port 8080...
echo  * The dashboard will open in your browser automatically.
echo  * If your browser opened too quickly, just press Refresh (F5)!
echo.

start "" powershell -NoProfile -Command "Start-Sleep -Seconds 4; Start-Process 'http://localhost:8080'"

java -jar "%JAR%"

pause
