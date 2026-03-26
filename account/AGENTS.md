# AGENTS.md - Account Module Guide for Coding Agents

This guide is for coding agents working inside `account`.
Use it as the default playbook for commands, architecture, and style.
If a touched file has a stronger local convention, follow the local file convention.

## Service Snapshot
- Module: `account` (Java 21, Spring Boot 3.5.5).
- Scope: auth, users, organizations, roles, permissions, subscriptions.
- Integrations: PostgreSQL + Flyway, Redis, Kafka, Keycloak.
- App entrypoint: `src/main/java/serp/project/account/AccountApplication.java`.

## Module Layout
- `src/main/java/serp/project/account/ui/controller` - public REST APIs and exception handler.
- `src/main/java/serp/project/account/ui/controller/internal` - internal service APIs.
- `src/main/java/serp/project/account/core/usecase` - orchestration/use case flows.
- `src/main/java/serp/project/account/core/service` and `core/port` - business services and port contracts.
- `src/main/java/serp/project/account/core/domain` - entities, DTOs, constants, enums, events.
- `src/main/java/serp/project/account/infrastructure/store` - adapters, repos, models, mappers, specs.
- `src/main/java/serp/project/account/infrastructure/client` - Kafka/Keycloak/Redis adapters.
- `src/main/java/serp/project/account/kernel` - config, typed properties, utility classes.
- `src/main/resources/db/migration` - Flyway SQL migrations.
- `src/test/java` - JUnit 5 tests.

## Build, Run, Test, and Checks
Use `./run-dev.sh` for local development because it loads `.env` before startup.
On Windows CMD/PowerShell, use `mvnw.cmd` instead of `./mvnw`.

```bash
cd account

# start app with development env vars
./run-dev.sh

# start app with production profile env vars
./run.sh

# start directly via Maven
./mvnw spring-boot:run

# compile only (quick sanity check)
./mvnw clean compile

# run all tests
./mvnw test

# run one test class
./mvnw -Dtest=RoleEnumUtilsTest test

# run one test method (preferred for narrow changes)
./mvnw -Dtest=RoleEnumUtilsTest#testGetSystemRoles test

# run multiple test classes
./mvnw -Dtest=RoleEnumUtilsTest,AccountApplicationTests test

# package artifact
./mvnw clean package

# package without tests
./mvnw -DskipTests clean package
```

Notes:
- `pom.xml` currently has no dedicated Checkstyle/SpotBugs/PMD/Spotless plugin configuration.
- There is no standalone lint command; use `clean compile` + `test` as the quality gate.

## Architecture and Layering Rules
- Keep controllers thin: validate input, resolve auth context, call use case, return wrapped response.
- Put orchestration in `core/usecase`; put reusable business logic in `core/service`.
- Core layer depends on port interfaces (`core/port`), not Spring Data repositories.
- Infrastructure adapters implement ports and bridge repositories + mappers.
- Persist outbox records in the same transaction as data changes; scheduler publishes later.

## Code Style Guidelines
### File header and formatting
- For new Java source files, use the module standard header:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
```

- Use 4-space indentation and keep wrapping consistent with the touched file.
- Prefer small methods with early returns for guard checks.
- Add comments only for non-obvious business rules.
- Use parameterized logging (for example `log.info("value {}", value)`).

### Imports
- Match import grouping already used in the edited file.
- Typical order in this module: framework/third-party imports, then `serp.project.account...`, then `java.*`.
- Keep static imports in tests after normal imports.
- Avoid wildcard imports unless the file already follows that style.

### Types and modeling
- Use `Long` for IDs and nullable numeric fields.
- Domain entities generally store timestamps as epoch millis (`Long`).
- JPA models generally store timestamps as `LocalDateTime`.
- Keep domain <-> model conversion in mapper classes.
- Use `Optional<T>` for nullable lookups where contracts expose it.
- Put validation on DTO fields (`@NotBlank`, `@NotNull`, nested `@Valid`).

### Naming conventions
- Classes/enums/interfaces: `PascalCase`.
- Methods/fields/variables: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Interfaces usually keep `I` prefix (`IUserService`, `IRolePort`, `IRoleRepository`).
- Domain classes end with `Entity`; persistence classes end with `Model`.
- Adapter/mapper/usecase suffixes: `*Adapter`, `*Mapper`, `*UseCase`.
- Tests end with `Test` and test method names should describe behavior.

### Dependency injection and layering
- Prefer constructor injection; `@RequiredArgsConstructor` is the common pattern.
- Controllers should depend on use cases/utilities, not repositories.
- Avoid bypassing ports or mappers with cross-layer shortcuts.
- Keep utility classes stateless where possible.

### Error handling and validation
- Use `AppException` for business failures.
- Reuse `Constants.ErrorMessage.*` values instead of repeating hardcoded strings.
- Let `GlobalExceptionHandler` map exceptions to API responses.
- Use `@Valid` on controller request bodies.
- Build responses via `ResponseUtils` and preserve `GeneralResponse<?>` shape.

### Transactions, persistence, and events
- Use `@Transactional(rollbackFor = Exception.class)` for write operations.
- Use `@Transactional(readOnly = true)` for read paths when appropriate.
- Repositories should remain under `infrastructure/store/repository` and extend `IBaseRepository<T>`.
- Use specifications/query builders for dynamic filtering instead of ad-hoc SQL in controllers.
- Use outbox flow for Kafka side effects; do not publish directly from controllers.
- Keep migration naming format: `V{N}__description.sql`.

### Security and auth context
- Route policy is configured by `RequestFilter` in `application.yml`.
- Public APIs are under `/api/**`; internal service APIs are under `/internal/**`.
- Internal endpoints require `SERP_SERVICES` role from service JWT claims.
- Use `AuthUtils` to extract `uid`, `tid`, email, and role claims.
- Enforce tenant/organization checks in core logic, not only at controller boundary.

## Testing Guidelines
- Testing stack is JUnit 5 via `spring-boot-starter-test`.
- Prefer focused unit tests for service/usecase/utility behavior.
- Use `@SpringBootTest` only for true integration/wiring scenarios.
- Place tests under matching package paths in `src/test/java`.
- For bug fixes, add/update a regression test that captures the failing path.

## Common Gotchas
- `run-dev.sh` and `run.sh` load env files; direct Maven runs can miss required env vars.
- Do not expose JPA models outside infrastructure layer.
- Keep `GeneralResponse<?>` contract stable for clients.
- If you add endpoints, update security route config to avoid accidental auth gaps.
- Keep business write and outbox write in the same transaction boundary.

## Before You Finish
- Run at least one relevant single test method or class.
- For cross-layer changes, run `./mvnw test`.
- Keep import/formatting consistency in touched files.
- Add Flyway migration for schema changes.
- Never commit `.env` or `.env.prod` values.
