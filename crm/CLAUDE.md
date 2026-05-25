# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## CRM service

Java 21 / Spring Boot 3.5.6 microservice for Customer Relationship Management in SERP. Manages leads, opportunities, accounts/customers, contacts, activities, teams, territories, and global CRM search.

Entrypoint: `src/main/java/serp/project/crm/CrmApplication.java`. Default port: `8086`. Servlet context path: `/crm`. Access through API Gateway at `/crm/api/v1`.

## Commands

Run from `crm/`. Prerequisites for local integration behavior: PostgreSQL, Redis, and Kafka from root `docker-compose.dev.yml`.

```bash
./run-dev.sh
./run-prod.sh
./mvnw spring-boot:run
./mvnw clean compile
./mvnw test
./mvnw test -Dtest=CrmApplicationTests
./mvnw test -Dtest=ClassName
./mvnw test -Dtest=ClassName#methodName
./mvnw clean package
./mvnw clean package -DskipTests
```

Use `mvnw.cmd` instead of `./mvnw` in Windows CMD/PowerShell. No dedicated Checkstyle/Spotless/PMD config exists; use compile and tests as quality gates.

## Runtime config

Main config: `src/main/resources/application.yml`. Production config: `src/main/resources/application-prod.yml`.

Required local env values include database, Redis, Kafka, Keycloak, account-service URL, and CRM client secret values consumed by these properties:

- `DB_USERNAME`, `DB_PASSWORD`, `DB_URL`
- `REDIS_HOST`, `REDIS_PORT`
- `KAFKA_BOOTSTRAP_SERVERS`
- `KEYCLOAK_URL`, `CLIENT_SECRET`
- `ACCOUNT_SERVICE_URL`

JPA uses `spring.jpa.hibernate.ddl-auto: validate`; schema changes must use Flyway migrations in `src/main/resources/db/migration` with `V{N}__description.sql` naming.

## Architecture

- `ui/controller`: REST controllers and global exception handler.
- `ui/internal`: internal service endpoints.
- `core/usecase`: orchestration and application flows.
- `core/service`: reusable domain/business services.
- `core/port/client`: external client/Kafka port interfaces.
- `core/port/store`: persistence port interfaces.
- `core/domain`: entities, DTOs, enums, constants, events, messages.
- `core/mapper`: DTO/domain mappers.
- `infrastructure/store/adapter`: persistence port implementations.
- `infrastructure/store/repository`: Spring Data repositories.
- `infrastructure/store/model`: JPA persistence models.
- `infrastructure/store/mapper`: domain/model mappers.
- `infrastructure/store/specification`: dynamic query specifications.
- `infrastructure/client`: Kafka and external service adapters.
- `kernel/config`: Spring/Kafka/Redis/security/WebClient config.
- `kernel/property`: typed configuration properties.
- `kernel/utils`: auth, response, JSON, data, collection, HTTP helpers.

Keep dependency flow: controllers -> use cases -> services/ports -> infrastructure adapters. Do not call repositories directly from controllers or use cases. Do not expose JPA models outside infrastructure; convert through mapper classes.

## API areas

Routes are under `/crm/api/v1` via gateway and `/crm` service context locally.

- Leads: capture, score, qualify, disqualify, convert.
- Opportunities: pipeline stage tracking and closing.
- Accounts/customers: individual, business, enterprise, hierarchy and credit data.
- Contacts: linked contacts with primary designation.
- Activities: calls, meetings, emails, tasks linked to CRM entities.
- Teams and territories: sales/support organization, members, routing.
- Search: cross-entity CRM search.

## Conventions

- New Java files should match nearby author header style.
- Use 4-space indentation and local import grouping.
- Prefer constructor injection, usually with Lombok `@RequiredArgsConstructor`.
- Use `Long` for IDs and nullable numeric fields.
- Domain entities use `*Entity`; JPA models use `*Model`; adapters use `*Adapter`; DTOs use `*Request`/`*Response`; interfaces usually use `I*`.
- Put validation on request DTO fields and use `@Valid` in controllers.
- Build API responses through `ResponseUtils` and preserve `GeneralResponse<?>` / `PageResponse` shapes.
- Use `AppException` for business failures and let `GlobalExceptionHandler` map responses.
- Use `AuthUtils` for authenticated user/tenant context. Enforce tenant and ownership rules in core logic, not only controllers.
- Use specifications for dynamic filters instead of ad-hoc query logic in controllers.
- Keep Kafka publishing behind `IKafkaPublisher` and adapter boundaries; do not publish directly from controllers.
- When adding fields, update entity, model, mapper, request/response DTOs, specifications if relevant, migration, and tests together.
- Keep external account-service calls behind `IUserProfileClient` or matching client ports.

## Testing

Testing stack is JUnit 5 via `spring-boot-starter-test` plus `spring-kafka-test`. Current checked-in test coverage is minimal (`CrmApplicationTests`), so add focused unit tests near changed use cases, services, mappers, and specifications when adding business logic or fixing bugs.

For narrow work, run one focused test class or method first. For cross-layer or migration changes, run `./mvnw test` and at least `./mvnw clean compile`.
