# API Gateway Configuration Summary

## Overview
The API Gateway serves as a single entry point for all frontend requests to the microservices architecture. All backend services are accessed through the gateway at port 8080.

## Service Routing Configuration

### Base URL
- **Gateway**: `http://localhost:8080`

### Route Mappings

| Frontend Route | Backend Service | Service URL | Description |
|---------------|-----------------|-------------|-------------|
| `/api/auth/**` | Auth Service | http://localhost:8081 | Authentication & authorization |
| `/api/users/**` | User Service | http://localhost:8082 | User management |
| `/api/inventory/**` | Inventory Service | http://localhost:8083 | Product & category management |
| `/api/orders/**` | Order Service | http://localhost:8084 | Order processing |

## Security Features

### Internal Endpoint Protection
Internal endpoints used for service-to-service communication are protected from external access through:

1. **InternalEndpointFilter** - A servlet filter that blocks all requests containing `/internal/` in the path
2. **ProxyController Guards** - Additional checks in user and inventory proxy methods to reject internal endpoint requests

Protected internal endpoints:
- `/internal/users` - User creation/update by auth service
- `/internal/users/{userId}/roles` - Role updates by auth service
- `/internal/id` - Get user ID by username
- `/internal/{userId}/increment-orders` - Order count updates
- `/internal/update` - Inventory updates by order service

## Frontend Integration

### Example API Calls from Frontend

```javascript
// Authentication
POST http://localhost:8080/api/auth/register
POST http://localhost:8080/api/auth/login
PUT http://localhost:8080/api/auth/update-role?userId={id}

// User Management
GET http://localhost:8080/api/users
GET http://localhost:8080/api/users/me
GET http://localhost:8080/api/users/customers
GET http://localhost:8080/api/users/{id}
PUT http://localhost:8080/api/users/{id}/manager-info

// Inventory Management
GET http://localhost:8080/api/inventory/categories
POST http://localhost:8080/api/inventory/admin/categories
PUT http://localhost:8080/api/inventory/admin/categories/{id}
DELETE http://localhost:8080/api/inventory/admin/categories/{id}

GET http://localhost:8080/api/inventory/products
GET http://localhost:8080/api/inventory/products/{id}
POST http://localhost:8080/api/inventory/admin/products
PUT http://localhost:8080/api/inventory/admin/products/{id}
DELETE http://localhost:8080/api/inventory/admin/products/{id}

// Order Management
POST http://localhost:8080/api/orders
GET http://localhost:8080/api/orders/my
GET http://localhost:8080/api/orders/{id}
GET http://localhost:8080/api/orders/admin/all
PUT http://localhost:8080/api/orders/admin/{id}/status
PUT http://localhost:8080/api/orders/manager/{id}/status
```

## Configuration Files

### application.properties
```properties
spring.application.name=gateway
server.port=8080

# Downstream service base URLs
services.auth=http://localhost:8081
services.user=http://localhost:8082
services.inventory=http://localhost:8083
services.order=http://localhost:8084
```

## Key Components

### ServiceConfig.java
- Loads service URLs from application.properties
- Provides getter methods for each service URL

### ProxyController.java
- Handles routing for `/api/auth/**`, `/api/users/**`, `/api/inventory/**`, `/api/orders/**`
- Blocks internal endpoint access for user and inventory services
- Forwards requests to appropriate backend services

### ProxyService.java
- Handles the actual HTTP forwarding logic
- Preserves headers, query parameters, and request bodies

### InternalEndpointFilter.java
- Servlet filter that runs before all requests (Order = 1)
- Blocks any request containing `/internal/` in the path
- Logs security violations for monitoring

## Path Extraction Logic

The `PathExtractor` utility extracts the remaining path after the gateway route prefix:
- Request: `GET http://localhost:8080/api/users/123`
- Extracted path: `/api/users/123`
- Forwarded to: `http://localhost:8082/api/users/123`

## Changes Made

### Fixed Issues:
1. ✅ Updated user service controller from `/users` to `/api/users` for consistent routing
2. ✅ Added internal endpoint protection in ProxyController for user and inventory services
3. ✅ Created InternalEndpointFilter for defense-in-depth security
4. ✅ All services now follow the `/api/*` pattern consistently

### Path Consistency:
- **Auth Service**: `/api/auth/**` ✅
- **User Service**: `/api/users/**` ✅ (fixed from `/users/**`)
- **Inventory Service**: `/api/inventory/**` ✅
- **Order Service**: `/api/orders/**` ✅

## Testing the Gateway

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Test Internal Endpoint Protection
```bash
# Should return 403 Forbidden
curl http://localhost:8080/api/users/internal
curl http://localhost:8080/api/inventory/internal/update
```

### Test Valid Endpoints
```bash
# Should work (if auth is configured)
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"test","password":"test"}'
curl http://localhost:8080/api/inventory/products
```

## Notes
- All backend services should remain inaccessible directly from the frontend
- Frontend should ONLY communicate with the gateway at port 8080
- JWT tokens should be passed in the Authorization header for authenticated requests
- Internal service-to-service calls bypass the gateway and communicate directly

