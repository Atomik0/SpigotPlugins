#!/usr/bin/env bash
# BuildCam & BuildCam-FPV Automated Release Script for Linux / Ubuntu

set -e

if [ -z "$1" ]; then
  echo "Usage: ./create_release.sh <version_tag>"
  echo "Example: ./create_release.sh v1.0.3"
  exit 1
fi

TAG=$1

echo "=== Step 1: Building and Verifying local project ==="
chmod +x gradlew
./gradlew build -x test

echo "=== Step 2: Committing pending changes ==="
if [ -n "$(git status --porcelain)" ]; then
  git add .
  git commit -m "Chore: Release $TAG"
  git push origin main
fi

echo "=== Step 3: Creating and pushing Git Tag $TAG ==="
git tag -d "$TAG" 2>/dev/null || true
git push --delete origin "$TAG" 2>/dev/null || true

git tag "$TAG"
git push origin "$TAG"

echo "=== Release Tag $TAG pushed successfully ==="
echo "GitHub Actions is now building and publishing the release assets at:"
echo "https://github.com/Atomik0/SpigotPlugins/releases"
