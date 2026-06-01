# AGENTS.md - First-Mile (TMS) Backend Guide for Coding Agents

This guide is for coding agents working inside `first-mile/` (first leg: post offices, pickup, orders, dispatch).
Use it together with the repository-root `AGENTS.md`. For second-mile (hubs, bags, linehaul), see `second-mile/AGENTS.md`.
For TMS UI work, see `serp_web/src/modules/first-mile/AGENTS.md`.

## Service Snapshot

- **Module:** `first-mile` — Spring Boot, Java 21, package `serp.project.first_mile`.
- **Default port:** `8093` (`SERVER_PORT`).
- **Database:** PostgreSQL `first-mile` (`DB_URL`).
- **Auth:** OAuth2 resource server (Keycloak JWT). Role checks use `TMS_*` roles via `@PreAuthorize`.
- **Integrations:** Kafka (user/order/hub sync, DLQ), Goong geocode/distance matrix, S3-compatible storage, optional payment caller.
- **Gateway path:** `/first-mile/api/v1/*` (proxied by `api_gateway`).

## Domain Scope (First-Mile)

| Area | Examples |
|------|----------|
| Master data | Provinces, wards, post offices, post office staff, product types |
| Operations | Orders, trips, pickup check-in, pickup optimization (auto/manual assign) |
| Fleet (local) | Post-office-scoped vehicles |
| Billing | Tariffs, surcharges, VAS (separate controllers under `/api/v1/billing`) |
| Platform | Import history, file storage, Kafka DLQ handlers |

Do not implement hub/bag/linehaul domain logic here when it belongs in `second-mile/`.

## Module Layout

```text
src/main/java/serp/project/first_mile/
  ui/controller/          # REST APIs — keep thin
  service/                # Interfaces
  service/impl/           # Business logic (@Transactional here)
  service/handler/        # Kafka DLQ handlers
  repository/             # Spring Data JPA
  repository/specification/
  domain/                 # JPA entities
  dto/request|response/   # API contracts
  mapper/                 # Entity <-> DTO
  enums/
  exception/              # AppException, ErrorCode, GlobalExceptionHandler
  kernel/config|utils/    # Security, async, AuthUtils, Excel helpers
  kafka/                  # Consumers/producers
  caller/                 # External HTTP (maps, payment)
src/main/resources/
  db/migration/           # SQL schema scripts
  application.yaml        # Registers schema-locations + app config
  i18n/messages*.properties
src/test/java/            # JUnit 5 unit tests (preferred for services)
```

**Layering:** `controller` → `service` → `repository`. Controllers must not call repositories directly.

## Build, Run, and Test

Run from `first-mile/`. On Windows use `mvnw.cmd`.

```bash
./mvnw spring-boot:run
./mvnw clean compile
./mvnw test
./mvnw -Dtest=PostOfficeServiceImplTest test
./mvnw -Dtest=OrderServiceImplTest#methodName test
./mvnw -DskipTests clean package
```

- No `run-dev.sh` in this module; set env vars (`DB_URL`, `KEYCLOAK_URL`, `KAFKA_BOOTSTRAP_SERVERS`, etc.) before `spring-boot:run`.
- Quality gate: `clean compile` + focused `test` for touched services.
- Route API calls through **api_gateway** (`http://localhost:8080/first-mile/api/v1/...`) when testing end-to-end with the web app.

## API Conventions

- Base path in service: `/api/v1/{resource}`.
- Query params: **snake_case** (`province_code`, `post_office_id`, `created_from`).
- Request bodies: camelCase JSON fields matching `dto/request/*`.
- Pagination: `page` (0-based), `size` (default 20).
- Responses:
  - Many endpoints wrap data in `ApiResponse<T>` (`message` + `result`).
  - Some list endpoints return `PageResponse<T>` directly (e.g. parts of `OrderController`).
  - **Match the nearest controller** in the same resource — do not introduce a new response shape without reason.
- User-facing API messages come from `MessageService` + `i18n/messages*.properties` (keys like `success.post_offices.list`).
- Business failures: `throw new AppException(ErrorCode.XXX)` or with detail; `GlobalExceptionHandler` maps to HTTP responses.

## Security and Multi-Tenancy

- Resolve tenant via `AuthUtils.getCurrentTenantId()` in services that need scoping.
- Common roles:
  - `TMS_ADMIN` — full TMS admin
  - `TMS_POSTOFFICER_MANAGER` — post office manager
  - `TMS_POSTOFFICER` — courier
  - `TMS_CUSTOMER` — customer-facing order APIs
- Add or tighten `@PreAuthorize` on new endpoints; mirror checks in service layer when data must be scoped by post office or user.
- Reuse `AuthUtils` / access helpers in `kernel/utils` instead of duplicating role parsing.

## Persistence and Schema Changes

- `spring.jpa.hibernate.ddl-auto: none` — never rely on Hibernate auto-DDL.
- New tables/columns: add SQL under `src/main/resources/db/migration/{name}.sql`.
- **Register new scripts** in `application.yaml` → `spring.sql.init.schema-locations` (keep dependency order; prefix with `zzz_` if a script must run last).
- `DB_INIT_MODE` defaults to `never`; fresh DBs need `always`/`embedded` once or manual SQL apply.
- Use `*Specification` classes for complex filters; keep ad-hoc queries in repositories, not controllers.
- `@Transactional` on write methods in `service/impl`; `@Transactional(readOnly = true)` for heavy reads.

## Kafka and Side Effects

- Consumers live under `kafka/`; DLQ retry via `service/handler/*DlqHandler`.
- Hub/post office sync events may be produced/consumed — coordinate topic names in `application.yaml` `app.kafka.topics`.
- On failure paths, prefer existing DLQ patterns over silent drops.

## Extensibility Guidelines

- **New feature:** interface in `service/`, impl in `service/impl/`, DTOs in `dto/`, controller method + `@PreAuthorize`, repository method or specification, types in frontend `serp_web` module.
- **Large service classes:** extract private helpers or sub-services (e.g. timeline, import) instead of growing a 1500+ line god class.
- **Enums** for statuses and types; avoid magic strings in DB and API.
- **Mappers** for entity ↔ response; do not expose JPA entities from controllers.
- Add a **focused unit test** in `src/test/java/.../service/impl/` when fixing non-trivial bugs.

## Code Style

### File headers (required for TMS)

- **Author must be `Nguyen The Anh`** on all new or touched Java/SQL files in this module. Do **not** use `QuanTuanHuy` from the root `AGENTS.md` example.
- When editing a file that already has a header, **keep** `Author: Nguyen The Anh`; do not rewrite authorship.
- New Java files — match neighboring block comment style:

```text
/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/
```

- New SQL under `db/migration/`: `-- Author: Nguyen The Anh` when the folder uses author comments.
- Lombok: `@RequiredArgsConstructor`, `@Slf4j`, `@Builder` on DTOs where already used.
- Imports: `jakarta.*`, Lombok, Spring, then `serp.project.first_mile.*`.
- 4-space indent; parameterized logging `log.info("orderId={}", id)`.
- Comments only for non-obvious business rules (pickup windows, optimization constraints, status transitions).

## Debugging Checklist

1. Confirm JWT roles match `@PreAuthorize` on the endpoint.
2. Confirm `tenantId` and post office filters in service layer.
3. Check gateway path: `/first-mile/api/v1/...` not bare `/api/v1/...` from browser.
4. For empty lists: verify specification filters and `DB_INIT_MODE`/migrations applied.
5. For Kafka issues: consumer group, topic name, and DLQ table rows.

## Before You Finish

- Run `./mvnw clean compile` and at least one relevant test class/method.
- If you added SQL, update `application.yaml` `schema-locations` when using init mode.
- Keep response contracts stable for `serp_web` transforms (`unwrapFirstMileResult`, `unwrapFirstMilePageResult`).
- Never commit `.env` or secrets.
