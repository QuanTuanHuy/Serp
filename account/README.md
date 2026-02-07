# Account Service

Core identity and access management microservice for the SERP ERP system. Handles authentication, authorization, users, organizations, roles, permissions, subscriptions, and Keycloak integration.

## Quick Start

```bash
# Prerequisites: PostgreSQL, Redis, Kafka, Keycloak running
cp .env.example .env  # Configure credentials
./run-dev.sh          # Starts on port 8081
```

Access via API Gateway: `http://localhost:8080/account-service/api/v1`

## Overview

- **Authentication** - Register, login, JWT tokens (access/refresh), password management
- **User Management** - CRUD, profiles, status tracking, role assignment
- **Organization Management** - Multi-tenancy, org-user relationships, departments
- **Role & Permission Management** - Dynamic RBAC with fine-grained permissions
- **Module Access** - Feature-based access control per user/organization
- **Subscription Plans** - Plan management, pricing models, billing cycles
- **Department Management** - Hierarchical org structure with member assignment
- **Menu Display** - Dynamic role-based menu configuration
- **Keycloak Integration** - User sync, realm/client roles, group management

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Database | PostgreSQL + Flyway |
| Cache | Redis |
| Messaging | Apache Kafka |
| Identity | Keycloak 26.0.5 |

## API Routes

All routes prefixed with `/account-service/api/v1`:

| Resource | Endpoints |
|----------|-----------|
| Auth | `/auth/register`, `/auth/login`, `/auth/refresh-token`, `/auth/change-password` |
| Users | `GET/POST /users`, `/users/profile/me`, `/users/{id}/info`, `/users/assign-roles` |
| Organizations | `/organizations/me`, `/organizations/{id}/users`, `/organizations/{id}/departments` |
| Roles | `CRUD /roles`, `/roles/{id}/permissions` |
| Permissions | `CRUD /permissions` |
| Modules | `/organizations/{id}/modules`, `/organizations/{id}/modules/{id}/users` |
| Subscriptions | `/subscriptions/me`, `/subscriptions/subscribe`, `/subscriptions/trial`, `/subscriptions/upgrade` |
| Subscription Plans | `CRUD /subscription-plans`, `/subscription-plans/{id}/modules` |
| Keycloak | `/keycloak/users`, `/keycloak/roles/realm`, `/keycloak/roles/client`, `/keycloak/groups` |

## Predefined Roles

| Role | Scope |
|------|-------|
| `SUPER_ADMIN` | System administrator |
| `SYSTEM_MODERATOR` | System moderator |
| `ORG_OWNER` | Organization owner |
| `ORG_ADMIN` | Organization administrator |
| `ORG_USER` | Organization user |

## Kafka Events

Published to topic `account-events`:

| Event | Description |
|-------|-------------|
| `user.created` | New user registered |
| `user.updated` | User info changed |
| `user.deleted` | User removed |
| `organization.created` | New organization |
| `subscription.created` | New subscription |
| `subscription.updated` | Subscription status changed |
| `notification.create` | Notification request |

## Configuration

Required environment variables (`.env`):

```bash
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/serp_account
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Keycloak
KEYCLOAK_URL=http://localhost:8180
KEYCLOAK_REALM=serp-realm
KEYCLOAK_CLIENT_ID=serp-client
KEYCLOAK_CLIENT_SECRET=
KEYCLOAK_ADMIN_USERNAME=
KEYCLOAK_ADMIN_PASSWORD=
```

## Project Structure

```
src/main/java/serp/project/account/
├── ui/controller/       # REST controllers
├── core/
│   ├── domain/          # Entities, DTOs, enums
│   ├── usecase/         # Business logic
│   ├── service/         # Domain services
│   ├── port/            # Repository/client interfaces
│   └── seed/            # Data initialization
├── infrastructure/
│   ├── store/           # JPA repositories & models
│   └── client/          # Keycloak, Kafka, Redis adapters
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
