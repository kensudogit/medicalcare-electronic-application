@echo off
echo Setting up Gradle wrapper...
echo.

echo Creating gradle/wrapper directory if it doesn't exist...
if not exist "gradle\wrapper" mkdir gradle\wrapper

echo Downloading gradle-wrapper.jar...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing}"

if %errorlevel% neq 0 (
    echo Failed to download gradle-wrapper.jar!
    echo Trying alternative method...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing"
    if %errorlevel% neq 0 (
        echo All download methods failed!
        pause
        exit /b 1
    )
)

echo gradle-wrapper.jar downloaded successfully!
echo.

echo Verifying gradle-wrapper.jar...
if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo gradle-wrapper.jar exists and is ready.
) else (
    echo gradle-wrapper.jar was not created properly.
    pause
    exit /b 1
)

echo.
echo Gradle setup completed successfully!
pause 