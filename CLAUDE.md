# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Unchained for Android is a Kotlin Android application that interfaces with Real Debrid APIs. It allows users to manage downloads, torrents, and stream media files. The app follows MVVM architecture with Dagger-Hilt dependency injection.

## Development Commands

### Building and Testing
```bash
# Build the project
./gradlew build

# Run lint checks (required before PR)
./gradlew ktLintCheck

# Clean and rebuild
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Code Formatting
```bash
# Check code style
./gradlew ktfmtCheck

# Format code automatically
./gradlew ktfmtFormat
```

## Architecture Overview

### Core Components
- **MVVM Pattern**: ViewModels manage UI state, Views handle presentation
- **Dagger-Hilt**: Dependency injection throughout the app
- **Navigation Component**: Fragment-based navigation with bottom navigation
- **Room Database**: Local data persistence for credentials, devices, and repositories
- **Retrofit + OkHttp**: Network layer for Real Debrid API calls
- **DataStore**: Secure storage for user credentials using Protocol Buffers
- **WorkManager**: Background downloads using OkHttp client

### Key Modules
- `authentication/`: FSM-based authentication flow handling
- `data/`: Repository pattern with local/remote data sources
- `di/`: Dagger-Hilt dependency injection modules
- `base/`: Main activity, application class, and shared components
- `lists/`: Download and torrent list management with pagination
- `search/`: Plugin-based search system with external repositories
- `settings/`: App configuration and preferences

### State Management
- **Authentication FSM**: Finite state machine for login flow in `statemachine/authentication/`
- **LiveData + Flow**: Reactive data streams between ViewModels and Views
- **Shared Preferences**: App settings and user preferences

### Network Architecture
- **ApiFactory**: Creates Retrofit instances with different OkHttp clients (classic and DNS-over-HTTPS)
- **API Helpers**: Repository pattern abstracting Retrofit service calls
- **Repository Classes**: Centralized data access combining local and remote sources

## Development Guidelines

### Branch Strategy
- Development happens on `dev` branch
- `master` branch is for releases
- Create feature branches from `dev`, not `master`

### Code Style
- Uses ktfmt with KotlinLang style (4 space indentation)
- Follow existing patterns in similar components
- No code comments unless explicitly required

### Architecture Patterns
- Follow existing MVVM + Repository pattern
- Use Dagger-Hilt for dependency injection
- Implement API helpers for new network calls
- Use Navigation Component for fragment navigation

### Key Dependencies
- **UI**: Material Design 3, DataBinding, Navigation Component
- **Network**: Retrofit, OkHttp, Moshi for JSON
- **Database**: Room with coroutines support
- **DI**: Dagger-Hilt
- **Async**: Kotlin Coroutines + Flow
- **Image Loading**: Coil
- **Parsing**: Jackson (XML), Jsoup (HTML)

### Testing
- Unit tests use JUnit and Robolectric
- Instrumented tests use Espresso
- Test resources include schema files for Room database

## File Structure Notes

### Main Packages
- `com.github.livingwithhippos.unchained.authentication`: Login flow management
- `com.github.livingwithhippos.unchained.data`: Data layer (local, remote, models, repositories)
- `com.github.livingwithhippos.unchained.di`: Dependency injection configuration
- `com.github.livingwithhippos.unchained.utilities`: Helper classes and extensions

### Build Configuration
- Uses Gradle version catalogs (`gradle/libs.versions.toml`)
- Supports signing configurations for release builds
- Protocol Buffers compilation for DataStore
- Proguard rules for release builds

### Resource Management
- Multi-language support (EN, IT, ES, FR)
- Material Design 3 theming with day/night modes
- Navigation graphs for different app sections