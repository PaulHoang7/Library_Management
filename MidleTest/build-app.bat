@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot"
set "MVN_BIN=%~dp0..\tools\apache-maven-3.9.10\bin\mvn.cmd"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] Khong tim thay Java tai: %JAVA_HOME%
  pause
  exit /b 1
)

if exist "%MVN_BIN%" (
  echo [INFO] Dang build bang Maven portable...
  "%MVN_BIN%" -DskipTests package
) else (
  where mvn >nul 2>nul
  if errorlevel 1 (
    echo [ERROR] Khong tim thay Maven portable hoac mvn trong PATH.
    pause
    exit /b 1
  )
  echo [INFO] Dang build bang Maven trong PATH...
  mvn -DskipTests package
)

if errorlevel 1 (
  echo [ERROR] Build that bai.
  pause
  exit /b 1
)

echo [OK] Build thanh cong.
pause
endlocal
