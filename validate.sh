#!/bin/bash

# ShopperApp Validation Script
# Checks if the project is ready for testing

echo "🔍 ShopperApp Project Validation"
echo "================================"

errors=0
warnings=0

# Check essential files
echo "📁 Checking project structure..."
essential_files=(
    "app/build.gradle"
    "build.gradle" 
    "settings.gradle"
    "gradle.properties"
    "app/src/main/AndroidManifest.xml"
    "google-services.json"
)

for file in "${essential_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (missing)"
        ((errors++))
    fi
done

# Check essential directories
echo ""
echo "📂 Checking directories..."
essential_dirs=(
    "app/src/main/java/com/shopperapp"
    "app/src/main/res"
    "app/src/main/java/com/shopperapp/ui/screens"
    "app/src/main/java/com/shopperapp/ui/viewmodels"
    "app/src/main/java/com/shopperapp/data"
)

for dir in "${essential_dirs[@]}"; do
    if [ -d "$dir" ]; then
        echo "✅ $dir"
    else
        echo "❌ $dir (missing)"
        ((errors++))
    fi
done

# Check for key Kotlin files
echo ""
echo "📱 Checking key Kotlin files..."
key_files=(
    "app/src/main/java/com/shopperapp/MainActivity.kt"
    "app/src/main/java/com/shopperapp/ShopperApplication.kt"
    "app/src/main/java/com/shopperapp/ui/screens/ShoppingListScreen.kt"
    "app/src/main/java/com/shopperapp/ui/screens/ScannerScreen.kt"
    "app/src/main/java/com/shopperapp/ui/viewmodels/ShoppingListViewModel.kt"
    "app/src/main/java/com/shopperapp/ui/viewmodels/ScannerViewModel.kt"
    "app/src/main/java/com/shopperapp/camera/CameraManager.kt"
    "app/src/main/java/com/shopperapp/ml/MLKitAnalyzer.kt"
)

for file in "${key_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $(basename $file)"
    else
        echo "❌ $(basename $file) (missing)"
        ((errors++))
    fi
done

# Check file count
echo ""
echo "📊 File count analysis:"
kotlin_files=$(find app/src/main/java -name "*.kt" 2>/dev/null | wc -l)
xml_files=$(find app/src/main/res -name "*.xml" 2>/dev/null | wc -l)

echo "📝 Kotlin files: $kotlin_files"
echo "📄 XML files: $xml_files"

if [ $kotlin_files -lt 30 ]; then
    echo "⚠️  Low Kotlin file count (expected 30+)"
    ((warnings++))
fi

# Check Gradle configuration
echo ""
echo "🔧 Checking Gradle configuration..."
if grep -q "compose-bom" app/build.gradle; then
    echo "✅ Compose BOM found"
else
    echo "⚠️  Compose BOM not found"
    ((warnings++))
fi

if grep -q "com.google.dagger:hilt-android" app/build.gradle; then
    echo "✅ Hilt dependency found"
else
    echo "⚠️  Hilt dependency not found"
    ((warnings++))
fi

if grep -q "com.google.firebase:firebase-bom" app/build.gradle; then
    echo "✅ Firebase BOM found"
else
    echo "⚠️  Firebase BOM not found"
    ((warnings++))
fi

# Summary
echo ""
echo "📋 Validation Summary:"
echo "===================="
echo "Errors: $errors"
echo "Warnings: $warnings"

if [ $errors -eq 0 ]; then
    echo "🎉 Project is ready for testing!"
    echo ""
    echo "Next steps:"
    echo "1. Open project in Android Studio: open -a 'Android Studio' ."
    echo "2. Sync Gradle dependencies"
    echo "3. Follow TESTING_GUIDE.md for detailed testing"
else
    echo "❌ Project has $errors critical issues that need to be fixed"
    exit 1
fi

if [ $warnings -gt 0 ]; then
    echo "⚠️  Project has $warnings warnings but should work"
fi

echo ""
echo "📖 Detailed testing guide available: TESTING_GUIDE.md"