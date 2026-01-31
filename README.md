# 🛒 ShopperApp

A modern Android grocery shopping assistant app built with Kotlin and Jetpack Compose.

## ✨ Features

- **🛍 Smart Scanning**: AI-powered barcode and text recognition
- **👥 Shared Lists**: Real-time collaboration between family/group members
- **💰 Finance Tracking**: Price history, spending analytics, and cost splitting
- **⏰ Expiry Management**: Automatic expiry date detection and notifications
- **📱 Modern UI**: Material 3 design with smooth animations
- **🔒 Offline Support**: Works without internet, syncs when reconnected

## 🏗️ Architecture

- **MVVM**: Model-View-ViewModel with clean architecture
- **Repository Pattern**: Centralized data access with offline-first approach
- **Dependency Injection**: Hilt for proper DI
- **Coroutines**: Reactive programming with Kotlin Coroutines
- **Room Database**: Local persistence with SQLite
- **Firebase**: Real-time sync and authentication

## 📦 Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Repository
- **DI**: Hilt
- **Database**: Room (local) + Firebase (remote)
- **Camera**: CameraX
- **ML/AI**: ML Kit (Barcode + Text Recognition)
- **Async**: Coroutines + Flow

## 🚀 Quick Start

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 24+
- JDK 11+
- Physical device or emulator

### Setup
```bash
# Clone the project
git clone <repository-url>
cd ShopperApp

# Run validation
bash validate.sh

# Open in Android Studio
open -a "Android Studio" .
```

## 📱 Installation

### Development Build
```bash
# Build and install debug APK
./gradlew installDebug

# Run tests
./gradlew test

# Build release
./gradlew assembleRelease
```

### Google Play Store
- Follow the **TESTING_GUIDE.md** for detailed testing
- Sign release APK with your keystore
- Upload to Google Play Console

## 🔧 Configuration

### Firebase Setup
1. Create project at [Firebase Console](https://console.firebase.google.com)
2. Enable Authentication (Email/Password)
3. Enable Realtime Database
4. Download `google-services.json`
5. Replace placeholder file in `app/` directory

### Permissions
The app requires these permissions:
- `CAMERA`: For scanning items
- `RECORD_AUDIO`: For video recording
- `INTERNET`: For Firebase sync
- `POST_NOTIFICATIONS`: For expiry reminders

## 🧪 Testing

### Automated Testing
```bash
# Run validation
bash validate.sh

# Run unit tests
./gradlew test

# Run integration tests
./gradlew connectedAndroidTest
```

### Manual Testing
Follow **TESTING_GUIDE.md** for comprehensive testing scenarios:
- ✅ Authentication flow
- ✅ Shopping list CRUD
- ✅ Camera scanner functionality
- ✅ Real-time collaboration
- ✅ Finance tracking
- ✅ Error handling

## 📁 Project Structure

```
app/src/main/java/com/shopperapp/
├── ui/
│   ├── screens/          # Compose UI screens
│   ├── viewmodels/       # ViewModels for state management
│   └── navigation/       # Navigation setup
├── data/
│   ├── database/         # Room database and DAOs
│   ├── repository/       # Repository implementations
│   └── models/          # Data models/entities
├── camera/             # CameraX integration
├── ml/                 # ML Kit analysis
├── di/                 # Hilt dependency injection
└── data/messaging/      # Firebase messaging
```

## 🎯 Key Features

### Smart Scanning
- **Video-based recording**: Capture items from multiple angles
- **Barcode detection**: EAN-13, UPC-A, QR codes
- **Text recognition**: Extract expiry dates from packaging
- **AI analysis**: Identify products without barcodes

### Real-time Collaboration
- **Live sync**: Changes appear instantly for all users
- **Presence tracking**: See who's currently shopping
- **Conflict resolution**: Handle simultaneous edits
- **Offline support**: Works without internet

### Finance Management
- **Price history**: Track prices across stores
- **Spending analytics**: Category breakdown and trends
- **Cost splitting**: Fair payment distribution
- **Store comparison**: Find best prices

### Expiry Management
- **Automatic detection**: ML extracts dates from packaging
- **Smart reminders**: Notifications before expiry
- **Status tracking**: Mark items as opened/closed
- **Waste reduction**: Use items before they expire

## 🔒 Security & Privacy

- **Local-first**: Data stored locally, encrypted in transit
- **Firebase Auth**: Secure user authentication
- **Minimal permissions**: Only request necessary permissions
- **No data mining**: User data stays private

## 📊 Performance

- **Optimized queries**: Efficient database access patterns
- **Memory efficient**: Proper resource management
- **Smooth UI**: 60fps animations and interactions
- **Battery conscious**: Efficient camera and ML usage

## 🛣️ Roadmap

- [ ] **Enhanced AI**: Better product recognition
- [ ] **Recipe suggestions**: Based on available items
- [ ] **Store integration**: Direct grocery store APIs
- [ ] **Voice commands**: Add items by voice
- [ ] **Wear OS support**: Smartwatch integration
- [ ] **Web dashboard**: Manage lists from browser

## 🤝 Contributing

1. Fork the project
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For issues and feature requests:
- Create an issue on GitHub
- Check existing issues first
- Provide device info and logs
- Include steps to reproduce

---

**Built with ❤️ for smart grocery shopping**