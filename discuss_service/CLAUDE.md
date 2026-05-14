# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Discuss service

Java 21 / Spring Boot 3.5 service for real-time communication. Combines REST APIs, WebSocket/STOMP handlers, Kafka consumers/producers, Redis caching, S3/MinIO attachments, and PostgreSQL persistence.

Runtime defaults: port `8092`, context path `/discuss`, WebSocket endpoint `/ws/discuss`, migrations in `src/main/resources/db/migration/`.

## Commands

Run from `discuss_service/`.

```bash
./run-dev.sh
./mvnw spring-boot:run
./mvnw clean package
./mvnw clean package -DskipTests
./mvnw -q -DskipTests compile
./mvnw test
./mvnw test -Dtest=MessageUseCaseTest
./mvnw test -Dtest=MessageUseCaseTest#testSendMessage_ValidRequest_SendsSuccessfully
./mvnw test -Dtest=MessageUseCaseTest,ChannelUseCaseTest
./mvnw test -Dtest=serp.project.discuss_service.core.usecase.MessageUseCaseTest
```

`DiscussServiceApplicationTests` is disabled by default because it expects real infrastructure. No Checkstyle, Spotless, or PMD config exists; use compile, tests, and package as quality gates.

## Architecture

Package root: `serp.project.discuss_service`.

- `core`: entities, DTOs, ports, services, use cases, listeners.
- `infrastructure`: JPA adapters, repositories, mappers, Redis/Kafka/S3/account adapters.
- `kernel`: config, properties, auth/WebSocket helpers, utilities.
- `ui`: REST controllers, WebSocket controllers, Kafka handlers.

Dependency flow: `ui` -> `core.usecase` -> `core.service` -> `core.port.*`; infrastructure implements ports. `kernel` supplies config/utilities, not business rules.

## Conventions

- New Java files use the standard author header from nearby files.
- Keep controllers and adapters thin; orchestration belongs in `*UseCase`, business logic in `*Service` or entities.
- Add external integrations behind `core.port.*` first, then implement in `infrastructure`.
- Keep SQL, JPA, cache, and storage-specific logic in adapters/repositories.
- Keep API responses wrapped in `GeneralResponse<T>` or `PaginatedResponse<T>`.
- Do not leak JPA models into `core`; convert via mapper classes.
- Add/update entity, model, mapper, response DTO, request DTO, and tests together when adding fields.
- Publish Kafka/WebSocket side effects after commit with `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- After message/channel/reaction mutations, update or invalidate `IDiscussCacheService` entries.
- REST controllers get user and tenant context from `SerpAuthContext`; WebSocket handlers use `WebSocketAuthChannelInterceptor.WebSocketPrincipal`.
- Route account-service calls through `IAccountServiceClient` / `AccountServiceClientAdapter`.
- Never log tokens, secrets, or full presigned URLs.
- Flyway migrations use `V{N}__description.sql`.

## Testing

Tests use JUnit 5 and Mockito. Prefer `@ExtendWith(MockitoExtension.class)`, `@Nested`, and `@DisplayName`. Reuse `src/test/java/serp/project/discuss_service/testutil/TestDataFactory.java`. Add mapper tests when conversion logic changes and usecase/service tests when business rules change.
