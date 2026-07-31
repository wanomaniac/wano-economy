@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo Building all to 1.21 to 1.21.11 
echo ==========================================

:: List of target versions from 1.21 to 1.21.11
set "VERSIONS=1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11"


for %%V in (%VERSIONS%) do (
    echo.
    echo ------------------------------------------
    echo  Building target version: Minecraft %%V
    echo ------------------------------------------

    :: 1. Export Environment Variable
    set "MINECRAFT_VERSION=%%V"
    set "MODRINTH_TOKEN=mrp_MLxVZx8DmnORoZ9c6KSc5evtVRXxdGwWimC8I81yRLKEUJ07jXKNmiWeUcfh"

    :: 2. Execute Gradle build
    call ./gradlew.bat clean modrinth -Pminecraft_version="%%V"

    :: Exit immediately if Gradle command fails (replicates 'set -e')
    if !errorlevel! neq 0 (
        echo.
        echo [ERROR] Build failed for Minecraft version %%V
        exit /b !errorlevel!
    )
)

echo.
echo ==========================================
echo  ALL BUILDS COMPLETED SUCCESSFULLY! 
echo  All VERSIONS HAVE BEEN PUBLISHED 
echo ==========================================

pause