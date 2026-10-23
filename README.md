# Khati Microservice Backend

A production-ready, role-based microservices backend built with **Spring Boot 3.5.7** and **Java 17**. The system is composed of five independent services that communicate through a central API Gateway, secured end-to-end with JWT authentication.

---

## Architecture Overview

```
                        ┌─────────────────────────────────┐
                        │         Client / Frontend        │
                        └────────────────┬────────────────┘
                                         │ All requests
                                         ▼
                        ┌─────────────────────────────────┐
                        │       API Gateway  :8080         │
                        │  (Routing + Security Filter)     │
                        └──┬──────────┬──────────┬───┬───┘
              /api/auth/**  │  /api/users/**       │   │  /api/inventory/**  /api/orders/**
                            │          │           │   │
              ┌─────────────▼──┐  ┌────▼──────┐  ┌▼───▼──────┐  ┌───────────────┐
              │  Auth Service   │  │   User    │  │ Inventory  │  │ Order Service │
              │    :8081        │  │  Service  │  │  Service   │  │    :8084      │
              │                 │  │   :8082   │  │   :8083    │  │               │
              └─────────────────┘  └───────────┘  └────────────┘  └───────────────┘
                      │                  │               │                 │
              ┌───────▼──────┐   ┌───────▼──────┐ ┌─────▼──────┐  ┌──────▼──────┐
              │   auth_db    │   │   userdb     │ │ inventorydb │  │  orderdb    │
              │  (PostgreSQL)│   │  (PostgreSQL)│ │ (PostgreSQL)│  │ (PostgreSQL)│
              └──────────────┘   └──────────────┘ └─────────────┘  └─────────────┘
```

### Service Registry

| Service               | Port | Responsibility                                   |
|-----------------------|------|--------------------------------------------------|
| **API Gateway**       | 8080 | Single entry point; request routing & security   |
| **Auth Service**      | 8081 | Registration, login, JWT issuance & RBAC         |
| **User Service**      | 8082 | User profile management & role-based data access |
| **Inventory Service** | 8083 | Product & category management                    |
| **Order Service**     | 8084 | Order creation, status tracking & fulfilment     |

---

## Services

### 🔐 Auth Service (`auth/`)
The identity provider for the entire platform. It is the **single source of truth** for credentials and roles.

**Key Features:**
- User registration (defaults to `ROLE_CUSTOMER`)
- JWT token generation (HS512, 24-hour expiry)
- Role-based access: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_CUSTOMER`
- BCrypt password hashing
- Actuator health endpoint
- On startup, seeds default roles automatically via a `DataLoader`

**Database:** `auth_db` — tables: `users`, `roles`, `user_roles`

---

### 👤 User Service (`user/`)
Manages extended user profiles with role-sensitive field visibility.

**Key Features:**
- Syncs user identity (id, username, email, roles) from Auth Service via internal API
- ADMIN-only fields: `salary`, `joiningDate`, `attendance`, `assignedManagerId`
- Aggregates `totalOrders` from Order Service on demand
- Role-based response filtering: ADMIN sees all fields, MANAGER sees their own + limited customer data, CUSTOMER sees own data only
- Validates JWT tokens via Spring OAuth2 Resource Server (issuer-uri points to Auth Service)

**Database:** `userdb`

---

### 📦 Inventory Service (`inventory/`)
Manages the product catalogue and categories with admin-controlled writes.

**Key Features:**
- Public read access to products and categories
- ADMIN-only create/update/delete operations
- JWT-secured endpoints via OAuth2 Resource Server
- Internal endpoint for order service to update stock levels (`/internal/update`)

**Database:** `inventorydb`

---

### 🛒 Order Service (`order/`)
Handles the full order lifecycle.

**Key Features:**
- Create orders (CUSTOMER/MANAGER)
- View own orders or all orders (ADMIN)
- Update order status (ADMIN: any status, MANAGER: limited transitions)
- Notifies User Service to increment order counts via internal API
- JWT-secured via OAuth2 Resource Server

**Database:** `orderdb`

---

### 🌐 API Gateway (`gateway/`)
The sole public-facing entry point. Clients **never** call backend services directly.

**Key Features:**
- Reverse-proxy routing via Spring WebFlux `WebClient`
- Blocks all `/internal/**` paths from external callers via `InternalEndpointFilter` (servlet filter, Order=1)
- Forwards headers, query parameters, and request bodies transparently
- Supports all HTTP methods (GET, POST, PUT, DELETE, PATCH, etc.)
- Actuator health check at `/actuator/health`

**Route Table:**

| Gateway Path          | Downstream Service      |
|-----------------------|-------------------------|
| `/api/auth/**`        | Auth Service :8081      |
| `/api/users/**`       | User Service :8082      |
| `/api/inventory/**`   | Inventory Service :8083 |
| `/api/orders/**`      | Order Service :8084     |

---

## Technology Stack

| Layer        | Technology                                                        |
|--------------|-------------------------------------------------------------------|
| Language     | Java 17                                                           |
| Framework    | Spring Boot 3.5.7                                                 |
| Security     | Spring Security + JWT (jjwt 0.11.5) / OAuth2 Resource Server     |
| Persistence  | Spring Data JPA + Hibernate                                       |
| Database     | PostgreSQL                                                        |
| HTTP Client  | Spring WebFlux WebClient (Gateway)                                |
| Build Tool   | Maven (Maven Wrapper included)                                    |
| Utilities    | Lombok                                                            |

---

## Prerequisites

- **Java 17+**
- **Maven 3.6+** (or use the included `mvnw` wrapper)
- **PostgreSQL 12+**

---

## Getting Started

### 1. Create Databases

```sql
-- Auth
CREATE DATABASE auth_db;
CREATE USER auth_user WITH ENCRYPTED PASSWORD 'auth_pass_123';
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;
\c auth_db
GRANT ALL ON SCHEMA public TO auth_user;

-- User, Inventory, Order
CREATE DATABASE userdb;
CREATE DATABASE inventorydb;
CREATE DATABASE orderdb;
```

### 2. Configure Each Service

Edit `application.properties` in each service's `src/main/resources/`:

```properties
# Auth Service
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=auth_user
spring.datasource.password=auth_pass_123
jwt.secret=<your-256-bit-secret>
jwt.expiration=86400000

# User Service
server.port=8082
spring.datasource.url=jdbc:postgresql://localhost:5432/userdb
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081

# Inventory Service
server.port=8083
spring.datasource.url=jdbc:postgresql://localhost:5432/inventorydb
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081

# Order Service
server.port=8084
spring.datasource.url=jdbc:postgresql://localhost:5432/orderdb
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081

# API Gateway
server.port=8080
services.auth=http://localhost:8081
services.user=http://localhost:8082
services.inventory=http://localhost:8083
services.order=http://localhost:8084
```

### 3. Build & Run Each Service

> **Start Auth Service first** — other services depend on it as JWT issuer.

```bash
# 1. Auth Service
cd auth/auth && ./mvnw spring-boot:run

# 2. User Service
cd user/user && ./mvnw spring-boot:run

# 3. Inventory Service
cd inventory/inventory && ./mvnw spring-boot:run

# 4. Order Service
cd order/order && ./mvnw spring-boot:run

# 5. API Gateway (last)
cd gateway/gateway && ./mvnw spring-boot:run
```

---

## API Reference (via Gateway at :8080)

### Authentication
```http
POST /api/auth/register    # Register new user (public)
POST /api/auth/login       # Login → returns JWT token (public)
GET  /api/auth/me          # Get current authenticated user
```

### User Management
```http
GET /api/users/me                  # Own profile
GET /api/users/customers           # Assigned customers (MANAGER)
GET /api/users/{id}                # User by ID (role-filtered)
PUT /api/users/{id}/manager-info   # Update manager fields (ADMIN)
```

### Inventory
```http
GET    /api/inventory/categories               # List categories
GET    /api/inventory/products                 # List products
GET    /api/inventory/products/{id}            # Product detail
POST   /api/inventory/admin/categories         # Create category (ADMIN)
PUT    /api/inventory/admin/categories/{id}    # Update category (ADMIN)
DELETE /api/inventory/admin/categories/{id}    # Delete category (ADMIN)
POST   /api/inventory/admin/products           # Create product (ADMIN)
PUT    /api/inventory/admin/products/{id}      # Update product (ADMIN)
DELETE /api/inventory/admin/products/{id}      # Delete product (ADMIN)
```

### Orders
```http
POST /api/orders                     # Place order
GET  /api/orders/my                  # Own orders
GET  /api/orders/{id}                # Order detail
GET  /api/orders/admin/all           # All orders (ADMIN)
PUT  /api/orders/admin/{id}/status   # Update status (ADMIN)
PUT  /api/orders/manager/{id}/status # Update status (MANAGER)
```

> All endpoints except `/api/auth/register` and `/api/auth/login` require:
> `Authorization: Bearer <jwt_token>`

---

## Security Model

| Aspect                   | Details                                                             |
|--------------------------|---------------------------------------------------------------------|
| Authentication           | Stateless JWT — no server-side sessions                             |
| Roles                    | `ROLE_ADMIN` › `ROLE_MANAGER` › `ROLE_CUSTOMER`                    |
| Password storage         | BCrypt hashing (Auth Service)                                       |
| Internal API protection  | `/internal/**` blocked at gateway; service-to-service only         |
| SQL injection prevention | JPA/Hibernate parameterized queries                                 |
| JWT algorithm            | HS512 (signed with shared secret)                                   |
| JWT expiry               | 24 hours                                                            |

---

## Repository Structure

```
Khati-microservice-backend/
├── auth/
│   └── auth/                  # Auth Service (Spring Boot)
├── gateway/
│   └── gateway/               # API Gateway (Spring Boot)
├── inventory/
│   └── inventory/             # Inventory Service (Spring Boot)
├── order/
│   └── order/                 # Order Service (Spring Boot)
└── user/
    └── user/                  # User Service (Spring Boot)
```

Each service is a self-contained Maven project with its own `pom.xml`, Maven wrapper, and `src/` directory.

---

## Service-to-Service Communication

Internal calls bypass the gateway and communicate directly on localhost:

| Caller            | Target            | Purpose                                     |
|-------------------|-------------------|---------------------------------------------|
| Auth Service      | User Service      | Create user record after registration       |
| Auth Service      | User Service      | Sync roles after role update                |
| User Service      | Order Service     | Fetch `totalOrders` count for a user        |
| Order Service     | User Service      | Increment user's order count                |
| Order Service     | Inventory Service | Update stock levels after order placement   |

---

## Contributing

1. Each microservice is **independently deployable** — changes to one service do not require rebuilding others.
2. Follow the existing package structure within each service.
3. Internal endpoints must **never** be exposed through the gateway.
4. Register new service routes in the gateway's `ProxyController` and `ServiceConfig`.
