# Smart Library Platform

> Production-style **microservices-based Library Management System** built with Java 17, Spring Boot, Spring Cloud Gateway, Eureka, JWT, RBAC, PostgreSQL, Flyway, OpenFeign, Resilience4j, Docker, and AWS-ready CI/CD.

---

## Table of Contents

- [Overview](#overview)
- [Business Problem](#business-problem)
- [Architecture](#architecture)
- [Implemented Microservices](#implemented-microservices)
- [Technology Stack](#technology-stack)
- [Security Design](#security-design)
- [Role-Based Access Control](#role-based-access-control)
- [Core Business Flows](#core-business-flows)
- [Database Design](#database-design)
- [Resilience and Fault Tolerance](#resilience-and-fault-tolerance)
- [Centralized Logging and Trace ID](#centralized-logging-and-trace-id)
- [Service-to-Service Communication](#service-to-service-communication)
- [API Gateway Routing and Security](#api-gateway-routing-and-security)
- [Analytics Service and Dashboard Summary](#analytics-service-and-dashboard-summary)
- [API Documentation](#api-documentation)
- [Local Setup](#local-setup)
- [Run Order](#run-order)
- [Maven Build Commands](#maven-build-commands)
- [Dockerization](#dockerization)
- [CI/CD Pipeline](#cicd-pipeline)
- [AWS Deployment Readiness](#aws-deployment-readiness)
- [API Demo Guide](#api-demo-guide)
- [Manager Demo Script](#manager-demo-script)
- [Questions to Discuss with Manager](#questions-to-discuss-with-manager)
- [Possible Manager Questions and Answers](#possible-manager-questions-and-answers)
- [Completed Milestones](#completed-milestones)
- [Future Roadmap](#future-roadmap)
- [Final Summary](#final-summary)

---

## Overview

**Smart Library Platform** is a production-style backend platform for managing library operations using a microservices architecture.

The platform includes authentication, user profile management, book catalog management, inventory management, borrow and return workflows, fine calculation, notification handling, dashboard analytics, API Gateway security, service discovery, trace logging, resilience, Dockerization, and CI/CD readiness.

### One-Line Summary

> Smart Library Platform is a secure, scalable, and modular microservices-based Library Management System with JWT authentication, RBAC authorization, service discovery, API Gateway, distributed tracing, resilience patterns, Dockerization, and AWS ECS deployment readiness.

---

## Business Problem

Traditional library systems are often monolithic and difficult to scale, maintain, test, and deploy independently.

This project solves that problem by splitting business capabilities into independent services:

```text
Authentication and authorization
User profile management
Book catalog management
Inventory management
Borrow and return workflow
Fine calculation
Notification handling
Dashboard analytics
Gateway routing
Service discovery
Observability and tracing
```

---

## Architecture

```text
Frontend / Postman
        |
        v
+------------------+
|   API Gateway    |
|  Port: 8080      |
|  JWT validation  |
|  CORS            |
|  Route security  |
|  Trace ID        |
+------------------+
        |
        v
+---------------------+
| Service Discovery   |
| Eureka: 8761        |
+---------------------+
        |
        +-------------------+------------------+------------------+--------------------+-----------------------+
        |                   |                  |                  |                    |                       |
        v                   v                  v                  v                    v                       v
+---------------+   +---------------+   +---------------+   +----------------+   +----------------------+   +-------------------+
| Auth Service  |   | User Service  |   | Book Service  |   | Borrow Service |   | Notification Service |   | Analytics Service |
| 8081          |   | 8082          |   | 8083          |   | 8084           |   | 8085                 |   | 8090              |
+---------------+   +---------------+   +---------------+   +----------------+   +----------------------+   +-------------------+
        |                   |                  |                  |                    |
        v                   v                  v                  v                    v
   Auth DB              User DB            Book DB           Borrow DB          Notification DB
```

The architecture follows these microservice principles:

```text
Each service owns its domain responsibility
Each service exposes REST APIs
Each domain service owns its database
API Gateway handles routing and route-level security
JWT is used for authentication and authorization
Internal APIs use X-Internal-Token for trusted service-to-service operations
Analytics service aggregates dashboard data without owning a database
```

---

## Implemented Microservices

```text
discovery-server
api-gateway
auth-service
user-service
book-service
borrow-service
notification-service
analytics-service
```

### Service Responsibilities

| Service | Responsibility |
|---|---|
| discovery-server | Eureka service registry |
| api-gateway | Central routing, JWT validation, permission validation, CORS, trace ID |
| auth-service | Login, registration, JWT, roles, permissions, auth user active status |
| user-service | User profile, phone, membership type/status, internal profile creation |
| book-service | Book catalog, search, availability, inventory stock |
| borrow-service | Borrow records, returns, overdue calculation, fine payment |
| notification-service | Internal notification creation, notification read/unread APIs |
| analytics-service | Dashboard summary aggregation from other services |

---

## Technology Stack

```text
Java 17
Spring Boot 3.2.5
Spring Cloud 2023.0.1
Spring Cloud Gateway
Spring Cloud OpenFeign
Spring Cloud Netflix Eureka
Spring Security
JWT - jjwt
RBAC
PostgreSQL
Flyway
Resilience4j
Swagger / OpenAPI
Lombok
MapStruct
Maven multi-module project
Docker
Docker Compose
GitHub Actions / Jenkins ready
AWS ECR
AWS ECS Fargate
AWS RDS PostgreSQL
AWS Secrets Manager
AWS CloudWatch
```

---

## Current Service Ports

```text
discovery-server       8761
api-gateway            8080
auth-service           8081
user-service           8082
book-service           8083
borrow-service         8084
notification-service   8085
analytics-service      8090
```

---

## Security Design

The platform follows **defense-in-depth security**.

```text
API Gateway = first security layer
Microservice = final service-level security layer
```

### Authentication

User login is handled by Auth Service.

```http
POST /api/auth/login
```

Auth Service validates:

```text
email
password
BCrypt password hash
active login status
```

Then Auth Service returns a JWT containing:

```text
userId
email / subject
roles
permissions
expiration
```

### Authorization

Authorization is permission-based using RBAC.

Recommended permissions:

```text
DASHBOARD_READ
USER_READ
USER_WRITE
BOOK_READ
BOOK_WRITE
INVENTORY_READ
INVENTORY_WRITE
BORROW_READ
BORROW_WRITE
RETURN_READ
RETURN_WRITE
```

---

## Role-Based Access Control

### MEMBER

```text
DASHBOARD_READ
BOOK_READ
BORROW_READ
BORROW_WRITE
RETURN_READ
RETURN_WRITE
```

### LIBRARIAN

```text
DASHBOARD_READ
BOOK_READ
BOOK_WRITE
INVENTORY_READ
INVENTORY_WRITE
BORROW_READ
BORROW_WRITE
RETURN_READ
RETURN_WRITE
```

### ADMIN

```text
All permissions
```

---

## JWT Example

```json
{
  "sub": "admin@library.com",
  "userId": 1,
  "roles": ["ADMIN"],
  "permissions": [
    "DASHBOARD_READ",
    "USER_READ",
    "USER_WRITE",
    "BOOK_READ",
    "BOOK_WRITE",
    "INVENTORY_READ",
    "INVENTORY_WRITE",
    "BORROW_READ",
    "BORROW_WRITE",
    "RETURN_READ",
    "RETURN_WRITE"
  ]
}
```

---

## API Gateway Routing and Security

API Gateway responsibilities:

```text
Single backend entry point
Route matching
JWT validation
Route-level permission validation
Trace ID creation and propagation
Forwarding user context headers
Centralized CORS
Optional Swagger aggregation
```

### Gateway Routes

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**

        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**,/api/internal/users/**

        - id: book-service
          uri: lb://book-service
          predicates:
            - Path=/api/books/**

        - id: borrow-service
          uri: lb://borrow-service
          predicates:
            - Path=/api/borrow-records/**

        - id: notification-service
          uri: lb://notification-service
          predicates:
            - Path=/api/notifications/**,/api/internal/notifications/**

        - id: analytics-service
          uri: lb://analytics-service
          predicates:
            - Path=/api/dashboard/**
```

> Do not use `StripPrefix` for these routes because each service controller expects the full `/api/...` path.

### Public Endpoints

```text
/api/auth/login
/api/auth/register
/actuator/health
/swagger-ui/**
/v3/api-docs/**
```

### Protected Admin Endpoints

Do not make this public:

```text
/api/auth/admin/**
```

---

## Suggested Gateway Permission Rules

```java
new RoutePermissionRule(HttpMethod.GET, "/api/dashboard/**", "DASHBOARD_READ"),

new RoutePermissionRule(HttpMethod.GET, "/api/auth/admin/**", "USER_READ"),
new RoutePermissionRule(HttpMethod.POST, "/api/auth/admin/**", "USER_WRITE"),
new RoutePermissionRule(HttpMethod.PATCH, "/api/auth/admin/**", "USER_WRITE"),

new RoutePermissionRule(HttpMethod.GET, "/api/users/**", "USER_READ"),
new RoutePermissionRule(HttpMethod.POST, "/api/users/**", "USER_WRITE"),
new RoutePermissionRule(HttpMethod.PUT, "/api/users/**", "USER_WRITE"),
new RoutePermissionRule(HttpMethod.PATCH, "/api/users/**", "USER_WRITE"),

new RoutePermissionRule(HttpMethod.GET, "/api/books/inventory/**", "INVENTORY_READ"),
new RoutePermissionRule(HttpMethod.POST, "/api/books/inventory/**", "INVENTORY_WRITE"),
new RoutePermissionRule(HttpMethod.PATCH, "/api/books/inventory/**", "INVENTORY_WRITE"),

new RoutePermissionRule(HttpMethod.GET, "/api/books/**", "BOOK_READ"),
new RoutePermissionRule(HttpMethod.POST, "/api/books/**", "BOOK_WRITE"),
new RoutePermissionRule(HttpMethod.PUT, "/api/books/**", "BOOK_WRITE"),
new RoutePermissionRule(HttpMethod.DELETE, "/api/books/**", "BOOK_WRITE"),

new RoutePermissionRule(HttpMethod.GET, "/api/borrow-records/**", "BORROW_READ"),
new RoutePermissionRule(HttpMethod.POST, "/api/borrow-records/**", "BORROW_WRITE"),
new RoutePermissionRule(HttpMethod.PATCH, "/api/borrow-records/*/return", "RETURN_WRITE"),
new RoutePermissionRule(HttpMethod.GET, "/api/borrow-records/*/fine", "BORROW_READ"),
new RoutePermissionRule(HttpMethod.PATCH, "/api/borrow-records/*/fine/pay", "RETURN_WRITE"),

new RoutePermissionRule(HttpMethod.GET, "/api/notifications/**", "BORROW_READ"),
new RoutePermissionRule(HttpMethod.PATCH, "/api/notifications/**", "BORROW_READ")
```

---

## Service-to-Service Communication

Internal service calls use OpenFeign.

Example from Auth Service to User Service:

```java
@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}"
)
public interface UserClient {

    @PostMapping("/api/internal/users")
    void createUser(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestBody CreateUserRequest request
    );
}
```

Internal APIs require:

```text
X-Internal-Token
```

This prevents public clients from directly calling trusted internal operations.

---

## Core Business Flows

### User Registration Flow

```text
Client
  ↓
API Gateway
  ↓
Auth Service
  ↓
Save credentials and role in Auth DB
  ↓
Feign call to User Service
  ↓
Create user profile in User DB
```

Public self-registration creates only `MEMBER`.

```http
POST /api/auth/register
```

```json
{
  "email": "member1@library.com",
  "password": "member123",
  "fullName": "Member One",
  "phone": "9876543210"
}
```

### Admin Create User Flow

```http
POST /api/auth/admin/users
Authorization: Bearer <ADMIN_TOKEN>
```

Admin can create:

```text
MEMBER
LIBRARIAN
```

### Login Flow

```text
Client
  ↓
API Gateway
  ↓
Auth Service
  ↓
Validate password and active status
  ↓
Generate JWT
  ↓
Return token, roles, permissions
```

### Borrow Book Flow

```text
Client
  ↓
API Gateway
  ↓
Borrow Service
  ↓
Check user and book request
  ↓
Call Book Service to validate availability
  ↓
Reduce available copies
  ↓
Save borrow record
  ↓
Call Notification Service
```

### Return Book Flow

```text
Client
  ↓
API Gateway
  ↓
Borrow Service
  ↓
Find borrow record
  ↓
Increase book available copies
  ↓
Calculate fine if overdue
  ↓
Update borrow record as RETURNED
  ↓
Call Notification Service
```

---

## Database Design

Each domain service owns its own database.

```text
auth_service_db
user_service_db
book_service_db
borrow_service_db
notification_service_db
```

`analytics-service` owns no database.

### Auth DB

```text
auth_users
roles
permissions
user_roles
role_permissions
```

### User DB

```text
users
```

### Book DB

```text
books
```

Important fields:

```text
total_copies
available_copies
```

### Borrow DB

```text
borrow_records
```

Important fields:

```text
borrowed_at
due_date
returned_at
status
overdue_days
fine_amount
fine_paid
fine_paid_at
```

### Notification DB

```text
notifications
```

Important fields:

```text
type
channel
status
subject
message
read
read_at
```

---

## Flyway Migration

Flyway is used for database versioning.

Benefits:

```text
Version-controlled schema changes
Consistent DB setup
Repeatable local/dev/prod migrations
Avoids manual database changes
```

Example migration files:

```text
V1__create_auth_tables.sql
V1__create_users_table.sql
V1__create_books_table.sql
V1__create_borrow_records_table.sql
V2__add_fine_columns_to_borrow_records.sql
V1__create_notifications_table.sql
V2__add_notification_read_fields.sql
```

---

## Resilience and Fault Tolerance

### OpenFeign Communication

```text
Auth Service → User Service
Borrow Service → Book Service
Borrow Service → Notification Service
Analytics Service → User/Book/Borrow/Notification Services
```

### Resilience4j

Recommended and/or implemented for:

```text
Auth Service → User Service
Borrow Service → Book Service
Borrow Service → Notification Service
Analytics Service → downstream read APIs
```

Features:

```text
Circuit breaker
Retry
Timeout
Fallback response
```

### Retry Strategy

Read operations can retry safely:

```text
GET /api/books/{id}
GET /api/dashboard/summary downstream reads
```

Mutation operations should not be retried blindly:

```text
borrow copy
return copy
create notification
```

Reason:

```text
Retrying mutation APIs can create duplicate inventory updates or duplicate records.
```

### Compensation Logic

If Borrow Service decreases book inventory but fails to save borrow record, Borrow Service should attempt compensation:

```text
Return copy back to Book Service
```

This is a basic consistency improvement. Later, this can be improved with Saga pattern and Kafka.

---

## Centralized Logging and Trace ID

### Goal

Track one request across multiple services.

### Flow

```text
API Gateway generates or receives X-Trace-Id
↓
Gateway forwards X-Trace-Id
↓
Downstream services store traceId in MDC
↓
Logs across services can be correlated
```

Example:

```text
traceId=64c4c0c8-8f6f-4c5d-a41c-f2fbdcc12345
```

This trace ID appears in logs of:

```text
API Gateway
Book Service
Borrow Service
Notification Service
Analytics Service
```

---

## API Documentation

Each service can expose Swagger/OpenAPI:

```text
http://localhost:<service-port>/swagger-ui.html
http://localhost:<service-port>/v3/api-docs
```

Example:

```text
Auth Service:          http://localhost:8081/swagger-ui.html
Book Service:          http://localhost:8083/swagger-ui.html
Borrow Service:        http://localhost:8084/swagger-ui.html
Notification Service:  http://localhost:8085/swagger-ui.html
Analytics Service:     http://localhost:8090/swagger-ui.html
```

---

## API Modules

### Auth Service APIs

```http
POST  /api/auth/register
POST  /api/auth/login
POST  /api/auth/admin/users
PATCH /api/auth/admin/users/{userId}/activate
PATCH /api/auth/admin/users/{userId}/deactivate
GET   /api/auth/admin/users/{userId}/status
GET   /api/auth/admin/users/statuses
```

### User Service APIs

```http
POST  /api/internal/users
GET   /api/users
GET   /api/users/{id}
PUT   /api/users/{id}
PATCH /api/users/{id}/status
```

### Book Service APIs

```http
POST   /api/books
GET    /api/books
GET    /api/books/{id}
GET    /api/books/search
GET    /api/books/available
PUT    /api/books/{id}
DELETE /api/books/{id}
```

### Inventory APIs

```http
GET   /api/books/inventory/{bookId}
GET   /api/books/inventory/low-stock
PATCH /api/books/inventory/{bookId}/add
PATCH /api/books/inventory/{bookId}/remove
PATCH /api/books/inventory/{bookId}/available
```

> If your actual backend uses `/add-copies`, `/remove-copies`, or `/available-copies`, update the endpoint names here to match the controller exactly.

### Borrow APIs

```http
POST  /api/borrow-records
GET   /api/borrow-records
GET   /api/borrow-records/{id}
GET   /api/borrow-records/user/{userId}
PATCH /api/borrow-records/{id}/return
GET   /api/borrow-records/{id}/fine
PATCH /api/borrow-records/{id}/fine/pay
```

### Notification APIs

```http
POST  /api/internal/notifications
GET   /api/notifications
GET   /api/notifications/{id}
GET   /api/notifications/user/{userId}
GET   /api/notifications/type/{type}
GET   /api/notifications/status/{status}
GET   /api/notifications/user/{userId}/unread-count
PATCH /api/notifications/{id}/read
PATCH /api/notifications/user/{userId}/read-all
```

### Analytics APIs

```http
GET /api/dashboard/summary
```

---

## Analytics Service and Dashboard Summary

The `analytics-service` is a stateless aggregation service.

It does not need:

```text
Entity
Repository
Database
Flyway
JPA
```

It calls:

```text
user-service
book-service
borrow-service
notification-service
```

using OpenFeign.

### Dashboard Summary API

```http
GET /api/dashboard/summary
Authorization: Bearer <TOKEN>
```

### Admin/Librarian Response

```json
{
  "totalUsers": 4,
  "totalBooks": 20,
  "availableBooks": 15,
  "lowStockBooks": 2,
  "borrowRecords": 8,
  "pendingFines": 1,
  "notifications": 6,
  "memberView": false
}
```

### Member Response

```json
{
  "totalUsers": 0,
  "totalBooks": 20,
  "availableBooks": 15,
  "lowStockBooks": 0,
  "borrowRecords": 2,
  "pendingFines": 1,
  "notifications": 3,
  "memberView": true
}
```

### Why Analytics Service?

Before:

```text
Angular dashboard called multiple backend APIs
```

After:

```text
Angular calls only GET /api/dashboard/summary
```

Benefits:

```text
Faster dashboard loading
Cleaner Angular DashboardService
Centralized role-based count logic
Better backend ownership of aggregation logic
Easier future reporting changes
```

---

## Error Code Standards

Recommended common error response:

```json
{
  "code": "AUTH_003",
  "message": "User already exists with email: elvin@gmail.com",
  "path": "/api/auth/register",
  "traceId": "82487dfa-ad50-4759-805b-edeee0adf740",
  "timestamp": "2026-07-10T19:49:42.2806275"
}
```

Recommended prefixes:

```text
AUTH_           auth-service
USER_           user-service
BOOK_           book-service
BORROW_         borrow-service
NOTIFICATION_   notification-service
ANALYTICS_      analytics-service
SEC_            common security errors
```

Common security codes:

```text
SEC_001 Authentication required
SEC_002 Access denied
```

Analytics error codes:

```text
ANALYTICS_001 Invalid analytics request
ANALYTICS_002 Required user context is missing
ANALYTICS_003 Unable to fetch data from user service
ANALYTICS_004 Unable to fetch data from book service
ANALYTICS_005 Unable to fetch data from borrow service
ANALYTICS_006 Unable to fetch data from notification service
ANALYTICS_007 Dashboard summary generation failed
ANALYTICS_500 Internal analytics service error
```

---

## Local Setup

Prerequisites:

```text
Java 17
Maven
Docker optional
PostgreSQL/MySQL depending on service database config
```

Clone repository:

```bash
git clone https://github.com/elvin-t/smart-library-platform.git
cd smart-library-platform
```

---

## Run Order

Recommended run order:

```text
1. discovery-server
2. api-gateway
3. auth-service
4. user-service
5. book-service
6. borrow-service
7. notification-service
8. analytics-service
```

If using Eureka, start discovery-server first.

---

## Maven Build Commands

Build all modules:

```bash
mvn clean install -DskipTests
```

Build one module with dependencies:

```bash
mvn clean package -pl analytics-service -am -DskipTests
```

Run one module:

```bash
mvn spring-boot:run -pl analytics-service
```

---

## Dockerization

Every backend service can have its own Dockerfile.

Docker Compose can run:

```text
PostgreSQL databases
Service Discovery
API Gateway
Auth Service
User Service
Book Service
Borrow Service
Notification Service
Analytics Service
```

Local run:

```bash
docker compose up --build -d
```

Useful commands:

```bash
docker compose ps
docker compose logs -f
docker compose down
```

---

## CI/CD Pipeline

Recommended CI/CD flow:

```text
GitHub
  ↓
Build with Maven
  ↓
Run tests
  ↓
Build Docker image
  ↓
Push image to AWS ECR
  ↓
Deploy to AWS ECS Fargate
```

Recommended GitHub Actions or Jenkins steps:

```text
Checkout source code
Setup Java 17
Run Maven build and tests
Authenticate to AWS using OIDC or credentials
Login to Amazon ECR
Build Docker image
Push Docker image to ECR
Render ECS task definition
Deploy ECS service
```

---

## AWS Deployment Readiness

The backend is designed to deploy on:

```text
Amazon ECR
Amazon ECS Fargate
Application Load Balancer
Amazon RDS PostgreSQL
AWS Secrets Manager
CloudWatch Logs
```

Production secrets should be stored in:

```text
AWS Secrets Manager
```

Examples:

```text
Database password
JWT secret
Internal service token
```

Angular frontend can be deployed separately using:

```text
S3 + CloudFront + Route 53
```

Angular frontend does not need Docker or Nginx when hosted through S3 and CloudFront.

---

## API Demo Guide

### Recommended Demo Order

#### 1. Show Architecture

Explain:

```text
Gateway → Services → Databases
```

Message:

> Each service has a separate responsibility and database.

#### 2. Show Eureka Dashboard

Open:

```text
http://localhost:8761
```

Show registered services:

```text
AUTH-SERVICE
USER-SERVICE
BOOK-SERVICE
BORROW-SERVICE
NOTIFICATION-SERVICE
ANALYTICS-SERVICE
API-GATEWAY
```

#### 3. Login Demo

```http
POST http://localhost:8080/api/auth/login
```

Show:

```text
JWT token
roles
permissions
```

#### 4. Dashboard Summary Demo

```http
GET http://localhost:8080/api/dashboard/summary
Authorization: Bearer <TOKEN>
```

Explain:

> Angular dashboard now calls one backend summary API instead of multiple APIs.

#### 5. Book API Demo

```http
GET http://localhost:8080/api/books/1
Authorization: Bearer <TOKEN>
```

Explain:

> Gateway validates token and permissions before forwarding to Book Service.

#### 6. Borrow Book Demo

```http
POST http://localhost:8080/api/borrow-records
Authorization: Bearer <TOKEN>
```

Body:

```json
{
  "userId": 1,
  "bookId": 1
}
```

Show:

```text
Borrow record created
Book available copies reduced
Notification created
Trace ID generated
```

#### 7. Return Book Demo

```http
PATCH http://localhost:8080/api/borrow-records/1/return
Authorization: Bearer <TOKEN>
```

Show:

```text
Book returned
Fine calculated if overdue
Notification sent
```

#### 8. Notification Read/Unread Demo

```http
PATCH http://localhost:8080/api/notifications/1/read
Authorization: Bearer <TOKEN>
```

#### 9. Show Trace ID

Show same trace ID in:

```text
API Gateway logs
Borrow Service logs
Book Service logs
Notification Service logs
Analytics Service logs
```

#### 10. Show Docker

```bash
docker compose ps
```

---

## Manager Demo Script

```text
This is my Smart Library Platform, implemented using microservices architecture.

I separated the system into Auth, User, Book, Borrow, Notification, Analytics, API Gateway, and Service Discovery services.

The Auth Service manages authentication, roles, permissions, user login status, and JWT generation.
The User Service manages user profile and membership data.
The Book Service manages book catalog and inventory.
The Borrow Service manages borrow and return workflows and fine calculation.
The Notification Service handles borrow, return, due date, and overdue notifications.
The Analytics Service provides a centralized dashboard summary API by aggregating data from other services.
The API Gateway is the single entry point and validates JWT, route permissions, CORS, and trace ID.
Eureka is used for service discovery.

Each domain service owns its database and uses Flyway for schema migration.
Security is implemented using JWT and RBAC permissions.
For production readiness, I added Resilience4j patterns, trace ID logging, Dockerization, and CI/CD readiness for AWS ECS Fargate deployment.
```

---

## Questions to Discuss with Manager

### Architecture Feedback

```text
Is this microservices separation aligned with enterprise standards?
Should any service be merged or separated differently?
Do our projects follow gateway-level security plus service-level security?
Should analytics-service remain stateless or later use a read model database?
```

### Security Feedback

```text
Is this JWT and RBAC approach suitable for enterprise-grade applications?
Should OAuth2/OIDC integration be added in the next phase?
Should internal APIs use mTLS or service-to-service tokens?
Should DASHBOARD_READ be a separate permission or covered under BOOK_READ?
```

### Cloud and DevOps Feedback

```text
For AWS deployment, should ECS Fargate or EKS be preferred?
Should AWS Secrets Manager be used for JWT and DB credentials?
Should Terraform scripts be created for infrastructure provisioning?
Should each service have independent pipelines?
```

### Observability Feedback

```text
Should centralized logging be integrated with CloudWatch?
Should distributed tracing using OpenTelemetry be added?
Should dashboards be created for service health and latency?
```

### Next Feature Feedback

```text
Should Kafka-based notification be implemented next?
Should audit logging be added?
Should role management APIs be added?
Should analytics reporting APIs be expanded?
```

---

## Possible Manager Questions and Answers

### Why did you use microservices?

```text
I used microservices to separate responsibilities, improve scalability, make services independently deployable, and follow enterprise-style architecture.
```

### Why does every service have its own database?

```text
This follows database-per-service architecture. Each service owns its data and avoids tight coupling between services.
```

### Why does analytics-service not have a database?

```text
Analytics-service currently acts as a stateless aggregation service. It does not own business data. It calls domain services and prepares dashboard summary values. If historical reports or trends are needed later, it can be extended with Kafka events and its own read-optimized database.
```

### Why validate JWT in both Gateway and services?

```text
Gateway validation blocks unauthorized requests early. Service-level validation provides defense-in-depth and prevents direct service access from bypassing security.
```

### Why use API Gateway?

```text
API Gateway gives a single entry point for clients and centralizes routing, CORS, JWT validation, trace ID handling, and future rate limiting.
```

### Why use Eureka?

```text
Eureka helps services register and discover each other dynamically instead of hardcoding service locations.
```

### Why use Resilience4j?

```text
Resilience4j protects services from downstream failures using circuit breaker, retry, timeout, and fallback mechanisms.
```

### Why keep inventory inside Book Service?

```text
Inventory is currently simple and directly tied to book copies. Since Book Service already manages totalCopies and availableCopies, keeping inventory inside Book Service avoids unnecessary service complexity.
```

### Why use Notification Service?

```text
Notification Service keeps notification logic separate from Borrow Service. Currently it uses REST, and later it can be moved to Kafka-based asynchronous communication.
```

### Why use Flyway?

```text
Flyway keeps database changes version-controlled and repeatable across environments.
```

### Is this ready for production?

```text
The architecture follows production-grade patterns like API Gateway, service discovery, JWT security, RBAC, database-per-service, resilience, trace ID logging, Dockerization, and CI/CD readiness.

For full production, the next steps are AWS infrastructure provisioning, Secrets Manager integration, monitoring, Kafka/event-driven communication, and load testing.
```

---

## Completed Milestones

```text
✅ Auth Service
✅ User Service
✅ Book Service
✅ Borrow Service
✅ Notification Service
✅ Analytics Service
✅ API Gateway
✅ Service Discovery
✅ JWT Security
✅ RBAC Permissions
✅ DASHBOARD_READ permission
✅ Admin user create/update/activate/deactivate flow
✅ Public member self-registration
✅ Flyway Migrations
✅ Inventory Management
✅ Fine Calculation
✅ REST Notification
✅ Notification read/unread
✅ Dashboard Summary API
✅ Resilience4j readiness
✅ Centralized Trace ID Logging
✅ Dockerization readiness
✅ CI/CD Pipeline Plan
✅ AWS ECS Deployment Plan
```

---

## Future Roadmap

```text
1. Kafka/Event-Driven Notification
2. Audit Logging Service
3. Admin Role and Permission Management APIs
4. OpenTelemetry Distributed Tracing
5. CloudWatch Logging and Metrics
6. Terraform Infrastructure Provisioning
7. AWS ECS Fargate Deployment
8. Redis caching for Dashboard Summary
9. Analytics reporting and export APIs
10. Monitoring dashboards
11. Load testing
12. Contract testing between services
13. API versioning
14. Swagger aggregation at API Gateway
```

---

## Suggested GitHub Repository Description

```text
Production-style Library Management Platform built with Java 17, Spring Boot, Spring Cloud Gateway, Eureka, JWT, RBAC, PostgreSQL, Flyway, OpenFeign, Resilience4j, Docker, and AWS ECS-ready CI/CD.
```

---

## Final Summary

Smart Library Platform demonstrates a complete enterprise-style microservices system.

It includes secure authentication, role-based authorization, API Gateway routing, service discovery, independent databases, Flyway migrations, book inventory, borrow and return workflow, fine calculation, notification handling, analytics dashboard aggregation, service resilience, centralized trace logging, Dockerization, and CI/CD readiness for AWS deployment.

The latest backend includes the new `analytics-service`, which exposes:

```http
GET /api/dashboard/summary
```

This API improves frontend dashboard performance by moving aggregation logic from Angular into the backend.

The project is suitable for:

```text
Local development
Manager demo
Interview discussion
Portfolio showcase
Cloud deployment extension
Production-grade architecture practice
```
