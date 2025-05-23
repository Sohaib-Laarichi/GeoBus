# GeoBus - Bus Tracking System for Marrakech

## Project Overview
GeoBus is a comprehensive bus tracking system for Marrakech, Morocco. The system consists of two main components:
1. **Backend Server**: A Spring Boot application that provides RESTful APIs for bus tracking, user authentication, and bus stop information.
2. **Android Mobile App**: A Kotlin-based Android application that allows users to track buses in real-time, find nearby bus stops, and estimate arrival times.

The system aims to improve the public transportation experience in Marrakech by providing real-time information about bus locations and estimated arrival times.

Each component has its own detailed README file:
- [Backend README](geobus-backend/README.md)
- [Android App README](geobusandroid/README.md)

## Architecture

### Backend Architecture
The backend follows a standard layered architecture:
- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Manages data access
- **Model Layer**: Defines data entities
- **Security Layer**: Manages authentication and authorization

### Android App Architecture
The Android app follows the MVVM (Model-View-ViewModel) architecture pattern:
- **Model**: Data classes and repositories
- **View**: Activities and Fragments
- **ViewModel**: Manages UI-related data and business logic
- **Repository**: Abstracts data sources from the rest of the app
- **Network**: Handles API communication

## Features
- **User Authentication**: Register and login functionality
- **Bus Stop Information**: View all bus stops in Marrakech
- **Nearest Bus Stop**: Find the nearest bus stop based on user location
- **Real-time Bus Tracking**: Track buses in real-time on the map
- **Time Estimation**: Estimate walking time to bus stops and bus arrival times

## Technologies Used

### Backend
- **Java 17**: Programming language
- **Spring Boot 3.1.0**: Application framework
- **Spring Data JPA**: Data access layer
- **Spring Security**: Authentication and authorization
- **MySQL**: Database
- **JWT**: Token-based authentication
- **Maven**: Build tool

### Android App
- **Kotlin**: Programming language
- **Android Jetpack**: UI components and architecture
- **Retrofit**: HTTP client
- **OkHttp**: HTTP client
- **Navigation Component**: In-app navigation
- **Google Maps**: Map visualization
- **Gradle**: Build tool

## Project Structure

### Backend Structure
```
geobus-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/geobus/marrakech/
│   │   │       ├── config/         # Configuration classes
│   │   │       ├── controller/     # REST API controllers
│   │   │       ├── dto/            # Data Transfer Objects
│   │   │       ├── exception/      # Exception handling
│   │   │       ├── model/          # Entity classes
│   │   │       ├── repository/     # Data access layer
│   │   │       ├── security/       # Security configuration
│   │   │       ├── service/        # Business logic
│   │   │       └── GeoBusApplication.java  # Main class
│   │   └── resources/              # Application properties, SQL scripts
│   └── test/                       # Unit and integration tests
└── pom.xml                         # Maven configuration
```

### Android App Structure
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

## Setup Instructions

### Backend Setup
1. Clone the repository
2. Configure MySQL database (create a database named `geobus`)
3. Update `application.properties` with your database credentials
4. Run `mvn clean install` to build the project
5. Run `mvn spring-boot:run` to start the server
6. The server will start on `http://localhost:8080`

### Android App Setup
1. Open the project in Android Studio
2. Update the `BASE_URL` in `ApiClient.kt` to point to your backend server
   - For emulator: use `10.0.2.2` (localhost of the host machine)
   - For physical device: use the IP address of your computer
3. Build and run the app on an emulator or physical device

## API Endpoints

### Authentication
- `POST /auth/register`: Register a new user
- `POST /auth/login`: Login a user

### Bus Stops
- `GET /stops/marrakech`: Get all bus stops in Marrakech
- `GET /stops/nearest?lat={lat}&lon={lon}`: Find the nearest bus stop
- `GET /stops/time?userLat={lat}&userLon={lon}&stopId={id}`: Get time estimation to a stop

### Bus Positions
- `GET /positions/latest`: Get latest bus positions
- `POST /positions/update`: Update bus position (authenticated)

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
This project is licensed under the MIT License - see the LICENSE file for details.
