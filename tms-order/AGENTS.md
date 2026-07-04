# AGENTS.md - TMS Order Backend Guide for Coding Agents

This guide is for coding agents working inside `tms-order/` (TMS order management).
Use it together with the repository-root `AGENTS.md`. For first-mile pickup operations, see `first-mile/AGENTS.md`; for second-mile hub/linehaul operations, see `second-mile/AGENTS.md`; for shipping fee rules, see `tms-billing-service/AGENTS.md`.

## Service Snapshot

- **Module:** `tms-order` - Spring Boot 3.5, Java 21, package `serp.project.tms_order`.
- **Purpose:** canonical TMS order management service for order lifecycle, order identity, customer-facing order state, and cross-leg order status coordination.
- **Default port:** `8105` (`SERVER_PORT`).
- **Database:** PostgreSQL `tms-order` (`DB_URL`).
- **Auth:** OAuth2 resource server (Keycloak JWT), with method security enabled.
- **Integrations:** Kafka order/handover/delivery-status topics, DLQ retry config, Goong geocode/distance matrix config, S3-compatible storage.
- **Gateway path:** `/tms-order/api/v1/*` (proxied by `api_gateway`) when calling from browser or frontend.

## Domain Scope

| Area | Examples |
|------|----------|
| Order core | Order creation, order detail, sender/receiver data, customer order code, order status |
| Lifecycle | Status transitions, cancellation, confirmation, delivery status updates |
| Coordination | Events consumed from first-mile, second-mile, last-mile, handover manifests, delivery status |
| Location data | Provinces, wards, geocoding for order addresses |
| Platform | Import history, file storage, Kafka DLQ handling |

Keep leg-specific operations out of this module:

- Pickup trips, courier check-in, post-office vehicle assignment belong in `first-mile/`.
- Hubs, bags, hub routes, and linehaul handling belong in `second-mile/`.
- Tariffs, surcharges, VAS, and shipping-fee calculation belong in `tms-billing-service/`.

## Module Layout

Current scaffold:

```text
src/main/java/serp/project/tms_order/
  caller/                 # External HTTP callers such as geocode
  domain/                 # JPA entities
  dto/                    # Shared API response wrappers
  enums/
  exception/              # AppException, ErrorCode, GlobalExceptionHandler, MessageService
  kernel/config/          # Security, locale, async, JPA auditing, MVC config
  kernel/interceptor/
  kernel/ratelimit/
  kernel/utils/           # AuthUtils and shared helpers
src/main/resources/
  db.migration/           # SQL scripts
  i18n/messages*.properties
  application.yaml
src/test/java/
```

When adding order functionality, use the normal backend layers:

```text
ui/controller/          # REST APIs - keep thin
service/                # Interfaces
service/impl/           # Business logic and transactions
repository/             # Spring Data JPA
repository/specification/
dto/request|response/   # API contracts
mapper/                 # Entity <-> DTO
kafka/                  # Consumers/producers for sync events
```

Layering: `controller` -> `service` -> `repository`. Controllers must not call repositories directly.

## Build, Run, and Test

Run from `tms-order/`. On Windows use `mvnw.cmd`.

```bash
./mvnw spring-boot:run
./mvnw clean compile
./mvnw test
./mvnw -Dtest=TmsOrderApplicationTests test
./mvnw -DskipTests clean package
```

- No `run-dev.sh` is currently checked in; set env vars (`DB_URL`, `KEYCLOAK_URL`, `KAFKA_BOOTSTRAP_SERVERS`, `GOONG_API_KEY`, storage keys) before `spring-boot:run`.
- Quality gate: `clean compile` plus focused tests for touched services.
- Route authenticated browser/frontend API calls through gateway: `http://localhost:8080/tms-order/api/v1/...`.

## API Conventions

- Base path in service: `/api/v1/{resource}`.
- Query params: **snake_case** (`order_code`, `customer_order_code`, `created_from`, `status`).
- Request bodies: camelCase JSON fields matching `dto/request/*`.
- Pagination: `page` is 0-based; `size` defaults should match nearby controllers.
- Responses should use the existing `ApiResponse<T>` / `PageResponse<T>` wrappers unless a nearby controller has a different established contract.
- User-facing messages should come from `MessageService` and `i18n/messages*.properties`.
- Business failures should throw `AppException(ErrorCode.XXX)`; `GlobalExceptionHandler` maps errors to `ApiResponse<Void>`.

## Security and Multi-Tenancy

- Resolve user and tenant context with `AuthUtils`; do not parse JWT claims manually in services.
- Keep order data tenant-scoped. Any read/write by ID should verify tenant ownership before returning or mutating data.
- Common TMS roles to align with sibling modules:
  - `TMS_ADMIN`
  - `TMS_POSTOFFICER_MANAGER`
  - `TMS_POSTOFFICER`
  - `TMS_CUSTOMER`
- Add `@PreAuthorize` on new endpoints when authorization is role-dependent. Mirror critical scoping in the service layer.
- Internal webhook-style endpoints may be public only when explicitly configured in `SecurityConfig`; keep that list narrow.
- Internal service calls use API key auth, not service bearer tokens or forwarded JWTs. Callers must send `X-Internal-Api-Key`, `X-Tenant-Id`, and `X-Internal-Service`, including from normal request handlers, Kafka consumers, DLQ retry, outbox retry, and scheduled jobs. Internal endpoints should be `permitAll` in `SecurityConfig` to avoid JWT requirements, while `InternalApiAuthenticationFilter` enforces the API key before controller logic.
- Receiving services authenticate internal calls through `InternalApiAuthenticationFilter`; service code should still use `AuthUtils` for tenant/role context. Configure `INTERNAL_API_KEY` consistently across `first-mile`, `second-mile`, and `tms-order`; never commit its value.

## Persistence and Schema Changes

- `spring.jpa.hibernate.ddl-auto: none` - never rely on Hibernate auto-DDL.
- Add SQL scripts under `src/main/resources/db.migration/`.
- `application.yaml` currently has `spring.sql.init.schema-locations: []`; if you expect init-mode execution, register scripts there in dependency order or document manual apply steps.
- Use specifications for complex list filters.
- Put `@Transactional` on write methods in `service/impl`; use `@Transactional(readOnly = true)` for read-heavy paths.
- Do not expose JPA entities from controllers; map to response DTOs.

## Kafka and Cross-Service Sync

- Topic names are configured in `application.yaml` under `app.kafka.topics`.
- Preserve idempotency in consumers; order status events may be duplicated or replayed.
- Persist failed event handling through the existing DLQ model/pattern instead of silently dropping messages.
- Keep status transition logic centralized so first-mile, second-mile, and delivery-status events cannot create conflicting order states.

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

- New SQL under `db.migration/`: `-- Author: Nguyen The Anh` when sibling scripts use author comments.
- Use Lombok patterns already present in sibling TMS modules (`@RequiredArgsConstructor`, `@Slf4j`, DTO builders where useful).
- Imports: `jakarta.*`, Lombok, Spring/third-party, then `serp.project.tms_order.*`, then JDK if the local file follows that style.
- 4-space indent; parameterized logging (`log.info("orderId={}", orderId)`).
- Comments only for non-obvious order lifecycle rules, idempotency guards, or cross-service status transitions.

## Debugging Checklist

1. Confirm gateway prefix `/tms-order/api/v1`, not bare `/api/v1`, when testing through the web app.
2. Confirm JWT has `uid`, `tid`, and expected TMS roles before debugging service logic.
3. Check tenant ownership filters for empty lists or 404-by-ID behavior.
4. Check Kafka topic names, consumer groups, and DLQ rows for missed sync events.
5. Verify DB scripts were applied; `DB_INIT_MODE` defaults to `never`.

## Before You Finish

- Run `./mvnw clean compile` and relevant focused tests.
- Add or update tests for non-trivial order lifecycle/status transition behavior.
- Keep `api_gateway` service mapping and `serp_web` API types/transforms in sync when API contracts change.
- Never commit `.env` files, secrets, or local credentials.
