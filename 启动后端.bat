@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   Yinwu Muxin Backend Service
echo ========================================
echo.

cd /d "%~dp0"

echo [INFO] Checking port 8080...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    set PID=%%a
)

if defined PID (
    if not "%PID%"=="0" (
        echo [INFO] Port 8080 is in use by PID: %PID%
        echo [INFO] Stopping process %PID%...
        taskkill /F /PID %PID% >nul 2>&1
        timeout /t 2 >nul
    )
)

echo [OK] Port 8080 is free

set BACKEND_PATH=%~dp0backend
set MAVEN_CMD=%~dp0maven\bin\mvn.cmd

if not exist "%BACKEND_PATH%" (
    echo [ERROR] Backend folder not found!
    pause
    exit /b 1
)

if not exist "%MAVEN_CMD%" (
    echo [ERROR] Maven not found!
    pause
    exit /b 1
)

echo [INFO] Cleaning and building project...
cd /d "%BACKEND_PATH%"
call "%MAVEN_CMD%" clean compile -q

echo [INFO] Starting Spring Boot...
echo.

set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
call "%MAVEN_CMD%" spring-boot:run

echo.
echo ========================================
echo   Service stopped. Press any key to exit...
echo ========================================
pause >nul