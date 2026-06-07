# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## PM core service

Java 21 / Spring Boot 3.5 service for project-management domain logic. Preserves Clean Architecture boundaries, Kafka-first async integration, Keycloak/JWT auth baseline, and API-Gateway-first authenticated routing.

## Commands

Run from `pm_core/`. Use `mvnw.cmd` instead of `./mvnw` in Windows CMD/PowerShell.

```bash
./run-dev.sh
./mvnw spring-boot:run
./mvnw clean compile
./mvnw test
./mvnw clean test
./mvnw -Dtest=CreateWorkItemCommandHandlerTest test
./mvnw -Dtest=CreateWorkItemCommandHandlerTest#executeShouldPersistDefaultCustomFieldValue test
./mvnw -Dtest='*WorkItem*Test' test
./mvnw clean package
./mvnw -DskipTests clean package
```

No Checkstyle, Spotless, PMD, JaCoCo, or Failsafe plugin config exists. Use `./mvnw clean compile` as fast quality gate and `./mvnw test` as behavioral gate.

## Architecture

- `src/main/java/serp/project/pmcore/ui`: REST controllers, Kafka consumers, exception mapping.
- `src/main/java/serp/project/pmcore/application`: command/query handlers and orchestration.
- `src/main/java/serp/project/pmcore/domain`: entities, services, ports, validators, exceptions.
- `src/main/java/serp/project/pmcore/infrastructure`: JPA models/repositories, adapters, mappers, clients.
- `src/main/java/serp/project/pmcore/kernel`: config, properties, utility helpers.
- `src/main/resources/db/migration`: Flyway migrations.
- `src/test/java`: JUnit 5 + Mockito tests.

Maintain boundaries: `ui -> application -> domain -> infrastructure`. Controllers resolve auth, validate input, delegate to a command/query handler, and return `GeneralResponse<?>` via `ResponseUtils`. Domain services depend on domain ports, not Spring Data repositories. Infrastructure adapters translate between domain entities and persistence models.

## Conventions

- New Java files use the standard author header from nearby files.
- Prefer constructor injection, usually `@RequiredArgsConstructor`.
- Put write orchestration in `application.*.command.*` and read orchestration in `application.*.query.*`.
- Keep business rules in domain services/validators, not controllers/adapters.
- Do not leak JPA models into domain or API contracts.
- Use `Long` for IDs and nullable numeric values.
- Keep time conversions in mappers, not controllers.
- Use `Optional<T>` for nullable read lookups and unwrap with domain-specific exceptions at service/handler boundaries.
- Use `@Transactional(rollbackFor = Exception.class)` for write commands/use cases and `@Transactional(readOnly = true)` for read query handlers.
- Keep repository access tenant-aware and respect soft-delete constraints.
- Add Flyway migrations for schema changes; `spring.jpa.hibernate.ddl-auto` is `validate`.
- Persist outbox records in the same transaction for write flows that emit events; outbox publisher handles Kafka publishing.
- Resolve auth context through `AuthUtils` (`uid`, `tid`, `groups`) and never bypass tenant scoping.

## Testing

Tests use JUnit 5 + Mockito. Prefer focused unit tests for application/domain logic; keep `@SpringBootTest` minimal. Test names should be behavior-driven, e.g. `executeShouldRejectAmbiguousCustomFieldContext`. For business failures, assert exception type and `DomainErrorCode`. Verify critical side effects with Mockito `verify(...)` and captors when needed.
