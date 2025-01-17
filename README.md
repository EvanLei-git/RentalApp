# Rental Application

A Spring Boot application for managing rental properties.

## Prerequisites

- Java 17 or higher
- Maven (optional, as the project includes Maven wrapper)
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
2. **Property Owners** - Can list properties, manage their properties, and handle visit requests
3. **Administrators** - Have full access to manage the platform

### Features by User Role

#### For Regular Users (Tenants)
1. **Property Browsing**
   - View all available properties
   - Filter properties by location, price, size, and amenities
   - View detailed property information and images

2. **Property Visits**
   - Schedule property visits
   - View upcoming and past visit appointments
   - Cancel or reschedule visits
   - Receive notifications about visit status

3. **Rental Applications**
   - Submit rental applications for properties
   - Track application status
   - Upload required documents
   - Communicate with property owners

#### For Property Owners
1. **Property Management**
   - List new properties
   - Edit property details and availability
   - Upload property images
   - Set rental terms and conditions

2. **Visit Management**
   - View and manage visit requests
   - Accept or decline visit requests
   - Set available time slots for visits
   - Mark properties as rented/unavailable

3. **Application Management**
   - Review rental applications
   - Accept or decline applications
   - Communicate with potential tenants
   - Generate rental agreements

#### For Administrators
1. **User Management**
   - Manage user accounts
   - Assign or modify user roles
   - Handle user reports and issues

2. **Platform Management**
   - Monitor system activity
   - Generate reports
   - Manage property listings
   - Handle disputes between users

### Getting Started

1. **Registration and Login**
   - Visit `http://localhost:8080/register` to create a new account
   - Choose your role (Tenant or Property Owner)
   - Complete the registration form
   - Login at `http://localhost:8080/login`

2. **Using the Dashboard**
   - After logging in, you'll be directed to your role-specific dashboard
   - Navigate through different sections using the sidebar menu
   - Access your profile settings and notifications

3. **Property Search**
   - Use the search bar to find properties
   - Apply filters to narrow down results
   - Save favorite properties
   - View property details and schedule visits

## Development

This is a standard Spring Boot application using:
- Spring Web
- Spring Data JPA
- Spring Security
- Maven for dependency management

## Database

The application uses [he application uses PostgreSQL database hosted on Render.com. The database configuration is already set up in 
[application.properties](cci:7://file:///home/harris/intellij/RentalApp/src/main/resources/application.properties:0:0-0:0). 

Required database dependencies are included in the project's `pom.xml`, so no additional database installation is needed locally. The application will connect to the remote database automatically when started.
]. 
Make sure you have the appropriate database setup and configured in `application.properties`.

