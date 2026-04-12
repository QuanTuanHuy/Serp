@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM Always run from project root (folder containing this script)
cd /d "%~dp0"

echo [INFO] Loading environment variables from .env ...
if exist ".env" (
  for /f "usebackq tokens=* delims=" %%L in (".env") do (
    set "line=%%L"
    if defined line (
      if not "!line:~0,1!"=="#" (
        for /f "tokens=1* delims==" %%A in ("!line!") do (
          if not "%%A"=="" set "%%A=%%B"
        )
      )
    )
  )
) else (
  echo [WARN] .env not found. Running with current environment.
)

if not defined APP_CONFIG_PATHS set "APP_CONFIG_PATHS=src/config"

echo [INFO] Starting API Gateway...
go run src/main.go
set "exit_code=%ERRORLEVEL%"

echo.
echo [INFO] Process exited with code %exit_code%
pause
exit /b %exit_code%
