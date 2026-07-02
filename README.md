# Smart Library Platform

> Production-style **microservices-based Library Management System** built with Java 17, Spring Boot, Spring Cloud, JWT, RBAC, PostgreSQL, Flyway, OpenFeign, Resilience4j, Docker, and AWS-ready CI/CD.

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
- [Dockerization](#dockerization)
- [CI/CD Pipeline](#cicd-pipeline)
- [AWS Deployment Readiness](#aws-deployment-readiness)
- [API Demo Guide](#api-demo-guide)
- [Manager Demo Script](#manager-demo-script)
- [Questions to Discuss with Manager](#questions-to-discuss-with-manager)
- [Completed Milestones](#completed-milestones)
- [Future Roadmap](#future-roadmap)

---

## Overview

**Smart Library Platform** is a production-style backend platform for managing library operations using a microservices architecture.

The platform includes authentication, user profile management, book catalog management, inventory management, borrow/return workflows, fine calculation, notification handling, API Gateway security, service discovery, trace logging, resilience, Dockerization, and CI/CD readiness.

### One-Line Summary

> Smart Library Platform is a secure, scalable, and modular microservices-based Library Management System with JWT authentication, RBAC authorization, service discovery, API Gateway, distributed tracing, resilience patterns, Dockerization, and AWS ECS deployment readiness.

---

## Business Problem

Traditional library systems are often monolithic and difficult to scale, maintain, and deploy independently.

This project solves that problem by splitting business capabilities into independent services:

```text
Authentication and authorization
User profile management
Book catalog management
Inventory management
Borrow and return workflow
Fine calculation
Notification handling
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
        +-------------------+------------------+------------------+
        |                   |                  |                  |
        v                   v                  v                  v
+---------------+   +---------------+   +---------------+   +----------------+
| Auth Service  |   | User Service  |   | Book Service  |   | Borrow Service |
| 8081          |   | 8082          |   | 8083          |   | 8084           |
+---------------+   +---------------+   +---------------+   +----------------+
        |                   |                  |                  |
        v                   v                  v                  v
   Auth DB              User DB            Book DB           Borrow DB
                                                              |
                                                              v
                                                     +----------------------+
                                                     | Notification Service |
                                                     | 8085                 |
                                                     +----------------------+
                                                              |
                                                              v
                                                     Notification DB
```

---

## Implemented Microservices

### 1. Auth Service

**Responsibility**

```text
Authentication
Authorization
JWT generation
Role management
Permission management
User registration
Login
```

**Key Features**

```text
Register user
Login user
Validate password using BCrypt
Generate JWT token
Return roles and permissions
Assign MEMBER or LIBRARIAN roles
Call User Service during registration
```

**Main APIs**

```text
POST /api/auth/register
POST /api/auth/login
```

**Why Auth Service?**

Auth Service centralizes authentication, role-permission management, and JWT generation. Other services do not store passwords or roles.

---

### 2. User Service

**Responsibility**

```text
User profile
Membership details
User contact information
```

**Key Features**

```text
Create user profile from Auth Service
Get user by ID
Get all users
Update profile
Update membership status
```

**Main APIs**

```text
POST  /api/internal/users
GET   /api/users/{id}
GET   /api/users
PUT   /api/users/{id}
PATCH /api/users/{id}/status
```

**Why User Service?**

Auth Service manages credentials and roles, while User Service manages profile and membership information. This follows separation of concerns.

---

### 3. Book Service

**Responsibility**

```text
Book catalog
Book search
Book availability
Inventory logic
```

**Key Features**

```text
Create book
Update book
Delete book
Search books
Get available books
Manage inventory
Add copies
Remove copies
Adjust available copies
```

**Main APIs**

```text
POST   /api/books
GET    /api/books
GET    /api/books/{id}
GET    /api/books/search
PUT    /api/books/{id}
DELETE /api/books/{id}

GET    /api/books/inventory/{bookId}
PATCH  /api/books/inventory/{bookId}/add-copies
PATCH  /api/books/inventory/{bookId}/remove-copies
PATCH  /api/books/inventory/{bookId}/available-copies
GET    /api/books/inventory/low-stock
```

**Inventory Decision**

Inventory is currently kept inside Book Service because inventory logic is directly related to book copies:

```text
totalCopies
availableCopies
```

A separate Inventory Service can be introduced later if inventory becomes more complex.

---

### 4. Borrow Service

**Responsibility**

```text
Borrow book
Return book
Borrow history
Due date tracking
Fine calculation
```

**Key Features**

```text
Borrow a book
Return a book
Calculate overdue fine
Mark fine as paid
Get borrow records
Get borrow records by user
Communicate with Book Service
Send notifications after borrow/return
```

**Main APIs**

```text
POST  /api/borrow-records
PATCH /api/borrow-records/{id}/return
GET   /api/borrow-records
GET   /api/borrow-records/{id}
GET   /api/borrow-records/user/{userId}
GET   /api/borrow-records/{id}/fine
PATCH /api/borrow-records/{id}/fine/pay
```

**Fine Calculation**

```text
fine = overdueDays * finePerDay
```

Example:

```text
overdueDays = 3
finePerDay = 10
fineAmount = 30
```

---

### 5. Notification Service

**Responsibility**

```text
User and library notifications
```

**Key Features**

```text
Borrow confirmation
Return confirmation
Due date reminder
Overdue reminder
Notification record storage
Internal REST API
```

**Current Flow**

```text
Borrow Service → Notification Service using REST
```

**Future Flow**

```text
Borrow Service → Kafka → Notification Service
```

**Main API**

```text
POST /api/internal/notifications
```

---

### 6. API Gateway

**Responsibility**

```text
Single entry point
Route requests
Validate JWT
Route-level authorization
Forward token
Forward user headers
Centralized CORS
Trace ID generation
```

**Gateway Port**

```text
8080
```

**Routes**

```text
/api/auth/**           → Auth Service
/api/users/**          → User Service
/api/books/**          → Book Service
/api/borrow-records/** → Borrow Service
```

**Why API Gateway?**

API Gateway provides a single entry point for client applications and avoids exposing every microservice directly.

---

### 7. Service Discovery

**Responsibility**

```text
Register services
Discover services dynamically
Support load-balanced routing
```

**Technology**

```text
Netflix Eureka
```

**URL**

```text
http://localhost:8761
```

---

## Technology Stack

```text
Java 17
Spring Boot 3.x
Spring Cloud Gateway
Spring Cloud OpenFeign
Spring Cloud Netflix Eureka
Spring Security
JWT
RBAC
PostgreSQL
Flyway
Resilience4j
Docker
Docker Compose
GitHub Actions
AWS ECR
AWS ECS Fargate
AWS RDS PostgreSQL
AWS Secrets Manager
AWS CloudWatch
```

---

## Security Design

### Authentication

User login is handled by Auth Service.

```text
POST /api/auth/login
```

Auth Service validates:

```text
email
password
BCrypt password hash
```

Then Auth Service returns:

```text
JWT token
roles
permissions
```

---

### Authorization

Authorization is permission-based using RBAC.

Example permissions:

```text
BOOK_READ
BOOK_WRITE
BORROW_READ
BORROW_WRITE
RETURN_READ
RETURN_WRITE
INVENTORY_READ
INVENTORY_WRITE
USER_READ
USER_WRITE
```

---

### Defense-in-Depth Security

The platform validates JWT at both Gateway and service level.

```text
API Gateway = first security layer
Microservice = final security layer
```

This prevents direct service access from bypassing Gateway security.

---

## Role-Based Access Control

### MEMBER

```text
BOOK_READ
BORROW_WRITE
BORROW_READ
RETURN_WRITE
RETURN_READ
```

### LIBRARIAN

```text
BOOK_READ
BOOK_WRITE
BORROW_WRITE
BORROW_READ
RETURN_WRITE
RETURN_READ
INVENTORY_READ
INVENTORY_WRITE
USER_READ
```

### ADMIN

```text
All permissions
```

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

Example request:

```json
{
  "email": "member1@library.com",
  "password": "member123",
  "fullName": "Member One",
  "phone": "9876543210",
  "role": "MEMBER"
}
```

---

### Login Flow

```text
Client
  ↓
API Gateway
  ↓
Auth Service
  ↓
Validate password
  ↓
Generate JWT
  ↓
Return token, roles, permissions
```

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "roles": ["MEMBER"],
  "permissions": [
    "BOOK_READ",
    "BORROW_WRITE",
    "BORROW_READ",
    "RETURN_WRITE",
    "RETURN_READ"
  ]
}
```

---

### Borrow Book Flow

```text
Client
  ↓
API Gateway
  ↓
Borrow Service
  ↓
Check existing borrow record
  ↓
Call Book Service to check availability
  ↓
Call Book Service to reduce available copies
  ↓
Save borrow record
  ↓
Call Notification Service
```

Example request:

```json
{
  "userId": 1,
  "bookId": 1
}
```

---

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
Call Book Service to increase available copies
  ↓
Calculate fine if overdue
  ↓
Update borrow record as RETURNED
  ↓
Call Notification Service
```

---

## Database Design

Each service owns its own database.

```text
auth_service_db
user_service_db
book_service_db
borrow_service_db
notification_service_db
```

This follows the database-per-service pattern.

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
```

---

## Resilience and Fault Tolerance

### OpenFeign Communication

Services communicate using OpenFeign:

```text
Auth Service → User Service
Borrow Service → Book Service
Borrow Service → Notification Service
```

### Resilience4j

Implemented for:

```text
Borrow Service → Book Service
Auth Service → User Service
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
```

Mutation operations are not retried blindly:

```text
borrow copy
return copy
```

Reason: retrying mutation APIs can create duplicate inventory updates.

### Compensation Logic

If Borrow Service decreases book inventory but fails to save borrow record, Borrow Service attempts compensation:

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
API Gateway generates X-Trace-Id
↓
Downstream services receive the same X-Trace-Id
↓
Each service logs traceId using MDC
```

Example:

```text
traceId=64c4c0c8-8f6f-4c5d-a41c-f2fbdcc12345
```

This trace ID appears in logs of:

```text
API Gateway
Borrow Service
Book Service
Notification Service
```

This makes debugging distributed requests easier.

---

## Dockerization

Every service has its own `Dockerfile`.

Docker Compose runs:

```text
PostgreSQL
Service Discovery
API Gateway
Auth Service
User Service
Book Service
Borrow Service
Notification Service
```

Local run command:

```bash
docker compose up --build -d
```

Useful Docker commands:

```bash
docker compose ps
docker compose logs -f
docker compose down
```

Benefits:

```text
Consistent local environment
Easy onboarding
Production-like setup
Container-ready deployment
```

---

## CI/CD Pipeline

The planned CI/CD flow is:

```text
GitHub
  ↓
Build with Maven
  ↓
Run tests
  ↓
Build Docker image
  ↓
Push to AWS ECR
  ↓
Deploy to AWS ECS Fargate
```

Recommended GitHub Actions workflow:

```text
Checkout source code
Setup Java 17
Run Maven build and tests
Authenticate to AWS using OIDC
Login to Amazon ECR
Build Docker image
Push Docker image to ECR
Render ECS task definition
Deploy ECS service
```

---

## AWS Deployment Readiness

The project is designed to deploy on:

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

---

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
API-GATEWAY
```

---

#### 3. Login Demo

Call:

```http
POST http://localhost:8080/api/auth/login
```

Show response:

```text
JWT token
roles
permissions
```

---

#### 4. Book API Demo

Call:

```http
GET http://localhost:8080/api/books/1
```

Explain:

> Gateway validates token first, then Book Service validates again.

---

#### 5. Borrow Book Demo

Call:

```http
POST http://localhost:8080/api/borrow-records
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

---

#### 6. Return Book Demo

Call:

```http
PATCH http://localhost:8080/api/borrow-records/1/return
```

Show:

```text
Book returned
Fine calculated if overdue
Notification sent
```

---

#### 7. Show Trace ID

Show the same trace ID in:

```text
API Gateway logs
Borrow Service logs
Book Service logs
Notification Service logs
```

---

#### 8. Show Docker

Run:

```bash
docker compose ps
```

Explain:

> All services can run as containers locally.

---

#### 9. Show GitHub Repository

Show:

```text
Service folders
Dockerfiles
docker-compose.yml
Flyway migrations
GitHub workflow
```

---

## Manager Demo Script

```text
This is my Smart Library Platform, implemented using microservices architecture.

I have separated the system into Auth, User, Book, Borrow, Notification, API Gateway, and Service Discovery services.

The Auth Service manages authentication, roles, permissions, and JWT generation.
The User Service manages profile and membership data.
The Book Service manages catalog and inventory.
The Borrow Service manages borrow/return workflows and fine calculation.
The Notification Service handles borrow and return notifications.
The API Gateway is the single entry point and validates JWT, route permissions, and CORS.
Eureka is used for service discovery.

Each service owns its own database and uses Flyway for schema migration.
Security is implemented using JWT and RBAC.
For better production readiness, I added Resilience4j for circuit breaker, retry, timeout, and fallback.
I also added trace ID logging to track one request across multiple services.
Finally, I dockerized the services and prepared the CI/CD pipeline for AWS ECS Fargate deployment.
```

---

## Questions to Discuss with Manager

### Architecture Feedback

```text
Is this microservices separation aligned with our enterprise standards?
Should any service be merged or separated differently?
Do our projects follow gateway-level security plus service-level security?
```

### Security Feedback

```text
Is this JWT and RBAC approach suitable for enterprise-grade applications?
Should OAuth2/OIDC integration be added in the next phase?
Should internal APIs use mTLS or service-to-service tokens?
```

### Cloud and DevOps Feedback

```text
For AWS deployment, should ECS Fargate or EKS be preferred?
Should AWS Secrets Manager be used for JWT and DB credentials?
Should Terraform scripts be created for infrastructure provisioning?
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
Should admin dashboard APIs be implemented?
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

### Why validate JWT in both Gateway and services?

```text
Gateway validation blocks unauthorized requests early. Service-level validation provides defense-in-depth and prevents direct service access from bypassing security.
```

### Why use API Gateway?

```text
API Gateway gives a single entry point for clients and centralizes routing, CORS, JWT validation, and future rate limiting.
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
✅ API Gateway
✅ Service Discovery
✅ JWT Security
✅ RBAC Permissions
✅ Flyway Migrations
✅ Inventory Management
✅ Fine Calculation
✅ REST Notification
✅ Resilience4j
✅ Centralized Trace ID Logging
✅ Dockerization
✅ CI/CD Pipeline Plan
✅ AWS ECS Deployment Plan
```

---

## Future Roadmap

```text
1. Kafka/Event-Driven Notification
2. Audit Logging
3. Admin Role Management APIs
4. OpenTelemetry Distributed Tracing
5. CloudWatch Logging
6. Terraform Infrastructure
7. AWS ECS Fargate Deployment
8. Angular Frontend Integration
9. Monitoring Dashboard
10. Load Testing
```

---

## Suggested GitHub Repository Description

```text
Production-style Library Management Platform built with Java 17, Spring Boot, Spring Cloud Gateway, Eureka, JWT, RBAC, PostgreSQL, Flyway, OpenFeign, Resilience4j, Docker, and AWS ECS-ready CI/CD.
```

---

## Final Summary

Smart Library Platform demonstrates a complete enterprise-style microservices system. It includes secure authentication, role-based authorization, API Gateway routing, service discovery, independent databases, Flyway migrations, book inventory, borrow/return workflow, fine calculation, notification handling, service resilience, centralized trace logging, Dockerization, and CI/CD readiness for AWS deployment.

This project shows practical implementation of modern backend engineering patterns using Java, Spring Boot, Spring Cloud, PostgreSQL, Docker, and AWS-ready deployment practices.
