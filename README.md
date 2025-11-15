# MyWH - Warehouse Management App

A specialized Android app for organizing and tracking folders and storage locations.

<img src="https://github.com/user-attachments/assets/b69b251f-68a1-4aab-8b33-677ab38bdb04" width="20%">

## ✨ Features

### 📂 Core Functionality
- Folder management: Create, edit, delete, organize with descriptions
- Storage management: Handle locations and assign folders
- Batch operations: Move/delete multiple folders via long-press
- Search: Real-time filtering with clear button
- Favorites: Mark folders for quick access, synced across screens

### 🔐 Security
- Password protection with SHA-256 encryption
- Biometric authentication (fingerprint if supported)
- Auto-lock: 1 hour / 1 day / 1 week intervals

### 🎨 Customization
- Themes: Dark/light with purple/violet scheme
- Languages: English, Russian, German
- Accessibility: Font size 85%-150%

### 💾 Data Management
- Import/Export: JSON backup/restore, MySQL dump import
- Statistics: Overview of folders, storages, metrics
- Recent folders: Quick access to modified items

## 📱 Requirements

- Android API 26+ (8.0 Oreo)
- ~3 MB app size (Release)
- Permissions: Biometric, storage access

## 🚀 Installation

### From APK
1. Download latest from [Releases](https://github.com/vtstv/MyWH/releases)
2. Install and grant permissions

### Build from Source

**Prerequisites:** Android Studio Hedgehog 2023.1.1+, JDK 11+, SDK API 35

1. Clone: `git clone https://github.com/vtstv/MyWH.git && cd MyWH`
2. Debug APK: `./gradlew assembleDebug`
3. Release APK: `./gradlew assembleRelease`
4. Sign: Use `sign-apk.ps1` or apksigner (debug keystore for testing)
5. Install: `adb install app/build/outputs/apk/release/app-release-signed.apk`

## 📖 Usage

### APK Signing
Script `sign-apk.ps1` creates keystore, signs, verifies, installs. 

### Main Screens
- Home: Recent folders, storages
- All Folders: Paginated list (30/page)
- Storages: Manage locations
- Favorites: Marked folders
- Statistics: Metrics
- Settings: Customize app

### Tips
- Long-press for batch mode
- Swipe to refresh
- Search bar for filtering, clear with X
- Enable password + biometric

## 🏗️ Architecture

### Tech Stack
- Kotlin, Jetpack Compose + Material 3, MVVM, Room, Coroutines + Flow, Compose Navigation, Manual DI, BiometricPrompt + SHA-256

## 📄 License

Developed for specific needs, not for general distribution.

**Author**: Murr (vtstv)  
**GitHub**: [github.com/vtstv/MyWH](https://github.com/vtstv/MyWH)  
**Version**: 1.0

---

Specialized for personal warehouse management.
