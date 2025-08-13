@echo off
echo Starting GREE HVAC Controller...
echo.

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 21 or higher
    pause
    exit /b 1
)

REM Check if Maven is available
mvn -version >nul 2>&1
if errorlevel 1 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven 3.6 or higher
    pause
    exit /b 1
)

echo Building and running the application...
echo.

REM Build and run the application
mvn clean javafx:run

echo.
echo Application finished.
pause


