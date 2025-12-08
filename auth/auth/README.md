# Auth Microservice

## Overview
This is the Authentication & Authorization microservice for the Khati Microservices Architecture. It runs on **port 8081** and handles user registration, login, and JWT token generation with role-based access control (RBAC).

## Features
- ✅ User registration and login
- ✅ JWT token generation and validation
- ✅ Role-based authorization (ROLE_ADMIN, ROLE_MANAGER, ROLE_CUSTOMER)
- ✅ Spring Security integration
- ✅ PostgreSQL database
- ✅ Password encryption with BCrypt

## Database Schema

### Tables
1. **users**
   - id (PK)
   - username (unique)
   - password (encrypted)
   - email (unique)
   - created_at

2. **roles**
   - id (PK)
   - name (unique) - e.g., "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CUSTOMER"

3. **user_roles** (join table)
   - user_id (FK to users)
   - role_id (FK to roles)

## Setup Instructions

### 1. Database Setup
Run the following commands in PostgreSQL:

```sql
CREATE DATABASE auth_db;
CREATE USER auth_user WITH ENCRYPTED PASSWORD 'auth_pass_123';
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;
\c auth_db
GRANT ALL ON SCHEMA public TO auth_user;
```

### 2. Run the Application
```bash
mvn clean install
mvn spring-boot:run
```

The application will:
- Start on port 8081
- Auto-create database tables
- Initialize default roles (ROLE_ADMIN, ROLE_MANAGER, ROLE_CUSTOMER)

## API Endpoints

### Public Endpoints (No Authentication Required)

#### 1. Register User
```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123",
  "email": "john@example.com"
}
```

**Response:**
```json
{
  "token": null,
  "username": "john_doe",
  "message": "User registered successfully"
}
```

#### 2. Login
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "john_doe",
  "message": "Login successful"
}
```

### Protected Endpoints (Authentication Required)

#### 3. Get Current User Info
```http
GET http://localhost:8081/api/auth/me
Authorization: Bearer <your-jwt-token>
```

#### 4. Admin Only Endpoint
```http
GET http://localhost:8081/api/auth/admin/test
Authorization: Bearer <admin-jwt-token>
```
Requires: ROLE_ADMIN

#### 5. Manager/Admin Endpoint
```http
GET http://localhost:8081/api/auth/manager/test
Authorization: Bearer <manager-or-admin-jwt-token>
```
Requires: ROLE_MANAGER or ROLE_ADMIN

## Data Flow

1. **Registration Flow:**
   ```
   Client → POST /register → AuthController → AuthService → UserRepository → Database
   - Password is encrypted with BCrypt
   - Default role ROLE_CUSTOMER is assigned
   - User saved to database
   ```

2. **Login Flow:**
   ```
   Client → POST /login → AuthController → AuthService
   - Validates username & password
   - Retrieves user roles from database
   - Generates JWT token with roles embedded
   - Returns token to client
   ```

3. **Authentication Flow:**
   ```
   Client → Protected Endpoint → JwtAuthenticationFilter
   - Extracts JWT from Authorization header
   - Validates token signature
   - Loads user details from database
   - Sets authentication in SecurityContext
   - Request proceeds to controller
   ```

## JWT Token Structure

The JWT token includes:
- **Subject:** username
- **Claims:**
  - roles: ["ROLE_CUSTOMER", "ROLE_MANAGER"]
- **Expiration:** 24 hours (86400000 ms)

## Role-Based Authorization

### How to Use in Controllers:

```java
// Requires ROLE_ADMIN
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/endpoint")
public ResponseEntity<?> adminOnly() {
    return ResponseEntity.ok("Admin access");
}

// Requires ROLE_ADMIN OR ROLE_MANAGER
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@GetMapping("/manager/endpoint")
public ResponseEntity<?> managerAccess() {
    return ResponseEntity.ok("Manager access");
}
```

## Configuration

### application.properties
```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=auth_user
spring.datasource.password=auth_pass_123
jwt.secret=<your-secret-key>
jwt.expiration=86400000
```

## Integration with Other Microservices

This auth service should be called by:
1. **API Gateway** - Routes `/api/auth/**` requests here
2. **Other Microservices** - Validate JWT tokens by:
   - Sharing the JWT secret
   - Using a shared JWT validation library
   - Or calling this service's validation endpoint

## Security Best Practices Implemented

✅ Passwords encrypted with BCrypt  
✅ JWT tokens signed with HS512  
✅ Stateless authentication (no sessions)  
✅ Role-based access control  
✅ CORS can be configured in API Gateway  
✅ SQL injection prevention (JPA/Hibernate)  

## Testing

### Test the flow:
1. Register a user → Get success message
2. Login with credentials → Get JWT token
3. Call `/api/auth/me` with token → See user info
4. Try `/api/auth/admin/test` without admin role → Get 403 Forbidden
5. Assign admin role manually in DB → Try again → Success

## Common Issues

### Issue: "Default role not found"
**Solution:** The DataLoader should automatically create roles. If not, manually insert:
```sql
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_MANAGER');
INSERT INTO roles (name) VALUES ('ROLE_CUSTOMER');
```

### Issue: "Cannot resolve table 'user_roles'"
**Solution:** This is just an IDE warning. Hibernate will create this table automatically when the app starts.

## Next Steps for Microservices Architecture

1. ✅ Auth Service (Port 8081) - **DONE**
2. ⬜ User Service (Port 8082) - Manage user profiles
3. ⬜ Inventory Service (Port 8083) - Manage products
4. ⬜ Order Service (Port 8084) - Manage orders
5. ⬜ API Gateway (Port 8080) - Route all requests

## Technologies Used
- Spring Boot 3.5.7
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (jjwt 0.11.5)
- Lombok
- Java 17

