# AGENTS.md - PM Core Guide for Coding Agents

This file is for coding agents working inside `pm_core`.
`pm_core` is a Java 21 / Spring Boot 3.5 service for project management domain logic.
It follows layered boundaries across `ui`, `application`, `domain`, `infrastructure`, and `kernel`.
Prefer this guide over generic repo advice when local code shows a clearer pattern.

## Module Layout
- `src/main/java/serp/project/pmcore/ui` - REST controllers, Kafka consumers, exception handling.
- `src/main/java/serp/project/pmcore/application` - commands, queries, validators, orchestration.
- `src/main/java/serp/project/pmcore/domain` - entities, DTOs, enums, exceptions, ports, services.
- `src/main/java/serp/project/pmcore/infrastructure/store` - JPA models, repositories, adapters, mappers, query builders.
- `src/main/java/serp/project/pmcore/kernel` - config, properties, and utilities.
- `src/main/resources/db/migration` - Flyway migrations.
- `src/test/java` - JUnit 5 + Mockito tests mirroring main packages.

## Build, Run, Test, and Checks
Use `./run-dev.sh` for local dev because it loads `.env` before Spring Boot starts.
Use `./mvnw.cmd ...` instead of `./mvnw ...` from plain Windows CMD or PowerShell.
```bash
cd pm_core

# start the app with .env loaded
./run-dev.sh

# start the app directly
./mvnw spring-boot:run

# run all tests
./mvnw test

# clean + run all tests
./mvnw clean test

# run one test class
./mvnw -Dtest=CreateWorkItemCommandTest test

# run one test method
./mvnw -Dtest=CreateWorkItemCommandTest#executeShouldPersistDefaultCustomFieldValue test

# build the jar
./mvnw clean package

# build without tests
./mvnw -DskipTests clean package
```
- `pom.xml` currently has no Checkstyle, Spotless, PMD, JaCoCo, or Failsafe config.
- There is no dedicated lint command; `./mvnw test` is the built-in quality gate.
- For a narrow change, run the most relevant single test first.
- Put schema changes in Flyway migrations even though `application.yaml` enables `ddl-auto: update`.

## Architecture Rules
- Keep controllers thin: get auth context, validate, call a command/query, wrap with `ResponseUtils`.
- Put write orchestration in `application.command.*` and read orchestration in `application.query.*`.
- Keep reusable business logic in domain services, not in controllers or adapters.
- Domain services depend on ports such as `IProjectPort`, not Spring Data repositories.
- Adapters in `infrastructure.store.adapter` implement ports and delegate to repositories + mappers.
- Repositories stay infrastructure-only and should remain tenant-aware.
- Mappers translate between domain entities using epoch millis and JPA models using Java time types.
- Kafka side effects follow the outbox pattern; do not publish directly from controllers.

## Code Style
### File headers and formatting
- Most Java source files start with this header; add it to new source files in this module:
```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
```
- Keep the package declaration after the header with one blank line.
- Prefer 4-space indentation in new or edited blocks.
- If a file already uses tabs or mixed indentation, match nearby style instead of reformatting the whole file.
- Split long builder chains one call per line.
- Add comments only for non-obvious tenancy, workflow, or validation logic.
- Use parameterized SLF4J logging.

### Imports
- Match the surrounding file instead of reordering unrelated imports across the whole file.
- The common local order is: `jakarta` / Lombok / Spring or other third-party, then `serp.project.pmcore...`, then `java.*`.
- In tests, place static imports after regular imports.
- Avoid wildcard imports unless the file already uses them and changing them would just create noise.

### Types and data modeling
- Use `Long` for IDs and nullable numeric fields.
- Domain entities commonly store timestamps and date-like values as epoch millis in `Long` fields.
- JPA models commonly store timestamps as `LocalDateTime` and sometimes `LocalDate`.
- Do time conversion in mappers via `BaseMapper`; do not scatter it through controllers or repositories.
- Use `Optional<T>` for nullable lookups from ports and repositories.
- Unwrap optionals at service or command boundaries with `orElseThrow(...)` and a domain-specific exception.
- Use records for small immutable computed types such as field policies and resolved configurations.
- Use Lombok heavily for DTOs, entities, and models; `@RequiredArgsConstructor` and `@SuperBuilder` are common.
- Prefer `List.of`, `Set.of`, and `Map.of` for immutable defaults.
- Use `ArrayList`, `LinkedHashMap`, or `LinkedHashSet` when mutation or stable ordering is required.
- Preserve `tenantId` in method signatures and queries; do not bypass tenant scoping.

### Naming conventions
- Classes, enums, DTOs, and records use `PascalCase`.
- Interfaces use an `I` prefix in this module: `IProjectService`, `IProjectPort`, `IProjectRepository`.
- Commands end with `Command`; queries end with `Query`; validators end with `Validator`.
- Persistence adapters end with `Adapter`; mappers end with `Mapper`; JPA persistence types end with `Model`.
- Domain objects end with `Entity`; request/response DTOs end with `Request` and `Response`.
- Constants use `UPPER_SNAKE_CASE`; package names stay lowercase.
- Test classes end with `Test`; prefer descriptive method names like `executeShouldRejectAmbiguousCustomFieldContext`.

### Dependency injection and layering
- Prefer constructor injection.
- Use `@RequiredArgsConstructor` when it keeps the class readable.
- Use an explicit constructor when a class has many collaborators and grouping them improves clarity.
- Controllers should depend on commands, queries, services, and utilities, not repositories.
- Application commands and queries may coordinate multiple domain services and ports.
- Domain services should not depend on controller DTOs.

### Error handling and validation
- Throw domain-specific exceptions for business failures; avoid raw `RuntimeException` for normal error paths.
- Use `ResourceNotFoundException` for missing data.
- Use `BusinessRuleViolationException` for invalid state, conflicts, or permission failures.
- Use `DomainValidationException` when you need structured violations.
- Use `AccessDeniedException` when auth context like user or tenant cannot be resolved.
- Add new error codes to `DomainErrorCode`.
- If a new business error needs a different HTTP status, update `ui/rest/exception/GlobalExceptionHandler.java`.
- Use Jakarta validation annotations on request DTOs and annotate controller bodies with `@Valid`.
- Prefer fail-fast checks and early returns.

### Persistence, transactions, and messaging
- Put `@Transactional(rollbackFor = Exception.class)` on write commands that change persistent state.
- Use `@Transactional(readOnly = true)` for read queries when appropriate.
- Respect soft-delete conventions such as `deletedAt` and `@SQLRestriction("deleted_at IS NULL")`.
- Keep repository method names tenant-aware, e.g. `findByIdAndTenantId(...)`.
- When a use case writes data and emits Kafka side effects, persist an outbox event in the same transaction.
- Let `OutboxPollingPublisher` publish later; do not bypass the outbox flow.
- Keep Kafka consumers inside `AbstractKafkaConsumerTemplate` so ack happens after commit.
- New schema changes belong in `src/main/resources/db/migration/V{N}__description.sql`.

## Testing Conventions
- Use JUnit 5 with Mockito for unit tests.
- Most tests use `@ExtendWith(MockitoExtension.class)`.
- Use `@MockitoSettings(strictness = Strictness.LENIENT)` only when reusable setup makes strict stubbing impractical.
- Mirror the main package structure under `src/test/java`.
- Prefer builder-based fixtures and helper methods for setup.
- Assert both the exception type and `DomainErrorCode` for failure scenarios.
- Verify important side effects with Mockito `verify(...)`.
- Keep `@SpringBootTest` usage minimal; most new tests should remain unit tests.

## Module-specific gotchas
- `AuthUtils` reads JWT claims `uid`, `tid`, and `groups`; controllers usually translate missing values into `AccessDeniedException`.
- REST responses are wrapped in `GeneralResponse<?>` via `ResponseUtils`; preserve that response shape.
- `PmcoreApplication` enables scheduling; outbox polling and cleanup run on schedules.
- Paginated searches commonly return `org.springframework.data.util.Pair<List<T>, Long>`.
- Many services set audit fields with `System.currentTimeMillis()` before saving.
- Query builders such as `WorkItemQueryBuilder` maintain allowlists for sortable columns; extend those cautiously.

## Before You Finish
- Run at least the most relevant single test for the code you changed.
- If the change is broad or cross-cutting, run `./mvnw test`.
- Keep imports, formatting, and line wrapping consistent with the touched file.
- Do not replace the entity-model mapper layer with direct JPA exposure.
