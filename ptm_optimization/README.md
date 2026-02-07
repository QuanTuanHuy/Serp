# PTM Optimization Service

Advanced scheduling optimization microservice for the SERP ERP system. Uses Google OR-Tools to solve complex task scheduling problems via MILP, CP-SAT, and heuristic algorithms.

## Quick Start

```bash
# Prerequisites: PostgreSQL, Redis, Kafka running
cp .env.example .env   # Configure credentials
./run-dev.sh           # Starts on port 8085
```

Access via API Gateway: `http://localhost:8080/ptm-optimization/api/v1`

## Overview

- **Multiple Strategies** - MILP, CP-SAT, Heuristic, Local Search algorithms
- **Auto Strategy Selection** - Chooses optimal algorithm based on problem size
- **Fallback Chain** - Tries strategies sequentially until success
- **Task Registration** - Sync tasks from PTM Task Service via Kafka
- **Configurable Weights** - Urgency, priority, preference, energy match scoring
- **Partial Solutions** - Returns best-effort results on timeout

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Optimizer | Google OR-Tools 9.11 |
| Database | PostgreSQL |
| Cache | Redis |
| Messaging | Apache Kafka |

## Strategy Comparison

| Strategy | Best For | Speed | Quality |
|----------|----------|-------|---------|
| **MILP** | <30 tasks, critical schedules | 1-30s | Optimal |
| **CP-SAT** | 30-100 tasks, daily scheduling | 2-45s | Near-optimal |
| **Heuristic** | 100+ tasks, real-time updates | <1s | Good |
| **Local Search** | Refining solutions | 5-60s | Very good |

**Auto Selection:**
- <30 tasks → MILP
- 30-100 tasks → CP-SAT  
- 100+ tasks → Heuristic + Local Search

## API Routes

All routes prefixed with `/ptm-optimization/api/v1`:

| Resource | Endpoints |
|----------|-----------|
| Optimization | `POST /optimization/schedule`, `/schedule-with-fallback`, `GET /strategies` |
| Task Registration | `CRUD /task-registrations` |

**Request Example:**
```json
{
  "tasks": [{ "id": 1, "durationMin": 60, "priority": 5, "deadlineMs": 1704153600000 }],
  "windows": [{ "dateMs": 1704067200000, "startMin": 540, "endMin": 1020 }],
  "params": { "strategy": "CP_SAT", "timeLimitSec": 30 }
}
```

## Kafka Integration

**Consumed** from `ptm.task.topic`:

| Event | Action |
|-------|--------|
| `taskManagerCreateTask` | Register task |
| `taskManagerUpdateTask` | Update registration |
| `taskManagerDeleteTask` | Remove registration |

## Configuration

Required environment variables (`.env`):

```bash
SERVER_PORT=8085
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/serp_ptm_optimization
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_DATA_REDIS_HOST=localhost
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**OR-Tools JVM Args** (configured in `pom.xml`):
```
--enable-native-access=ALL-UNNAMED
--add-opens java.base/java.lang=ALL-UNNAMED
-XX:+EnableDynamicAgentLoading
-Xmx1g
```

## Project Structure

```
src/main/java/serp/project/ptm_optimization/
├── ui/
│   ├── controller/          # REST controllers
│   └── kafkahandler/        # Kafka message handlers
├── core/
│   ├── domain/              # Entities, DTOs, enums
│   ├── usecase/             # Business logic
│   ├── service/             # Domain services
│   └── port/                # Interfaces (strategy, store)
├── infrastructure/
│   ├── algorithm/           # OR-Tools implementations
│   │   ├── strategy/        # MILP, CP-SAT, Heuristic, LocalSearch
│   │   ├── milp/            # MILP solver
│   │   ├── cpsat/           # CP-SAT solver
│   │   └── heuristic/       # Greedy algorithms
│   ├── factory/             # Strategy factory
│   └── store/               # JPA repositories
└── kernel/                  # Config, utils
```

## Development

```bash
./mvnw test                        # Run tests
./mvnw test -Dtest=ClassName       # Single test class
./mvnw clean package               # Build JAR
```

Database migrations: `src/main/resources/db/migration/V{n}__description.sql`

## See Also

- [AGENTS.md](../AGENTS.md) - Development guidelines
- [PTM Schedule Service](../ptm_schedule) - Main consumer of optimization API
- [PTM Task Service](../ptm_task) - Publishes task events
