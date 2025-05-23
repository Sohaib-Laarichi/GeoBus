# GeoBus Android App

## Overview
The GeoBus Android App is a Kotlin-based mobile application that allows users to track buses in real-time, find nearby bus stops, and estimate arrival times in Marrakech. It provides a user-friendly interface for accessing the GeoBus system's features.

## Architecture
The Android app follows the MVVM (Model-View-ViewModel) architecture pattern:
- **Model**: Data classes and repositories
- **View**: Activities and Fragments
- **ViewModel**: Manages UI-related data and business logic
- **Repository**: Abstracts data sources from the rest of the app
- **Network**: Handles API communication

## Project Structure
```
geobusandroid/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/geobus/marrakech/
│   │   │   │       ├── api/            # API interfaces
│   │   │   │       ├── model/          # Data models
│   │   │   │       ├── network/        # Network configuration
│   │   │   │       ├── repository/     # Data repositories
│   │   │   │       ├── ui/             # Activities and Fragments
│   │   │   │       ├── util/           # Utility classes
│   │   │   │       ├── GeoBusApplication.kt  # Application class
│   │   │   │       └── MainActivity.kt       # Main activity
│   │   │   ├── res/                    # Resources (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml     # App manifest
│   │   ├── androidTest/                # Instrumented tests
│   │   └── test/                       # Unit tests
│   └── build.gradle                    # Gradle build script
└── build.gradle                        # Project-level Gradle script
```

## Technologies Used
- **Kotlin**: Programming language
- **Android Jetpack**: UI components and architecture
- **Retrofit**: HTTP client for API communication
- **OkHttp**: HTTP client for network operations
- **Navigation Component**: In-app navigation
- **Google Maps**: Map visualization
- **Coroutines**: Asynchronous programming
- **LiveData**: Observable data holder
- **ViewModel**: UI state management
- **Dagger Hilt**: Dependency injection
- **Gradle**: Build tool

## Features
- **User Authentication**: Register and login functionality
- **Bus Stop Information**: View all bus stops in Marrakech
- **Nearest Bus Stop**: Find the nearest bus stop based on user location
- **Real-time Bus Tracking**: Track buses in real-time on the map
- **Time Estimation**: Estimate walking time to bus stops and bus arrival times
- **Offline Support**: Basic functionality works offline
- **Notifications**: Receive notifications about bus arrivals

## Setup Instructions
1. Clone the repository
2. Open the project in Android Studio
3. Update the `BASE_URL` in `ApiClient.kt` to point to your backend server
   - For emulator: use `10.0.2.2` (localhost of the host machine)
   - For physical device: use the IP address of your computer
4. Obtain a Google Maps API key and add it to `local.properties`:
   ```
   MAPS_API_KEY=***************************
   ```
5. Build and run the app on an emulator or physical device

## Development Guidelines
1. Follow the Kotlin style guide and naming conventions
2. Use ViewModels to manage UI-related data
3. Use LiveData for observable data
4. Use Coroutines for asynchronous operations
5. Write unit tests for all new functionality
6. Use the Repository pattern for data access
7. Handle configuration changes properly
8. Follow Material Design guidelines for UI components

## UI Components
The app includes the following main screens:
- **Login/Register**: User authentication
- **Map View**: Shows bus locations and stops on a map
- **Bus Stop List**: Lists all bus stops
- **Bus Stop Details**: Shows details about a specific stop
- **Settings**: User preferences

## Data Flow
1. User interacts with the UI
2. ViewModel processes the user input
3. Repository fetches data from the API or local database
4. ViewModel updates the LiveData
5. UI observes LiveData changes and updates accordingly

## Error Handling
The app handles various error scenarios:
- Network errors
- API errors
- Location permission errors
- Google Maps errors

## Performance Considerations
- Use of efficient data structures
- Pagination for large lists
- Image caching
- Minimizing network requests
- Background processing for heavy operations

## Security
- Secure storage of user credentials
- HTTPS for all network communications
- Input validation
- Protection against common vulnerabilities

## Testing
- Unit tests for ViewModels and Repositories
- Instrumented tests for UI components
- End-to-end tests for critical user flows

## Deployment
1. Update version code and name in `build.gradle`
2. Generate a signed APK or App Bundle
3. Test the release build thoroughly
4. Deploy to Google Play Store
