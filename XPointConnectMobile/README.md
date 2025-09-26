# XPoint Connect Mobile App

A modern Android application for EV charging station booking and management.

## 🚀 Getting Started with Android Studio

### Prerequisites

- **Android Studio Hedgehog** (2023.1.1) or later
- **JDK 17** or higher
- **Android SDK 34** (API level 34)
- **Minimum SDK 24** (API level 24)

### Opening the Project

1. **Open Android Studio**
2. **Select "Open an Existing Project"**
3. **Navigate to and select this folder:**
   ```
   d:\Campus\New folder\EAD\XPoint-Connect\XPointConnectMobile
   ```
4. **Click "OK"**

### First Time Setup

1. **Sync Project**: Android Studio will automatically start syncing. If not, click "Sync Now" in the notification bar.

2. **SDK Setup**: If prompted, allow Android Studio to download required SDK components.

3. **Build the Project**:
   - Go to **Build → Make Project** (Ctrl+F9)
   - Or click the hammer icon in the toolbar

### Running the App

1. **Connect an Android Device** (API 24+) or **Start an Emulator**

2. **Run the App**:
   - Click the green "Run" button (▶️)
   - Or press **Shift+F10**
   - Select your device/emulator

## 📱 App Features

- **User Authentication**: Login/Register with JWT tokens
- **Dashboard**: Overview of bookings and nearby stations
- **Station Browser**: Find and view charging stations
- **Booking Management**: Create and manage reservations
- **Profile Management**: User settings and preferences

## 🏗️ Architecture

- **MVVM Pattern**: ViewModel + LiveData + Repository
- **Material Design 3**: Modern UI components
- **Retrofit**: REST API integration
- **Kotlin Coroutines**: Asynchronous programming
- **Navigation Component**: Fragment navigation

## 🔧 Project Structure

```
app/src/main/java/com/xpoint/connect/
├── data/
│   ├── api/          # API interfaces and clients
│   ├── model/        # Data models and DTOs
│   └── repository/   # Repository pattern implementation
├── ui/
│   ├── auth/         # Login, Register, Splash
│   ├── main/         # MainActivity, Fragments, Adapters
│   ├── bookings/     # Booking management
│   └── profile/      # User profile
└── utils/            # Utility classes and extensions
```

## 🔗 Backend Integration

The app connects to the **XPoint-Connect API** backend:

- Base URL: Configure in `ApiClient.kt`
- Authentication: JWT token-based
- Endpoints: Users, Stations, Bookings

## 🛠️ Troubleshooting

### Build Issues

- **Clean Project**: Build → Clean Project
- **Invalidate Caches**: File → Invalidate Caches and Restart
- **Check Gradle**: Ensure Gradle sync completed successfully

### Common Issues

- **SDK Missing**: Install required Android SDK versions
- **Network Issues**: Check network security config for local development
- **Emulator Issues**: Use API 24+ emulator with Google Play Services

## 📋 Dependencies

Key dependencies included:

- **AndroidX**: Core, AppCompat, Material Design
- **Lifecycle**: ViewModel, LiveData
- **Navigation**: Fragment navigation
- **Retrofit**: HTTP client
- **Gson**: JSON parsing
- **RecyclerView**: List displays

---

**Ready to run in Android Studio!** 🎯
