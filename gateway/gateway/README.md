# API Gateway - Simplified Structure

## Overview
This is a simplified API Gateway that routes requests to different microservices.

## Project Structure

```
gateway/
├── config/
│   ├── SecurityConfig.java         # Security configuration (if needed)
│   └── ServiceConfig.java          # Service URL configuration
├── proxy/
│   └── ProxyController.java        # Routes incoming requests
├── service/
│   └── ProxyService.java           # Handles request forwarding logic
└── util/
    └── PathExtractor.java          # Extracts paths from requests
```

## Components

### 1. ProxyController (proxy/ProxyController.java)
- **Purpose**: Routes incoming HTTP requests to appropriate microservices
- **Routes**:
  - `/api/auth/**` → Auth Service
  - `/users/**` → User Service
  - `/api/inventory/**` → Inventory Service
  - `/api/orders/**` → Order Service

### 2. ProxyService (service/ProxyService.java)
- **Purpose**: Handles the actual HTTP forwarding logic
- **Features**:
  - Forwards requests using WebClient
  - Filters sensitive headers
  - Handles request body and query parameters
  - Supports all HTTP methods (GET, POST, PUT, DELETE, etc.)

### 3. ServiceConfig (config/ServiceConfig.java)
- **Purpose**: Centralizes microservice URL configuration
- **Configuration**: Loaded from `application.properties`

### 4. PathExtractor (util/PathExtractor.java)
- **Purpose**: Extracts the remaining path after route matching
- **Example**: `/api/auth/login` → `/login`

## Configuration

Edit `application.properties` to configure microservice URLs:

```properties
services.auth=http://localhost:8081
services.user=http://localhost:8082
services.inventory=http://localhost:8083
services.order=http://localhost:8084
```

## How It Works

1. Client sends request to gateway (e.g., `GET /api/auth/login`)
2. ProxyController matches the route (`/api/auth/**`)
3. PathExtractor extracts remaining path (`/login`)
4. ProxyService forwards request to configured service URL
5. Response is returned to client

## Benefits of This Structure

1. **Separation of Concerns**: Each class has a single responsibility
2. **Easy to Maintain**: Logic is organized into focused classes
3. **Easy to Test**: Each component can be tested independently
4. **Easy to Extend**: Add new routes by adding one method to ProxyController
5. **Clean Code**: No duplicated logic, follows DRY principle

## Adding a New Route

To add a new microservice route:

1. Add URL to `application.properties`:
   ```properties
   services.newservice=http://localhost:8085
   ```

2. Add getter to `ServiceConfig.java`:
   ```java
   @Value("${services.newservice}")
   private String newServiceUrl;
   ```

3. Add route to `ProxyController.java`:
   ```java
   @RequestMapping("/api/newservice/**")
   public ResponseEntity<byte[]> newServiceProxy(
           HttpMethod method,
           HttpServletRequest request,
           @RequestHeader HttpHeaders headers,
           @RequestParam MultiValueMap<String, String> query,
           @RequestBody(required = false) byte[] body) {
       
       String path = PathExtractor.extractRemainingPath(request);
       return proxyService.forwardRequest(
               serviceConfig.getNewServiceUrl(), path, method, headers, query, body);
   }
   ```

That's it! Three simple steps.

