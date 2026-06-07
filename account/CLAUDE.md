# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Account service

Java 21 / Spring Boot service for auth, users, organizations, roles, permissions, and subscriptions. Integrates PostgreSQL + Flyway, Redis, Kafka, and Keycloak.

Entrypoint: `src/main/java/serp/project/account/AccountApplication.java`.

## Commands

Run from `account/`. Use `mvnw.cmd` instead of `./mvnw` in Windows CMD/PowerShell.

```bash
./run-dev.sh
./run.sh
./mvnw spring-boot:run
./mvnw clean compile
./mvnw test
./mvnw -Dtest=RoleEnumUtilsTest test
./mvnw -Dtest=RoleEnumUtilsTest#testGetSystemRoles test
./mvnw -Dtest=RoleEnumUtilsTest,AccountApplicationTests test
./mvnw clean package
./mvnw -DskipTests clean package
```

No dedicated lint plugin exists. Use `./mvnw clean compile` and `./mvnw test` as quality gates.

## Architecture

- `ui/controller`: public REST APIs and global exception handling.
- `ui/controller/internal`: internal service APIs.
- `core/usecase`: orchestration/use-case flows.
- `core/service` and `core/port`: business services and port contracts.
- `core/domain`: entities, DTOs, constants, enums, events.
- `infrastructure/store`: adapters, repositories, models, mappers, specs.
- `infrastructure/client`: Kafka, Keycloak, Redis adapters.
- `kernel`: config, typed properties, utilities.
- `src/main/resources/db/migration`: Flyway SQL migrations.

Layering: controllers -> use cases -> services -> ports -> infrastructure adapters. Controllers must not call repositories directly. Core depends on port interfaces, not Spring Data repositories. Persist outbox records in the same transaction as data changes; scheduler publishes later.

## Conventions

- New Java files use the standard author header from nearby files.
- Use constructor injection, usually Lombok `@RequiredArgsConstructor`.
- Build responses with `ResponseUtils` and preserve `GeneralResponse<?>` API shape.
- Use `AppException` for business failures and `Constants.ErrorMessage.*` for reusable messages.
- Use `@Transactional(rollbackFor = Exception.class)` for writes and `@Transactional(readOnly = true)` for reads.
- Repositories live under `infrastructure/store/repository` and extend `IBaseRepository<T>`.
- Keep domain/entity to persistence/model conversion in mapper classes.
- Use `AuthUtils` for `uid`, `tid`, email, and role claims.
- Public APIs are under `/api/**`; internal APIs are under `/internal/**` and require `SERP_SERVICES` role.
- Add Flyway migrations as `V{N}__description.sql` for schema changes.

## Testing

Tests use JUnit 5 via `spring-boot-starter-test`. Prefer focused unit tests for service/usecase/utility behavior. Use `@SpringBootTest` only for true integration/wiring scenarios. Run at least a relevant single class or method before handoff; run `./mvnw test` for cross-layer changes.
