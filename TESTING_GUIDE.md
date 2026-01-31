# ShopperApp Testing Guide

## Prerequisites

### Required Software
- **Android Studio** (latest version recommended)
- **Android SDK** (API 24+ target)
- **Java Development Kit** (JDK 11+)
- **Physical Android device** or **Emulator** with API 24+

### Firebase Setup (Optional for initial testing)
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project: "ShopperApp"
3. Download `google-services.json` and replace the placeholder file
4. Enable Authentication (Email/Password) and Realtime Database

---

## Step 1: Project Setup

### 1.1 Open Project in Android Studio
```bash
# Navigate to project directory
cd /Users/jamesbate/opencodetest/ShopperApp

# Open in Android Studio (or open IDE and navigate to folder)
open -a "Android Studio" .
```

### 1.2 Sync Dependencies
1. Android Studio will prompt to sync Gradle files
2. Wait for sync to complete (may take several minutes)
3. If sync fails, check:
   - Network connection
   - Android SDK installed
   - Gradle wrapper permissions

### 1.3 Check Project Structure
- Verify all folders appear correctly in Project view
- Look for `app/src/main/java/com/shopperapp` package structure
- Confirm `google-services.json` is in `app/` directory

---

## Step 2: Build & Run

### 2.1 Create Virtual Device (if needed)
1. **Tools → Device Manager**
2. Click **Create device**
3. Choose:
   - **Pixel 6** or similar modern phone
   - **API 34** (Android 14)
   - **Download** system image if needed
4. Launch emulator

### 2.2 Physical Device Setup (Recommended)
1. Enable **Developer Options**:
   - Settings → About Phone → Tap "Build number" 7 times
2. Enable **USB Debugging**:
   - Developer Options → USB Debugging
3. Connect device via USB and trust computer
4. Verify device appears in Android Studio

### 2.3 Build APK
```bash
# Clean and build
./gradlew clean
./gradlew assembleDebug

# Or use Android Studio: Build → Build Bundle(s) → Build APK(s)
```

### 2.4 Install & Launch
```bash
# Install on connected device/emulator
./gradlew installDebug

# Or use Android Studio's green "Run" button (▶️)
```

---

## Step 3: Testing Core Features

### 3.1 App Startup
1. **Launch app** on device/emulator
2. Verify **splash screen** appears (2 seconds)
3. Should redirect to **Login screen**

### 3.2 Authentication Flow
1. **Test Registration**:
   - Click "Don't have an account? Sign up"
   - Enter email, password, display name
   - Click "Sign Up"
   - Should redirect to shopping list
   
2. **Test Login**:
   - Log out and return to login
   - Enter registered credentials
   - Click "Sign In"
   - Should redirect to shopping list

3. **Test Guest Mode**:
   - Click "Continue as Guest"
   - Should work without authentication

### 3.3 Shopping List
1. **Add Items**:
   - Type item name in search bar
   - Press Enter or click floating action button
   - Item should appear in list

2. **Item Management**:
   - Click checkbox to complete items
   - Click ⋮ menu for edit/delete options
   - Toggle "Show completed items"

3. **Search & Suggestions**:
   - Type in search bar
   - See real-time filtering
   - Check for autocomplete suggestions

### 3.4 Camera Scanner
1. **Navigate to Scanner**:
   - Click bottom navigation "Scan"
   - Grant camera/audio permissions when prompted

2. **Test Recording**:
   - Click "Start Recording"
   - See red "REC" indicator
   - Point camera at item with barcode
   - Click stop button

3. **Test Scan Results**:
   - Dialog should show detected barcode/product
   - Add item to shopping list
   - Should return to shopping list with new item

### 3.5 Finance Screen
1. **Navigate to Finance**:
   - Click bottom navigation "Finance"
   - Should show spending analytics

2. **Test Views**:
   - Spending overview cards
   - Price history entries
   - Category breakdowns

---

## Step 4: Permissions Testing

### 4.1 Camera Permissions
1. Clear app data: Settings → Apps → ShopperApp → Storage → Clear Data
2. Launch app and navigate to Scanner
3. Should show permission request dialog
4. Test both "Allow" and "Deny" scenarios

### 4.2 Storage Permissions
1. Test on older Android versions (API < 29)
2. Verify storage access for saving videos

---

## Step 5: Firebase Integration Testing

### 5.1 Real-time Sync
1. Install app on **two different devices**
2. Login with same account on both
3. Add item on device A
4. Should appear on device B within seconds

### 5.2 Offline Mode
1. Disconnect from network
2. Add items to shopping list
3. Items should save locally
4. Reconnect to network
5. Should sync to Firebase automatically

---

## Step 6: Performance Testing

### 6.1 Memory Usage
1. Use Android Studio **Profiler**
2. Monitor memory while using scanner
3. Check for memory leaks after camera use

### 6.2 Camera Performance
1. Test video recording in various lighting
2. Verify smooth preview
3. Check ML Kit processing time

### 6.3 Database Performance
1. Add 100+ items to shopping list
2. Test search performance
3. Check UI smoothness

---

## Step 7: Error Handling Testing

### 7.1 Network Errors
1. Turn off WiFi/data
2. Try to login/register
3. Should show appropriate error messages

### 7.2 Invalid Inputs
1. Try empty item names
2. Test invalid email formats
3. Verify validation messages

### 7.3 Camera Errors
1. Cover camera lens
2. Test in very dark environments
3. Verify graceful error handling

---

## Step 8: UI/UX Testing

### 8.1 Screen Rotation
1. Rotate device in all screens
2. Verify layout adapts properly
3. Check data persistence

### 8.2 Different Screen Sizes
1. Test on tablets if available
2. Verify responsive layouts
3. Check button sizes and text readability

### 8.3 Material Design
1. Verify Material 3 theming
2. Check color contrast
3. Test dark mode if supported

---

## Step 9: Debugging Common Issues

### 9.1 Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# Check Gradle logs
./gradlew assembleDebug --stacktrace
```

### 9.2 Runtime Crashes
1. Check **Logcat** in Android Studio
2. Look for exceptions in app logs
3. Focus on:
   - Camera permission errors
   - Firebase initialization failures
   - ML Kit processing errors

### 9.3 Firebase Issues
1. Verify `google-services.json` is correct
2. Check Firebase console project settings
3. Ensure Authentication is enabled

### 9.4 Camera Issues
1. Check device camera hardware
2. Verify permissions in Settings
3. Test on different devices

---

## Step 10: Production Preparation

### 10.1 Release Build
```bash
# Build release APK
./gradlew assembleRelease

# Or build App Bundle (recommended)
./gradlew bundleRelease
```

### 10.2 Code Obfuscation
1. Enable ProGuard/R8 in release builds
2. Test obfuscated builds thoroughly
3. Keep necessary classes for Firebase/ML Kit

### 10.3 Signing Configuration
1. Create signing key in Android Studio
2. Configure build.gradle with signing config
3. Test signed APK installation

---

## Test Data for Validation

### Sample Items to Scan
- **Books:** ISBN barcodes
- **Groceries:** UPC barcodes on packaging
- **QR Codes:** Generate test QR codes online

### Test Scenarios
1. **Multiple users** editing same list
2. **Network connectivity** changes
3. **Large data sets** (100+ items)
4. **Long scanning sessions** (5+ minutes)

---

## Automated Testing Setup

### Unit Tests
```bash
# Run unit tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTestJacocoReport
```

### Integration Tests
```bash
# Run instrumented tests
./gradlew connectedAndroidTest

# Run on specific device
./gradlew connectedDebugAndroidTest
```

---

## Final Verification Checklist

- [ ] App launches without crashes
- [ ] All screens navigate properly
- [ ] Camera scanner works and detects items
- [ ] Shopping list CRUD operations work
- [ ] Real-time sync functions (if Firebase configured)
- [ ] Permissions handled gracefully
- [ ] UI is responsive on all screen sizes
- [ ] Memory usage is reasonable
- [ ] Build succeeds for release configuration

---

## Next Steps After Testing

1. **Fix any identified issues**
2. **Enhance based on user feedback**
3. **Add missing features** (expiry notifications, group management)
4. **Performance optimization**
5. **Prepare for app store submission**

This comprehensive testing guide covers all aspects of ShopperApp functionality and ensures the app works correctly across different devices and usage scenarios.