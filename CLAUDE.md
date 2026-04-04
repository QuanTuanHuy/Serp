# CLAUDE.md — SERP

SERP is a polyglot microservices ERP platform. Each service is independently deployable; all external traffic routes through the API gateway.

> Before making non-trivial changes, read the nearest `AGENTS.md` — it takes precedence over this file for work inside that module. Root guide: `AGENTS.md`.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 15, React 19, TypeScript, Redux Toolkit + RTK Query, Tailwind CSS, Shadcn UI |
| Java services | Spring Boot 3.5.x, Spring Data JPA, Flyway, Spring Kafka, Spring Security + Keycloak |
| Go services | Go 1.25, Gin, Uber FX (DI), Zap logging |
| Python service | Python 3.12, FastAPI, SQLAlchemy async, Alembic, Poetry |
| Infrastructure | PostgreSQL 16, Redis 7, Kafka (Confluent), Keycloak 26, MinIO |
| API Gateway | Go/Gin on port 8080 — JWT validation, routing, rate limiting |

---

## Key Directories

```
serp_web/            Next.js frontend (port 3000)
api_gateway/         Go gateway — all traffic enters here (port 8080)
account/             Auth, users, orgs, RBAC (Java, port 8081)
ptm_task/            Personal task management (Go, port 8083)
ptm_schedule/        Scheduling (Go, port 8084)
ptm_optimization/    Task optimization algorithms (Java, port 8085)
crm/                 Customer relationship management (Java, port 8086)
sales/               Orders and quotations (Java, port 8087)
purchase_service/    Purchase orders (Java, port 8088)
logistics/           Warehouse and inventory (Java, port 8089)
notification_service/ Push notifications (Go, port 8090)
mailservice/         Email sending (Java, port 8091)
discuss_service/     Real-time chat via WebSocket (Java, port 8092)
serp_llm/            AI assistant, RAG (Python/FastAPI, port 8089)
first-mile/          First-mile delivery logistics (Java)
ttcrs/               Truck/container routing service (Spring Boot 4, Java)
pm_core/             Project management core (Java)
serp_java_platform/  Shared Maven BOM and starters (kafka, redis, security)
docker-compose.dev.yml  Full local infrastructure
```

---

## Essential Commands

### Infrastructure
```bash
docker-compose -f docker-compose.dev.yml up -d
docker-compose -f docker-compose.dev.yml logs -f <service>
```

### Java services (run from service directory)
```bash
./run-dev.sh                         # preferred; loads .env
./mvnw spring-boot:run               # alternative
./mvnw test
./mvnw -Dtest=ClassName#methodName test
./mvnw -DskipTests clean package
```
On Windows CMD/PowerShell use `mvnw.cmd`.

### Go services (run from service directory)
```bash
./run-dev.sh
go run src/main.go
go test ./...
go test ./src/core/usecase -run '^TestName$' -count=1
go vet ./...
```

### Frontend (`serp_web/`)
```bash
npm install && npm run dev
npm run lint && npm run type-check   # pre-handoff check
npm run build                        # required for routing/provider changes
```
No test framework is configured yet — do not invent test commands.

### Python (`serp_llm/`)
```bash
poetry install && ./run-dev.sh
poetry run pytest tests/test_file.py::test_name
poetry run alembic upgrade head
```

### Shared Java platform (`serp_java_platform/`)
```bash
mvn test
mvn -pl serp-starter-kafka -am package
```

---

## Cross-Service Conventions

- **Auth**: Keycloak issues JWTs; gateway validates them before forwarding. Services read tenant/user from token via `AuthUtils` (Java) or shared middleware (Go/Python).
- **Async events**: Services publish domain events to Kafka after writes; consumers in other services react asynchronously.
- **DB migrations**: Java → Flyway (`db/migration/V{N}__description.sql`, auto-runs on startup). Python → Alembic (`migrations/`).
- **Register new components**: Go services using Uber FX must register new components in `cmd/bootstrap/all.go`.

---

## Additional Documentation

Check these when relevant:

- `.claude/docs/architectural_patterns.md` — Layer boundaries, DI patterns, response shapes, frontend state, error handling conventions
- `AGENTS.md` — Root developer/agent guide: build commands, style rules, testing conventions, naming
- `serp_web/AGENTS.md` — Frontend-specific architecture, module boundaries, RTK Query patterns, naming
- `account/AGENTS.md`, `api_gateway/AGENTS.md`, `discuss_service/AGENTS.md`, `notification_service/AGENTS.md`, `pm_core/AGENTS.md` — Service-specific guides
- `.github/workflows/build.yml` — CI pipeline: per-service path filtering, Docker image tagging
- `.github/workflows/deploy.yml` — CD pipeline: SSH deploy on merge to main
