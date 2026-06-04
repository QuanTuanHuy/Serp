# AGENTS.md - SERP Root Guide for Coding Agents

This is the repository-wide default guide for coding agents working in SERP.
Keep it cross-repo and lightweight; when a module has its own `AGENTS.md`, that local guide overrides this file for work inside that module.

## Local Guide Precedence
- Read the nearest local guide first when you work in:
  - `account/AGENTS.md`
  - `api_gateway/AGENTS.md`
  - `discuss_service/AGENTS.md`
  - `first-mile/AGENTS.md` (TMS first-mile backend)
  - `second-mile/AGENTS.md` (TMS second-mile backend)
  - `tms-billing-service/AGENTS.md` (TMS billing / shipping fee)
  - `tms-order/AGENTS.md` (TMS order management backend)
  - `notification_service/AGENTS.md`
  - `pm_core/AGENTS.md`
  - `serp_web/AGENTS.md`
  - `serp_web/src/modules/first-mile/AGENTS.md` (TMS first-mile + second-mile UI)
- Modules without a local guide currently rely on this root file plus nearby code conventions.

## Repo Map
- Frontend: `serp_web/` - Next.js 15, React 19, TypeScript.
- Python service: `serp_llm/` - FastAPI, SQLAlchemy async, Poetry.
- Go services: `api_gateway/`, `notification_service/`, `ptm_schedule/`, `ptm_task/`.
- Spring Boot services: `account/`, `crm/`, `discuss_service/`, `first-mile/`, `second-mile/`, `tms-billing-service/`, `tms-order/`, `logistics/`, `mailservice/`, `pm_core/`, `ptm_optimization/`, `purchase_service/`, `sales/`.
- Shared Java libraries: `serp_java_platform/`.
- Local infrastructure entrypoint: `docker-compose.dev.yml`.

## Cross-Repo Workflow
- Start infrastructure first when integration behavior matters:
```bash
docker-compose -f docker-compose.dev.yml up -d
docker-compose -f docker-compose.dev.yml down
docker-compose -f docker-compose.dev.yml logs -f <service>
```
- Prefer the smallest relevant verification before handoff, then broaden if you changed wiring, config, persistence, or cross-module behavior.
- Use `run-dev.sh` when a module has it; those scripts usually load `.env`.
- Never commit `.env` files, generated secrets, or machine-local credentials.
- Route authenticated API traffic through `api_gateway/` unless the module's own guide says otherwise.

## Build, Lint, and Test Commands

### Frontend (`serp_web`)
Run from `serp_web/`.
```bash
npm install
npm run dev
npm run build
npm run start
npm run lint
npm run lint:fix
npm run format
npm run format:check
npm run type-check
npx eslint src/path/to/file.tsx
npx prettier --check src/path/to/file.tsx
```
- There is currently no `test` script and no checked-in frontend test framework, so there is no supported single-test command today.

### Spring Boot services
Applies to `account`, `crm`, `discuss_service`, `first-mile`, `second-mile`, `tms-order`, `logistics`, `mailservice`, `pm_core`, `ptm_optimization`, `purchase_service`, and `sales`.
Run from the service directory.
```bash
./run-dev.sh                    # when present
./mvnw spring-boot:run
./mvnw clean compile
./mvnw test
./mvnw -Dtest=RoleEnumUtilsTest test
./mvnw -Dtest=RoleEnumUtilsTest#testGetSystemRoles test
./mvnw clean package
./mvnw -DskipTests clean package
```
- On Windows CMD or PowerShell, use `mvnw.cmd`.
- `sales/`, `first-mile/`, and `second-mile/` currently do not have `run-dev.sh`; start them via the Maven wrapper.
- Most services do not have a dedicated lint plugin, so `clean compile` and `test` are the practical quality gate.

### Shared Java platform (`serp_java_platform`)
Run from `serp_java_platform/`.
```bash
mvn test
mvn -pl serp-starter-kafka test
mvn -pl serp-starter-kafka -Dtest=SerpKafkaTopicResolverTest test
mvn -pl serp-starter-kafka -Dtest=SerpKafkaTopicResolverTest#shouldResolveDeadLetterTopicWithSuffix test
mvn -pl serp-starter-kafka -am package
```
- This directory currently uses plain `mvn`; there is no checked-in Maven wrapper here.

### Go services
Applies to `api_gateway`, `notification_service`, `ptm_schedule`, and `ptm_task`.
Run from the service directory.
```bash
go mod download
./run-dev.sh
go run src/main.go
go build -o bin/app src/main.go
go fmt ./...
go vet ./...
go test ./...
go test ./src/core/usecase -run '^TestCreateTask_Success_StandaloneTask$' -count=1
go test ./src/ui/controller/common -run '^TestGenericProxyController_CRM_POSTDoesNotRetry$' -count=1
```
- Prefer package-scoped single tests over `go test ./... -run ...` for faster feedback.
- Use `-count=1` when rerunning a single test so Go does not reuse cached results.

### Python service (`serp_llm`)
Run from `serp_llm/`.
```bash
poetry install
./run-dev.sh
poetry run uvicorn src.main:app --reload
poetry run pytest
poetry run pytest tests/test_file.py
poetry run pytest tests/test_file.py::test_name
poetry run pytest -k pattern
poetry run black .
poetry run ruff check .
poetry run mypy src
poetry run alembic upgrade head
poetry run alembic revision --autogenerate -m "message"
```
- `pyproject.toml` configures pytest, but there are currently no checked-in tests under `serp_llm/tests/`.

## Style Rules

### Shared architecture
- Keep layer boundaries explicit: UI/controller -> use case/application -> service/domain -> port/repository/adapter.
- Keep controllers, routes, and handlers thin; put business rules in use cases, services, or domain entities.
- Do not leak persistence models into domain or API response layers.
- Preserve tenant, organization, and user context handling through shared auth utilities.
- For schema changes, add a migration instead of relying on implicit auto-update behavior.
- Code style: add brief comments for tricky logic; keep files under ~500 LOC when feasible (split/refactor as needed).


### File headers and comments
- Many backend files start with an `Author:` / `Description:` header block; add it to new Go/Java files and match local Python header style when present.
- **Default (most of the monorepo):**
```text
Author: QuanTuanHuy
Description: Part of Serp Project
```
- **TMS modules override the default author** — do **not** use `QuanTuanHuy` in `first-mile/`, `second-mile/`, `tms-billing-service/`, `tms-order/`, or `serp_web/src/modules/first-mile/`. Those guides require `Author: Nguyen The Anh` (see each module’s `AGENTS.md`). When editing an existing TMS file, **preserve** its current `Author:` line; never replace `Nguyen The Anh` with `QuanTuanHuy`.
- Add comments only for non-obvious business rules or tricky control flow.

### Imports
- TypeScript: Next/React, then third-party, then `@/` aliases, then relative imports.
- Go: standard library, then external packages, then internal module imports.
- Java: match the local file; common order is `jakarta`, Lombok, Spring or third-party, internal packages, then JDK.
- Python: standard library, third-party, then `src.` first-party imports.

### Formatting
- `serp_web`: Prettier is the source of truth - 2 spaces, semicolons, single quotes, trailing commas `es5`, `printWidth: 80`.
- Go: always let `go fmt` decide layout; do not hand-format against it.
- Java: use 4 spaces and keep wrapping and import order consistent with the touched file.
- Python: Black and Ruff use `line-length = 100`.

### Types and naming
- TypeScript: keep `strict`-safe types, prefer explicit props/request/response types, use `import type`, avoid new `any`, components and types in `PascalCase`, functions and variables in `camelCase`, hooks as `useThing`, files usually in `kebab-case`.
- Go: exported names `PascalCase`, unexported names `camelCase`, interfaces commonly use the `I` prefix, constructors use `New...`, errors are returned last, and `context.Context` is the first parameter in service and adapter methods.
- Java: classes and interfaces `PascalCase`, fields and methods `camelCase`, constants `UPPER_SNAKE_CASE`, interfaces often use the `I` prefix, DTOs use `*Request` and `*Response`, domain types use `*Entity`, persistence types use `*Model`, adapters use `*Adapter`, mappers use `*Mapper`.
- Python: classes `PascalCase`, functions and variables `snake_case`, constants `UPPER_SNAKE_CASE`, keep type hints on important public functions, and prefer async I/O patterns already used by FastAPI and SQLAlchemy.

### Error handling and transactions
- TypeScript: use RTK Query `.unwrap()` inside `try/catch`; reuse shared error or toast helpers and `api.injectEndpoints()` with `extraOptions: { service: '...' }`.
- Go: prefer early returns, wrap lower-level failures with `%w`, return `error` instead of panicking, use transaction services for multi-step writes, and register new components in `cmd/bootstrap/all.go` when the service uses FX.
- Java: throw module-specific business exceptions (`AppException` or the module's domain exception types), use `@Transactional` for write operations and `@Transactional(readOnly = true)` for read paths, and preserve `ResponseUtils` and `GeneralResponse<?>` response shapes.
- Python: raise custom app exceptions or `HTTPException`, keep exception-to-response mapping in the shared FastAPI middleware or handlers, and do not swallow infrastructure errors silently.

### Internal service authentication
- Do not use hard-coded JWTs or service bearer-token env vars for backend-to-backend calls that may run without HTTP context.
- For internal Spring service calls, prefer the shared API-key pattern: send `X-Internal-Api-Key`, `X-Tenant-Id`, and `X-Internal-Service`; receive them through the module's `InternalApiAuthenticationFilter`, then read tenant/roles through `AuthUtils`.
- Internal service endpoints should be public at the Spring Security route layer (`permitAll`) so they do not require JWT, but the API-key filter must enforce `X-Internal-Api-Key` for internal paths before controller logic runs.
- Internal service callers should send the API key headers even when a user JWT exists; use the JWT only for normal user-facing HTTP traffic, not service-to-service internal endpoints.
- Keep `INTERNAL_API_KEY` out of source control. Configure it consistently on all participating services and rotate it like any other shared secret.

### Testing conventions
- Java tests are JUnit 5; most modules use Mockito and `spring-boot-starter-test`.
- Go tests use the standard `testing` package; `testify` is present in `ptm_task` and `ptm_schedule`.
- Python tests use `pytest` with `asyncio_mode = auto`.
- When fixing a bug, prefer adding or updating the nearest focused regression test.
- If a module has its own `AGENTS.md`, follow its testing advice over this root summary.

## Practical Reminders
- Read the nearest module guide before making non-trivial changes.
- Match nearby code before applying global preferences.
- Run the narrowest relevant command first, then the full module check if the change is cross-cutting.
- Keep new dependencies, routes, migrations, and DI registrations synchronized with the module's wiring files.
