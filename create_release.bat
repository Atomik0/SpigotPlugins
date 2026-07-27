@echo off
REM BuildCam & BuildCam-FPV Automated Release Script for Windows

if "%~1"=="" (
    echo Usage: create_release.bat ^<version_tag^>
    echo Example: create_release.bat v1.0.3
    exit /b 1
)

set TAG=%~1

echo === Step 1: Building and Verifying local project ===
call gradlew.bat build -x test
if %ERRORLEVEL% NEQ 0 (
    echo Build failed! Aborting release.
    exit /b %ERRORLEVEL%
)

echo === Step 2: Committing pending changes ===
git add .
git commit -m "Chore: Release %TAG%"
git push origin main

echo === Step 3: Creating and pushing Git Tag %TAG% ===
git tag -d "%TAG%" 2>nul
git push --delete origin "%TAG%" 2>nul

git tag "%TAG%"
git push origin "%TAG%"

echo === Release Tag %TAG% pushed successfully ===
echo GitHub Actions is now building and publishing release assets at:
echo https://github.com/Atomik0/SpigotPlugins/releases
