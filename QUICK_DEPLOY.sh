#!/bin/bash
echo "🚀 SILENCE DETECTION FIX - QUICK DEPLOY SCRIPT"
echo "================================================"
echo ""
echo "Step 1: Building APK..."
cd /Users/ayush/StudioProjects/Lucifer2
./gradlew assembleDebug
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "Step 2: Installing APK..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Installation successful!"
        echo ""
        echo "Step 3: Starting logcat (filter for HomeViewModel)..."
        echo "Run tests in the app and watch the logs!"
        echo ""
        adb logcat | grep "HomeViewModel"
    else
        echo "❌ Installation failed!"
        exit 1
    fi
else
    echo "❌ Build failed!"
    exit 1
fi
