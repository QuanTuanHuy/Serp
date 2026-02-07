# PTM Schedule Service

Intelligent task scheduling microservice for the SERP ERP system. Consumes task events, manages availability calendars, and generates optimized schedules using built-in algorithms or the PTM Optimization Service.

## Quick Start

```bash
# Prerequisites: PostgreSQL, Redis, Kafka running + PTM Task Service publishing events
cp src/config/local.yaml src/config/default.yaml  # Configure settings
./run-dev.sh                                       # Starts on port 8084
```

Access via API Gateway: `http://localhost:8080/ptm-schedule/api/v1`

## Overview

- **Schedule Plans** - Manage schedule lifecycle (DRAFT → ACTIVE → ARCHIVED)
- **Schedule Events** - Time block assignments with move, split, complete, pin operations
- **Schedule Tasks** - Materialized view of tasks synced from PTM Task Service
- **Availability Calendar** - Weekly patterns, working hours, exceptions
- **Schedule Windows** - Generated available time slots for task placement
- **Auto Rescheduling** - Background worker triggered by task changes via Kafka
- **Hybrid Scheduling** - Built-in algorithm with multi-factor scoring
- **Optimization Integration** - Delegate to PTM Optimization for MILP/CP-SAT solving
- **Idempotent Processing** - Reliable Kafka event handling with deduplication

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Go 1.25 |
| Framework | Gin |
| Database | PostgreSQL (GORM) |
| Cache | Redis |
| Messaging | Apache Kafka (consumer + producer) |
| DI | Uber FX |

## API Routes

All routes prefixed with `/ptm-schedule/api/v1`, require JWT auth:

| Resource | Endpoints |
|----------|-----------|
| Schedule Plans | `POST/GET /schedule-plans`, `/active`, `/active/detail`, `/history`, `/reschedule`, `/deep-optimize`, `/:id/apply`, `/:id/revert` |
| Schedule Events | `GET/POST /schedule-events`, `/:id/move`, `/:id/complete`, `/:id/split` |
| Availability | `GET/POST/PUT /availability-calendar` |
| Windows | `GET /schedule-windows`, `/materialize` |
| Tasks | `GET /schedule-tasks` |

## Scheduling Strategies

| Strategy | When Used | Speed | Quality |
|----------|-----------|-------|---------|
| **Quick Reschedule** | Task changes, real-time | Fast | Good |
| **Deep Optimization** | User request, complex schedules | Slow | Optimal |
| **Fallback Chain** | Try multiple strategies sequentially | Variable | Best available |

**Built-in Hybrid Algorithm Scoring:**
- Urgency (deadline proximity)
- Priority (task importance)
- Preference (preferred time match)
- Energy (deep work during peak hours)
- Continuity bonus / Gap penalty

## Kafka Integration

**Consumed** from `serp.ptm.task.events` (group: `ptm-schedule-group`):

| Event Type | Action |
|------------|--------|
| `ptm.task.created` | Add to schedule tasks, queue reschedule |
| `ptm.task.updated` | Update metadata, queue reschedule |
| `ptm.task.deleted` | Remove from schedule |
| `ptm.task.bulk_deleted` | Remove multiple tasks |

**Published** to `ptm-schedule-events`:

| Event | Trigger |
|-------|---------|
| `SCHEDULE_UPDATED` | Plan changes |

## Configuration

Edit `src/config/local.yaml`:

```yaml
server:
  port: 8084
  context_path: /ptm-schedule

database:
  host: localhost
  port: 5432
  name: serp_ptm_schedule

kafka:
  brokers: ["localhost:9092"]
  consumer_group: ptm-schedule-group
  topics:
    consume: serp.ptm.task.events
    produce: serp.ptm.schedule.events

optimization_service:
  url: http://localhost:8085/ptm-optimization
  timeout_sec: 30
```

## Project Structure

```
src/
├── main.go
├── cmd/bootstrap/       # DI, Kafka consumer, workers
├── ui/
│   ├── controller/      # HTTP handlers
│   ├── kafka/           # Kafka message handlers
│   └── worker/          # Reschedule worker
├── core/
│   ├── domain/
│   │   ├── entity/      # Schedule plan, event, task, window
│   │   ├── algorithm/   # Hybrid scheduler, scoring
│   │   └── dto/
│   ├── usecase/         # Business logic
│   ├── service/         # Domain + reschedule services
│   └── port/            # Interfaces
├── infrastructure/
│   ├── store/           # GORM adapters
│   └── client/          # Optimization client, Kafka
└── config/
```

## Background Workers

| Worker | Purpose |
|--------|---------|
| **Reschedule Worker** | Process queued reschedule requests with debouncing |
| **Event Cleanup** | Remove old processed events (retention policy) |

## Development

```bash
go test ./...              # Run tests
go fmt ./...               # Format code
go vet ./...               # Static analysis
```

**Critical:** Register all new components in `cmd/bootstrap/all.go`

## See Also

- [AGENTS.md](../AGENTS.md) - Development guidelines
- [PTM Task Service](../ptm_task) - Publishes task events
- [PTM Optimization Service](../ptm_optimization) - Advanced MILP/CP-SAT algorithms
