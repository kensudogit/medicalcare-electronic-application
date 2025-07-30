@echo off
echo Building backend only...
echo.
echo Cleaning up previous builds...
docker system prune -f
echo.
echo Checking current directory...
dir
echo.
echo Checking gradle directory...
if exist "gradle" (
    dir gradle
    if exist "gradle\wrapper" (
        dir gradle\wrapper
    ) else (
        echo gradle\wrapper directory does not exist
    )
) else (
    echo gradle directory does not exist
)
echo.
echo Starting backend build...
docker build -f Dockerfile.backend -t medicalcare-backend . --no-cache --progress=plain
if %errorlevel% neq 0 (
    echo.
    echo Backend build failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)
echo.
echo Backend build successful!
pause 