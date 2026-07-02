# AWS Architecture Diagram + Deployment Flow Document

## 1. Document Purpose

This document provides a **production-ready AWS architecture diagram** and a **complete deployment flow** for an application built with:

- **Frontend:** Angular
- **Backend:** Java 17 + Spring Boot Microservices
- **Database:** MySQL
- **Cloud:** AWS
- **Container Platform:** ECS Fargate (recommended)

It is intended to help with:
- Solution design reviews
- Technical documentation
- Interview explanations
- Project planning
- Production deployment readiness

---

## 2. Recommended AWS Architecture Overview

### Key Services Used
- **Route 53** for DNS
- **CloudFront** for CDN
- **S3** for Angular frontend hosting
- **AWS WAF** for web application protection
- **API Gateway** (optional external API facade)
- **Application Load Balancer (ALB)** for routing
- **Amazon ECS Fargate** for Spring Boot microservices
- **Amazon ECR** for Docker images
- **Amazon RDS MySQL** for relational data
- **Amazon ElastiCache Redis** for caching
- **Amazon MSK / SQS / SNS** for asynchronous communication
- **CloudWatch + X-Ray** for monitoring and tracing
- **Secrets Manager** for secrets
- **IAM / Security Groups / ACM / KMS** for security

---

## 3. High-Level AWS Architecture Diagram

> This Mermaid diagram can be rendered in GitHub, Markdown viewers that support Mermaid, or documentation tools like Azure DevOps Wiki / GitLab / Obsidian.

```mermaid
flowchart TB
    U[Users / Browser / Mobile] --> R53[Route 53 DNS]
    R53 --> CF[CloudFront CDN]

    CF --> S3[S3 Bucket - Angular Frontend]
    CF --> WAF[AWS WAF]
    WAF --> APIGW[API Gateway Optional]
    APIGW --> ALB[Application Load Balancer]

    subgraph VPC[AWS VPC - Multi AZ]
        subgraph Pub[Public Subnets]
            ALB
            NAT[NAT Gateway]
        end

        subgraph App[Private App Subnets]
            AUTH[Auth Service\nSpring Boot]
            USER[User Service\nSpring Boot]
            CATALOG[Catalog Service\nSpring Boot]
            ORDER[Order Service\nSpring Boot]
            NOTIFY[Notification Service\nSpring Boot]
        end

        subgraph Data[Private Data Subnets]
            RDS[(Amazon RDS MySQL)]
            REDIS[(ElastiCache Redis)]
            MSK[(Amazon MSK / SQS)]
        end
    end

    ALB --> AUTH
    ALB --> USER
    ALB --> CATALOG
    ALB --> ORDER
    ALB --> NOTIFY

    AUTH --> RDS
    USER --> RDS
    CATALOG --> RDS
    ORDER --> RDS

    AUTH --> REDIS
    USER --> REDIS
    CATALOG --> REDIS
    ORDER --> REDIS

    ORDER --> MSK
    MSK --> NOTIFY

    subgraph Shared[Shared AWS Platform Services]
        ECR[ECR - Docker Images]
        CW[CloudWatch Logs / Metrics / Alarms]
        XR[X-Ray / OpenTelemetry]
        SEC[Secrets Manager / Parameter Store]
        CT[CloudTrail]
    end

    AUTH --> CW
    USER --> CW
    CATALOG --> CW
    ORDER --> CW
    NOTIFY --> CW

    AUTH --> XR
    USER --> XR
    CATALOG --> XR
    ORDER --> XR
    NOTIFY --> XR

    AUTH --> SEC
    USER --> SEC
    CATALOG --> SEC
    ORDER --> SEC
    NOTIFY --> SEC
```

---

## 4. Layer-by-Layer Architecture Explanation

## 4.1 Edge Layer

### Route 53
Used for domain routing:
- `app.example.com` -> CloudFront
- `api.example.com` -> API Gateway / ALB

### CloudFront
Used to:
- Deliver Angular static assets globally
- Improve frontend performance
- Terminate HTTPS with ACM
- Reduce latency through edge caching

### WAF
Used for:
- OWASP rule protection
- IP rate limiting
- Bot filtering
- Threat mitigation for public endpoints

---

## 4.2 Frontend Layer

### S3 + CloudFront
The Angular application is built and deployed as static assets to S3.
CloudFront serves the content globally.

### Angular Runtime Flow
1. User loads the Angular app from CloudFront
2. Angular initializes configuration
3. Angular calls backend API via API Gateway or ALB
4. JWT token is attached using Angular HTTP interceptor

---

## 4.3 API Entry Layer

### API Gateway (Optional but Recommended for Public APIs)
Use API Gateway when you need:
- Centralized throttling
- API key / usage plans
- Request validation
- Better external API management
- Token authorizers and custom auth integrations

### ALB
Use ALB for path-based routing to ECS services.

Example routing:
- `/auth/*` -> Auth Service
- `/users/*` -> User Service
- `/catalog/*` -> Catalog Service
- `/orders/*` -> Order Service
- `/notifications/*` -> Notification Service

---

## 4.4 Compute Layer

### ECS Fargate
Each microservice is packaged as a Docker image and deployed independently as an ECS service.

Each service includes:
- Task definition
- CPU and memory allocation
- Auto scaling configuration
- CloudWatch log configuration
- Health checks

### Why ECS Fargate?
- No server maintenance
- Easy deployment model
- Native AWS integration
- Good fit for Spring Boot microservices

---

## 4.5 Data Layer

### Amazon RDS MySQL
Recommended for persistent relational data.

Best practices:
- Multi-AZ enabled
- Automated backups
- Performance Insights
- Encryption at rest using KMS
- Read replicas for high read traffic

### ElastiCache Redis
Recommended for:
- Caching frequently accessed data
- Session/token state if required
- Rate limiting counters
- Short-lived verification / OTP use cases

### MSK / SQS / SNS
Recommended for async communication:
- Order events
- Notification workflows
- Audit messages
- Reporting triggers

---

## 5. Production Network Diagram

```mermaid
flowchart LR
    Internet[Internet] --> CF[CloudFront]
    CF --> S3[S3 Frontend Bucket]
    CF --> WAF[WAF]
    WAF --> APIGW[API Gateway]
    APIGW --> ALB[Public ALB]

    subgraph VPC[AWS VPC]
        subgraph PUB[Public Subnets]
            ALB
            NAT1[NAT Gateway AZ-A]
            NAT2[NAT Gateway AZ-B]
        end

        subgraph APPA[Private App Subnet AZ-A]
            A1[Auth Service Task]
            U1[User Service Task]
            C1[Catalog Service Task]
        end

        subgraph APPB[Private App Subnet AZ-B]
            A2[Auth Service Task]
            O2[Order Service Task]
            N2[Notification Service Task]
        end

        subgraph DBA[Private DB Subnet AZ-A]
            RDS1[(RDS Primary)]
            RED1[(Redis Primary)]
        end

        subgraph DBB[Private DB Subnet AZ-B]
            RDS2[(RDS Standby / Multi-AZ)]
            RED2[(Redis Replica)]
        end
    end

    ALB --> A1
    ALB --> A2
    ALB --> U1
    ALB --> C1
    ALB --> O2
    ALB --> N2

    A1 --> RDS1
    A2 --> RDS1
    U1 --> RDS1
    C1 --> RDS1
    O2 --> RDS1

    A1 --> RED1
    U1 --> RED1
    C1 --> RED1
    O2 --> RED1
```

---

## 6. Request / Runtime Flow Diagram

```mermaid
sequenceDiagram
    participant User as User Browser
    participant CF as CloudFront
    participant S3 as S3 Angular App
    participant AG as API Gateway/ALB
    participant Auth as Auth Service
    participant Order as Order Service
    participant Redis as Redis
    participant DB as RDS MySQL
    participant MQ as Kafka/MSK
    participant Notify as Notification Service

    User->>CF: Open application URL
    CF->>S3: Fetch Angular static files
    S3-->>CF: Return index.html, JS, CSS
    CF-->>User: Angular app loaded

    User->>AG: Login request
    AG->>Auth: POST /auth/login
    Auth->>DB: Validate user credentials
    DB-->>Auth: User record
    Auth-->>AG: JWT access token + refresh token
    AG-->>User: Login success

    User->>AG: Create order API with JWT
    AG->>Order: POST /orders
    Order->>Redis: Check cache if needed
    Order->>DB: Save order
    DB-->>Order: Order persisted
    Order->>MQ: Publish OrderCreatedEvent
    MQ->>Notify: Consume event
    Notify-->>User: Email/SMS notification async
    Order-->>AG: 201 Created
    AG-->>User: Response success
```

---

## 7. CI/CD Deployment Flow Diagram

```mermaid
flowchart LR
    DEV[Developer Commit] --> GIT[GitHub / Azure Repos / Bitbucket]
    GIT --> CI[CI Pipeline - Jenkins / GitHub Actions]
    CI --> TEST[Run Unit Tests / Integration Tests / Lint / Sonar]
    TEST --> BUILD[Build Angular + Maven Package]
    BUILD --> IMG[Build Docker Images]
    IMG --> ECR[ECR Push Versioned Images]
    BUILD --> ART[Store Frontend Build Artifact]

    ECR --> CD[CD Pipeline]
    ART --> CD
    CD --> DEVENV[Deploy to Dev]
    DEVENV --> QA[Deploy to QA / UAT]
    QA --> APPROVAL[Manual Approval for Production]
    APPROVAL --> PROD[Production Deployment]
    PROD --> SMOKE[Smoke Tests / Health Checks]
    SMOKE --> MON[Monitoring and Alerts]
```

---

## 8. Frontend Deployment Flow

### Angular Deployment Steps
1. Developer commits frontend code
2. CI pipeline installs dependencies
3. Run lint and unit tests
4. Build Angular for production:

```bash
ng build --configuration=production
```

5. Upload `dist/` files to S3 bucket
6. Invalidate CloudFront cache
7. Run smoke test for UI availability

### Frontend Deployment Diagram

```mermaid
flowchart TD
    A[Angular Source Code] --> B[Install Dependencies]
    B --> C[Run Lint and Tests]
    C --> D[Production Build]
    D --> E[Upload Files to S3]
    E --> F[CloudFront Cache Invalidation]
    F --> G[Frontend Live]
```

---

## 9. Backend Deployment Flow

### Spring Boot Microservice Deployment Steps
1. Developer commits backend code
2. CI pipeline runs Maven build and tests
3. JAR is packaged
4. Docker image is built
5. Image is tagged with version
6. Image is pushed to ECR
7. ECS service is updated with new task definition
8. ALB routes traffic to healthy new tasks
9. Old tasks are drained and removed

### Backend Deployment Diagram

```mermaid
flowchart TD
    A[Spring Boot Source Code] --> B[Maven Clean Verify]
    B --> C[Build JAR]
    C --> D[Docker Build]
    D --> E[Tag Docker Image]
    E --> F[Push to ECR]
    F --> G[Update ECS Task Definition]
    G --> H[Deploy ECS Service]
    H --> I[Health Check via ALB]
    I --> J[Traffic Shift to Healthy Tasks]
```

---

## 10. End-to-End Production Deployment Flow

### End-to-End Sequence

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant Git as Source Control
    participant CI as CI Pipeline
    participant ECR as ECR
    participant S3 as S3
    participant ECS as ECS Fargate
    participant ALB as ALB
    participant CF as CloudFront
    participant User as End User

    Dev->>Git: Push frontend + backend changes
    Git->>CI: Trigger pipeline
    CI->>CI: Run tests, quality scan, security scan
    CI->>S3: Upload Angular build
    CI->>CF: Invalidate CloudFront cache
    CI->>ECR: Push Docker images
    CI->>ECS: Update task definitions/services
    ECS->>ALB: Register healthy tasks
    ALB-->>ECS: Health check OK
    CF-->>User: Serve updated frontend
    User->>ALB: API requests reach new backend version
```

---

## 11. Recommended Deployment Strategies

## 11.1 Rolling Deployment
Suitable for most standard releases.

**Flow:**
- Start new ECS tasks
- Wait for health check success
- Gradually stop old tasks

**Pros:**
- Simple
- Low maintenance

**Cons:**
- Small risk during partial rollout

---

## 11.2 Blue-Green Deployment
Recommended for production-critical applications.

**Flow:**
- Deploy new version in parallel environment
- Run smoke / validation tests
- Shift traffic from Blue to Green
- Rollback quickly if required

**Pros:**
- Fast rollback
- Safer release process

**Cons:**
- Higher cost during deployment window

---

## 11.3 Canary Deployment
Recommended for high-risk changes.

**Flow:**
- Route small percentage of traffic to new version
- Monitor metrics and error rate
- Gradually increase traffic if stable

**Pros:**
- Safest for critical changes
- Real production validation

**Cons:**
- More operational complexity

---

## 12. ECS Deployment Model

Each microservice in ECS should have:
- ECS cluster
- Task definition
- ECS service
- Auto scaling policy
- Target group
- ALB listener rule
- CloudWatch log group
- IAM task execution role
- IAM task role

### Example Mapping
- `auth-service` -> target group `tg-auth`
- `user-service` -> target group `tg-user`
- `catalog-service` -> target group `tg-catalog`
- `order-service` -> target group `tg-order`
- `notification-service` -> internal event consumer service

---

## 13. Environment Promotion Flow

```mermaid
flowchart LR
    LOCAL[Local Development] --> DEV[Dev Environment]
    DEV --> QA[QA Environment]
    QA --> UAT[UAT Environment]
    UAT --> PROD[Production]
```

### Best Practice
- Build once
- Promote the same Docker image across all environments
- Change only environment-specific configurations

---

## 14. Infrastructure as Code Recommendation

Use **Terraform** or **CloudFormation** for:
- VPC
- Subnets
- ALB
- ECS cluster and services
- RDS
- Redis
- ECR
- IAM roles
- Security groups
- Route 53
- CloudFront
- WAF
- Secrets Manager

### Suggested Terraform Modules
- `networking/`
- `security/`
- `frontend/`
- `ecs-cluster/`
- `ecs-services/`
- `rds/`
- `redis/`
- `messaging/`
- `monitoring/`

---

## 15. Operational Monitoring Flow

```mermaid
flowchart TD
    U[Users] --> APP[Frontend + Backend]
    APP --> LOGS[CloudWatch Logs]
    APP --> METRICS[CloudWatch Metrics / Prometheus]
    APP --> TRACE[X-Ray / OpenTelemetry]
    LOGS --> ALERT[CloudWatch Alarms]
    METRICS --> ALERT
    TRACE --> ALERT
    ALERT --> TEAM[Email / Teams / Slack / PagerDuty]
```

---

## 16. Rollback Flow

### Frontend Rollback
- Restore previous S3 artifact version
- Re-invalidate CloudFront cache

### Backend Rollback
- Redeploy previous stable Docker image tag from ECR
- Update ECS service to last known good revision

### Rollback Diagram

```mermaid
flowchart TD
    A[Deployment Failure Detected] --> B{Frontend or Backend?}
    B -->|Frontend| C[Restore Previous S3 Version]
    C --> D[Invalidate CloudFront]
    B -->|Backend| E[Select Previous ECR Image Tag]
    E --> F[Update ECS Service to Stable Task Definition]
    D --> G[Service Restored]
    F --> G[Service Restored]
```

---

## 17. Production Readiness Checklist

### Frontend
- [ ] Angular production build optimized
- [ ] S3 bucket private
- [ ] CloudFront configured with HTTPS only
- [ ] Cache invalidation strategy defined
- [ ] Error pages configured

### Backend
- [ ] All services containerized
- [ ] Health checks implemented
- [ ] Readiness/liveness endpoints configured
- [ ] Resource requests and limits tuned
- [ ] Auto scaling enabled
- [ ] Secure secrets retrieval configured

### Data
- [ ] RDS Multi-AZ enabled
- [ ] Backups configured
- [ ] Performance Insights enabled
- [ ] Redis replication configured
- [ ] Migration strategy defined

### Security
- [ ] WAF enabled
- [ ] IAM least privilege applied
- [ ] TLS enforced
- [ ] Security groups restricted
- [ ] Secrets Manager used
- [ ] JWT expiration and refresh flow implemented

### Operations
- [ ] CloudWatch alarms configured
- [ ] Deployment rollback plan tested
- [ ] DR and backup restore tested
- [ ] CI/CD approval gates configured

---

## 18. Interview-Friendly Explanation

> The Angular frontend is hosted in S3 and delivered globally through CloudFront. Public traffic is protected using WAF and optionally routed through API Gateway. API requests are sent to an Application Load Balancer, which performs path-based routing to Spring Boot microservices running on ECS Fargate in private subnets. Persistent data is stored in Amazon RDS MySQL, Redis is used for caching, and asynchronous workflows are handled via MSK or SQS. The complete deployment is automated through CI/CD, using ECR for Docker images, CloudWatch for monitoring, and Secrets Manager for secure configuration management.

---

## 19. Suggested Next Deliverables

The next useful files you may want are:

1. **Terraform starter structure**
2. **ECS task definition samples**
3. **GitHub Actions or Jenkins pipeline YAML**
4. **AWS security design document**
5. **Microservices low-level design (LLD)**
6. **Detailed database-per-service architecture note**
7. **Production go-live checklist**

---

## 20. Quick Copy-Paste Architecture Summary

```text
Users -> Route53 -> CloudFront -> S3 (Angular)
                         |
                         -> WAF -> API Gateway -> ALB
                                              |
                                              -> ECS Fargate Spring Boot Microservices
                                                     |
                                                     -> RDS MySQL
                                                     -> ElastiCache Redis
                                                     -> MSK / SQS

Cross-cutting:
- ECR
- CloudWatch
- X-Ray
- Secrets Manager
- IAM
- ACM
- WAF
```

---

**Document Version:** 1.0  
**Prepared By:** M365 Copilot  
**File Type:** Markdown with Mermaid diagrams  
**Usage:** Download and open in any Markdown editor. For best results, use a Markdown viewer that supports Mermaid.
