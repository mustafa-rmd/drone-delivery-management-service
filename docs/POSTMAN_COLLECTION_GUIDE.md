# Drone Delivery Service - Postman Collection Guide

This guide explains how to use the Postman collection for testing the Acme Drone Delivery Service API.

## Collection File

**File**: `Drone-Delivery-Service.postman_collection.json`

## Getting Started

### 1. Import the Collection

1. Open Postman
2. Click **Import** button
3. Select the `Drone-Delivery-Service.postman_collection.json` file
4. The collection will be imported with all endpoints and variables

### 2. Collection Variables

The collection includes pre-configured variables:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8088` | Base URL of the drone delivery service |
| `jwt_token` | (empty) | JWT token - automatically set after login |
| `drone_name` | `Drone-Alpha-1` | Default drone name |
| `enduser_name` | `john_doe` | Default end user name |
| `admin_name` | `admin` | Default admin name |
| `order_id` | `1` | Order ID - automatically set after creating an order |

### 3. Start the Service

Before using the collection, make sure the Drone Delivery Service is running:

```bash
cd services/drone-delivery
mvn spring-boot:run
```

The service will start on port **8088** by default.

## API Endpoints Overview

### Authentication (3 endpoints)

All endpoints require JWT authentication except the login endpoints.

1. **Login as Admin** - `POST /api/auth/login`
2. **Login as Drone** - `POST /api/auth/login`
3. **Login as End User** - `POST /api/auth/login`

### Order Management (4 endpoints)

1. **Create Order** - `POST /api/orders` (ENDUSER role)
2. **Get My Orders** - `GET /api/orders/my-orders` (ENDUSER role)
3. **Withdraw Order** - `POST /api/orders/{orderId}/withdraw` (ENDUSER role)
4. **Get All Orders** - `GET /api/orders` (ADMIN role)

### Admin - Order Management (2 endpoints)

1. **Update Order Origin** - `PUT /api/orders/{orderId}/origin` (ADMIN role)
2. **Update Order Destination** - `PUT /api/orders/{orderId}/destination` (ADMIN role)

### Drone Operations (9 endpoints)

1. **Get Available Jobs** - `GET /api/drones/jobs` (DRONE role)
2. **Reserve Job** - `POST /api/drones/jobs/{orderId}/reserve` (DRONE role)
3. **Pickup Order** - `POST /api/drones/jobs/{orderId}/pickup` (DRONE role)
4. **Deliver Order** - `POST /api/drones/jobs/{orderId}/deliver` (DRONE role)
5. **Update Location** - `PUT /api/drones/location` (DRONE role)
6. **Mark as Broken** - `POST /api/drones/broken` (DRONE role - self-service)
7. **Mark as Fixed** - `POST /api/drones/fixed` (DRONE role - self-service)
8. **Get All Drones** - `GET /api/drones` (ADMIN role)
9. **Get Drone by Name** - `GET /api/drones/{name}` (ADMIN or DRONE role)

### Admin - Drone Management (2 endpoints)

1. **Mark Drone as Broken** - `POST /api/drones/{name}/broken` (ADMIN role)
2. **Mark Drone as Fixed** - `POST /api/drones/{name}/fixed` (ADMIN role)

## Authentication Flow

### User Types

The system supports three user types:

- **ADMIN** - Full access to all endpoints
- **DRONE** - Access to drone operations and job management
- **ENDUSER** - Access to create and manage their own orders

### How to Authenticate

1. **Select the appropriate login request** based on your role:
   - `Login as Admin`
   - `Login as Drone`
   - `Login as End User`

2. **Send the request** - The JWT token will be automatically saved to the `jwt_token` variable

3. **All subsequent requests** will use this token automatically via Bearer authentication

### Example Login Request

```json
{
  "name": "Drone-Alpha-1",
  "type": "DRONE"
}
```

### Example Login Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "name": "Drone-Alpha-1",
  "type": "DRONE"
}
```

## Usage Scenarios

### Scenario 1: End User Creates an Order

1. **Login as End User**
   - Request: `Login as End User`
   - This creates a user and returns a JWT token

2. **Create an Order**
   - Request: `Create Order (End User)`
   - Body:
     ```json
     {
       "originLatitude": 40.7128,
       "originLongitude": -74.0060,
       "destinationLatitude": 40.7589,
       "destinationLongitude": -73.9851
     }
     ```
   - The `order_id` is automatically saved

3. **View My Orders**
   - Request: `Get My Orders (End User)`
   - Returns all orders for the current user

### Scenario 2: Drone Picks Up and Delivers an Order

1. **Login as Drone**
   - Request: `Login as Drone`
   - This creates a drone entity and returns a JWT token

2. **Get Available Jobs**
   - Request: `Get Available Jobs (Drone)`
   - Returns list of pending orders

3. **Reserve a Job**
   - Request: `Reserve Job (Drone)`
   - Uses the `order_id` variable

4. **Update Location** (optional)
   - Request: `Update Location (Drone)`
   - Body:
     ```json
     {
       "latitude": 40.7200,
       "longitude": -74.0100
     }
     ```

5. **Pickup Order**
   - Request: `Pickup Order (Drone)`
   - Marks the order as picked up

6. **Deliver Order**
   - Request: `Deliver Order (Drone)`
   - Marks the order as delivered

### Scenario 3: Admin Monitors and Manages the System

1. **Login as Admin**
   - Request: `Login as Admin`

2. **View All Orders**
   - Request: `Get All Orders (Admin)`
   - Returns all orders in the system

3. **Update Order Location** (if needed)
   - Request: `Update Order Origin (Admin)` or `Update Order Destination (Admin)`
   - Body:
     ```json
     {
       "latitude": 40.7200,
       "longitude": -74.0100
     }
     ```
   - Note: Origin can only be updated for PENDING/RESERVED orders; Destination cannot be updated for DELIVERED/FAILED/WITHDRAWN orders

4. **View All Drones**
   - Request: `Get All Drones (Admin)`
   - Returns all drones with their status

5. **View Specific Drone**
   - Request: `Get Drone by Name`
   - Returns detailed information about a specific drone

6. **Manage Drone Status**
   - Request: `Mark Drone as Broken (Admin)` or `Mark Drone as Fixed (Admin)`
   - Note: When a drone is marked broken, any PICKED_UP orders are reset to PENDING at the drone's last location for handoff

## Order Status Flow

Orders go through the following statuses:

1. **PENDING** - Order created, waiting for drone assignment
2. **RESERVED** - Drone has reserved the order
3. **PICKED_UP** - Drone has picked up the package
4. **DELIVERED** - Package has been delivered
5. **WITHDRAWN** - Order was cancelled by the end user

## Drone Status Flow

Drones have the following statuses:

1. **AVAILABLE** - Ready to accept jobs
2. **BUSY** - Currently assigned to an order
3. **BROKEN** - Out of service (can be marked as fixed)

## Response Examples

### Create Order Response

```json
{
  "id": 1,
  "enduserName": "john_doe",
  "originLatitude": 40.7128,
  "originLongitude": -74.0060,
  "destinationLatitude": 40.7589,
  "destinationLongitude": -73.9851,
  "status": "PENDING",
  "droneId": null,
  "droneName": null,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "pickedUpAt": null,
  "deliveredAt": null
}
```

### Get Drone Response

```json
{
  "id": 1,
  "name": "Drone-Alpha-1",
  "status": "AVAILABLE",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "isBroken": false,
  "createdAt": "2024-01-15T09:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## Testing Tips

1. **Use different users** - Test with multiple drones and end users by changing the variable values
2. **Check the Console** - Postman scripts log important information to the console
3. **Test error cases** - Try accessing endpoints without proper authentication
4. **Monitor order flow** - Create an order as end user, then switch to drone to complete it
5. **Test concurrent operations** - Use multiple drones to reserve different orders

## Troubleshooting

### Issue: "401 Unauthorized"
- **Solution**: Make sure you've logged in and the JWT token is set
- Check the `jwt_token` variable in collection variables

### Issue: "403 Forbidden"
- **Solution**: You're using the wrong user type for this endpoint
- Example: Only ADMIN can access `GET /api/orders`

### Issue: "Connection refused"
- **Solution**: Make sure the service is running on port 8088
- Check with: `curl http://localhost:8088/actuator/health`

### Issue: Order ID not found
- **Solution**: Create an order first using `Create Order (End User)`
- The `order_id` variable will be automatically set

## Additional Resources

- **Swagger UI**: http://localhost:8088/swagger-ui.html
- **API Docs**: http://localhost:8088/v3/api-docs
- **Health Check**: http://localhost:8088/actuator/health

## Quick Start Workflow

1. Import the collection into Postman
2. Start the Drone Delivery Service
3. Run `Login as End User` → Creates user and saves token
4. Run `Create Order` → Creates an order and saves order ID
5. Run `Login as Drone` → Creates drone and saves token
6. Run `Get Available Jobs` → See the pending order
7. Run `Reserve Job` → Assign order to drone
8. Run `Pickup Order` → Mark as picked up
9. Run `Deliver Order` → Complete the delivery

Enjoy testing the Drone Delivery Service!

