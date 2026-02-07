# CRM Service

Customer Relationship Management microservice for the SERP ERP system. Manages leads, opportunities, customers, contacts, activities, and teams.

## Quick Start

```bash
# Prerequisites: PostgreSQL, Redis, Kafka running (docker-compose -f docker-compose.dev.yml up -d)
cp .env.example .env  # Configure database credentials
./run-dev.sh          # Starts on port 8086
```

Access via API Gateway: `http://localhost:8080/crm/api/v1`

## Overview

- **Lead Management** - Capture, score, qualify, and convert leads through the sales funnel
- **Opportunity Tracking** - Manage deals through pipeline stages (Prospecting → Closed Won/Lost)
- **Customer Accounts** - Individual, business, and enterprise accounts with hierarchical support
- **Contact Management** - Multiple contacts per customer with primary contact designation
- **Activity Tracking** - Log calls, meetings, emails, and tasks linked to any entity
- **Team Management** - Organize sales/support teams with member roles
- **Global Search** - Cross-entity search across all CRM data

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Database | PostgreSQL |
| Cache | Redis |
| Messaging | Apache Kafka |
| Auth | JWT via Keycloak |

## API Routes

All routes prefixed with `/crm/api/v1`:

| Resource | Endpoints |
|----------|-----------|
| Leads | `POST/GET/PATCH/DELETE /leads`, `/leads/{id}/qualify`, `/leads/{id}/convert` |
| Opportunities | `POST/GET/PATCH/DELETE /opportunities`, `/opportunities/{id}/close` |
| Customers | `POST/GET/PATCH/DELETE /customers` |
| Contacts | `CRUD /customers/{id}/contacts`, `GET /contacts` |
| Activities | `POST/GET/PUT/DELETE /activities`, `/activities/{id}/complete`, `/activities/{id}/cancel` |
| Teams | `CRUD /teams`, `/teams/{id}/members` |
| Search | `GET /search?q={query}&limit={n}` |

## Configuration

Required environment variables (`.env`):

```bash
SERVER_PORT=8086
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/serp_crm
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Project Structure

```
src/main/java/serp/project/crm/
├── ui/controller/       # REST controllers
├── core/
│   ├── domain/          # Entities, DTOs, enums
│   ├── usecase/         # Business logic
│   ├── service/         # Domain services
│   └── port/            # Repository interfaces
├── infrastructure/
│   ├── store/           # JPA repositories & mappers
│   └── client/          # External service clients
└── kernel/              # Config, utils, security
```

## Development

```bash
./mvnw test                        # Run tests
./mvnw test -Dtest=ClassName       # Single test class
./mvnw clean package               # Build JAR
```

Database migrations: `src/main/resources/db/migration/V{n}__description.sql`

## See Also

- [AGENTS.md](../AGENTS.md) - Development guidelines and coding standards
- [Main README](../README.md) - Repository overview and architecture
