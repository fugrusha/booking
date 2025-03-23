# Unit Manager Application

A Spring Boot application for managing accommodation units, bookings, and payments. This system allows property managers to list various types of accommodation units, while users can search for available units, make bookings, and process payments.

## Features

### Unit Management
- Create, read, update, and delete accommodation units
- Search for units by various criteria:
  - Number of rooms
  - Accommodation type (HOME, FLAT, APARTMENTS)
  - Floor
  - Price range
  - Availability for specific date ranges
- Redis-based caching for available units count

### Booking Management
- Create bookings for specific date ranges
- View booking details
- Cancel bookings
- View bookings by user or unit
- Automatic expiration of unpaid bookings

### Payment Processing
- Process payments for bookings
- Track payment status

### Scheduled Jobs
- Automatic processing of expired bookings
- Hourly refresh of the unit availability cache

## Tech Stack

- **Java 23** - Latest Java version with enhanced features
- **Spring Boot 3.4.3** - Framework for building production-ready applications
- **Spring Data JPA** - Data access layer with Hibernate
- **Spring Data Redis** - Redis integration for caching
- **Spring Web** - RESTful API development
- **PostgreSQL 16** - Relational database for persistent storage
- **Redis 7** - In-memory data store for caching
- **Liquibase** - Database schema version control
- **MapStruct 1.5.5** - Object mapping between entities and DTOs
- **Lombok** - Reduces boilerplate code
- **Springdoc OpenAPI 2.8.5** - API documentation
- **Testcontainers 1.19.7** - Integration testing with containerized dependencies

## API Endpoints

### Units
- `POST /api/v1/units` - Create a new unit
- `GET /api/v1/units/{id}` - Get a unit by ID
- `PUT /api/v1/units/{id}` - Update a unit
- `DELETE /api/v1/units/{id}` - Delete a unit
- `GET /api/v1/units` - Search for units by criteria
- `GET /api/v1/units/{id}/availability` - Check if a unit is available for a date range
- `GET /api/v1/units/available/count` - Get count of available units

### Bookings
- `POST /api/v1/bookings` - Create a new booking
- `GET /api/v1/bookings/{id}` - Get a booking by ID
- `POST /api/v1/bookings/{id}/cancel` - Cancel a booking
- `GET /api/v1/bookings/user/{userId}` - Get bookings by user ID
- `GET /api/v1/bookings/unit/{unitId}` - Get bookings by unit ID

### Payments
- `POST /api/v1/payments` - Process payment for a booking

## Running Locally

### Prerequisites
- Docker and Docker Compose
- Java 23 (for development)
- Gradle (for development)

### Using Docker Compose

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd unitmanager
   ```

2. Start the required services (PostgreSQL and Redis) using Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. Build and run the application:
   ```bash
   ./gradlew bootRun
   ```

4. Access the API at `http://localhost:8080/api/v1`
   
5. Access the Swagger UI documentation at `http://localhost:8080/swagger-ui.html`

### Configuration

The application can be configured through `src/main/resources/application.properties`. Key configurations include:

- Database connection settings
- Redis connection settings
- Booking payment threshold (15 minutes by default)
- System markup for pricing (15% by default)
- Scheduled job cron expressions

## Database Schema

The application uses the following main entities:

1. **Units** - Stores information about accommodation units
   - Properties: number of rooms, accommodation type, floor, base cost, total cost, description

2. **Bookings** - Stores information about bookings
   - Properties: unit, user, start date, end date, total price, status, payment deadline

3. **Payments** - Stores information about payments
   - Properties: booking, amount, status

4. **Users** - Stores user information
   - Properties: username, email, password hash, first name, last name

## Caching Strategy

The application uses Redis for caching:

- Available units count is cached and updated when units are booked or released
- Cache is refreshed hourly via a scheduled job

## Scheduled Jobs

1. **ExpiredBookingJob** - Runs every 5 minutes to process bookings that have passed their payment deadline
2. **RefreshCacheJob** - Runs hourly to refresh the unit availability cache

## Development

### Building the Application
```bash
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

### Integration Tests
The application uses Testcontainers for integration testing, which requires Docker to be running.
```bash
./gradlew integrationTest
