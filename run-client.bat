@echo off
title TheKnife Restaurant Management System
echo ====================================================
echo    TheKnife Restaurant Management System
echo ====================================================
echo.

echo Starting TheKnife application...
java --module-path lib --add-modules ALL-MODULE-PATH --enable-native-access=javafx.graphics -jar bin/clientTK-client.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Failed to start the application.
)

pause
