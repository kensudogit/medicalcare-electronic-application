@echo off
echo Downloading gradle-wrapper.jar...
powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'"
if %errorlevel% neq 0 (
    echo Failed to download gradle-wrapper.jar!
    pause
    exit /b 1
)
echo gradle-wrapper.jar downloaded successfully!
pause 