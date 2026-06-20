@echo off
setlocal
cd /d "%~dp0"

set "JAVA_BIN=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot\bin\java.exe"

if not exist "%JAVA_BIN%" (
  echo [ERROR] Khong tim thay Java tai: %JAVA_BIN%
  pause
  exit /b 1
)

if not exist "target\library-management-1.0.0.jar" (
  echo [ERROR] Chua co file JAR.
  echo Hay chay build-app.bat truoc.
  pause
  exit /b 1
)

if exist ".env" (
  for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    if /I "%%A"=="DB_USER" if "%DB_USER%"=="" set "DB_USER=%%B"
    if /I "%%A"=="DB_PASS" if "%DB_PASS%"=="" set "DB_PASS=%%B"
    if /I "%%A"=="DB_HOST" if "%DB_HOST%"=="" set "DB_HOST=%%B"
    if /I "%%A"=="DB_NAME" if "%DB_NAME%"=="" set "DB_NAME=%%B"
    if /I "%%A"=="DB_URL"  if "%DB_URL%"==""  set "DB_URL=%%B"
    if /I "%%A"=="MAIL_USER" if "%MAIL_USER%"=="" set "MAIL_USER=%%B"
    if /I "%%A"=="MAIL_PASS" if "%MAIL_PASS%"=="" set "MAIL_PASS=%%B"
    if /I "%%A"=="MAIL_FROM" if "%MAIL_FROM%"=="" set "MAIL_FROM=%%B"
    if /I "%%A"=="GOOGLE_CLIENT_ID" if "%GOOGLE_CLIENT_ID%"=="" set "GOOGLE_CLIENT_ID=%%B"
  )
)

if "%DB_USER%"=="" set "DB_USER=root"
if "%DB_HOST%"=="" set "DB_HOST=localhost"
if "%DB_NAME%"=="" set "DB_NAME=library_db"

echo [INFO] DB_USER=%DB_USER%
echo [INFO] DB_HOST=%DB_HOST%
echo [INFO] DB_NAME=%DB_NAME%
echo [INFO] Dang chay app tai http://localhost:3000
echo [INFO] Nhan Ctrl + C de dung app.

"%JAVA_BIN%" -jar "target\library-management-1.0.0.jar"

endlocal
