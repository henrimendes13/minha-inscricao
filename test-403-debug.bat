@echo off
setlocal enabledelayedexpansion

REM 403 Error Debug Test Script for Windows
REM This script tests POST /api/usuarios with different scenarios to isolate 403 issues

set BASE_URL=http://localhost:8080
set ENDPOINT=/api/usuarios
set TEST_USER={"nome":"Debug Test User","email":"debug@test.com","tipoUsuario":"ATLETA","senha":"password123"}

echo 🔍 403 Error Debug Test Script (Windows)
echo ========================================
echo.

REM Check if curl is available
curl --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ curl is not available. Please install curl or use Git Bash.
    echo    You can download curl from: https://curl.se/windows/
    pause
    exit /b 1
)

REM Check if server is running
echo 🏁 Checking if application is running...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/actuator/health" > temp_health.txt 2>nul
set /p HEALTH_CODE=<temp_health.txt
del temp_health.txt 2>nul

if not "%HEALTH_CODE%"=="200" (
    echo ❌ Application is not running at %BASE_URL%
    echo    Please start the application first:
    echo    mvn spring-boot:run
    echo.
    pause
    exit /b 1
)

echo ✅ Application is running
echo.

REM Test 1: Basic POST request
echo 🧪 Testing: Basic POST request (no extra headers^)
echo ----------------------------------------
echo URL: %BASE_URL%%ENDPOINT%
echo Method: POST
echo Headers: Content-Type: application/json
echo Body: %TEST_USER%
echo.

curl -s -w "Status: %%{http_code}, Time: %%{time_total}s" ^
    -X POST "%BASE_URL%%ENDPOINT%" ^
    -H "Content-Type: application/json" ^
    -d "%TEST_USER%"
echo.
echo.

REM Test 2: With Origin header
echo 🧪 Testing: With Origin header (browser simulation^)
echo ----------------------------------------
echo Adding Origin header to simulate browser request
echo.

curl -s -w "Status: %%{http_code}, Time: %%{time_total}s" ^
    -X POST "%BASE_URL%%ENDPOINT%" ^
    -H "Content-Type: application/json" ^
    -H "Origin: http://localhost:3000" ^
    -d "%TEST_USER%"
echo.
echo.

REM Test 3: OPTIONS preflight request
echo 🧪 Testing: OPTIONS preflight request
echo ----------------------------------------
echo This tests if CORS preflight is working correctly
echo.

curl -s -w "Status: %%{http_code}" ^
    -X OPTIONS "%BASE_URL%%ENDPOINT%" ^
    -H "Origin: http://localhost:3000" ^
    -H "Access-Control-Request-Method: POST" ^
    -H "Access-Control-Request-Headers: Content-Type" > temp_options.txt

set /p OPTIONS_RESPONSE=<temp_options.txt
del temp_options.txt

echo 📊 OPTIONS Response: %OPTIONS_RESPONSE%
echo.

REM Extract status code (simple parsing for batch)
for /f "tokens=2 delims=:" %%a in ('echo %OPTIONS_RESPONSE%') do set OPTIONS_CODE=%%a
set OPTIONS_CODE=%OPTIONS_CODE: =%

if "%OPTIONS_CODE%"=="200" (
    echo ✅ OPTIONS SUCCESS: CORS preflight is working
) else if "%OPTIONS_CODE%"=="403" (
    echo 🚫 OPTIONS FORBIDDEN: CORS preflight is failing
    echo 💡 This is likely the root cause of the POST 403 error
) else if "%OPTIONS_CODE%"=="404" (
    echo 🔍 OPTIONS NOT FOUND: OPTIONS method may not be configured
) else (
    echo ❓ OPTIONS UNEXPECTED: %OPTIONS_CODE%
)

echo.
echo ========================================
echo.

REM Summary and recommendations
echo 📋 DEBUGGING SUMMARY
echo ====================
echo.
echo If you're seeing 403 errors, try these debug profiles:
echo.
echo 1. Complete security bypass:
echo    mvn spring-boot:run -Dspring.profiles.active=debug
echo.
echo 2. CORS debugging:
echo    mvn spring-boot:run -Dspring.profiles.active=cors-debug
echo.
echo 3. Filter chain debugging:
echo    mvn spring-boot:run -Dspring.profiles.active=filter-debug
echo.
echo 4. Full debugging (combines all^):
echo    mvn spring-boot:run -Dspring.profiles.active=dev,filter-debug,cors-debug
echo.
echo Check the application logs for detailed debug information.
echo See DEBUG_403_GUIDE.md for detailed analysis instructions.
echo.
echo 🔧 To run this script again: test-403-debug.bat
echo.

pause