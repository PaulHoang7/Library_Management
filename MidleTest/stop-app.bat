@echo off
setlocal
cd /d "%~dp0"

set "PORT=3000"
echo [INFO] Dang tim tien trinh dang LISTEN o cong %PORT%...

powershell -NoProfile -Command ^
  "$pids = Get-NetTCPConnection -LocalPort %PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; " ^
  "if (-not $pids) { exit 2 }; " ^
  "foreach ($procId in $pids) { " ^
  "  try { Stop-Process -Id $procId -Force -ErrorAction Stop; Write-Host ('[OK] Da dung PID ' + $procId) } " ^
  "  catch { Write-Host ('[WARN] Khong dung duoc PID ' + $procId + ': ' + $_.Exception.Message) } " ^
  "}; " ^
  "exit 0"

if errorlevel 2 (
  echo [INFO] Khong co app nao dang chay o cong %PORT%.
  endlocal
  exit /b 0
)

if errorlevel 1 (
  echo [ERROR] Dung app that bai. Thu chay cmd/PowerShell bang Run as Administrator.
  endlocal
  exit /b 1
)

echo [INFO] Kiem tra lai cong %PORT%...
netstat -ano | findstr :%PORT% >nul
if errorlevel 1 (
  echo [OK] Cong %PORT% da trong.
) else (
  echo [WARN] Van con ket noi TIME_WAIT/CLOSE_WAIT tam thoi. Thu lai sau vai giay.
)

endlocal
