# Drone Delivery Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-ready, enterprise-grade Spring Boot application for managing autonomous drone delivery operations. Built with modern Java technologies and cloud-native patterns.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Components](#components)
- [Features](#features)
- [Getting Started](#getting-started)
- [Performance & Optimizations](#performance--optimizations)
- [Security](#security)
- [API Documentation](#api-documentation)
- [Release Process](#release-process)
- [Contributing](#contributing)

---

## Overview

This repository contains a complete drone delivery management platform. The system handles:

- **Order Management**: Create, track, and manage delivery orders with real-time status updates
- **Drone Fleet Management**: Monitor and control autonomous drone operations
- **Job Assignment**: Intelligent job reservation and assignment to available drones
- **Failure Recovery**: Automatic order reassignment when drones malfunction
- **Authentication & Authorization**: Secure JWT-based access control with role-based permissions

### Key Highlights

- **Production-Ready**: Thread-safe operations with pessimistic locking
- **High Performance**: Optimized database queries with JOIN FETCH (95% query reduction)
- **Scalable**: HikariCP connection pooling with leak detection
- **Secure**: JWT authentication with role-based access control (RBAC)
- **Observable**: Comprehensive logging, health checks, and metrics
- **Cloud-Native**: Containerized deployment with Docker Compose

---

## Architecture

### Application Architecture

```
+-----------------------------+
|   Drone Delivery Service    | :8088
|      (Spring Boot)          |
|                             |
|  - REST API                 |
|  - JWT Authentication       |
|  - Business Logic           |
|  - OpenAPI/Swagger          |
+-------------+---------------+
              |
              | JDBC + HikariCP
              |
+-------------v---------------+
|      PostgreSQL 16          | :5432
|                             |
|  - Flyway Migrations        |
|  - Pessimistic Locking      |
|  - Optimized Queries        |
+-----------------------------+
```

### Communication

- **Client to Service**: HTTP/REST with JWT Bearer tokens
- **Service to Database**: JDBC with HikariCP connection pooling
- **Authentication**: Stateless JWT tokens (24-hour expiration)

---

## Technology Stack

### Core Technologies

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.4.5 |
| **Database** | PostgreSQL | 16 |
| **Migration** | Flyway | Latest |
| **Security** | Spring Security + JWT | JJWT 0.12.6 |
| **Object Mapping** | MapStruct | 1.6.3 |
| **Boilerplate Reduction** | Lombok | Latest |
| **API Documentation** | SpringDoc OpenAPI | 2.7.0 |
| **Testing** | JUnit 5 + Testcontainers | 1.20.6 |
| **Containerization** | Docker + Docker Compose | Latest |

### Key Libraries

- **HikariCP**: High-performance JDBC connection pooling
- **Jackson**: JSON serialization/deserialization
- **SLF4J + Logback**: Structured logging
- **Spring Data JPA**: Database abstraction with Hibernate
- **Bean Validation (JSR-380)**: Request validation
- **Testcontainers**: Docker-based integration testing with PostgreSQL
- **MockMvc**: Spring MVC testing framework
- **AssertJ**: Fluent assertion library

---

## Components

### Drone Delivery Service (Port 8088)

The main application service providing:

- **RESTful API**: Complete CRUD operations for orders and drones
- **JWT Authentication**: Passwordless authentication with auto-provisioning
- **Role-Based Access Control**: ADMIN, ENDUSER, DRONE roles with method-level security
- **Order Management**: Create, track, withdraw, and manage delivery orders
- **Drone Operations**: Job discovery, reservation, pickup, delivery, and location tracking
- **Failure Recovery**: Automatic order reassignment when drones malfunction
- **OpenAPI Documentation**: Interactive Swagger UI at `/swagger-ui.html`
- **Health Checks**: Liveness and readiness probes for container orchestration
- **Metrics**: Prometheus-compatible metrics endpoint

---

## Features

### Order Management

- **Create Orders**: End users can create delivery orders with origin/destination coordinates
- **Track Orders**: Real-time order status tracking (PENDING -> RESERVED -> PICKED_UP -> DELIVERED)
- **Withdraw Orders**: Users can cancel orders before delivery
- **Admin Controls**: Admins can update order origins and destinations

### Drone Operations

- **Job Discovery**: Drones can view available delivery jobs
- **Job Reservation**: Thread-safe job reservation with pessimistic locking
- **Pickup & Delivery**: Complete delivery workflow with timestamp tracking
- **Location Updates**: Real-time drone location tracking
- **Failure Handling**: Automatic order reassignment when drones break

### Security Features

- **Passwordless Authentication**: Auto-provisioning of users and drones
- **JWT Tokens**: Secure, stateless authentication (24-hour expiration)
- **Role-Based Access Control**: ADMIN, ENDUSER, DRONE roles
- **Method-Level Security**: `@PreAuthorize` annotations on implementation classes

### Data Validation

- **Multi-Layer Validation**: Bean Validation (JSR-380) + business logic validation
- **Custom Validators**: Coordinate validation (origin != destination, ~1m minimum)
- **State Transition Guards**: Prevents invalid state changes
- **Ownership Validation**: Users can only modify their own orders

---

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **Maven 3.8+**
- **PostgreSQL 16** (or use Docker Compose)

### Quick Start with Docker Compose

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd drone-delivery-management
   ```

2. **Start all services**
   ```bash
   docker-compose up -d
   ```

3. **Verify service is running**
   ```bash
   # Check Drone Delivery Service
   curl http://localhost:8088/actuator/health

   # Expected response:
   # {"status":"UP"}
   ```

4. **Access Swagger UI**
   ```
   http://localhost:8088/swagger-ui.html
   ```

### Local Development Setup

1. **Start PostgreSQL**
   ```bash
   docker run -d \
     --name postgres \
     -e POSTGRES_DB=acme \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:16-alpine
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run Drone Delivery Service**
   ```bash
   cd services/drone-delivery
   mvn spring-boot:run
   ```

### Environment Variables

```bash
# Database Configuration
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_DATABASE=acme
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres

# JWT Configuration
export JWT_SECRET=your-secret-key-minimum-256-bits
export JWT_EXPIRATION=86400000

# HikariCP Configuration (Optional)
export HIKARI_MAX_POOL_SIZE=20
export HIKARI_MIN_IDLE=5
```

### Database Optimizations

- **Indexes**: Strategic indexes on `status`, `drone_id`, `enduser_name`, `(name, user_type)`
- **Lazy Loading**: `@ManyToOne(fetch = FetchType.LAZY)` for drone relationships
- **Read-Only Transactions**: `@Transactional(readOnly = true)` for query methods
- **Bulk Updates**: Single query for drone failure recovery
- **Open-in-View Disabled**: Forces explicit fetching, prevents lazy loading issues

### HikariCP Configuration

```yaml
hikari:
  maximum-pool-size: 20        # Max connections
  minimum-idle: 5              # Min idle connections
  connection-timeout: 30000    # 30 seconds
  idle-timeout: 600000         # 10 minutes
  max-lifetime: 1800000        # 30 minutes
  leak-detection-threshold: 60000  # 60 seconds
```

---

## Security

### Authentication Flow

1. **Login**: User/Drone sends credentials to `/api/auth/login`
2. **Auto-Provisioning**: System creates user if not exists (passwordless)
3. **JWT Generation**: Server returns JWT token (24-hour expiration)
4. **Authorization**: Client includes token in `Authorization: Bearer <token>` header
5. **Validation**: Service validates JWT signature and expiration

### Role-Based Access Control (RBAC)

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full access to all endpoints, can update orders, manage drones |
| **ENDUSER** | Create orders, view own orders, withdraw own orders |
| **DRONE** | View available jobs, reserve jobs, pickup/deliver orders, update location |

### Security Best Practices

- JWT tokens with HMAC-SHA256 signing
- Stateless session management
- Method-level security with `@PreAuthorize`
- Input validation with Bean Validation (JSR-380)
- SQL injection prevention with JPA/Hibernate
- CORS configuration for cross-origin requests
- **TODO**: Move JWT secret to environment variable (currently in config)
- **TODO**: Implement token refresh mechanism

---

## API Documentation

### Swagger UI

Access interactive API documentation at:
```
http://localhost:8088/swagger-ui.html
```

### OpenAPI Specification

Download OpenAPI 3.0 specification:
```
http://localhost:8088/v3/api-docs
```

### Key Endpoints

#### Authentication
```http
POST /api/auth/login
Content-Type: application/json

{
  "name": "john_doe",
  "type": "enduser"
}
```

#### Order Management (ENDUSER)
```http
# Create Order
POST /api/orders
Authorization: Bearer <token>

# Get My Orders
GET /api/orders/my?page=0&size=20

# Withdraw Order
POST /api/orders/{orderId}/withdraw
```

#### Drone Operations (DRONE)
```http
# Get Available Jobs
GET /api/drones/jobs/available?page=0&size=20

# Reserve Job
POST /api/drones/jobs/{orderId}/reserve

# Pickup Order
POST /api/drones/jobs/{orderId}/pickup

# Deliver Order
POST /api/drones/jobs/{orderId}/deliver

# Update Location
PUT /api/drones/location
```

#### Admin Operations (ADMIN)
```http
# Get All Orders
GET /api/admin/orders?page=0&size=20

# Update Order Origin
PUT /api/admin/orders/{orderId}/origin

# Mark Drone as Broken
POST /api/admin/drones/{droneName}/broken

# Mark Drone as Fixed
POST /api/admin/drones/{droneName}/fixed
```

### Response Format

All responses follow RFC 7807 Problem Details format for errors:

```json
{
  "type": "https://acme.com/problems/business-rule-violation",
  "title": "Business Rule Violation",
  "status": 400,
  "detail": "Order is not available for reservation",
  "instance": "/api/drones/jobs/123/reserve"
}
```

---

## Release Process

To release a stable version of the service:

1. **Create a GitHub release** with a semantic version tag (e.g., `v1.2.3`)

2. **Specify components** in the release body using the following format:
   - For services: include `service:servicename` (e.g., `service:drone-delivery`)

3. **Multiple components** can be released simultaneously by including multiple entries

4. **CI workflow** will process the release and only build/publish stable versions for the specified components

5. **Components not mentioned** in the release body will be skipped

### Example Release Body

```markdown
Release v1.2.3

Includes:
- service:drone-delivery

Release notes:
- Fixed race conditions in job reservation (pessimistic locking)
- Optimized database queries (N+1 fix with JOIN FETCH)
- Added HikariCP connection pooling configuration
- Enhanced JWT authentication and authorization

Breaking Changes:
- None

Migration Notes:
- Update environment variables for HikariCP configuration (optional)
```

### Deployment Pipeline

```
GitHub Release -> CI Build -> Docker Images -> Container Registry
```

The Docker images can be deployed to any environment using Docker Compose or container orchestration platforms.

---

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
cd services/drone-delivery
mvn test

# Run specific test class
mvn test -Dtest=AuthIntegrationTest

# Run integration tests with Testcontainers
mvn verify
```

### Test Suite Overview

**Total: 27 comprehensive integration tests** - All passing

The test suite uses **Testcontainers** with PostgreSQL 16-alpine for realistic integration testing. All tests run against a real database with Flyway migrations.

#### Test Classes

| Test Class | Tests | Coverage |
|------------|-------|----------|
| **AuthIntegrationTest** | 7 | Authentication, validation, auto-provisioning |
| **OrderIntegrationTest** | 10 | Order CRUD, validation, authorization, withdrawal |
| **DroneIntegrationTest** | 8 | Drone operations, workflows, location updates, failure handling |
| **ConcurrencyIntegrationTest** | 2 | Race conditions, pessimistic locking |

### Key Test Scenarios

#### Authentication Tests
- Admin, end user, and drone authentication
- Auto-provisioning of users and drones
- Validation (name length, pattern, required fields)
- JWT token generation

#### Order Management Tests
- Order creation with coordinate validation
- Pagination and filtering (my orders, all orders)
- Authorization (users can only access their own orders)
- Order withdrawal with state validation
- Custom validation (origin != destination)
- Latitude/longitude range validation

#### Drone Operations Tests
- Complete delivery workflow (reserve -> pickup -> deliver)
- Job discovery and reservation
- Location updates
- Drone failure handling with order reassignment
- Order failure scenarios
- Authorization (only drones can access drone endpoints)

#### Concurrency Tests
- **Race condition prevention**: Multiple drones cannot reserve the same order (pessimistic locking)
- **Drone breakdown with active order**: Automatic order reassignment and location relocation

### Test Infrastructure

**BaseIntegrationTest** provides:
- PostgreSQL Testcontainer setup (reused across all tests)
- MockMvc configuration for REST API testing
- Authentication helper methods
- Automatic Flyway migrations

```java
// Example: Using BaseIntegrationTest
@DisplayName("Order Management Integration Tests")
class OrderIntegrationTest extends BaseIntegrationTest {

  @Test
  void shouldCreateOrderAsEndUser() throws Exception {
    String token = authenticateAsEndUser("john_doe");

    mockMvc.perform(post("/api/orders")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("pending"));
  }
}
```

### Test Configuration

Tests use a dedicated `application-test.yaml` profile:
- PostgreSQL Testcontainer with dynamic port binding
- HikariCP connection pool (max 20 connections)
- Flyway migrations enabled
- JPA validation mode (ddl-auto: validate)
- RFC 7807 Problem Details enabled

---

## Monitoring & Observability

### Health Checks

```bash
# Liveness probe
curl http://localhost:8088/actuator/health/liveness

# Readiness probe
curl http://localhost:8088/actuator/health/readiness

# Detailed health
curl http://localhost:8088/actuator/health
```

### Metrics

```bash
# Prometheus metrics
curl http://localhost:8088/actuator/prometheus

# Application metrics
curl http://localhost:8088/actuator/metrics
```

### Logging

Structured logging with SLF4J + Logback:

```java
log.info("Order {} reserved by drone {}", orderId, droneName);
log.warn("Business rule violation: {}", ex.getMessage());
log.error("Unexpected error occurred", ex);
```

---

## Roadmap

### Completed
- [x] Core order management functionality
- [x] Drone fleet management
- [x] JWT authentication & authorization
- [x] Race condition fix (pessimistic locking)
- [x] N+1 query optimization (JOIN FETCH)
- [x] Connection pooling configuration (HikariCP)
- [x] Database migrations (Flyway)
- [x] API documentation (Swagger/OpenAPI)
- [x] Docker containerization with Docker Compose
- [x] **Comprehensive integration test suite (27 tests with Testcontainers)**
- [x] **Race condition tests (pessimistic locking validation)**
- [x] **Business logic integration tests (authentication, orders, drones)**
- [x] **Validation and authorization tests**

### Planned
- [ ] Event sourcing for audit trail
- [ ] Circuit breaker pattern (Resilience4j)
- [ ] Distributed tracing (OpenTelemetry)
- [ ] Caching layer (Redis)
- [ ] Rate limiting (Bucket4j)
- [ ] Job queue for drone distribution (RabbitMQ/Kafka)
- [ ] Real-time WebSocket notifications
- [ ] Analytics dashboard

---

## Contributing

### Development Workflow

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Follow existing code style and patterns
   - Add tests for new functionality
   - Update documentation as needed

3. **Run tests**
   ```bash
   mvn clean verify
   ```

4. **Commit your changes**
   ```bash
   git commit -m "feat: add new feature"
   ```

   Follow [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat:` New feature
   - `fix:` Bug fix
   - `docs:` Documentation changes
   - `refactor:` Code refactoring
   - `test:` Adding tests
   - `chore:` Maintenance tasks

5. **Push to your branch**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**

### Code Style

- **Java**: Follow Google Java Style Guide
- **Formatting**: Use IDE auto-formatting (IntelliJ IDEA recommended)
- **Naming**: Use descriptive names for classes, methods, and variables
- **Comments**: Add JavaDoc for public APIs
- **Validation**: Use Bean Validation annotations on DTOs
- **Logging**: Use SLF4J with appropriate log levels


---

**Built with Java 21 and Spring Boot 3.4.5**

