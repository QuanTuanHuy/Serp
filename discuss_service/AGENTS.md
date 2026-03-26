# AGENTS.md - Guide for AI Coding Agents

This guide is specific to `discuss_service/`.
Use it when reading, changing, or extending the Discuss Service.

## Module Overview
`discuss_service` is a Java 21 + Spring Boot 3.5 service for real-time communication.
It combines REST APIs, WebSocket/STOMP handlers, Kafka consumers/producers, Redis caching, S3/MinIO attachments, and PostgreSQL persistence.
```text
src/main/java/serp/project/discuss_service/
├── core/            # entities, DTOs, ports, services, use cases, listeners
├── infrastructure/  # JPA adapters, repositories, mappers, Redis/Kafka/S3/account adapters
├── kernel/          # config, properties, auth/websocket helpers, utilities
└── ui/              # REST controllers, WebSocket controllers, Kafka handlers
```
Respect the dependency flow:
- `ui` -> `core.usecase`
- `core.usecase` -> `core.service`
- `core.service` -> `core.port.*`
- `infrastructure` implements ports
- `kernel` supplies config/utilities, not business rules

## Build, Run, Test, and Quality Commands
Run commands from `discuss_service/`.

### Development / Build
```bash
./run-dev.sh                      # load .env and start locally
./mvnw spring-boot:run            # start if env vars are already loaded
./mvnw clean package              # build jar and run tests
./mvnw clean package -DskipTests  # build jar without tests
./mvnw -q -DskipTests compile     # fast compile-only check
```

### Tests
```bash
./mvnw test
./mvnw test -Dtest=MessageUseCaseTest
./mvnw test -Dtest=MessageUseCaseTest#testSendMessage_ValidRequest_SendsSuccessfully
./mvnw test -Dtest=MessageUseCaseTest,ChannelUseCaseTest
./mvnw test -Dtest=serp.project.discuss_service.core.usecase.MessageUseCaseTest
```
Single-method execution uses Surefire `ClassName#methodName` syntax.
Most tests are JUnit 5 + Mockito unit tests.

### Integration-style Test
```bash
./run-dev.sh
./mvnw test -Dtest=DiscussServiceApplicationTests
```
`DiscussServiceApplicationTests` is disabled by default because it expects real infrastructure.

### Lint / Quality Notes
`pom.xml` does not configure Checkstyle, Spotless, or PMD.
Use these as the practical quality gate:
```bash
./mvnw -q -DskipTests compile
./mvnw test
./mvnw clean package
```
Runtime defaults: config in `src/main/resources/application.yaml`, port `8092`, context path `/discuss`, WebSocket endpoint `/ws/discuss`, migrations in `src/main/resources/db/migration/`, env loaded by `./run-dev.sh`.

## Architecture Rules
- Keep controllers and adapters thin; put orchestration in `*UseCase` and business logic in `*Service` / entities.
- Do not call repositories directly from controllers.
- Do not let `infrastructure` depend on `ui`.
- Add new external integrations behind a `core.port.*` interface first, then implement them in `infrastructure`.
- Keep SQL, JPA, cache, and storage-specific logic in adapters/repositories, not in use cases.

## File and Class Conventions
Most source files use this header; keep it for new Java files:
```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - <short description>
 */
```
- Package root is `serp.project.discuss_service`.
- Use `PascalCase` for classes, enums, DTOs, and test classes.
- Interfaces use the `I` prefix: `IMessageService`, `IMessagePort`, `IMessageRepository`.
- Use stable suffixes: `*UseCase`, `*Service`, `*Adapter`, `*Mapper`, `*Controller`, `*Request`, `*Response`, `*Model`, `*Entity`.
- Enum members use `UPPER_SNAKE_CASE`.

## Imports and Formatting
- Keep imports grouped consistently: `jakarta`, Lombok, Spring, third-party (`io.github.serp`, Jackson, Kafka, AWS), internal `serp.project.discuss_service.*`, JDK, then static imports in tests.
- Use 4-space indentation.
- Keep one top-level class per file.
- Prefer guard clauses and early returns.
- Split long method calls so each argument is on its own line.
- Preserve the surrounding style when touching older files.
- Avoid wildcard imports unless the file already uses them in tests.

## Types and Data Modeling
- Use `Long` for IDs and most domain timestamps.
- Domain entities usually keep timestamps as epoch milliseconds.
- JPA models use `LocalDateTime`; conversions belong in `infrastructure.store.mapper.BaseMapper`.
- Use `Optional<T>` for absent lookups instead of returning `null`.
- DTOs commonly use Lombok `@Data` + `@Builder`; entities often use `@Getter`, `@Setter`, and `@SuperBuilder`.
- Small immutable transport shapes may use Java `record`.
- Put Jakarta validation annotations on request DTOs (`@NotBlank`, `@Size`, etc.).
- Keep API responses wrapped in `GeneralResponse<T>` or `PaginatedResponse<T>`.

## Domain, Persistence, and Mapping
- Keep rich behavior on entities like `ChannelEntity` and `MessageEntity`.
- Call entity validation methods such as `validateForCreation()` before persistence.
- Do not leak JPA models into `core`; convert via mapper classes.
- When adding a field, update entity, model, mapper, response DTO, request DTO if needed, and tests together.
- Be careful with timestamp and collection conversions in mappers.

## Transactions, Events, and Cache
- Write operations in use cases generally use `@Transactional`.
- Read methods that hit persistence should prefer `@Transactional(readOnly = true)`.
- Publish internal Spring events inside the transaction via `ApplicationEventPublisher`.
- Publish Kafka/WebSocket side effects after commit with `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- Do not publish Kafka messages directly from REST controllers.
- After message/channel/reaction mutations, update or invalidate `IDiscussCacheService` entries.
- Follow the existing pattern where listeners handle post-commit cache and event fan-out work.

## Error Handling and Responses
- Use `AppException(ErrorCode)` for business/API failures.
- Add new failures to `core.exception.ErrorCode` with the correct `HttpStatus`.
- Use `IllegalArgumentException` or `IllegalStateException` for internal invariant failures.
- Let `ui.controller.GlobalExceptionHandler` translate exceptions to HTTP responses.
- REST controllers should return `ResponseEntity<GeneralResponse<T>>` and use `ResponseUtils`.
- Use `orElseThrow(() -> new AppException(...))` for required auth, tenant, and lookup values.
- Do not expose stack traces or raw exception text to clients.

## Security, Logging, and External Calls
- REST controllers get user and tenant context from `SerpAuthContext`.
- WebSocket handlers use `WebSocketAuthChannelInterceptor.WebSocketPrincipal`.
- Keep token parsing and auth helpers in `kernel`, not in business logic.
- Route account-service calls through `IAccountServiceClient` / `AccountServiceClientAdapter`.
- Use `@Slf4j`; log IDs like `channelId`, `messageId`, and `userId`, but never log tokens, secrets, or full presigned URLs.

## Testing Style
- Prefer JUnit 5 unit tests with `@ExtendWith(MockitoExtension.class)`.
- Use `@Nested` and `@DisplayName` to group scenarios.
- Reuse `src/test/java/serp/project/discuss_service/testutil/TestDataFactory.java` for fixtures.
- Name tests clearly, usually `testAction_Context_ExpectedOutcome`.
- Verify both return values and side effects such as `verify(...)`, cache writes, and event publication.
- Add mapper tests when conversion logic changes and use case/service tests when business rules change.

## Module-Specific Gotchas
- `run-dev.sh` is the safest local start path because it loads `.env`.
- `DiscussServiceApplicationTests` does not run in normal unit-test flow.
- New database changes require Flyway files named `V{N}__description.sql`.
- Message and channel mutations often require both cache invalidation and post-commit event handling.
- REST, WebSocket, Kafka, Redis, and storage code paths are tightly connected; trace the full flow before refactoring shared behavior.
