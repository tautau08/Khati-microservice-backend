# User Service - Clean Architecture

## Project Structure

```
user-service/
├── controller/
│   ├── UserController.java         (Public API - JWT secured)
│   └── InternalController.java     (Internal API - for auth-service)
├── service/
│   └── UserService.java            (Business logic + filtering)
├── entity/
│   └── User.java                   (Database entity)
├── repository/
│   └── UserRepository.java         (Data access)
├── dto/
│   ├── UserDTO.java               (Data transfer object)
│   └── UpdateRolesRequest.java    (Role update request)
├── config/
│   └── SecurityConfig.java        (JWT + OAuth2 config)
└── exception/
    └── GlobalExceptionHandler.java (Error handling)
```

## API Endpoints

### Public Endpoints (JWT Required)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/users/me` | Any authenticated user | Get current user profile |
| GET | `/users/customers` | MANAGER | Get assigned customers |
| GET | `/users/{id}` | ADMIN / Owner / MANAGER (if assigned) | Get user by ID |
| PUT | `/users/{id}/manager-info` | ADMIN only | Update manager-specific fields |

### Internal Endpoints (No JWT - Protected at Gateway)

| Method | Endpoint | Called By | Description |
|--------|----------|-----------|-------------|
| POST | `/internal/users` | auth-service | Create user record |
| PUT | `/internal/users/{userId}/roles` | auth-service | Update user roles |
| PUT | `/internal/users/{userId}/sync` | auth-service | Sync user info |

## Data Flow

### 1. User Registration
```
Client → auth-service → auth-service creates account
                     ↓
                     internal API call
                     ↓
                     user-service creates user record
```

### 2. Get User Profile
```
Client → user-service/users/me (JWT token)
         ↓
         UserService.getUserProfile()
         ↓
         - Fetch user from database
         - Apply role-based filtering
         - TODO: Fetch totalOrders from order-service
         ↓
         Return filtered UserDTO
```

### 3. ADMIN Updates Manager Info
```
ADMIN → PUT /users/{id}/manager-info
        ↓
        Update: salary, joiningDate, attendance, assignedManagerId
```

## Role-Based Access

### ADMIN
✅ View all users with all fields  
✅ Update manager-specific fields  
✅ See: salary, joiningDate, attendance, assignedManagerId  

### MANAGER
✅ View own profile with manager fields  
✅ View assigned customers (only basic fields + totalOrders)  
❌ Cannot see other managers' sensitive fields  

### CUSTOMER
✅ View own profile  
❌ Cannot view other users  

## Key Features

✨ **Clean Controller**: Only routing and @PreAuthorize - no business logic  
✨ **Service Layer**: All business logic, filtering, and data access  
✨ **Role-Based Filtering**: Automatic field visibility based on viewer's role  
✨ **Separation of Concerns**: Each service manages its own data  
✨ **Internal API**: Secure communication between microservices  

## Security

- **JWT Authentication**: OAuth2 Resource Server validates tokens from auth-service
- **Method-Level Security**: @PreAuthorize annotations on all endpoints
- **Role-Based Authorization**: ADMIN, MANAGER, CUSTOMER roles
- **Field-Level Security**: Sensitive fields filtered based on viewer's role

## Next Steps

- [ ] Implement RestTemplate/WebClient to fetch totalOrders from order-service
- [ ] Add caching for frequently accessed user data
- [ ] Implement pagination for customer lists
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Set up integration tests

