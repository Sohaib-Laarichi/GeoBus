# GeoBus Backend

## Overview
The GeoBus Backend is a Spring Boot application that provides RESTful APIs for the GeoBus bus tracking system in Marrakech. It handles bus tracking, user authentication, and bus stop information.

## Architecture
The backend follows a standard layered architecture:
- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Manages data access
- **Model Layer**: Defines data entities
- **Security Layer**: Manages authentication and authorization

## Project Structure
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

## Technologies Used
- **Java 17**: Programming language
- **Spring Boot 3.1.0**: Application framework
- **Spring Data JPA**: Data access layer
- **Spring Security**: Authentication and authorization
- **MySQL**: Database
- **JWT**: Token-based authentication
- **Maven**: Build tool

## Setup Instructions
1. Ensure you have Java 17 and Maven installed
2. Configure MySQL database (create a database named `geobus`)
3. Update `application.properties` with your database credentials
4. Run `mvn clean install` to build the project
5. Run `mvn spring-boot:run` to start the server
6. The server will start on `http://localhost:8080`

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

## Development Guidelines
1. Follow the standard Java code style and naming conventions
2. Write unit tests for all new functionality
3. Document all API endpoints using Javadoc
4. Use DTOs for data transfer between layers
5. Handle exceptions properly using the exception handling mechanism

## Database Schema
The application uses the following main entities:
- **User**: Stores user information for authentication
- **BusPosition**: Stores real-time bus positions
- **Stop**: Stores information about bus stops
- **Route**: Stores information about bus routes

## Security
The application uses JWT (JSON Web Token) for authentication. The security flow is as follows:
1. User registers or logs in
2. Server generates a JWT token
3. Client includes the token in the Authorization header for subsequent requests
4. Server validates the token for protected endpoints

## Logging
The application uses SLF4J with Logback for logging. Log levels can be configured in the `application.properties` file.

## Monitoring
The application exposes actuator endpoints for monitoring. These can be accessed at `/actuator`.

## Deployment
The application can be deployed as a JAR file or as a Docker container. For Docker deployment:
1. Build the Docker image: `docker build -t geobus-backend .`
2. Run the container: `docker run -p 8080:8080 geobus-backend`