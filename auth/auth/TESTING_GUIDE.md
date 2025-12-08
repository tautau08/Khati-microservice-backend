# Auth Service - Testing Guide

## Prerequisites
- PostgreSQL installed and running
- Java 17 installed
- Maven installed

## Step 1: Database Setup

Open PostgreSQL terminal and run:

```sql
CREATE DATABASE auth_db;
CREATE USER auth_user WITH ENCRYPTED PASSWORD 'auth_pass_123';
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;
\c auth_db
GRANT ALL ON SCHEMA public TO auth_user;
```

## Step 2: Start the Application

Navigate to the project directory:
```bash
cd "D:\Intership Period\Khati_microservice\auth\auth"
mvn clean install
mvn spring-boot:run
```

You should see in the logs:
```
Created role: ROLE_ADMIN
Created role: ROLE_MANAGER
Created role: ROLE_CUSTOMER
Default roles initialized successfully
Started AuthApplication in X.XXX seconds
```

The service should be running on **http://localhost:8081**

## Step 3: Test Registration

### Request:
```bash
curl -X POST http://localhost:8081/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"password\":\"password123\",\"email\":\"test@example.com\"}"
```

### Expected Response:
```json
{
  "token": null,
  "username": "testuser",
  "message": "User registered successfully"
}
```

## Step 4: Test Login

### Request:
```bash
curl -X POST http://localhost:8081/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"password\":\"password123\"}"
```

### Expected Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0NVU1RPTUVSIl0sInN1YiI6InRlc3R1c2VyIiwiaWF0IjoxNzMwNDgwMDAwLCJleHAiOjE3MzA1NjY0MDB9.abc123...",
  "username": "testuser",
  "message": "Login successful"
}
```

**SAVE THIS TOKEN!** You'll need it for the next tests.

## Step 5: Test Protected Endpoint (Authenticated)

Replace `YOUR_TOKEN_HERE` with the token from Step 4:

### Request:
```bash
curl -X GET http://localhost:8081/api/auth/me ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Expected Response:
```
Authenticated user: testuser, Authorities: [ROLE_CUSTOMER]
```

## Step 6: Test Admin Endpoint (Should Fail)

### Request:
```bash
curl -X GET http://localhost:8081/api/auth/admin/test ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Expected Response:
```json
{
  "timestamp": "2024-11-01T...",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/auth/admin/test"
}
```

This is CORRECT! The user doesn't have ROLE_ADMIN.

## Step 7: Assign Admin Role to User

Connect to PostgreSQL:
```bash
psql -U auth_user -d auth_db
```

Run these queries:
```sql
-- Check current user and roles
SELECT * FROM users WHERE username = 'testuser';
SELECT * FROM roles;
SELECT * FROM user_roles;

-- Get the IDs
-- Let's say user id=1 and ROLE_ADMIN id=1

-- Assign admin role to user
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- Verify
SELECT u.username, r.name 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id;
```

## Step 8: Login Again to Get New Token with Admin Role

Since we changed the user's roles, we need a fresh token:

```bash
curl -X POST http://localhost:8081/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"password\":\"password123\"}"
```

Get the NEW token from the response.

## Step 9: Test Admin Endpoint Again (Should Succeed)

```bash
curl -X GET http://localhost:8081/api/auth/admin/test ^
  -H "Authorization: Bearer NEW_TOKEN_HERE"
```

### Expected Response:
```
Admin access granted!
```

✅ **SUCCESS!** Role-based authorization is working!

## Step 10: Test Manager Endpoint

```bash
curl -X GET http://localhost:8081/api/auth/manager/test ^
  -H "Authorization: Bearer NEW_TOKEN_HERE"
```

### Expected Response:
```
Manager or Admin access granted!
```

This works because the user has ROLE_ADMIN, which is allowed on this endpoint.

## Verify Database Tables

Check that all tables were created:

```sql
\c auth_db
\dt

-- Should show:
-- public | roles      | table | auth_user
-- public | user_roles | table | auth_user
-- public | users      | table | auth_user
```

Check the data:

```sql
-- Check roles
SELECT * FROM roles;
-- Should show:
-- 1 | ROLE_ADMIN
-- 2 | ROLE_MANAGER
-- 3 | ROLE_CUSTOMER

-- Check users
SELECT id, username, email, created_at FROM users;

-- Check user-role assignments
SELECT u.username, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;
```

## Test with Postman (Alternative to curl)

### 1. Register
- Method: POST
- URL: `http://localhost:8081/api/auth/register`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "username": "john",
  "password": "pass123",
  "email": "john@test.com"
}
```

### 2. Login
- Method: POST
- URL: `http://localhost:8081/api/auth/login`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "username": "john",
  "password": "pass123"
}
```
- Copy the token from response

### 3. Test Protected Endpoint
- Method: GET
- URL: `http://localhost:8081/api/auth/me`
- Headers: `Authorization: Bearer YOUR_TOKEN`

### 4. Test Admin Endpoint
- Method: GET
- URL: `http://localhost:8081/api/auth/admin/test`
- Headers: `Authorization: Bearer YOUR_TOKEN`

## Common Issues & Solutions

### Issue: "User not found" on login
**Solution:** Make sure you registered first with the exact same username.

### Issue: "Default role not found"
**Solution:** Check that the DataLoader ran successfully. Restart the app or manually insert roles:
```sql
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_MANAGER'), ('ROLE_CUSTOMER');
```

### Issue: Token expired
**Solution:** Tokens expire after 24 hours. Login again to get a fresh token.

### Issue: 401 Unauthorized
**Solution:** 
- Check that you included the Authorization header
- Make sure the token is prefixed with "Bearer "
- Verify the token hasn't expired

### Issue: 403 Forbidden
**Solution:** The user doesn't have the required role. Assign the role in the database and get a new token.

## Decoding JWT Token

You can decode (but not verify) your JWT token at https://jwt.io

Paste your token and you'll see:

**Header:**
```json
{
  "alg": "HS512"
}
```

**Payload:**
```json
{
  "roles": [
    "ROLE_CUSTOMER",
    "ROLE_ADMIN"
  ],
  "sub": "testuser",
  "iat": 1730480000,
  "exp": 1730566400
}
```

- `roles`: User's roles
- `sub`: Username (subject)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

## Next Steps

Now that your Auth Service is working, you can:

1. Build the other microservices (User, Inventory, Order)
2. Create the API Gateway to route requests
3. Implement service-to-service authentication
4. Add refresh tokens
5. Implement OAuth2 for third-party authentication

## Success Criteria

✅ Users can register  
✅ Users can login and receive JWT token  
✅ JWT token includes user's roles  
✅ Protected endpoints require valid token  
✅ Role-based endpoints enforce role checks  
✅ ROLE_CUSTOMER assigned by default on registration  
✅ Multiple roles can be assigned to one user  
✅ Database tables created automatically  
✅ Passwords are encrypted  

**Your auth service is fully functional and ready for integration!**

