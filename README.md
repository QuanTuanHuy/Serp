# SERP - Smart ERP System

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
![Java](https://img.shields.io/badge/Java-21-orange)
![Go](https://img.shields.io/badge/Go-1.22+-00ADD8)
![Python](https://img.shields.io/badge/Python-3.12+-3776AB)
![Next.js](https://img.shields.io/badge/Next.js-15-black)

SERP is a modern, event-driven microservices ERP system designed with **Clean Architecture**, **Kafka messaging**, and **Keycloak authentication**. It aims to provide a scalable and modular solution similar to Odoo but built with a polyglot microservices approach.

---

## 📑 Table of Contents
- [Quick Start](#-quick-start)
- [Architecture Overview](#-architecture-overview)
- [Services](#-services)
- [Getting Started](#-getting-started)
- [Testing](#-testing)
- [Project Structure & Patterns](#-project-structure--patterns)
- [License](#license)

---

## ⚡ Quick Start

```bash
# 1. Start infrastructure (PostgreSQL, Kafka, Redis, Keycloak, Minio)
docker-compose -f docker-compose.dev.yml up -d

# 2. Run services (in separate terminals)
cd account && ./run-dev.sh
cd ptm_task && ./run-dev.sh
cd serp_web && npm install && npm run dev

```

---

## 🚀 Architecture Overview

The system is composed of multiple microservices communicating via an API Gateway. It uses a mix of **Java (Spring Boot)**, **Go (Gin)**, and **Python (FastAPI)** for backend services, and **Next.js** for the frontend.

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (Browser)                        │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP Requests
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  API Gateway (Go - Port 8080)                   │
│              JWT Validation │ Routing │ Rate Limiting           │
└──────┬──────────┬─────────┬─────────┬──────────┬────────────────┘
       │          │         │         │          │
       ▼          ▼         ▼         ▼          ▼
    ┌────┐    ┌─────┐   ┌─────┐  ┌──────┐   ┌──────┐
    │Acct│    │ CRM │   │Sales│  │Discu │   │ PTM  │  ... (12+ services)
    │8081│    │8086 │   │8087 │  │8092  │   │8083  │
    └─┬──┘    └──┬──┘   └──┬──┘  └──┬───┘   └──┬───┘
      │          │         │        │          │
      └──────────┴─────────┴────────┴──────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
    ┌──────────┐    ┌──────────┐    ┌─────────┐
    │PostgreSQL│    │  Kafka   │    │  Redis  │
    │  :5432   │    │  :9092   │    │  :6379  │
    └──────────┘    └──────────┘    └─────────┘
         │               │
         │   Event-Driven Architecture
         │   ┌─────────────────────────┐
         └───│ Services publish events │
             │ (async communication)   │
             └─────────────────────────┘
```

### Clean Architecture Layers (Flow)

```
HTTP Request → Controller → UseCase → Service → Port → Adapter → Database
                    ↓           ↓         ↓
                   DTO       Domain    Entity
                             Logic    Validation
```

### Core Technologies
- **Backend**: Java 21 (Spring Boot), Go 1.22+ (Gin), Python 3.12+ (FastAPI)
- **Frontend**: Next.js 15, Redux Toolkit, Shadcn UI
- **Infrastructure**: PostgreSQL, Redis, Apache Kafka, Keycloak
- **AI**: Google Gemini via Python service

## 📦 Services

| Service | Port | Language | Description |
|---------|------|----------|-------------|
| **api_gateway** | 8080 | Go | Routes requests, validates JWT, rate limiting |
| **account** | 8081 | Java | User, Auth, Organization, RBAC (Keycloak integration) |
| **logging_tracker** | 8082 | Java | Centralized audit trails & monitoring |
| **ptm_task** | 8083 | Go | Personal Task Management |
| **ptm_schedule** | 8084 | Go | Scheduling and Calendar |
| **ptm_optimization** | 8085 | Java | Task optimization algorithms |
| **crm** | 8086 | Java | Customer Relationship Management |
| **sales** | 8087 | Go | Order management, quotations |
| **purchase_service** | 8088 | Java | Purchase management |
| **logistics** | 8089 | Java | Logistics management |
| **serp_llm** | 8089 | Python | AI Assistant (RAG, Chat) |
| **notification_service** | 8090 | Go | Push notifications |
| **mailservice** | 8091 | Java | Email templates |
| **discuss_service** | 8092 | Java | Discussions, attachments (S3), WebSockets |
| **serp_web** | 3000 | TypeScript | Web Frontend (Next.js 15 + Redux + Shadcn) |

### Infrastructure URLs
- **PostgreSQL**: `localhost:5432`
- **Redis**: `localhost:6379`
- **Kafka**: `localhost:9092`
- **Keycloak**: `localhost:8180`

## 🛠️ Getting Started

### Prerequisites
- **Docker** & Docker Compose
- **Java** 21+
- **Go** 1.22+
- **Python** 3.12+ (with Poetry)
- **Node.js** 20+

### Step 1: Start Infrastructure

Start the required infrastructure (PostgreSQL, Redis, Kafka, Keycloak):

```bash
docker-compose -f docker-compose.dev.yml up -d
```

### Step 2: Run Services Locally

Each service can be run independently. **Ensure infrastructure is up first.**

**Java Services** (account, crm, purchase_service, logistics, etc.):
```bash
cd <service-name>
./run-dev.sh             
# Alternative: ./mvnw spring-boot:run
```

**Go Services** (api_gateway, ptm_task, ptm_schedule, notification_service):
```bash
cd <service-name>
./run-dev.sh             
# Alternative: go run src/main.go
```

**Python Service** (serp_llm):
```bash
cd serp_llm
poetry install               
./run-dev.sh                 
# Alternative: poetry run uvicorn src.main:app --reload
```

**Frontend** (serp_web):
```bash
cd serp_web
npm install                  
npm run dev             
```

---

## 🧪 Testing

### Run Tests

**Java Services:**
```bash
cd <service-name>
./mvnw test                                      
./mvnw test -Dtest=ClassName                     
./mvnw test -Dtest=ClassName#methodName          
```

**Go Services:**
```bash
cd <service-name>
go test ./...                                    
go test -v ./src/core/usecase                    
go test -run TestFunctionName                   
```

**Python Service:**
```bash
cd serp_llm
poetry run pytest                                
poetry run pytest tests/test_file.py             
poetry run pytest -k test_name                   
```

**Frontend:**
```bash
cd serp_web
npm run lint                                     
npm run type-check                               
npm run format:check                             
```

### Database Migrations

**Java (Flyway):**
- Migrations auto-run on service startup
- Location: `src/main/resources/db/migration/V{N}__description.sql`
- Example: `V1__Initial_schema.sql`, `V2__Add_users_table.sql`

**Python (Alembic):**
```bash
cd serp_llm
poetry run alembic upgrade head                  # Apply migrations
poetry run alembic revision --autogenerate -m "Add table"  # Create migration
```

---

## 🏗️ Project Structure & Patterns

The project strictly follows **Clean Architecture**.

### Backend Structure
```
src/
├── core/
│   ├── domain/      # Entities, DTOs, Enums
│   ├── port/        # Interfaces for repositories and clients
│   ├── service/     # Domain business logic
│   └── usecase/     # Application logic & orchestration
├── infrastructure/
│   ├── store/       # Database implementations (Models, Repositories)
│   └── client/      # External clients (Redis, Kafka)
├── ui/
│   ├── controller/  # HTTP Endpoints
│   └── kafka/       # Kafka Consumers
└── kernel/          # Shared utilities & config
```

### Key Patterns
- **Clean Architecture**: Controllers -> UseCases -> Services -> Domain.
- **Event-Driven**: Services publish events to Kafka topics for async processing.
- **Security**: All requests are authenticated via JWT tokens issued by Keycloak.
- **Database**: Separation of Domain Entities and Persistence Models.

---

## License

This project is part of the SERP ERP system and is licensed under the Apache License 2.0. See the [LICENSE](./LICENSE) file for details.
