@echo off
setlocal
cd /d "%~dp0"

python publish_release.py %*

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Terjadi kesalahan saat merilis pembaruan.
    pause
    exit /b %errorlevel%
)

echo.
pause
