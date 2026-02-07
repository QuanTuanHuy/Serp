# PTM Task Service

Core task and project management microservice for the SERP ERP system. Manages projects, hierarchical tasks, dependencies, notes, and publishes domain events for downstream scheduling services.

## Quick Start

```bash
# Prerequisites: PostgreSQL, Redis, Kafka running
cp src/config/local.yaml src/config/default.yaml  # Configure settings
./run-dev.sh                                       # Starts on port 8083
```

Access via API Gateway: `http://localhost:8080/ptm-task/api/v1`

## Overview

- **Project Management** - Create projects with status, priority, deadlines, and progress tracking
- **Task Management** - Hierarchical tasks with parent-child relationships and subtask progress
- **Task Dependencies** - Define execution order (FINISH_TO_START, START_TO_START, etc.)
- **Task Templates** - Reusable templates for common workflows
- **Task Reminders** - Schedule reminders (BEFORE_START, AT_DEADLINE, CUSTOM)
- **Notes** - Attach documentation to projects or tasks with tags
- **Activity Tracking** - Audit log for all CRUD operations
- **Event Publishing** - Kafka events for PTM Schedule and Optimization services

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Go 1.25 |
| Framework | Gin |
| Database | PostgreSQL (GORM) |
| Cache | Redis |
| Messaging | Apache Kafka |
| DI | Uber FX |

## API Routes

All routes prefixed with `/ptm-task/api/v1`, require JWT auth (PTM_ADMIN or PTM_USER):

| Resource | Endpoints |
|----------|-----------|
| Projects | `CRUD /projects`, `/projects/:id/tasks`, `/projects/:id/notes` |
| Tasks | `CRUD /tasks`, `/tasks/:id/tree`, `/tasks/:id/notes` |
| Notes | `POST/GET/PATCH/DELETE /notes`, `/notes/search` |

## Kafka Events

Published to topic `serp.ptm.task.events`:

| Event Type | Trigger |
|------------|---------|
| `ptm.task.created` | New task |
| `ptm.task.updated` | Task modified |
| `ptm.task.deleted` | Task removed |
| `ptm.task.bulk_deleted` | Multiple tasks deleted |

**Message Format:**
```json
{
  "meta": {
    "id": "uuid",
    "type": "ptm.task.created",
    "source": "ptm-task",
    "v": "1.0",
    "ts": 1704067200000,
    "traceId": "uuid"
  },
  "data": {
    "taskId": 456,
    "userId": 123,
    "tenantId": 1,
    "title": "Implement feature",
    "priority": "HIGH",
    "status": "TODO",
    "estimatedDurationMin": 120,
    "deadlineMs": 1704153600000,
    "isDeepWork": true,
    "isFlexible": false
  }
}
```

**Consumers:** PTM Schedule Service, PTM Optimization Service

## Configuration

Edit `src/config/local.yaml`:

```yaml
server:
  port: 8083
  context_path: /ptm-task

database:
  host: localhost
  port: 5432
  name: serp_ptm_task
  user: 
  password: 

redis:
  host: localhost
  port: 6379

kafka:
  brokers: ["localhost:9092"]
  topic: serp.ptm.task.events
```

## Project Structure

```
src/
├── main.go
├── cmd/bootstrap/       # DI & initialization (all.go critical)
├── ui/
│   ├── controller/      # HTTP handlers
│   ├── router/          # Route definitions
│   └── middleware/      # JWT, role validation
├── core/
│   ├── domain/          # Entities, DTOs, enums, mappers
│   ├── usecase/         # Business logic
│   ├── service/         # Domain services
│   └── port/            # Repository interfaces
├── infrastructure/
│   ├── store/           # GORM adapters & models
│   └── client/          # Kafka, Redis adapters
└── config/              # YAML configs
```

## Development

```bash
go test ./...              # Run tests
go fmt ./...               # Format code
go vet ./...               # Static analysis
go build -o bin/app src/main.go  # Build
```

**Critical:** Register all new components in `cmd/bootstrap/all.go`

## See Also

- [AGENTS.md](../AGENTS.md) - Development guidelines
- [PTM Schedule Service](../ptm_schedule) - Consumes task events
- [PTM Optimization Service](../ptm_optimization) - Advanced scheduling algorithms
