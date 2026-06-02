# AGENTS.md - Last-Mile (TMS) Backend Guide for Coding Agents

This guide is for coding agents working inside `last-mile/` (final delivery leg: delivery stations, couriers, dispatch, proof of delivery, returns).
Use it together with the repository-root `AGENTS.md`. For first-mile pickup, see `first-mile/AGENTS.md`; for middle-mile hub/linehaul, see `second-mile/AGENTS.md`.
For TMS UI work, see `serp_web/src/modules/first-mile/AGENTS.md` until a dedicated last-mile UI guide exists.

## Service Snapshot

- **Module:** `last-mile` - Spring Boot, Java 21, package `serp.project.last_mile`.
- **Default port:** `8098` (`SERVER_PORT`).
- **Database:** PostgreSQL `last-mile` (`DB_URL`).
- **Auth:** OAuth2 resource server (Keycloak JWT). Use TMS roles via `@PreAuthorize`.
- **Integrations:** Kafka for order/handover sync and delivery status events, Goong geocode/distance matrix, S3-compatible storage.
- **Gateway path:** `/last-mile/api/v1/*` once wired in `api_gateway`.

## Domain Scope (Last-Mile)

| Area | Examples |
|------|----------|
| Network | Delivery stations, station staff, service areas |
| Delivery | Delivery orders, courier assignment, routes, attempts, proof of delivery |
| Returns | Failed delivery handover, return-to-post-office or return-to-hub flows |
| Fleet | Station-scoped vehicles and couriers |
| Platform | Import history, file storage, Kafka DLQ handlers |

Do not implement pickup/post-office creation logic here when it belongs in `first-mile/`.
Do not implement hub, bag, or linehaul logic here when it belongs in `second-mile/`.

## Module Layout

```text
src/main/java/serp/project/last_mile/
  ui/controller/          # REST APIs - keep thin
  service/                # Interfaces
  service/impl/           # Business logic (@Transactional here)
  service/handler/        # Kafka DLQ handlers
  repository/             # Spring Data JPA
  repository/specification/
  domain/                 # JPA entities
  dto/request|response/   # API contracts
  mapper/                 # Entity <-> DTO
  enums/
  exception/
  kernel/config|utils/    # Security, auth, common helpers
  kafka/
  caller/
src/main/resources/
  db/migration/           # SQL schema scripts
  application.yaml
  i18n/messages*.properties
src/test/java/            # JUnit 5 unit tests
```

**Layering:** `controller` -> `service` -> `repository`. Controllers must not call repositories directly.

## Build, Run, and Test

Run from `last-mile/`. On Windows use `mvnw.cmd`.

```bash
./mvnw spring-boot:run
./mvnw clean compile
./mvnw test
./mvnw -Dtest=DeliveryOrderServiceImplTest test
./mvnw -DskipTests clean package
```

- Set env vars from `.env.example` before `spring-boot:run`.
- Local default URL: `http://localhost:8098/api/v1/...`.
- End-to-end through gateway after wiring: `http://localhost:8080/last-mile/api/v1/...`.
- Quality gate: `clean compile` + focused tests for touched service logic.

## API Conventions

- Base path in service: `/api/v1/{resource}`.
- Query params: **snake_case** (`delivery_station_id`, `courier_id`, `created_from`).
- Request bodies: camelCase JSON fields matching `dto/request/*`.
- Pagination: `page` (0-based), `size` (default 20).
- Responses should use the same TMS shapes as first-mile/second-mile: `ApiResponse<T>` for single results and `ApiResponse<PageResponse<T>>` or the nearest local convention for paginated lists.
- User-facing API messages come from `MessageService` + `i18n/messages*.properties` where present.
- Business failures should use module-specific exceptions (`AppException`, `ErrorCode`) once the exception package exists.

## Security and Access

- Resolve tenant/user context through shared auth utilities when added.
- Common roles:
  - `TMS_ADMIN` - full TMS admin
  - `TMS_DELIVERY_MANAGER` - delivery station manager
  - `TMS_COURIER` - courier
  - `TMS_CUSTOMER` - customer-facing delivery tracking/actions
- Add `@PreAuthorize` on new endpoints and mirror scoping in service logic when data must be restricted by station, courier, tenant, or user.

## Persistence and Schema Changes

- `spring.jpa.hibernate.ddl-auto: none` - never rely on Hibernate auto-DDL.
- New tables/columns: add SQL under `src/main/resources/db/migration/`.
- Register SQL scripts in `application.yaml` -> `spring.sql.init.schema-locations` if the module uses Spring SQL init locally.
- `DB_INIT_MODE` defaults to `never`; fresh DBs need `always` once or manual SQL apply.
- Use `*Specification` classes for complex filters.
- `@Transactional` on write methods in `service/impl`; `@Transactional(readOnly = true)` for read paths.

## Kafka and Cross-Service Sync

- Configure topics under `app.kafka.topics`.
- Expected flows:
  - consume order/handover data from earlier TMS legs,
  - publish delivery status updates,
  - send failures to DLQ/retry handlers when implemented.
- Keep sync consumers idempotent; do not create duplicate delivery orders on replay.

## Code Style

### File headers (required for TMS)

- **Author must be `Nguyen The Anh`** on all new or touched Java/SQL files in this module. Do **not** use `QuanTuanHuy` from the root `AGENTS.md` example.
- When editing a file that already has a header, **preserve** `Author: Nguyen The Anh`.
- New Java files:

```text
/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/
```

- New SQL under `db/migration/`: `-- Author: Nguyen The Anh` when sibling scripts use author comments.
- Lombok: prefer `@RequiredArgsConstructor`, `@Slf4j`, `@Builder` where neighboring code uses them.
- Imports: `jakarta.*`, Lombok, Spring/third-party, then `serp.project.last_mile.*`, then JDK if matching local files.
- 4-space indent; parameterized logging (`log.info("deliveryOrderId={}", id)`).
- Comments only for non-obvious business rules such as delivery attempt windows, route constraints, or return transitions.

## Debugging Checklist

1. Confirm gateway prefix `/last-mile/api/v1` after `api_gateway` wiring.
2. Confirm JWT roles match `@PreAuthorize`.
3. Confirm tenant, station, and courier filters in service layer.
4. For empty lists: verify filters and `DB_INIT_MODE`/migrations.
5. For Kafka issues: check topic name, consumer group, and DLQ rows/logs.

## Before You Finish

- Run `./mvnw clean compile` and relevant tests.
- If you added SQL, update `application.yaml` `schema-locations` or document manual apply steps.
- Keep frontend TMS types/transforms in sync when exposing APIs.
- Never commit real credentials or secrets.
