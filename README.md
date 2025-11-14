# MyWH - Warehouse Management App

A specialized Android application for warehouse management, developed to meet specific user requirements for organizing and tracking folders and storage locations.

## Features

- **Folder Management**: Create, edit, delete, and organize folders with descriptions
- **Storage Management**: Manage multiple storage locations and assign folders to them
- **Search Functionality**: Quick search through folders and their contents
- **Import/Export**: Support for JSON data export/import and MySQL dump import
- **Multi-language Support**: English, Russian, and German localization
- **Themes**: Dark and light theme options
- **Statistics**: Overview of folders, storages, and usage statistics
- **Favorites**: Mark important folders for quick access

## Requirements

- Android API 24+ (Android 7.0)
- Kotlin 1.8+
- Room database for local storage

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/vtstv/MyWH.git
   ```

2. Open in Android Studio

3. Build and run on device/emulator

## Usage

- **Home Screen**: View recent folders and quick access to main functions
- **Folders Tab**: Browse and manage all folders with search
- **Storages Tab**: Manage storage locations
- **Favorites Tab**: Access marked folders
- **Settings**: Configure themes, language, import/export data

## Architecture

- **MVVM Pattern**: ViewModels, LiveData, and Repository pattern
- **Room Database**: Local SQLite database with entities for folders and storages
- **Material Design**: Modern Android UI components
- **Coroutines**: Asynchronous operations

## License

This project is developed for specific user requirements and is not intended for general distribution.
