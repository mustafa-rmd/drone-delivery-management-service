# Validation Guide - Drone Delivery Service

This document describes all validation rules applied to API requests in the Drone Delivery Service.

## Overview

The service implements multi-layer validation:
1. **Request-level validation** - Bean Validation (JSR-380) annotations on DTOs
2. **Business logic validation** - Service layer checks for state transitions and business rules
3. **Custom validators** - Domain-specific validation logic

All validation errors return HTTP 400 Bad Request with detailed error messages.

---

## Authentication Validation

### POST /api/auth/login

**Request Body: `AuthRequest`**

| Field | Validation Rules | Error Message |
|-------|-----------------|---------------|
| `name` | Required, not blank | "Name is required" |
| `name` | Length: 3-255 characters | "Name must be between 3 and 255 characters" |
| `name` | Pattern: `^[a-zA-Z0-9_-]+$` | "Name must contain only alphanumeric characters, hyphens, and underscores" |
| `type` | Required, not null | "User type is required" |
| `type` | Enum: admin, enduser, drone | "Invalid UserType value: {value}" |

**Valid Examples:**
```json
{"name": "john_doe", "type": "enduser"}
{"name": "Drone-Alpha-1", "type": "drone"}
{"name": "admin", "type": "admin"}
```

**Invalid Examples:**
```json
{"name": "ab", "type": "enduser"}           // Too short (min 3)
{"name": "user@domain.com", "type": "admin"} // Invalid characters (@, .)
{"name": "john_doe", "type": "invalid"}      // Invalid user type
```

---

## Order Management Validation

### POST /api/orders (Create Order)

**Request Body: `CreateOrderRequest`**

| Field | Validation Rules | Error Message |
|-------|-----------------|---------------|
| `originLatitude` | Required, not null | "Origin latitude is required" |
| `originLatitude` | Range: -90.0 to 90.0 | "Origin latitude must be between -90 and 90" |
| `originLatitude` | Precision: max 2 integer digits, 8 decimal places | "Origin latitude must have at most 2 integer digits and 8 decimal places" |
| `originLongitude` | Required, not null | "Origin longitude is required" |
| `originLongitude` | Range: -180.0 to 180.0 | "Origin longitude must be between -180 and 180" |
| `originLongitude` | Precision: max 3 integer digits, 8 decimal places | "Origin longitude must have at most 3 integer digits and 8 decimal places" |
| `destinationLatitude` | Required, not null | "Destination latitude is required" |
| `destinationLatitude` | Range: -90.0 to 90.0 | "Destination latitude must be between -90 and 90" |
| `destinationLatitude` | Precision: max 2 integer digits, 8 decimal places | "Destination latitude must have at most 2 integer digits and 8 decimal places" |
| `destinationLongitude` | Required, not null | "Destination longitude is required" |
| `destinationLongitude` | Range: -180.0 to 180.0 | "Destination longitude must be between -180 and 180" |
| `destinationLongitude` | Precision: max 3 integer digits, 8 decimal places | "Destination longitude must have at most 3 integer digits and 8 decimal places" |
| **Class-level** | Origin ≠ Destination (min ~1m distance) | "Origin and destination coordinates must be different (minimum distance: ~1 meter)" |

**Valid Example:**
```json
{
  "originLatitude": 40.7128,
  "originLongitude": -74.0060,
  "destinationLatitude": 40.7589,
  "destinationLongitude": -73.9851
}
```

**Invalid Examples:**
```json
// Same origin and destination
{
  "originLatitude": 40.7128,
  "originLongitude": -74.0060,
  "destinationLatitude": 40.7128,
  "destinationLongitude": -74.0060
}

// Latitude out of range
{
  "originLatitude": 95.0,
  "originLongitude": -74.0060,
  "destinationLatitude": 40.7589,
  "destinationLongitude": -73.9851
}

// Longitude out of range
{
  "originLatitude": 40.7128,
  "originLongitude": -200.0,
  "destinationLatitude": 40.7589,
  "destinationLongitude": -73.9851
}
```

### PUT /api/orders/{orderId}/origin (Admin)

**Request Body: `UpdateLocationRequest`**

| Field | Validation Rules | Error Message |
|-------|-----------------|---------------|
| `latitude` | Required, not null | "Latitude is required" |
| `latitude` | Range: -90.0 to 90.0 | "Latitude must be between -90 and 90" |
| `latitude` | Precision: max 2 integer digits, 8 decimal places | "Latitude must have at most 2 integer digits and 8 decimal places" |
| `longitude` | Required, not null | "Longitude is required" |
| `longitude` | Range: -180.0 to 180.0 | "Longitude must be between -180 and 180" |
| `longitude` | Precision: max 3 integer digits, 8 decimal places | "Longitude must have at most 3 integer digits and 8 decimal places" |

**Business Logic Validation:**
- Order must exist (404 if not found)
- Order status must be PENDING or RESERVED
- Error if order is PICKED_UP, IN_TRANSIT, DELIVERED, FAILED, or WITHDRAWN

**Valid Example:**
```json
{
  "latitude": 40.7200,
  "longitude": -74.0100
}
```

### PUT /api/orders/{orderId}/destination (Admin)

**Request Body: `UpdateLocationRequest`** (same validation as origin)

**Business Logic Validation:**
- Order must exist (404 if not found)
- Order status must NOT be DELIVERED, FAILED, or WITHDRAWN
- Allowed for PENDING, RESERVED, PICKED_UP, IN_TRANSIT

---

## Drone Operations Validation

### PUT /api/drones/location (Drone)

**Request Body: `UpdateLocationRequest`**

Same validation rules as order origin/destination updates (see above).

**Valid Example:**
```json
{
  "latitude": 40.7589,
  "longitude": -73.9851
}
```

---

## Coordinate Validation Details

### Latitude Constraints
- **Range:** -90.0 to 90.0 (decimal degrees)
- **Precision:** Up to 2 integer digits, 8 decimal places
- **Examples:**
  - ✅ Valid: `40.71280000`, `-89.99999999`, `0.00000001`
  - ❌ Invalid: `91.0` (out of range), `100.5` (too many integer digits)

### Longitude Constraints
- **Range:** -180.0 to 180.0 (decimal degrees)
- **Precision:** Up to 3 integer digits, 8 decimal places
- **Examples:**
  - ✅ Valid: `-74.00600000`, `179.99999999`, `0.00000001`
  - ❌ Invalid: `181.0` (out of range), `1000.5` (too many integer digits)

### Precision Notes
- 8 decimal places provides ~1.1mm accuracy at the equator
- Database columns: `DECIMAL(10, 8)` for latitude, `DECIMAL(11, 8)` for longitude
- Epsilon tolerance for same-coordinate check: `0.00000001` (~1.1mm)

---

## Error Response Format

### Validation Error Response (HTTP 400)

```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "originLatitude",
      "rejectedValue": 95.0,
      "message": "Origin latitude must be between -90 and 90"
    },
    {
      "field": "name",
      "rejectedValue": "ab",
      "message": "Name must be between 3 and 255 characters"
    }
  ],
  "path": "/api/orders"
}
```

### Business Logic Error Response (HTTP 400)

```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot update origin - order already picked up or in terminal state",
  "path": "/api/orders/123/origin"
}
```

---

## Testing Validation

### Using Postman

1. **Test valid requests** - Use examples from this guide
2. **Test boundary values:**
   - Latitude: -90.0, 90.0
   - Longitude: -180.0, 180.0
3. **Test invalid values:**
   - Out of range: latitude = 91, longitude = 181
   - Missing fields: omit required fields
   - Invalid patterns: name with special characters
4. **Test business rules:**
   - Try updating origin for delivered order
   - Try creating order with same origin/destination

### Using cURL

```bash
# Valid request
curl -X POST http://localhost:8088/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "originLatitude": 40.7128,
    "originLongitude": -74.0060,
    "destinationLatitude": 40.7589,
    "destinationLongitude": -73.9851
  }'

# Invalid request (latitude out of range)
curl -X POST http://localhost:8088/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "originLatitude": 95.0,
    "originLongitude": -74.0060,
    "destinationLatitude": 40.7589,
    "destinationLongitude": -73.9851
  }'
```

---

## Implementation Details

### Validation Annotations Used

- `@NotNull` - Field must not be null
- `@NotBlank` - String must not be null, empty, or whitespace-only
- `@Size` - String length constraints
- `@Pattern` - Regular expression matching
- `@DecimalMin` / `@DecimalMax` - Numeric range constraints
- `@Digits` - Numeric precision constraints
- `@ValidCoordinates` - Custom class-level validator

### Custom Validators

**`@ValidCoordinates`** - Class-level validator for `CreateOrderRequest`
- Location: `com.acme.services.dronedelivery.validation.ValidCoordinates`
- Implementation: `ValidCoordinatesValidator`
- Purpose: Ensures origin and destination are not identical (minimum ~1m distance)
- Epsilon: `0.00000001` decimal degrees

---

## Best Practices

1. **Always validate on the client side first** - Provide immediate feedback
2. **Handle validation errors gracefully** - Display user-friendly messages
3. **Test edge cases** - Boundary values, null values, extreme precision
4. **Use appropriate precision** - Don't send more decimal places than needed
5. **Respect coordinate ranges** - Latitude: [-90, 90], Longitude: [-180, 180]

---

## Related Documentation

- [Postman Collection Guide](POSTMAN_COLLECTION_GUIDE.md) - API testing examples
- [OpenAPI/Swagger UI](http://localhost:8088/swagger-ui.html) - Interactive API documentation
- [API Docs](http://localhost:8088/v3/api-docs) - OpenAPI specification

