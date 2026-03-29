# AGENTS.md - PM Core Guide for Coding Agents

This guide is for coding agents working inside `pm_core/`.
`pm_core` is a Java 21 / Spring Boot 3.5 service for project management domain logic.
Use this file as the local source of truth, then apply the repo root `AGENTS.md` for shared rules.

## Rule Sources and Precedence
- Primary for this module: `pm_core/AGENTS.md`.
- Secondary shared guidance: repo root `AGENTS.md`.
- Preserve Copilot architecture constraints: Clean Architecture boundaries, Kafka-first async integration, Keycloak/JWT auth baseline, and API-Gateway-first authenticated routing.

## Module Layout
- `src/main/java/serp/project/pmcore/ui` - REST controllers, Kafka consumers, exception mapping.
- `src/main/java/serp/project/pmcore/application` - command/query handlers and orchestration.
- `src/main/java/serp/project/pmcore/domain` - entities, services, ports, validators, exceptions.
- `src/main/java/serp/project/pmcore/infrastructure` - JPA models/repositories, adapters, mappers, clients.
- `src/main/java/serp/project/pmcore/kernel` - config, properties, utility helpers.
- `src/main/resources/db/migration` - Flyway SQL migrations (`V{N}__description.sql`).
- `src/test/java` - JUnit 5 + Mockito tests, mostly package-mirrored unit tests.

## Build, Run, Test, and Quality Commands
Run all commands from `pm_core/`.
On Windows CMD/PowerShell prefer `./mvnw.cmd ...`; on Bash use `./mvnw ...`.

```bash
# Run app with .env loaded (preferred dev path)
./run-dev.sh

# Run app directly
./mvnw spring-boot:run

# Compile-only sanity check
./mvnw clean compile

# Run full tests
./mvnw test

# Clean + full tests
./mvnw clean test

# Run one test class
./mvnw -Dtest=CreateWorkItemCommandHandlerTest test

# Run one test method
./mvnw -Dtest=CreateWorkItemCommandHandlerTest#executeShouldPersistDefaultCustomFieldValue test

# Run multiple tests by class pattern
./mvnw -Dtest='*WorkItem*Test' test

# Build artifact
./mvnw clean package

# Build without tests (only when needed)
./mvnw -DskipTests clean package
```

## Linting and Static Checks
- `pom.xml` currently has no dedicated lint/static plugin setup (no Checkstyle, Spotless, PMD, JaCoCo, or Failsafe plugin config).
- Use `./mvnw clean compile` as the fast code-quality gate.
- Use `./mvnw test` as the behavioral quality gate.
- For narrow changes, run one focused test first, then expand scope only if needed.

## Testing Conventions
- Testing stack: JUnit 5 + Mockito (`spring-boot-starter-test`, `spring-kafka-test`).
- Typical unit-test annotation: `@ExtendWith(MockitoExtension.class)`.
- Use `@MockitoSettings(strictness = Strictness.LENIENT)` only when strict stubbing causes significant setup noise.
- Keep `@SpringBootTest` usage minimal; prefer focused unit tests for application/domain logic.
- Test naming should be behavior-driven (for example `executeShouldRejectAmbiguousCustomFieldContext`).
- For business failures, assert exception type and `DomainErrorCode`.
- Verify critical side effects with Mockito `verify(...)` and captors when needed.

## Architecture and Layering
- Maintain clean boundaries: `ui -> application -> domain -> infrastructure`.
- Keep controllers thin: resolve auth, validate input, delegate to a single command/query handler, return `GeneralResponse<?>` via `ResponseUtils`.
- Put write orchestration in `application.*.command.*` and read orchestration in `application.*.query.*`.
- Keep business rules in domain services/validators, not in controllers/adapters.
- Domain services should depend on domain ports, not Spring Data repositories.
- Infrastructure adapters implement ports and translate between domain entities and persistence models.
- Do not leak JPA models into domain or API contracts.

## Formatting and File Style
- Most Java files in this module use this header; add it for new source files:
```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
```
- Keep package declaration immediately after the header with a blank line separator.
- Use 4-space indentation for new/modified blocks.
- Match the touched file's local formatting; avoid unrelated whole-file reformatting.
- Keep long builder chains readable (usually one chained call per line).
- Use parameterized SLF4J logging; avoid string concatenation in log statements.
- Add comments only for non-obvious business rules (tenant, security, workflow edge cases).

## Imports, Types, and Naming
- Follow existing import ordering in the touched file; common local order is `jakarta`, Lombok, Spring/third-party, `serp.project.pmcore...`, then `java.*`.
- In tests, keep static imports grouped after regular imports.
- Avoid wildcard imports unless they are already established in the file.
- Use `Long` for IDs and nullable numeric values.
- Domain entities commonly store timestamps/dates as epoch millis (`Long`).
- Persistence models commonly use `LocalDateTime`/`LocalDate`.
- Keep time conversions in mappers (for example via `BaseMapper`), not in controllers.
- Use `Optional<T>` for nullable read lookups and unwrap with domain-specific exceptions at service/handler boundaries.
- Prefer immutable collection factories (`List.of`, `Set.of`, `Map.of`) unless mutation/order control is required.
- Naming conventions to preserve: interfaces often `I*`; domain `*Entity`; persistence `*Model`; adapters `*Adapter`; mappers `*Mapper`; DTOs `*Request`/`*Response`; handlers `*Command`/`*Query`.

## Dependency Injection and Transactions
- Prefer constructor injection, commonly via `@RequiredArgsConstructor`.
- Use explicit constructors when collaborator count is high and readability improves.
- Typical transaction patterns:
  - `@Transactional(rollbackFor = Exception.class)` for write commands/use cases.
  - `@Transactional(readOnly = true)` for read query handlers.
- Keep transaction boundaries out of controllers.

## Error Handling and Validation
- Use domain exceptions for business paths; avoid generic `RuntimeException` for expected domain failures.
- Common exceptions include `ResourceNotFoundException`, `BusinessRuleViolationException`, `DomainValidationException`, and `AccessDeniedException`.
- Add/update `DomainErrorCode` when introducing new business error conditions.
- If a new business error needs different HTTP mapping, update `ui/rest/exception/GlobalExceptionHandler.java`.
- Use Jakarta validation annotations on request DTOs and `@Valid` in controllers.
- Keep API responses in the `GeneralResponse<?>` envelope via `ResponseUtils`.

## Persistence, Messaging, and Security
- Keep repository access tenant-aware (`...AndTenantId(...)` patterns).
- Respect soft-delete constraints (`@SQLRestriction("deleted_at IS NULL")` is widely used).
- Use Flyway migrations for schema changes under `src/main/resources/db/migration`.
- `application.yaml` sets `spring.jpa.hibernate.ddl-auto: validate`; do not rely on auto schema creation.
- For write flows that emit events, persist outbox records in the same transaction.
- Let outbox publisher scheduling perform Kafka publish; do not publish directly from controllers.
- Keep consumer processing in the provided consumer template flow so commits/acks stay consistent.
- Resolve auth context through `AuthUtils` (`uid`, `tid`, `groups`) and fail securely when claims are missing.
- Never bypass tenant scoping in reads or writes.

## Practical Workflow and Handoff Checklist
- Read nearby classes in the same package before editing; mirror established conventions.
- Keep edits scoped to the requested change; avoid opportunistic refactors.
- Run the narrowest test that proves your change, then broaden if the change is cross-cutting.
- Never commit `.env`, credentials, or machine-local secrets.
- Note: `run-dev.sh` prints "Starting Discuss Service" but still starts `pm_core` with `./mvnw spring-boot:run`.
- Before handoff, verify compile/tests, style consistency, layer boundaries, and tenant/security/outbox behavior.
