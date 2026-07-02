# Production-Ready AWS Architecture Document (Custom API Gateway + Kafka + PostgreSQL + GitHub + Jenkins + Docker)

## 1. Document Overview

This document defines a **production-ready end-to-end architecture** for a modern web application with the following required decisions:

- **Frontend:** Angular
- **Backend:** Java 17 + Spring Boot Microservices
- **API Gateway:** **Custom API Gateway** (not AWS API Gateway)
- **Messaging:** Kafka
- **Database:** PostgreSQL (PSQL)
- **Source Control:** GitHub
- **CI:** Jenkins
- **Build & Packaging:** Docker
- **Cloud Platform:** AWS
- **Container Runtime:** ECS Fargate (recommended) or EKS (optional)

This version explicitly avoids AWS API Gateway and instead uses a dedicated gateway service implemented in the application stack.

---

## 2. Final Architecture Decisions

### Confirmed Design Choices
- **Do not use AWS API Gateway**
- Use a **separate custom API Gateway service**
- Use **Kafka** for asynchronous communication
- Use **PostgreSQL** instead of MySQL
- Maintain all code in **GitHub**
- Use **Jenkins** for CI/CD orchestration
- Use **Docker** to build and package all deployable components

---

## 3. Recommended Production Architecture

## 3.1 Recommended Stack

### Frontend
- Angular 16+ / 17+
- TypeScript
- Angular Router
- RxJS
- Angular Material / PrimeNG
- Hosted on **Amazon S3 + CloudFront**

### Backend
- Java 17
- Spring Boot 3+
- Spring Data JPA
- Spring Security
- **Spring Cloud Gateway** as custom API Gateway
- OpenFeign / WebClient
- Resilience4j

### Data and Messaging
- **Amazon RDS PostgreSQL**
- **Amazon MSK (Managed Kafka)**
- Amazon ElastiCache Redis

### DevOps and Delivery
- GitHub (source control)
- Jenkins (CI/CD)
- Docker
- Amazon ECR
- Amazon ECS Fargate
- Terraform (recommended)

### Security and Operations
- Route 53
- CloudFront
- AWS WAF
- Application Load Balancer
- Secrets Manager
- IAM
- CloudWatch
- X-Ray / OpenTelemetry

---

## 4. Updated High-Level Architecture Diagram

```text
Users
  |
  v
Route53
  |
  v
CloudFront
  |
  +-----------------------------+
  |                             |
  v                             v
S3 (Angular App)           ALB (Public Entry)
                                 |
                                 v
                      Custom API Gateway Service
                      (Spring Cloud Gateway)
                                 |
      +--------------------------+---------------------------+
      |                          |                           |
      v                          v                           v
 Auth Service              User Service                Order Service
 Spring Boot               Spring Boot                 Spring Boot
      |                          |                           |
      +--------------+-----------+-------------+-------------+
                     |                         |
                     v                         v
              Redis / ElastiCache         Kafka / MSK
                     |
                     v
              PostgreSQL / RDS

Cross-cutting:
- GitHub
- Jenkins
- Docker
- ECR
- CloudWatch
- Secrets Manager
- WAF
```

---

## 5. Why a Custom API Gateway Instead of AWS API Gateway?

A **custom API Gateway** is recommended here because your backend is already based on Spring Boot microservices and you want full control over routing and backend behavior.

### Recommended Custom Gateway
Use **Spring Cloud Gateway** as a standalone microservice.

### Why Spring Cloud Gateway?
- Fits naturally into Java + Spring Boot architecture
- Easy path-based and predicate-based routing
- Centralized authentication and token validation
- Request/response filters
- Header enrichment
- Rate limiting support
- Circuit breaker integration
- Works well inside a microservices environment

### Responsibilities of the Custom API Gateway
- Route requests to internal services
- Validate JWT tokens (or delegate auth checks)
- Add correlation IDs
- Centralize logging and request tracing
- Apply rate limiting and IP restrictions where needed
- Transform headers / requests if required
- Hide internal service topology from clients

### Example Routing Rules
- `/auth/**` -> auth-service
- `/users/**` -> user-service
- `/catalog/**` -> catalog-service
- `/orders/**` -> order-service
- `/notifications/**` -> notification-service

---

## 6. AWS Production Architecture Layers

## 6.1 Edge Layer

### Route 53
Used for DNS routing:
- `app.company.com` -> CloudFront
- `api.company.com` -> ALB

### CloudFront
Used for:
- Angular application delivery
- Asset caching
- Global performance optimization
- TLS termination with ACM

### WAF
Used in front of CloudFront and/or ALB for:
- OWASP protections
- Rate limiting
- IP blocking
- Bot protection

---

## 6.2 Frontend Layer

### Angular Hosting Model
- Build Angular app
- Upload static build files to S3
- Serve with CloudFront
- Use environment-specific API base URL pointing to ALB/custom gateway

### Angular Best Practices
- Lazy loading modules
- Interceptors for JWT
- Route guards
- Environment-based configuration
- Error handling interceptor
- Retry only where safe

---

## 6.3 API Entry and Routing Layer

### Public Traffic Flow
```text
User -> Route53 -> CloudFront/ALB -> Custom API Gateway -> Internal Services
```

### External Entry Point
Use **Application Load Balancer (ALB)** as the public load balancer.

### Internal Routing
ALB forwards all API traffic to the **custom API Gateway service** running on ECS.
The gateway then routes traffic to internal services.

### Why This Design?
- Keeps one centralized public API entry
- Allows application-owned routing logic
- Prevents direct public exposure of all microservices
- Supports gateway-level authentication and filters

---

## 6.4 Compute Layer

### Recommended Runtime: ECS Fargate
Deploy all services as Docker containers on ECS Fargate.

### Services to Deploy
- api-gateway-service (Spring Cloud Gateway)
- auth-service
- user-service
- catalog-service
- order-service
- notification-service
- audit-service

### Why ECS Fargate?
- No server management
- Autoscaling support
- Easy AWS integration
- Good fit for containerized Spring Boot services

### Optional Alternative
Use EKS only if Kubernetes is a hard requirement.

---

## 7. Microservices Architecture

## 7.1 Suggested Services

### API Gateway Service
- Separate Spring Boot application using Spring Cloud Gateway
- Handles all external API entry
- Performs routing and cross-cutting concerns

### Auth Service
- Login / registration
- JWT generation and validation
- Refresh token handling
- Role / permission claims

### User Service
- Profile management
- User preferences
- Internal user master data

### Catalog Service
- Product / book / item data
- Search and filtering
- Availability data

### Order / Borrow Service
- Core business transactions
- Order creation / status tracking
- Publishes Kafka events

### Notification Service
- Kafka consumer
- Sends email / SMS / push notifications

### Audit Service
- Stores audit trail events
- Compliance and operational visibility

---

## 7.2 Recommended Service Structure

```text
src/main/java/com/company/service/
  controller/
  service/
  repository/
  entity/
  dto/
  mapper/
  config/
  security/
  exception/
  client/
  kafka/
  util/
```

---

## 8. PostgreSQL Architecture

## 8.1 Database Choice
Use **Amazon RDS PostgreSQL** instead of MySQL.

### Why PostgreSQL?
- Excellent ACID compliance
- Strong indexing and query optimization
- JSONB support for semi-structured data
- Better advanced SQL capabilities
- Strong ecosystem for enterprise applications

---

## 8.2 Production PostgreSQL Design

### Recommended Setup
- Amazon RDS PostgreSQL
- Multi-AZ enabled
- Automated backup enabled
- Encryption at rest using KMS
- Performance Insights enabled
- Read replicas for read-heavy workloads if needed

### Database Strategy for Microservices
Recommended:
- **Database per service** or
- **Schema per service** if budget constraints exist

Example:
- `auth_db`
- `user_db`
- `catalog_db`
- `order_db`

### Recommended Practices
- Use HikariCP connection pooling
- Use Flyway or Liquibase migrations
- Add indexes carefully based on query patterns
- Use pagination for large result sets
- Monitor slow queries
- Avoid cross-service joins

---

## 9. Kafka Architecture

## 9.1 Kafka in the Platform
Use **Kafka** for asynchronous communication between services.

### Recommended AWS Service
- **Amazon MSK** for managed Kafka

### Why Kafka?
- Decouples services
- Supports event-driven architecture
- Scales well for asynchronous workloads
- Enables replayability and durable event processing

---

## 9.2 Typical Kafka Use Cases
- Order created events
- Notification triggers
- Audit events
- Inventory update events
- Payment status events
- User activity tracking

### Example Topics
- `user-events`
- `auth-events`
- `order-events`
- `notification-events`
- `audit-events`

### Example Flow
1. Order Service saves transaction in PostgreSQL
2. Order Service publishes `OrderCreatedEvent` to Kafka
3. Notification Service consumes event
4. Audit Service consumes event
5. Other downstream services react asynchronously

### Recommended Patterns
- Outbox pattern for reliable event publishing
- Dead letter topic for poison messages
- Idempotent consumers
- Consumer groups per service responsibility

---

## 10. Caching with Redis

Use **Amazon ElastiCache Redis** for:
- Frequently accessed lookups
- User/session cache if needed
- OTP or temporary tokens
- Rate limiting counters
- Token revocation or blacklist storage if required

### Pattern
- Cache-aside
- Time-to-live (TTL)
- Explicit invalidation on update paths

---

## 11. Security Architecture

## 11.1 Authentication and Authorization

### Authentication Flow
- Angular calls custom API Gateway
- Gateway routes login to Auth Service
- Auth Service validates credentials
- JWT access token and refresh token are generated
- Angular attaches access token for future requests
- Gateway and/or downstream services validate token

### Authorization
- Role-based access control (RBAC)
- Fine-grained checks at service and method level

### Security Best Practices
- Stateless security using JWT
- Password hashing with BCrypt or Argon2
- Refresh token lifecycle controls
- Method-level authorization
- Token expiry and revocation logic

---

## 11.2 AWS Security Controls

### IAM
- Separate Jenkins deployment role
- Separate ECS task execution role
- Separate ECS task runtime role
- Least privilege permissions only

### Secrets Management
Use **AWS Secrets Manager** for:
- PostgreSQL credentials
- Kafka credentials/config if needed
- JWT signing secrets or keys
- SMTP / external API credentials

### Network Security
- Public subnets only for ALB / NAT
- Private app subnets for ECS services
- Private data subnets for PostgreSQL / Redis / Kafka connectivity
- Tight security group rules

### TLS
- HTTPS everywhere
- ACM certificates for CloudFront and ALB

---

## 12. Networking Design

## 12.1 Recommended VPC Layout

```text
VPC
├── Public Subnet A
├── Public Subnet B
├── Private App Subnet A
├── Private App Subnet B
├── Private DB Subnet A
└── Private DB Subnet B
```

### Public Subnets
- ALB
- NAT Gateway

### Private App Subnets
- Custom API Gateway service
- Spring Boot microservices

### Private Data Subnets
- RDS PostgreSQL
- Redis
- Kafka / MSK endpoints

---

## 12.2 Runtime Flow

```text
User Browser
   -> CloudFront
   -> S3 (Angular assets)
   -> ALB
   -> Custom API Gateway
   -> Internal Spring Boot services
   -> PostgreSQL / Redis / Kafka
```

---

## 13. GitHub + Jenkins + Docker Delivery Model

## 13.1 Source Control
Maintain the code in **GitHub**.

### Suggested Repository Strategy
#### Option A - Monorepo
Recommended if you want centralized management:

```text
platform-root/
  frontend/
  api-gateway/
  auth-service/
  user-service/
  catalog-service/
  order-service/
  notification-service/
  audit-service/
  deployment/
  docs/
```

#### Option B - Polyrepo
Use separate repositories per service if release independence is more important.

For your current architecture style, **monorepo or domain-based multi-repo both work**, but monorepo is simpler for coordinated microservice evolution.

---

## 13.2 CI/CD Flow with Jenkins

### Final Required Pipeline Flow
```text
GitHub -> Jenkins -> Build -> Test -> Docker Build -> Push to ECR -> Deploy to ECS
```

### Pipeline Stages
1. Developer pushes code to GitHub
2. GitHub webhook triggers Jenkins job/pipeline
3. Jenkins checks out source code
4. Jenkins runs frontend and backend tests
5. Jenkins builds Angular application
6. Jenkins packages Spring Boot services using Maven
7. Jenkins builds Docker images
8. Jenkins pushes images to ECR
9. Jenkins updates ECS services / task definitions
10. ALB routes traffic to healthy containers

---

## 13.3 Docker Build Strategy

### Frontend
Option 1:
- Build Angular in Jenkins
- Upload final static build to S3

Option 2:
- Package Angular in an Nginx Docker image
- Deploy as container

**Recommended for AWS:** S3 + CloudFront for Angular.

### Backend
Each Spring Boot service should have a Dockerfile.

Example standard flow:
```text
mvn clean package -> docker build -> ecr push -> ecs deploy
```

### Sample Backend Dockerfile Pattern
```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 13.4 Jenkins Pipeline Stages (Conceptual)

### Frontend Pipeline
- Checkout from GitHub
- Install npm dependencies
- Run lint and tests
- Build Angular production bundle
- Upload bundle to S3
- Invalidate CloudFront cache

### Backend Pipeline
- Checkout from GitHub
- Run Maven tests
- Build JARs
- Build Docker images
- Push images to ECR
- Update ECS task definitions
- Deploy services
- Run smoke tests

---

## 14. Deployment Flow

## 14.1 Frontend Deployment Flow

```text
Developer -> GitHub -> Jenkins -> Angular Build -> S3 Upload -> CloudFront Invalidation -> Live
```

## 14.2 Backend Deployment Flow

```text
Developer -> GitHub -> Jenkins -> Maven Build -> Docker Build -> ECR -> ECS Deploy -> ALB Health Check -> Live
```

## 14.3 End-to-End Deployment Flow

```text
Code Push
  -> GitHub
  -> Jenkins Trigger
  -> Test + Validate
  -> Build Angular + Spring Boot
  -> Dockerize backend services
  -> Push images to ECR
  -> Deploy to ECS Fargate
  -> Upload frontend to S3
  -> Invalidate CloudFront cache
  -> Smoke test
  -> Monitor via CloudWatch
```

---

## 15. Observability and Monitoring

Use the following for production monitoring:

### Logs
- CloudWatch Logs
- Structured JSON logging
- Correlation ID per request

### Metrics
- CloudWatch metrics
- Micrometer
- Prometheus and Grafana (optional)

### Tracing
- AWS X-Ray or OpenTelemetry

### Alerts
- High latency
- Error spikes
- ECS unhealthy task count
- Kafka lag
- PostgreSQL CPU/storage alarms
- Redis memory alarms

---

## 16. Resilience Patterns

Use these production patterns:
- Timeout on all synchronous service calls
- Circuit breaker with Resilience4j
- Retry only for transient safe operations
- Bulkhead isolation
- Fallback responses where applicable
- Outbox pattern for Kafka publishing
- Dead letter handling for failed consumers
- Idempotent event consumers

---

## 17. Production Readiness Checklist

### API Gateway
- [ ] Custom API Gateway deployed as separate service
- [ ] Routing rules defined
- [ ] JWT validation implemented
- [ ] Rate limiting configured
- [ ] Correlation IDs added

### Backend
- [ ] All services containerized
- [ ] Health checks implemented
- [ ] Readiness/liveness endpoints added
- [ ] Resource limits configured
- [ ] Secure configuration externalized

### Database
- [ ] PostgreSQL Multi-AZ enabled
- [ ] Backups configured
- [ ] Migrations automated
- [ ] Index review completed
- [ ] Slow query monitoring enabled

### Kafka
- [ ] Topics designed properly
- [ ] Retry / DLQ strategy defined
- [ ] Consumer groups configured
- [ ] Outbox pattern considered

### CI/CD
- [ ] GitHub webhook to Jenkins configured
- [ ] Jenkins credentials managed securely
- [ ] Docker build/push automated
- [ ] ECS deployment automated
- [ ] Rollback plan tested

### Security
- [ ] WAF enabled
- [ ] Secrets Manager integrated
- [ ] HTTPS enforced
- [ ] Security groups hardened
- [ ] IAM least privilege applied

---

## 18. Interview-Friendly Explanation

> We use Angular for the frontend, hosted on S3 and delivered using CloudFront. Instead of AWS API Gateway, we use a separate custom API Gateway built with Spring Cloud Gateway, which acts as the central entry point for all backend APIs. The backend consists of multiple Spring Boot microservices deployed as Docker containers on ECS Fargate. Persistent data is stored in Amazon RDS PostgreSQL, Redis is used for caching, and Kafka on Amazon MSK is used for asynchronous event-driven communication. The source code is maintained in GitHub, Jenkins is used for CI/CD, and Docker is used to build and package all backend services before pushing images to ECR and deploying to ECS.

---

## 19. Final Recommended Architecture Summary

```text
Users
  -> Route53
  -> CloudFront
  -> S3 (Angular Frontend)
  -> ALB
  -> Custom API Gateway (Spring Cloud Gateway)
  -> Spring Boot Microservices
  -> PostgreSQL (RDS)
  -> Redis (ElastiCache)
  -> Kafka (MSK)

Delivery Pipeline:
GitHub -> Jenkins -> Build/Test -> Docker Build -> ECR -> ECS Deploy
```

---

## 20. Suggested Next Files

The next most useful downloadable files are:

1. **Updated AWS architecture diagram document with these exact technology choices**
2. **Jenkins pipeline (Jenkinsfile) for Angular + Spring Boot + Docker + ECS**
3. **Terraform project structure for this AWS setup**
4. **Spring Cloud Gateway starter project structure**
5. **ECS task definition samples**
6. **Kafka topic and event design document**

---

**Document Version:** 2.0  
**Prepared By:** M365 Copilot  
**Architecture Variant:** Custom API Gateway + Kafka + PostgreSQL + GitHub + Jenkins + Docker
