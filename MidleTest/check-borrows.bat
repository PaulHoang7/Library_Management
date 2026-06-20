@echo off
setlocal
cd /d "%~dp0"

set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 9.6\bin\mysql.exe"

if not exist "%MYSQL_BIN%" (
  echo [ERROR] Khong tim thay MySQL client tai: %MYSQL_BIN%
  pause
  exit /b 1
)

if exist ".env" (
  for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    if /I "%%A"=="DB_USER" if "%DB_USER%"=="" set "DB_USER=%%B"
    if /I "%%A"=="DB_PASS" if "%DB_PASS%"=="" set "DB_PASS=%%B"
    if /I "%%A"=="DB_NAME" if "%DB_NAME%"=="" set "DB_NAME=%%B"
  )
)

if "%DB_USER%"=="" set "DB_USER=root"
if "%DB_NAME%"=="" set "DB_NAME=library_db"

set "SQL=SELECT id, reader_id, book_id, status, is_overdue, borrow_date, return_date FROM borrows ORDER BY id DESC;"

if "%DB_PASS%"=="" (
  "%MYSQL_BIN%" -u "%DB_USER%" -p -D "%DB_NAME%" -e "%SQL%"
) else (
  "%MYSQL_BIN%" -u "%DB_USER%" -p%DB_PASS% -D "%DB_NAME%" -e "%SQL%"
)

echo.
pause
endlocal
