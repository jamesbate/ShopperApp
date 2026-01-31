#!/bin/bash

# 🏗️ Interactive Gradle Build Helper for ShopperApp
# Run this script to interact with Gradle build and Android Studio

echo "🏗️ ShopperApp Interactive Gradle Build Helper"
echo "=========================================="
echo ""

# Check current directory
if [ ! -f "build.gradle" ]; then
    echo "❌ Error: Please run from ShopperApp root directory"
    exit 1
fi

echo "📍 Current Directory: $(pwd)"
echo ""

# Show menu
show_menu() {
    echo "🎯 Gradle Build Operations Menu:"
    echo "1. 🧹 Clean Build Environment"
    echo "2. 🔍 Check Dependencies"
    echo "3. 🏗️ Debug Build"
    echo "4. 📱 Assemble Debug APK"
    echo "5. 📦 Assemble Release APK"
    echo "6. 🔧 Open in Android Studio"
    echo "7. 📊 Show Project Info"
    echo "8. 🚪 Exit"
    echo ""
    echo -n "Select option [1-8]: "
}

# Clean build environment
clean_build() {
    echo "🧹 Cleaning build environment..."
    rm -rf app/build
    rm -rf .gradle
    ./gradlew clean
    echo "✅ Build environment cleaned"
}

# Check and resolve dependencies
check_dependencies() {
    echo "🔍 Checking and resolving dependencies..."
    ./gradlew dependencies --configuration=compileClasspath
    echo "✅ Dependencies checked"
}

# Debug build with detailed output
debug_build() {
    echo "🏗️ Starting debug build..."
    ./gradlew assembleDebug --info --stacktrace
    BUILD_RESULT=$?
    
    if [ $BUILD_RESULT -eq 0 ]; then
        echo "✅ Debug build completed successfully!"
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            echo "📱 Debug APK: $(ls -lh app/build/outputs/apk/debug/*.apk)"
        fi
    else
        echo "❌ Debug build failed with exit code: $BUILD_RESULT"
        echo "🔍 Check build log above for details"
    fi
}

# Assemble debug APK
assemble_debug() {
    echo "📱 Assembling debug APK..."
    ./gradlew assembleDebug
    
    if [ $? -eq 0 ]; then
        echo "✅ Debug APK assembled successfully!"
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            echo "📱 APK Location: app/build/outputs/apk/debug/app-debug.apk"
            echo "📊 APK Size: $(du -h app/build/outputs/apk/debug/*.apk)"
        fi
    else
        echo "❌ Debug build failed!"
    fi
}

# Assemble release APK
assemble_release() {
    echo "📦 Assembling release APK..."
    ./gradlew assembleRelease
    
    if [ $? -eq 0 ]; then
        echo "✅ Release APK assembled successfully!"
        if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
            echo "📱 APK Location: app/build/outputs/apk/release/app-release.apk"
        fi
    else
        echo "❌ Release build failed!"
    fi
}

# Open in Android Studio
open_android_studio() {
    echo "🔧 Opening Android Studio..."
    studio /Users/jamesbate/opencodetest/ShopperApp
    echo "✅ Android Studio opening..."
}

# Show project information
show_project_info() {
    echo "📊 ShopperApp Project Information:"
    echo "==============================="
    echo "📁 Kotlin Files: $(find . -name "*.kt" | wc -l)"
    echo "📋 XML Files: $(find . -name "*.xml" | wc -l)"
    echo "🏗️ Build Files: $(find . -name "build.gradle" | wc -l)"
    echo "🎯 Total Lines of Code: $(find . -name "*.kt" -exec wc -l {} + | tail -1 | awk '{sum+=$1} END {print sum}')"
    echo ""
    echo "🔧 Gradle Version: $(./gradlew --version 2>/dev/null)"
    echo "📱 Java Version: $(java -version 2>&1 | head -1)"
    echo ""
    echo "🌐 Repository: https://github.com/jamesbate/ShopperApp"
    echo "📋 Current Branch: $(git branch --show-current)"
    echo "📝 Last Commit: $(git log -1 --oneline)"
}

# Install APK on device
install_apk() {
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        echo "📱 Installing APK on device..."
        ./gradlew installDebug
        
        if [ $? -eq 0 ]; then
            echo "✅ APK installed successfully!"
        else
            echo "❌ APK installation failed!"
        fi
    else
        echo "❌ Debug APK not found! Build first."
    fi
}

# Test build
test_build() {
    echo "🧪 Running tests..."
    ./gradlew test
    TEST_RESULT=$?
    
    if [ $TEST_RESULT -eq 0 ]; then
        echo "✅ Tests completed successfully!"
    else
        echo "❌ Tests failed with exit code: $TEST_RESULT"
    fi
}

# Android Studio troubleshooter
android_studio_troubleshooter() {
    echo "🔧 Android Studio Troubleshooter:"
    echo "================================="
    echo "1. File → Settings → Build, Execution, Deployment → Build Tools"
    echo "2. Set 'Gradle JVM' to 'Embedded JDK'"
    echo "3. Check 'Use local Gradle distribution' is enabled"
    echo "4. File → Invalidate Caches / Restart"
    echo "5. File → Sync Project with Gradle Files"
    echo ""
    echo "📋 If issues persist:"
    echo "- Check Gradle version compatibility"
    echo "- Verify JDK is version 11+"
    echo "- Clear: rm -rf ~/.gradle/caches"
}

# Main menu loop
while true; do
    show_menu
    read -n choice
    echo ""
    
    case $choice in
        1)
            clean_build
            ;;
        2)
            check_dependencies
            ;;
        3)
            debug_build
            ;;
        4)
            assemble_debug
            ;;
        5)
            assemble_release
            ;;
        6)
            open_android_studio
            ;;
        7)
            show_project_info
            ;;
        8)
            echo "👋 Goodbye!"
            exit 0
            ;;
        *)
            echo "❌ Invalid option. Please select 1-8."
            ;;
    esac
    
    echo ""
    echo "Press Enter to continue..."
    read
done