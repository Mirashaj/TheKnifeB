@echo off
title TheKnife Restaurant Management System
echo ====================================================
echo    TheKnife Restaurant Management System
echo ====================================================
echo.

echo Starting TheKnife application...
java -jar bin/serverTK-server.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Failed to start the application.
    pause
)
