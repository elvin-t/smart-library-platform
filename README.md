# Smart Library Platform Backend

Smart Library Platform Backend is a Spring Boot microservices-based backend application for managing authentication, users, books, inventory, borrow records, returns, overdue fines, and notifications.

The backend follows a production-style microservices architecture using Spring Cloud Gateway, Eureka Service Discovery, PostgreSQL, Apache Kafka, JWT authentication, role-based access control, and Flyway migrations.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Backend Services](#backend-services)
- [Prerequisites](#prerequisites)
- [Recommended Startup Order](#recommended-startup-order)
- [PostgreSQL Setup](#postgresql-setup)
- [Kafka Setup on Windows](#kafka-setup-on-windows)
- [Kafka Topics](#kafka-topics)
- [Service Ports](#service-ports)
- [Discovery Server Setup](#discovery-server-setup)
- [API Gateway Setup](#api-gateway-setup)
- [Auth Service Setup](#auth-service-setup)
- [User Service Setup](#user-service-setup)
- [Book Service Setup](#book-service-setup)
- [Borrow Service Setup](#borrow-service-setup)
- [Notification Service Setup](#notification-service-setup)
- [Kafka Event Flow](#kafka-event-flow)
- [Run Services](#run-services)
- [Verify Services](#verify-services)
- [Test APIs](#test-apis)
- [Common Issues and Fixes](#common-issues-and-fixes)
- [Final Checklist](#final-checklist)

---

## Overview

The Smart Library Platform backend is designed to support a full library management workflow.

It supports:

- Admin, Librarian, and Member roles
- JWT authentication
- Permission-based authorization
- Book catalog management
- Inventory management
- Borrow and return workflows
- Overdue fine calculation
- Fine payment workflow
- Kafka-based asynchronous notifications
- API Gateway routing
- Eureka service registration and discovery

---

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Cloud Gateway
- Netflix Eureka Service Discovery
- Spring Security
- JWT Authentication
- Role-Based Access Control
- PostgreSQL
- Flyway Migration
- Apache Kafka
- Spring Kafka
- Maven
- Lombok
- OpenAPI/Swagger

---

## Backend Services

The backend contains the following services:

```text
discovery-server
api-gateway
auth-service
user-service
book-service
borrow-service
notification-service
```

---

## Prerequisites

Install the following tools before running the backend:

```text
Java 17+
Maven 3.8+
PostgreSQL
Apache Kafka
Git
Postman
IntelliJ IDEA / VS Code
```

Check versions:

```bash
java -version
mvn -version
git --version
```

---

## Recommended Startup Order

Start the backend components in this order:

```text
1. PostgreSQL
2. Kafka
3. discovery-server
4. api-gateway
5. auth-service
6. user-service
7. book-service
8. borrow-service
9. notification-service
```

Kafka must be running before starting `borrow-service` and `notification-service`.

Eureka Discovery Server must be running before starting API Gateway and other microservices.

---

## PostgreSQL Setup

Create separate databases for each service.

```sql
CREATE DATABASE smart_library_auth_db;
CREATE DATABASE smart_library_user_db;
CREATE DATABASE smart_library_book_db;
CREATE DATABASE smart_library_borrow_db;
CREATE DATABASE smart_library_notification_db;
```

Recommended local credentials:

```text
username: postgres
password: postgres
```

Update each service `application.yml` file with your PostgreSQL credentials.

Example:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_library_auth_db
    username: postgres
    password: postgres
```

---

## Kafka Setup on Windows

Kafka is used for asynchronous event-driven communication between `borrow-service` and `notification-service`.

Kafka local installation path example:

```text
C:\kafka
```

### Start Kafka

Open PowerShell:

```powershell
cd C:\kafka
bin\windows\kafka-server-start.bat config\server.properties
```

Keep this terminal open.

### Verify Kafka Port

Open a new PowerShell terminal:

```powershell
netstat -ano | findstr 9092
```

Expected output:

```text
TCP    0.0.0.0:9092    ...    LISTENING
```

---

## Kafka Topics

Kafka topics used by the backend:

```text
book-borrowed-events
book-returned-events
fine-paid-events
```

Create topics:

```powershell
cd C:\kafka
```

```powershell
bin\windows\kafka-topics.bat --create --topic book-borrowed-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

```powershell
bin\windows\kafka-topics.bat --create --topic book-returned-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

```powershell
bin\windows\kafka-topics.bat --create --topic fine-paid-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

Verify topics:

```powershell
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
```

Expected output:

```text
book-borrowed-events
book-returned-events
fine-paid-events
```

---

## Service Ports

Recommended local ports:

```text
discovery-server       : 8761
api-gateway            : 8080
auth-service           : 8081
user-service           : 8082
book-service           : 8083
borrow-service         : 8084
notification-service   : 8085
```

---

## Discovery Server Setup

### `discovery-server/application.yml`

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### Run Discovery Server

```bash
cd discovery-server
mvn spring-boot:run
```

Eureka dashboard:

```text
http://localhost:8761
```

---

## API Gateway Setup

### `api-gateway/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway

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
            - Path=/api/users/**

        - id: book-service
          uri: lb://book-service
          predicates:
            - Path=/api/books/**,/api/inventory/**

        - id: borrow-service
          uri: lb://borrow-service
          predicates:
            - Path=/api/borrow-records/**,/api/fines/**

        - id: notification-service
          uri: lb://notification-service
          predicates:
            - Path=/api/notifications/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Run API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

Gateway base URL:

```text
http://localhost:8080
```

---

## Auth Service Setup

### `auth-service/application.yml`

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service

  datasource:
    url: jdbc:postgresql://localhost:5432/smart_library_auth_db
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

  flyway:
    enabled: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: your-secure-secret-key
  expiration: 86400000
```

### Run Auth Service

```bash
cd auth-service
mvn spring-boot:run
```

Auth APIs:

```text
POST http://localhost:8080/api/auth/login
POST http://localhost:8080/api/auth/register
```

---

## User Service Setup

### `user-service/application.yml`

```yaml
server:
  port: 8082

spring:
  application:
    name: user-service

  datasource:
    url: jdbc:postgresql://localhost:5432/smart_library_user_db
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

  flyway:
    enabled: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Run User Service

```bash
cd user-service
mvn spring-boot:run
```

---

## Book Service Setup

### `book-service/application.yml`

```yaml
server:
  port: 8083

spring:
  application:
    name: book-service

  datasource:
    url: jdbc:postgresql://localhost:5432/smart_library_book_db
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

  flyway:
    enabled: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Run Book Service

```bash
cd book-service
mvn spring-boot:run
```

Book APIs:

```text
GET  http://localhost:8080/api/books
POST http://localhost:8080/api/books
GET  http://localhost:8080/api/inventory
```

---

## Borrow Service Setup

### `borrow-service/application.yml`

```yaml
server:
  port: 8084

spring:
  application:
    name: borrow-service

  datasource:
    url: jdbc:postgresql://localhost:5432/smart_library_borrow_db
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

  flyway:
    enabled: true

  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: false

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

app:
  borrow:
    default-borrow-days: 14
    fine-per-day: 10.00

  kafka:
    topics:
      book-borrowed: book-borrowed-events
      book-returned: book-returned-events
      fine-paid: fine-paid-events
```

### Run Borrow Service

```bash
cd borrow-service
mvn spring-boot:run
```

Borrow APIs:

```text
POST  http://localhost:8080/api/borrow-records
GET   http://localhost:8080/api/borrow-records
GET   http://localhost:8080/api/borrow-records/{id}
PATCH http://localhost:8080/api/borrow-records/{id}/return
GET   http://localhost:8080/api/borrow-records/{id}/fine
PATCH http://localhost:8080/api/borrow-records/{id}/fine/pay
```

---

## Notification Service Setup

### `notification-service/application.yml`

```yaml
server:
  port: 8085

spring:
  application:
    name: notification-service

  datasource:
    url: jdbc:postgresql://localhost:5432/smart_library_notification_db
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

  flyway:
    enabled: true

  kafka:
    bootstrap-servers: localhost:9092

    consumer:
      group-id: notification-service
      auto-offset-reset: earliest
      enable-auto-commit: false

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: false

    listener:
      ack-mode: record

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

app:
  kafka:
    topics:
      book-borrowed: book-borrowed-events
      book-returned: book-returned-events
      fine-paid: fine-paid-events
```

### Run Notification Service

```bash
cd notification-service
mvn spring-boot:run
```

Notification APIs:

```text
GET   http://localhost:8080/api/notifications
GET   http://localhost:8080/api/notifications/user/{userId}
PATCH http://localhost:8080/api/notifications/{id}/read
PATCH http://localhost:8080/api/notifications/user/{userId}/read-all
```

---

## Kafka Event Flow

Kafka is used for asynchronous communication between `borrow-service` and `notification-service`.

### Borrow Book Event Flow

```text
Client
  ↓
API Gateway
  ↓
Eureka Service Discovery
  ↓
Borrow Service
  ↓
Borrow record saved
  ↓
BookBorrowedEvent published to Kafka
  ↓
Topic: book-borrowed-events
  ↓
Notification Service consumes event
  ↓
Notification record created
```

### Return Book Event Flow

```text
Borrow Service returns book
  ↓
Fine calculated if overdue
  ↓
BookReturnedEvent published to Kafka
  ↓
Notification Service consumes event
  ↓
Return notification created
```

### Fine Paid Event Flow

```text
Admin/Librarian marks fine as paid
  ↓
FinePaidEvent published to Kafka
  ↓
Notification Service consumes event
  ↓
Fine payment notification created
```

Kafka topics:

```text
book-borrowed-events
book-returned-events
fine-paid-events
```

---

## Run Services

Run each service from its own terminal.

Example:

```bash
cd discovery-server
mvn spring-boot:run
```

```bash
cd api-gateway
mvn spring-boot:run
```

```bash
cd auth-service
mvn spring-boot:run
```

```bash
cd user-service
mvn spring-boot:run
```

```bash
cd book-service
mvn spring-boot:run
```

```bash
cd borrow-service
mvn spring-boot:run
```

```bash
cd notification-service
mvn spring-boot:run
```

---

## Verify Services

Open Eureka dashboard:

```text
http://localhost:8761
```

Expected registered services:

```text
API-GATEWAY
AUTH-SERVICE
USER-SERVICE
BOOK-SERVICE
BORROW-SERVICE
NOTIFICATION-SERVICE
```

---

## Test APIs

### Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "password"
}
```

Copy the JWT token from the response.

Use the token in secured APIs:

```http
Authorization: Bearer <TOKEN>
```

---

### Borrow Book

```http
POST http://localhost:8080/api/borrow-records
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "userId": 3,
  "bookId": 10
}
```

Expected result:

```text
Borrow record created
Book inventory reduced
BookBorrowedEvent published to Kafka
Notification Service consumes event
Notification created
```

---

### Get Notifications

```http
GET http://localhost:8080/api/notifications/user/3
Authorization: Bearer <TOKEN>
```

Expected result:

```text
Borrow notification should be returned
```

---

### Return Book

```http
PATCH http://localhost:8080/api/borrow-records/{borrowRecordId}/return
Authorization: Bearer <TOKEN>
```

Expected result:

```text
Borrow record status changes to RETURNED
Inventory copy increases
Fine is calculated if overdue
BookReturnedEvent published
Notification created
```

---

### Get Fine Details

```http
GET http://localhost:8080/api/borrow-records/{borrowRecordId}/fine
Authorization: Bearer <TOKEN>
```

Example response:

```json
{
  "borrowRecordId": 1,
  "overdueDays": 1,
  "finePerDay": 10.00,
  "fineAmount": 10.00,
  "finePaid": false
}
```

---

### Mark Fine as Paid

```http
PATCH http://localhost:8080/api/borrow-records/{borrowRecordId}/fine/pay
Authorization: Bearer <TOKEN>
```

Expected result:

```text
finePaid = true
finePaidAt updated
FinePaidEvent published
Notification created
```

---

## Common Issues and Fixes

### Kafka Connection Error

Error:

```text
Connection to node -1 localhost:9092 could not be established
```

Fix:

```powershell
cd C:\kafka
bin\windows\kafka-server-start.bat config\server.properties
```

Check port:

```powershell
netstat -ano | findstr 9092
```

---

### Kafka Storage Error on Windows

Error:

```text
KafkaStorageException
AccessDeniedException
```

Fix:

1. Stop Kafka.
2. Stop Spring Boot services.
3. Delete Kafka log directory.
4. Reformat Kafka storage.
5. Restart Kafka.

Recommended Kafka log path in `server.properties`:

```properties
log.dirs=C:/kafka/data/kraft-combined-logs
```

---

### Eureka Service Not Visible

Check service config:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

Make sure `discovery-server` is running before other services.

---

### Flyway Migration Error

Do not edit old Flyway migration files after they already ran.

Create a new migration instead:

```text
V12__add_user_read_permission_to_member.sql
```

---

### Unauthorized Error

Make sure requests include:

```http
Authorization: Bearer <TOKEN>
```

---

## Final Checklist

Before starting Angular UI, verify:

```text
PostgreSQL is running
Kafka is running on localhost:9092
Kafka topics are created
Eureka dashboard shows all services
API Gateway is running on port 8080
Auth login works
Borrow Service can publish Kafka events
Notification Service can consume Kafka events
Notification APIs return data
```

Angular environment should point to API Gateway:

```ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080'
};
```

---

## Project Status

```text
Status: Active Development
Architecture: Spring Boot Microservices
Communication: REST + Kafka Event-Driven Flow
Security: JWT + RBAC + Permissions
Database: PostgreSQL + Flyway
```
