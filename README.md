# Rental Application @EvanLei-git @TriantisTheo @steliosorfa

A Spring Boot application for managing rental properties.

## User Guide

### User Roles

The application supports three types of users:
1. **Regular Users (Tenants)** - Can browse properties, schedule visits, and submit rental applications
2. **Property Owners** - Can list properties, manage their properties, and handle visit & rental requests
3. **Administrators** - Have full access to manage the platform.

### Main paths
- / & /home   -> home page url.
- /login      -> user can login straight away!
- /register   -> user can register as a tenant or landlord.
- /dashboard  -> role+ personal based dashboard.
- /edit       -> user can edit any info they want.
- /property/${houseid}/details  -> User can see more info of the house, schedule a time based visit, apply a rental application and/or report the apartment!
### Here are a few pictures of the page! :)
![image](https://github.com/user-attachments/assets/b2b76952-8704-437a-8e89-2d860e841735)
![image](https://github.com/user-attachments/assets/5190f030-c7e5-455a-b3d5-01aac3bcee8e)
![image](https://github.com/user-attachments/assets/bd2c9162-d246-441b-b0c1-0756c8e620d8)


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


## Development

This is a standard Spring Boot application using:
- Spring Web
- Spring Data JPA
- Spring Security
- Maven for dependency management

## Database info

Required database dependencies are included in the project's `pom.xml`, so no additional database installation is needed locally. The application will connect to the remote database automatically when started.
].
Make sure you have the appropriate database setup and configured in `application.properties`.
(We used [Render.com](https://render.com/) by @renderinc for our application tests. The database configuration needs to be setup by a user)

### application.properties configuration example
```
# Application Configuration
spring.application.name=RentalApp
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://your.server.goes.here:5432/dbsetup
spring.datasource.username=dbusername
spring.datasource.password=dbpassword
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pool Configuration
spring.datasource.hikari.minimumIdle=5
spring.datasource.hikari.maximumPoolSize=20
spring.datasource.hikari.idleTimeout=300000
spring.datasource.hikari.maxLifetime=1200000
spring.datasource.hikari.connectionTimeout=20000
spring.datasource.hikari.autoCommit=false

# SQL initialization - enable automatic schema creation
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
spring.jpa.hibernate.ddl-auto=update

# Show and format SQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Suppress constraint warnings
#logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper=ERROR

# Dialect
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# File Upload Configuration
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

# Performance Optimization
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.jdbc.fetch_size=50
spring.jpa.properties.hibernate.query.in_clause_parameter_padding=true
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Transaction settings
spring.jpa.properties.hibernate.connection.provider_disables_autocommit=true

# Large Object settings
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
```

