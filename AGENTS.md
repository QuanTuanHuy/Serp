# AGENTS.md - Guide for AI Coding Agents

This document provides essential information for AI coding agents working in the SERP ERP repository.

## Repository Overview

SERP is an event-driven microservices ERP system built with Clean Architecture, using Java 21 (Spring Boot), Go 1.22+ (Gin), Python 3.12+ (FastAPI), and Next.js 15/TypeScript for the frontend. Services communicate via Kafka with JWT authentication through an API Gateway.

## Build, Test, and Lint Commands

### Frontend (serp_web)
```bash
cd serp_web
npm install                    # Install dependencies
npm run dev                    # Start development server (port 3000)
npm run build                  # Production build
npm run lint                   # Run ESLint
npm run lint:fix               # Auto-fix lint issues
npm run format                 # Format with Prettier
npm run format:check           # Check formatting
npm run type-check             # TypeScript type checking (no emit)
```

### Java Services (account, crm, purchase_service, logistics, discuss_service, etc.)
```bash
cd <service-name>
./run-dev.sh                   # Run with .env loaded
./mvnw spring-boot:run         # Alternative run command
./mvnw clean package           # Build JAR
./mvnw test                    # Run all tests
./mvnw test -Dtest=ClassName   # Run single test class
./mvnw test -Dtest=ClassName#methodName  # Run single test method
./mvnw clean install           # Build and install to local Maven repo
```

### Go Services (ptm_task, ptm_schedule, notification_service, api_gateway)
```bash
cd <service-name>
./run-dev.sh                   # Run with .env loaded
go run src/main.go             # Alternative run command
go build -o bin/app src/main.go  # Build binary
go test ./...                  # Run all tests
go test -v ./src/core/usecase  # Run tests in specific package
go test -run TestFunctionName  # Run single test function
go fmt ./...                   # Format code
go vet ./...                   # Static analysis
```

### Python Service (serp_llm)
```bash
cd serp_llm
poetry install                 # Install dependencies
./run-dev.sh                   # Run with environment setup
poetry run uvicorn src.main:app --reload  # Alternative run command
poetry run pytest              # Run all tests
poetry run pytest tests/test_file.py  # Run single test file
poetry run pytest tests/test_file.py::test_function  # Run single test
poetry run pytest -k test_name # Run tests matching pattern
poetry run black .             # Format code (line-length: 100)
poetry run ruff check .        # Lint with ruff
poetry run mypy src            # Type checking
poetry run alembic upgrade head           # Run database migrations
poetry run alembic revision --autogenerate -m "message"  # Create migration
```

### Infrastructure
```bash
docker-compose -f docker-compose.dev.yml up -d     # Start all infrastructure
docker-compose -f docker-compose.dev.yml down      # Stop all
docker-compose -f docker-compose.dev.yml logs -f <service>  # View logs
```

## Code Style Guidelines

### General Principles
- **Clean Architecture**: Follow the layered structure strictly (Controller → UseCase → Service → Port → Adapter)
- **Separation of Concerns**: Keep domain entities separate from persistence models
- **Error Handling**: Return errors explicitly; use custom error constants
- **File Headers**: All source files must include author/description comments
- **Code Comments**: Add brief comments for tricky or non-obvious logic

### File Header Format
```go
// Go/Java:
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/
```
```python
# Python:
"""
Author: QuanTuanHuy
Description: Part of Serp Project
"""
```

### Import Organization

**TypeScript/JavaScript (serp_web):**
```typescript
// 1. React imports
import React from 'react';
// 2. Third-party libraries
import { useRouter } from 'next/router';
// 3. Internal imports with @ alias
import { Button } from '@/shared/components/ui/button';
import { useCRM } from '@/modules/crm';
```

**Go:**
```go
import (
    // 1. Standard library
    "context"
    "errors"
    
    // 2. External packages
    "github.com/gin-gonic/gin"
    "gorm.io/gorm"
    
    // 3. Internal packages (module path)
    "github.com/serp/ptm-task/src/core/domain/entity"
    "github.com/serp/ptm-task/src/core/port/store"
)
```

**Java:**
```java
// 1. Jakarta/javax
import jakarta.validation.Valid;
// 2. Lombok
import lombok.RequiredArgsConstructor;
// 3. Spring framework
import org.springframework.stereotype.Service;
// 4. Internal packages
import serp.project.account.core.domain.entity.UserEntity;
```

### Naming Conventions

**TypeScript/JavaScript:**
- Components/Types: `PascalCase` (e.g., `CustomerCard`, `UserProps`)
- Functions/variables: `camelCase` (e.g., `getUserData`, `isLoading`)
- Files: `kebab-case` (e.g., `customer-list.tsx`)
- Custom hooks: `use` prefix (e.g., `useCustomers`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `API_BASE_URL`)

**Go:**
- Exported identifiers: `PascalCase` (e.g., `CreateTask`, `TaskEntity`)
- Unexported identifiers: `camelCase` (e.g., `taskService`, `validateData`)
- Interfaces: `I` prefix (e.g., `ITaskService`, `ITaskAdapter`)
- Test files: `_test.go` suffix (e.g., `task_usecase_test.go`)
- Constructor functions: `New` prefix (e.g., `NewTaskService`)

**Java:**
- Classes/Interfaces: `PascalCase` (e.g., `UserEntity`, `ILeadService`)
- Methods/variables: `camelCase` (e.g., `getUserById`, `totalCount`)
- Interfaces: `I` prefix (e.g., `IUserPort`)
- DTOs: Descriptive suffix (e.g., `CreateUserRequest`, `UserResponse`)
- Test classes: `Test` suffix (e.g., `RoleEnumUtilsTest`)

**Python:**
- Classes: `PascalCase` (e.g., `ChatService`)
- Functions/variables: `snake_case` (e.g., `get_user_data`, `is_active`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_TOKENS`)
- Private members: `_` prefix (e.g., `_internal_method`)

### TypeScript Patterns
- Use explicit interfaces for props and function parameters
- Prefer `type` imports: `import type { User } from '@/types'`
- Use Zod for runtime validation
- Leverage utility types: `ReturnType`, `Partial`, `Pick`, `Omit`
- Client components: Add `'use client'` directive at top
- Use `cn()` utility for conditional Tailwind classes

### Go Patterns
- Early returns for error handling: `if err != nil { return nil, err }`
- Use `context.Context` as first parameter in functions
- Pass `*gorm.DB` transaction explicitly to service layer methods
- Pointer receivers for struct methods that modify state
- Table-driven tests with `testify/assert` and `testify/mock`

### Java Patterns
- Constructor injection via `@RequiredArgsConstructor`
- Lombok annotations: `@Getter`, `@Setter`, `@SuperBuilder` on entities
- Use `Optional<T>` for potentially null returns
- Validation: `@Valid` on DTOs, business validation in service layer
- Logging: `@Slf4j` with structured messages
- Transactions: `@Transactional` on use case methods

### Python Patterns
- Type hints on all function signatures
- Async/await for I/O operations (SQLAlchemy, HTTP)
- Pydantic models for validation
- Dependency injection via FastAPI `Depends()`
- Black formatting (line length: 100)

### Error Handling

**Go:**
```go
// Return errors as last value
func CreateTask(ctx context.Context, task *entity.TaskEntity) (*entity.TaskEntity, error) {
    if err := validate(task); err != nil {
        return nil, fmt.Errorf("%s: %w", constant.ValidationFailed, err)
    }
    return task, nil
}
```

**Java:**
```java
// Use custom AppException with error constants
if (user == null) {
    throw new AppException(ErrorMessage.USER_NOT_FOUND);
}
```

**TypeScript:**
```typescript
// RTK Query handles errors automatically; use try-catch for manual calls
try {
    const result = await createCustomer(data).unwrap();
} catch (error) {
    toast.error('Failed to create customer');
}
```

## Critical Development Rules

### Go Services
1. **ALWAYS register new components in `cmd/bootstrap/all.go`** - Missing registration causes runtime panics
2. Use `TransactionService.ExecuteInTransaction` for database transactions
3. Extract user/tenant context via `utils.GetUserIDFromContext(c)` and `utils.GetTenantIDFromContext(c)`
4. Kafka events must be sent AFTER successful DB commit within transaction
5. Entity ↔ Model mapping uses separate mapper structs

### Java Services
1. Use `./run-dev.sh` to load `.env` variables properly (not direct `./mvnw`)
2. Database migrations: Flyway in `src/main/resources/db/migration/V{N}__description.sql`
   - Format: `V1__Initial_schema.sql`, `V2__Add_user_table.sql`
   - Migrations run automatically on startup
3. Extract user context via `authUtils.getCurrentUserId()` and `authUtils.getCurrentTenantId()`
4. Use `@Transactional(rollbackFor = Exception.class)` on service methods
5. Entities extend `BaseEntity` with `@SuperBuilder` annotation
6. Repository naming: `I{Entity}Port` interface, `{Entity}Adapter` implementation

### Frontend (serp_web)
1. **Module isolation**: No cross-imports between feature modules (e.g., crm cannot import from sales)
2. Use `api.injectEndpoints()` with `extraOptions: { service: 'serviceName' }`
3. Always run `npm run lint`, `npm run type-check`, `npm run format:check` before committing
4. Use Shadcn components from `@/shared/components/ui/`
5. State management: Redux Toolkit + RTK Query for API calls
6. File naming: `kebab-case.tsx` for components, `PascalCase` for component names
7. Use `'use client'` for client components (forms, interactivity, hooks)
8. Import path aliases: Use `@/` for src root (e.g., `@/modules/crm`, `@/shared/components`)

### General Rules
1. Run infrastructure with `docker-compose -f docker-compose.dev.yml up -d` before starting services
2. Never commit `.env` files (use `.env.example` for templates)
3. All API requests flow through API Gateway (port 8080)
4. JWT tokens required for all authenticated endpoints
5. Each service has its own PostgreSQL schema/database

## Testing Guidelines

- **Coverage**: Write tests for use cases and critical business logic
- **Go**: Table-driven tests with `testify/assert` and `testify/mock` for ports/adapters
  - Test files: `*_test.go` in same directory as implementation
  - Mock interfaces defined in `core/port/`
  - Example: `task_usecase_test.go` tests `task_usecase.go`
- **Java**: `@SpringBootTest` for integration tests, JUnit 5 for unit tests
  - Test classes in `src/test/java/` mirror `src/main/java/` structure
  - Use `@ExtendWith(MockitoExtension.class)` for mocks
- **Python**: Pytest with async support (`asyncio_mode = "auto"`)
  - Test files: `test_*.py` in `tests/` directory
  - Use `pytest-asyncio` for async tests
  - Use `pytest-httpx` for mocking HTTP calls
- **Frontend**: Component tests with React Testing Library (if implemented)

## Common Gotchas

1. **Go FX Registration**: Forgetting `fx.Provide()` in `cmd/bootstrap/all.go` → runtime panic
   - Every new adapter, service, usecase, controller must be registered
2. **Entity/Model Mismatch**: Ensure mappers handle all field conversions (e.g., Unix ms ↔ `time.Time`)
   - Go: Entities use `*int64` for timestamps, Models use `time.Time`
   - Java: Entities use `Long`, Models use `Instant` or `LocalDateTime`
3. **Transaction Rollback**: Manual `tx.Begin()` doesn't auto-rollback; use `txService.ExecuteInTransaction`
4. **Environment Variables**: Use `./run-dev.sh`, not direct `mvnw` or `go run`
   - Services won't start without proper `.env` configuration
5. **Keycloak JWT**: Verify JWKS URL is accessible and realm name matches config
   - Default realm: `serp-realm`, admin: `serp-admin/serp-admin`
6. **Port Conflicts**: `logistics` and `serp_llm` both use 8089 by default - configure `SERVER_PORT`
7. **Kafka Events**: Always send events AFTER DB commit, inside transaction callback
8. **Import Order**: Go and Java have strict import grouping (std lib, external, internal)

## Additional Resources

- Architecture details: `README.md` and `.github/copilot-instructions.md`
- API Gateway routes: Check `api_gateway/src/ui/router/`
- Database schemas: Each service has `db/migration/` or `alembic/versions/`
- Infrastructure URLs: PostgreSQL:5432, Redis:6379, Kafka:9092, Keycloak:8180
