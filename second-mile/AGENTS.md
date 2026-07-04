# AGENTS.md - Second-Mile (TMS) Backend Guide for Coding Agents

This guide is for coding agents working inside `second-mile/` (middle leg: hubs, bags, routes, hub vehicles).
Use it together with the repository-root `AGENTS.md`. For first-mile (post office pickup), see `first-mile/AGENTS.md`.
For TMS UI work, see `serp_web/src/modules/first-mile/AGENTS.md`.

## Service Snapshot

- **Module:** `second-mile` — Spring Boot, Java 21, package `serp.project.second_mile`.
- **Default port:** `8102` (`SERVER_PORT`).
- **Database:** PostgreSQL `second-mile` (`DB_URL`).
- **Auth:** OAuth2 resource server (Keycloak JWT), `@PreAuthorize` where enforced.
- **Integrations:** Kafka (order sync, user sync, hub–post office sync, DLQ), Goong geocode/distance matrix, S3 storage.
- **Gateway path:** `/second-mile/api/v1/*` (proxied by `api_gateway`).

## Domain Scope (Second-Mile)

| Area | Examples |
|------|----------|
| Network | Hubs, hub staff, hub–post office mappings |
| Transport | Routes (fixed lines), vehicles (hub/driver scoped) |
| Handling | Bags, bag orders, order sync from first-mile |
| Platform | Import history, Kafka DLQ, file storage |

Post office pickup, courier trips, and first-mile order creation belong in `first-mile/`, not here.

## Module Layout

```text
src/main/java/serp/project/second_mile/
  ui/controller/
  service/ + service/impl/
  service/handler/        # DLQ handlers
  repository/ + repository/specification/
  domain/
  dto/request|response/
  enums/
  exception/
  kernel/config|utils/    # AuthUtils, SecondMileAccessUtils, Excel helpers
  kafka/
  caller/
src/main/resources/
  db.migration/           # Note: folder name uses a dot (db.migration)
  application.yaml
  i18n/messages.properties (+ messages_en, messages_vi for API messages)
  excel/                  # Import templates
```

**Layering:** `controller` → `service` → `repository`. Keep controllers thin.

## Build, Run, and Test

Run from `second-mile/`. On Windows use `mvnw.cmd`.

```bash
./mvnw spring-boot:run
./mvnw clean compile
./mvnw test
./mvnw -DskipTests clean package
```

- Set `DB_URL`, `KEYCLOAK_URL`, `KAFKA_BOOTSTRAP_SERVERS`, storage keys as needed.
- End-to-end with UI: gateway URL `http://localhost:8080/second-mile/api/v1/...`.

## API Conventions

- Base path: `/api/v1/{resource}`.
- Query params: **snake_case** (`hub_id`, `license_plate`, `vehicle_type`).
- Responses: typically `ApiResponse<T>` with `message` + `result`; paginated lists use `ApiResponse<PageResponse<T>>`.
- Use `MessageService.getMessage("success....")` for consistent API messages.
- Errors: `AppException` + `ErrorCode`; handled by `GlobalExceptionHandler`.

**Frontend contract:** RTK Query uses `extraOptions: { service: 'second-mile' }` and helpers in `serp_web/.../api/transforms.ts`. Some endpoints (e.g. hub import history) return raw shapes — check existing transforms before adding endpoints.

## Security and Access

- Use `AuthUtils` for tenant/user context.
- Hub-scoped operations may use `SecondMileAccessUtils` — follow existing service patterns.
- When adding endpoints, align `@PreAuthorize` with first-mile TMS roles where the feature is shared (`TMS_ADMIN`, etc.).
- Internal service calls use API key auth, not service bearer tokens or forwarded JWTs. Callers must send `X-Internal-Api-Key`, `X-Tenant-Id`, and `X-Internal-Service`, including from normal request handlers, Kafka consumers, DLQ retry, outbox retry, and scheduled jobs. Internal endpoints should be `permitAll` in `SecurityConfig` to avoid JWT requirements, while `InternalApiAuthenticationFilter` enforces the API key before controller logic.
- Receiving services authenticate internal calls through `InternalApiAuthenticationFilter`; service code should still use `AuthUtils` for tenant/role context. Configure `INTERNAL_API_KEY` consistently across `first-mile`, `second-mile`, and `tms-order`; never commit its value.

## Persistence and Schema Changes

- `ddl-auto: none`.
- Add SQL files under `src/main/resources/db.migration/`.
- Use filename ordering (`zzz_` prefix) when scripts must run after others (see `zzz_routes.sql`, `zz_bag_orders.sql`).
- This module does **not** mirror first-mile’s `spring.sql.init.schema-locations` list in `application.yaml`; coordinate DB apply with your environment (manual migration, init job, or future init config). Document new scripts in PR notes.
- Prefer JPA specifications for list filters.

## Kafka and Cross-Service Sync

- Topics configured under `app.kafka.topics` (`sync-order`, `sync-user`, `sync-hub-post-office`).
- Consumers: `kafka/consumer/*`; failures → DLQ handlers in `service/handler/`.
- Hub/post office mapping sync interacts with first-mile — read `HUB_POST_OFFICE_FEATURE.md` when touching mappings.
- Do not break idempotency on sync consumers without explicit design.

## Extensibility Guidelines

- New resource: `*Service` + `*ServiceImpl`, DTOs, controller, repository, enum types.
- Keep hub/bag/route concerns separated; shared logic goes in `kernel/utils` only if truly cross-cutting.
- Excel import: follow `VehicleService` / hub import patterns (validate → import history → async processing).
- Add unit tests under `src/test/java` when fixing regression-prone service logic.

## Code Style

### File headers (required for TMS)

- **Author must be `Nguyen The Anh`** on all new or touched Java/SQL files in this module. Do **not** use `QuanTuanHuy` from the root `AGENTS.md` example.
- When editing a file that already has a header, **keep** `Author: Nguyen The Anh`; do not rewrite authorship.
- New Java files:

```text
/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/
```

- New SQL under `db.migration/`: `-- Author: Nguyen The Anh` when sibling scripts use author comments.
- `@RequiredArgsConstructor`, `@Slf4j` on controllers/services.
- 4-space indent; early returns in services.
- API message keys in `i18n/messages.properties` (English default); Vietnamese in `messages_vi.properties` if the product team maintains them — **web UI stays English** (see frontend AGENTS.md).

## Debugging Checklist

1. Gateway prefix `/second-mile/api/v1` (not `/first-mile/...`).
2. Kafka consumer group and topic names in `application.yaml`.
3. Hub ID / mapping filters for “empty list” bugs.
4. DLQ table and handler logs for failed sync events.
5. Response wrapper: ensure frontend `unwrap*` helpers match your controller return type.

## Before You Finish

- `./mvnw clean compile` and tests if present for touched code.
- Document new `db.migration/*.sql` and how to apply them locally.
- Update `serp_web` types and `firstMileApi` endpoints (second-mile section) when exposing new APIs.
- Never commit credentials or `.env` files.
