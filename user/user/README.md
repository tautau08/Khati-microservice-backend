# User Service - Microservice

A Spring Boot microservice for managing user profile data with role-based access control.

## Architecture Overview

### Data Ownership

| Service | Manages | Description |
|---------|---------|-------------|
| **auth-service** | username, email, roles | Single source of truth for authentication and authorization |
| **user-service** | salary, joiningDate, attendance, assignedManagerId | Manages additional user profile fields (ADMIN sets these) |
| **order-service** | totalOrders | Manages order-related data |

### How It Works

1. **User Registration**: 
   - auth-service creates user account
   - auth-service calls user-service internal API to create user record with basic info (id, username, email, roles)

2. **Role Updates**:
   - auth-service updates roles
   - auth-service calls user-service to sync roles

3. **Manager Info** (ADMIN only):
   - ADMIN uses user-service dashboard to set/update: salary, joiningDate, attendance
   - ADMIN assigns customers to managers using assignedManagerId

4. **Fetching Complete User Data**:
   - user-service fetches username, email, roles from local database (synced from auth-service)
   - user-service fetches totalOrders from order-service via REST call
   - user-service provides its own fields: salary, joiningDate, attendance, assignedManagerId

## Technologies

- Java 17
- Spring Boot 3.5.7
- Spring Security with OAuth2 Resource Server (JWT)
- Spring Data JPA
- PostgreSQL
- Lombok
- Maven

## API Endpoints

### Internal Endpoints (For auth-service)

#### Create User
```http
POST /internal/users
Content-Type: application/json

{
    "id": 1,
    "username": "john.doe",
    "email": "john@example.com",
    "roles": ["CUSTOMER"]
}
```

#### Update User Roles
```http
PUT /internal/users/{userId}/roles
Content-Type: application/json

{
    "roles": ["CUSTOMER", "MANAGER"]
}
```

#### Sync User Info
```http
PUT /internal/users/{userId}/sync
Content-Type: application/json

{
    "username": "john.doe",
    "email": "newemail@example.com",
    "roles": ["CUSTOMER"]
}
```

### Public Endpoints (JWT Required)

#### Get Current User
```http
GET /users/me
Authorization: Bearer {jwt_token}
```
Returns user data with role-based field filtering.

#### Get Customers (Manager Only)
```http
GET /users/customers
Authorization: Bearer {jwt_token}
```
Returns customers assigned to the authenticated manager.

#### Get User by ID
```http
GET /users/{id}
Authorization: Bearer {jwt_token}
```
Access rules:
- **ADMIN**: Can view any user with all fields
- **User**: Can view their own data
- **MANAGER**: Can view assigned customers (limited fields)

#### Update Manager Info (ADMIN Only)
```http
PUT /users/{id}/manager-info
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "salary": 50000.00,
    "joiningDate": "2025-01-15",
    "attendance": 22,
    "assignedManagerId": 5
}
```
Only ADMIN can set/update these fields.

## Role-Based Field Visibility

### ADMIN
- All fields including:
  - salary, joiningDate, attendance (manager fields)
  - assignedManagerId (customer assignment)
  - totalOrders

### MANAGER
- **Own data**: Can see their salary, joiningDate, attendance
- **Assigned customers**: Can only see totalOrders, not sensitive manager fields

### CUSTOMER
- **Own data only**: Basic info + totalOrders

## Configuration

### application.properties
```properties
spring.application.name=user-service
server.port=8082

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/userdb
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT - Update with your auth-service URL
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081
```

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- auth-service running (for JWT validation)

### Database Setup
```sql
CREATE DATABASE userdb;
```

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

The service runs on port 8082.

## Integration with Other Services

### From auth-service
```java
// After user registration
RestTemplate restTemplate = new RestTemplate();
String url = "http://localhost:8082/internal/users";
UserDTO userDTO = new UserDTO();
userDTO.setId(user.getId());
userDTO.setUsername(user.getUsername());
userDTO.setEmail(user.getEmail());
userDTO.setRoles(user.getRoles());
restTemplate.postForEntity(url, userDTO, UserDTO.class);
```

### From user-service to order-service
```java
// Fetch totalOrders
RestTemplate restTemplate = new RestTemplate();
String url = "http://localhost:8083/internal/orders/user/" + userId + "/count";
Integer totalOrders = restTemplate.getForObject(url, Integer.class);
```

## Security

- JWT tokens contain: username, roles
- Internal endpoints (`/internal/**`) are not JWT-secured but should be:
  - Protected at API Gateway level
  - Only accessible within internal network

## License

Part of Khati Microservice architecture.


