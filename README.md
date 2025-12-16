# SERP - Smart ERP System

SERP is a modern, event-driven microservices ERP system designed with **Clean Architecture**, **Kafka messaging**, and **Keycloak authentication**. It aims to provide a scalable and modular solution similar to Odoo but built with a polyglot microservices approach.

## 🚀 Architecture Overview

The system is composed of multiple microservices communicating via an API Gateway. It uses a mix of **Java (Spring Boot)**, **Go (Gin)**, and **Python (FastAPI)** for backend services, and **Next.js** for the frontend.

### Core Technologies
- **Backend**: Java 21 (Spring Boot), Go 1.22 (Gin), Python 3.11 (FastAPI)
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
| **sales** | 8088 | Go | Order management, quotations |
| **serp_llm** | 8089 | Python | AI Assistant (RAG, Chat) |
| **mailservice** | 8090 | Java | Email management |
| **notification_service** | 8088 | Go | Notification management |
| **purchase_service** | 8088 | Java | Purchase management |
| **logistics** | 8089 | Java | Logistics management |
| **serp_web** | 3000 | TypeScript | Web Frontend |

> **Note:** Several services are configured to use ports 8088 and 8089 by default. You may need to configure `SERVER_PORT` or `PORT` environment variables to avoid conflicts if running all services simultaneously.

## 🛠️ Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21+
- Go 1.22+
- Python 3.11+
- Node.js 20+

### Running Infrastructure
Start the required infrastructure (PostgreSQL, Redis, Kafka, Keycloak):

```bash
docker-compose -f docker-compose.dev.yml up -d
```

### Running Services Locally

Each service can be run independently. Make sure infrastructure is up first.

**Java Services (e.g., Account, CRM):**
```bash
cd account
./run-dev.sh
# Or: ./mvnw spring-boot:run
```

**Go Services (e.g., PTM Task):**
```bash
cd ptm_task
./run-dev.sh
# Or: go run src/main.go
```

**Python Service (Serp LLM):**
```bash
cd serp_llm
./run-dev.sh
# Or: poetry run uvicorn src.main:app --reload
```

**Frontend:**
```bash
cd serp_web
npm install
npm run dev
```

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

## 📚 Documentation

Detailed documentation is available in the `docs/` directory:

## 🤝 Contributing
