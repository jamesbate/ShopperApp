#!/bin/bash

# Android Studio Gradle Build Fix Script
# Automated script to resolve common Gradle build issues

echo "🔧 Android Studio Gradle Build Fix Script"
echo "========================================="

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    echo "❌ Error: Please run this script from the ShopperApp root directory"
    exit 1
fi

# 1. Clean Environment
echo "🧹 Cleaning build environment..."
rm -rf app/build
rm -rf .gradle
./gradlew clean

# 2. Kill Gradle Daemons
echo "🔄 Stopping all Gradle daemons..."
./gradlew --stop

# 3. Clear Caches
echo "🗑️ Clearing Gradle and IDE caches..."
rm -rf ~/.gradle/caches
rm -rf .idea/caches

# 4. Set Memory Configuration
echo "💾 Setting optimal Gradle memory configuration..."
export GRADLE_OPTS="-Xmx3g -XX:MaxMetaspaceSize=512m -Dorg.gradle.daemon.idletimeout=300000"

# 5. Check Dependencies
echo "🔍 Checking dependency resolution..."
./gradlew dependencies --configuration=debug --no-daemon

# 6. Build with Maximum Debugging
echo "🏗 Starting debug build..."
echo "Command: ./gradlew assembleDebug --info --stacktrace --no-daemon"
echo "================================"

# Run build with timeout and capture output
timeout 300 ./gradlew assembleDebug --info --stacktrace --no-daemon 2>&1 | tee build.log

BUILD_RESULT=$?

# 7. Analyze Results
echo ""
echo "📊 Build Results Analysis"
echo "========================="

if [ $BUILD_RESULT -eq 0 ]; then
    echo "✅ Build completed successfully!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
elif [ $BUILD_RESULT -eq 124 ]; then
    echo "⏰ Build timed out after 5 minutes"
else
    echo "❌ Build failed with exit code: $BUILD_RESULT"
    
    # Search for common errors in the log
    echo ""
    echo "🔍 Searching for common errors..."
    if grep -q "OutOfMemoryError" build.log; then
        echo "❌ Memory Error: Increase heap size with -Xmx4g"
    fi
    
    if grep -q "Could not determine" build.log; then
        echo "❌ Configuration Error: Check settings.gradle"
    fi
    
    if grep -q "failed to resolve" build.log; then
        echo "❌ Dependency Error: Check network connection"
    fi
    
    if grep -q "Permission denied" build.log; then
        echo "❌ Permission Error: Check file permissions"
    fi
fi

# 8. Android Studio Instructions
echo ""
echo "🏗 Android Studio Instructions"
echo "============================"
echo "1. Open Android Studio: open -a 'Android Studio' ."
echo "2. File → Invalidate Caches / Restart"
echo "3. File → Sync Project with Gradle Files"
echo "4. Check Build tab for real-time progress"
echo "5. Review build.log file for detailed errors"

# 9. Log file location
echo ""
echo "📝 Build log saved to: $(pwd)/build.log"

echo ""
echo "🎉 Script completed!"
echo "If build failed, check build.log for detailed error information."