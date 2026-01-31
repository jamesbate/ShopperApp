#!/bin/bash

# ShopperApp Quick Setup Script
# This script helps set up the development environment quickly

echo "🛒 ShopperApp Quick Setup"
echo "========================="

# Check if we're in the right directory
if [ ! -f "build.gradle" ] || [ ! -d "app" ]; then
    echo "❌ Error: Please run this script from the ShopperApp root directory"
    exit 1
fi

# Check Android SDK
echo "📱 Checking Android SDK..."
if command -v adb &> /dev/null; then
    echo "✅ Android SDK found"
else
    echo "❌ Android SDK not found. Please install Android Studio first."
    exit 1
fi

# Check Java
echo "☕ Checking Java..."
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
    if [ "$java_version" -ge "11" ]; then
        echo "✅ Java $java_version found"
    else
        echo "⚠️  Java version $java_version found. JDK 11+ recommended"
    fi
else
    echo "❌ Java not found. Please install JDK 11+"
    exit 1
fi

# Gradle wrapper permissions
echo "🔧 Setting up Gradle..."
chmod +x gradlew
if [ ! -f "gradlew" ]; then
    echo "❌ Gradle wrapper not found"
    exit 1
fi
echo "✅ Gradle wrapper ready"

# Create local.properties if missing
if [ ! -f "local.properties" ]; then
    echo "📝 Creating local.properties..."
    echo "# ShopperApp local configuration" > local.properties
    echo "sdk.dir=$(echo \$ANDROID_HOME)" >> local.properties
    echo "✅ local.properties created"
fi

# Check Firebase config
echo "🔥 Checking Firebase configuration..."
if [ ! -f "app/google-services.json" ]; then
    echo "⚠️  google-services.json found (using placeholder)"
    echo "   To use real Firebase, replace with your own config"
else
    echo "✅ Firebase configuration found"
fi

echo ""
echo "🚀 Quick Commands:"
echo "=================="
echo "Build and install:   ./gradlew installDebug"
echo "Run tests:         ./gradlew test"
echo "Clean build:        ./gradlew clean"
echo "Release build:       ./gradlew assembleRelease"
echo ""
echo "📖 For detailed testing instructions, see: TESTING_GUIDE.md"
echo ""
echo "🎉 Setup complete! You can now open the project in Android Studio."
echo "   Command: open -a 'Android Studio' ."