# Rental Application @EvanLei-git @TriantisTheo @steliosorfa

A Spring Boot application for managing rental properties.

## Prerequisites

- Java 21
- Maven
- Git

## Installation

1. Clone the repository:
```bash
git clone [https://github.com/EvanLei-git/RentalApp]
cd RentalApp
```

## Running the Application

You can run the application in two ways:

### Using Maven Wrapper (Recommended)

```bash
./mvnw spring-boot:run
```

### Using Maven (if installed globally)

```bash
mvn spring-boot:run
```

The application will start and be available at `http://localhost:8080` by default.

To stop the application, press `Ctrl+C` in the terminal.

## User Guide

### User Roles

The application supports three types of users:
1. **Regular Users (Tenants)** - Can browse properties, schedule visits, and submit rental applications
2. **Property Owners** - Can list properties, manage their properties, and handle visit & rental requests
3. **Administrators** - Have full access to manage the platform


## Development

This is a standard Spring Boot application using:
- Spring Web
- Spring Data JPA
- Spring Security
- Maven for dependency management

## Database

The application uses PostgreSQL database hosted on Render.com. The database configuration is already set up in
[application.properties](cci:7://file:///home/harris/intellij/RentalApp/src/main/resources/application.properties:0:0-0:0).

Required database dependencies are included in the project's `pom.xml`, so no additional database installation is needed locally. The application will connect to the remote database automatically when started.
].
Make sure you have the appropriate database setup and configured in `application.properties`.

