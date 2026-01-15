# PTM Task Service

PTM Task Service is a microservice in the SERP ERP system responsible for managing projects, tasks, and notes within the Project & Task Management (PTM) module. It provides hierarchical task organization, dependency tracking, and activity event publishing.

## Overview

This service handles the core task management functionality, allowing users to organize work into projects with hierarchical tasks, track dependencies, and attach notes for documentation.

**Technology Stack:**
- Go 1.25.0
- Gin Web Framework
- GORM (PostgreSQL)
- Kafka (Event Publishing)
- Redis (Caching)
- Uber FX (Dependency Injection)

**Port:** 8083  
**Context Path:** `/ptm-task`

## Architecture

This service follows **Clean Architecture** with clear separation of concerns:

```
src/
├── main.go                    # Application entry point
├── cmd/
│   └── bootstrap/            # Dependency injection & initialization
│       ├── all.go           # Main module assembly
│       ├── config.go        # Configuration setup
│       ├── infrastructure.go # Infrastructure components
│       ├── http.go          # HTTP server setup
│       ├── logger.go        # Logging configuration
│       ├── migrate.go       # Database migrations
│       ├── properties.go    # Properties binding
│       └── server.go        # Server lifecycle
├── ui/
│   ├── controller/          # HTTP request handlers
│   │   ├── project_controller.go
│   │   ├── task_controller.go
│   │   └── note_controller.go
│   ├── router/              # Route definitions
│   │   └── router.go
│   └── middleware/          # HTTP middlewares
│       ├── jwt_middleware.go
│       └── role_middleware.go
├── core/
│   ├── usecase/             # Business logic orchestration
│   │   ├── project_usecase.go
│   │   ├── task_usecase.go
│   │   ├── task_dependency_usecase.go
│   │   ├── task_template_usecase.go
│   │   └── note_usecase.go
│   ├── service/             # Domain services
│   │   ├── project_service.go
│   │   ├── task_service.go
│   │   ├── task_dependency_service.go
│   │   ├── task_template_service.go
│   │   ├── note_service.go
│   │   ├── activity_event_service.go
│   │   └── transaction_service.go
│   ├── port/                # Interfaces
│   │   ├── store/          # Database port interfaces
│   │   └── client/         # External client interfaces
│   └── domain/              # Domain models
│       ├── entity/         # Domain entities
│       ├── dto/            # Data transfer objects
│       ├── enum/           # Enumerations
│       ├── mapper/         # Entity mappers
│       └── constant/       # Constants
├── infrastructure/
│   ├── store/              # Database implementations
│   │   ├── adapter/       # Store adapters
│   │   ├── model/         # Database models
│   │   └── mapper/        # Model-Entity mappers
│   └── client/             # External clients
│       ├── kafka_producer_adapter.go
│       └── redis_adapter.go
├── kernel/
│   ├── properties/         # Configuration properties
│   └── utils/              # Utility functions
└── config/
    ├── default.yaml        # Default configuration
    ├── local.yaml          # Local development config
    └── production.yaml     # Production config
```

## Core Features

### 1. Project Management

Organize tasks into projects with progress tracking and metadata.

**Key Capabilities:**
- Create, update, delete projects
- Track project status (NEW, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED)
- Set priorities (LOW, MEDIUM, HIGH, URGENT)
- Define start dates and deadlines
- Monitor progress percentage
- Customization (color, icon, favorites)
- Computed statistics (total tasks, completed tasks, estimated/actual hours)

**Domain Model:**
```go
type ProjectEntity struct {
    UserID, TenantID    int64
    Title, Description  string
    Status, Priority    string
    StartDateMs, DeadlineMs  *int64
    ProgressPercentage  int
    Color, Icon         *string
    IsFavorite          bool
    TotalTasks, CompletedTasks  int
    EstimatedHours, ActualHours float64
}
```

### 2. Task Management

Hierarchical task organization with rich metadata and tracking.

**Key Capabilities:**
- Create, update, delete tasks
- Hierarchical structure (parent-child relationships)
- Task dependencies tracking
- Priority management with scoring
- Time tracking (estimated vs actual duration)
- Duration learning from historical data
- Deadline and preferred start date
- Categories and tags
- Task status (TODO, IN_PROGRESS, DONE, CANCELLED)
- Deep work and meeting flags
- Flexible scheduling flag
- Subtask progress tracking

**Domain Model:**
```go
type TaskEntity struct {
    UserID, TenantID      int64
    Title, Description    string
    Priority              string
    PriorityScore         *float64
    
    EstimatedDurationMin  *int
    ActualDurationMin     *int
    IsDurationLearned     bool
    
    PreferredStartDateMs  *int64
    DeadlineMs            *int64
    EarliestStartMs       *int64
    
    Category              *string
    Tags                  []string
    
    ParentTaskID          *int64
    HasSubtasks           bool
    TotalSubtaskCount     int
    CompletedSubtaskCount int
    
    ProjectID             *int64
    
    IsRecurring           bool
    RecurrencePattern     *string
    RecurrenceConfig      *string
    
    IsDeepWork            bool
    IsMeeting             bool
    IsFlexible            bool
    
    Status, ActiveStatus  string
    CompletedAt           *int64
}
```

### 3. Task Dependencies

Manage task dependencies to ensure proper execution order.

**Key Capabilities:**
- Define task dependencies (FINISH_TO_START, START_TO_START, etc.)
- Dependency graph validation (prevent cycles)
- Cascade updates when tasks change
- Dependency type tracking

**Domain Model:**
```go
type TaskDependencyGraphEntity struct {
    TaskID           int64
    DependsOnTaskID  int64
    DependencyType   string
}
```

### 4. Task Templates

Reusable task templates for common workflows.

**Key Capabilities:**
- Create task templates
- Template-based task creation
- Predefined configurations
- Category-based organization

### 5. Task Reminders

Schedule reminders for tasks.

**Key Capabilities:**
- Set reminder times
- Multiple reminder types (BEFORE_START, AT_DEADLINE, CUSTOM)
- Reminder notifications

### 6. Notes

Attach documentation to projects and tasks.

**Key Capabilities:**
- Create, update, delete notes
- Attach to projects or tasks
- Rich text content support
- Search notes by content
- Tag-based organization

**Domain Model:**
```go
type NoteEntity struct {
    UserID, TenantID  int64
    Title, Content    string
    ProjectID         *int64
    TaskID            *int64
    Tags              []string
}
```

### 7. Activity Event Tracking

Track all changes to projects and tasks for audit and analytics.

**Key Capabilities:**
- Record all CRUD operations
- Event types (CREATED, UPDATED, DELETED, STATUS_CHANGED, etc.)
- Change log with before/after values
- Event metadata (user, timestamp, entity type)

### 8. Event Publishing

Publish domain events to Kafka for other services to consume.

**Published Events:**
- `task.created` - New task created
- `task.updated` - Task modified
- `task.deleted` - Task deleted
- `task.status.changed` - Task status updated
- `task.completed` - Task marked as complete
- `project.created` - New project created
- `project.updated` - Project modified
- `project.deleted` - Project deleted

**Topics:**
- `ptm-task-events`

## API Routes

All routes require JWT authentication and appropriate roles (PTM_ADMIN or PTM_USER).

### Project Routes

```
POST   /ptm-task/api/v1/projects              # Create project
GET    /ptm-task/api/v1/projects              # List user's projects
GET    /ptm-task/api/v1/projects/:id          # Get project by ID
GET    /ptm-task/api/v1/projects/:id/tasks    # Get tasks by project
GET    /ptm-task/api/v1/projects/:id/notes    # Get notes by project
PATCH  /ptm-task/api/v1/projects/:id          # Update project
DELETE /ptm-task/api/v1/projects/:id          # Delete project
```

### Task Routes

```
POST   /ptm-task/api/v1/tasks                 # Create task
GET    /ptm-task/api/v1/tasks                 # List user's tasks
GET    /ptm-task/api/v1/tasks/:id             # Get task by ID
GET    /ptm-task/api/v1/tasks/:id/tree        # Get task with subtasks (hierarchical)
GET    /ptm-task/api/v1/tasks/:id/notes       # Get notes by task
PATCH  /ptm-task/api/v1/tasks/:id             # Update task
DELETE /ptm-task/api/v1/tasks/:id             # Delete task
```

### Note Routes

```
POST   /ptm-task/api/v1/notes                 # Create note
GET    /ptm-task/api/v1/notes/search          # Search notes
GET    /ptm-task/api/v1/notes/:id             # Get note by ID
PATCH  /ptm-task/api/v1/notes/:id             # Update note
DELETE /ptm-task/api/v1/notes/:id             # Delete note
```

## Domain Enumerations

### Task Status
- `TODO` - Not started
- `IN_PROGRESS` - Currently working on
- `DONE` - Completed
- `CANCELLED` - Cancelled

### Task Priority
- `LOW` - Low priority
- `MEDIUM` - Medium priority
- `HIGH` - High priority
- `URGENT` - Urgent priority

### Project Status
- `NEW` - Newly created
- `IN_PROGRESS` - Active project
- `COMPLETED` - Finished project
- `ON_HOLD` - Temporarily paused
- `CANCELLED` - Cancelled project

### Recurrence Pattern
- `DAILY` - Repeats daily
- `WEEKLY` - Repeats weekly
- `MONTHLY` - Repeats monthly
- `CUSTOM` - Custom recurrence

### Event Type
- `CREATED` - Entity created
- `UPDATED` - Entity updated
- `DELETED` - Entity deleted
- `STATUS_CHANGED` - Status changed
- `COMPLETED` - Task completed
- `ASSIGNED` - Task assigned
- `DEPENDENCY_ADDED` - Dependency added

## Event-Driven Communication

### Published Events

PTM Task Service publishes domain events to Kafka for consumption by other services (PTM Schedule, PTM Optimization).

**Event Format:**
```json
{
  "eventType": "TASK_CREATED",
  "eventId": "uuid",
  "timestamp": 1704067200000,
  "userId": 123,
  "tenantId": 1,
  "aggregateId": "task-456",
  "aggregateType": "TASK",
  "payload": {
    "id": 456,
    "title": "Implement feature X",
    "priority": "HIGH",
    "estimatedDurationMin": 120,
    "deadlineMs": 1704153600000,
    "status": "TODO"
  }
}
```

**Consumers:**
- **PTM Schedule Service** - Listens to task events to trigger automatic rescheduling
- **PTM Optimization Service** - Registers tasks for optimization algorithms

## Development Guidelines

### Code Style

**Go Conventions:**
- Exported identifiers: `PascalCase`
- Unexported identifiers: `camelCase`
- Interfaces: `I` prefix (e.g., `ITaskPort`)
- File names: `snake_case`
- File header: Include author and description

**Import Organization:**
```go
import (
    // Standard library
    "context"
    "fmt"
    
    // External packages
    "github.com/gin-gonic/gin"
    "gorm.io/gorm"
    
    // Internal packages
    "github.com/serp/ptm-task/src/core/domain/entity"
    "github.com/serp/ptm-task/src/core/port/store"
)
```

### Adding New Features

1. **Define Entity**: `core/domain/entity/`
2. **Create DTOs**: `core/domain/dto/request/` and `core/domain/dto/response/`
3. **Define Port Interface**: `core/port/store/`
4. **Implement Service**: `core/service/`
5. **Create UseCase**: `core/usecase/`
6. **Add Controller**: `ui/controller/`
7. **Implement Adapter**: `infrastructure/store/adapter/`
8. **Create Models**: `infrastructure/store/model/`
9. **Add Mapper**: `infrastructure/store/mapper/`
10. **Register in Bootstrap**: `cmd/bootstrap/all.go`

### Critical Rules

1. **Transaction Management**: Use `TransactionService.ExecuteInTransaction` for database transactions
2. **Context Extraction**: Use `utils.GetUserIDFromContext(c)` and `utils.GetTenantIDFromContext(c)`
3. **Event Publishing**: Publish Kafka events AFTER successful DB commit within transaction
4. **Entity ↔ Model Mapping**: Always use mapper structs for conversions
5. **Error Handling**: Return errors explicitly using custom error constants

## Related Services

- **API Gateway** (8080) - Routes requests to this service
- **PTM Schedule Service** (8084) - Consumes task events for scheduling
- **PTM Optimization Service** (8085) - Consumes task events for optimization
- **Account Service** (8081) - Provides user authentication and authorization

## Contributing

1. Follow Go code style guidelines in `AGENTS.md`
2. Write tests for use cases and critical business logic
3. Use table-driven tests with testify
4. Run tests before committing:
   ```bash
   go test ./...
   go fmt ./...
   go vet ./...
   ```

## License

This project is part of the SERP ERP system and is licensed under the MIT License. See the [LICENSE](../LICENSE) file in the root directory for details.

---

For more information about the overall SERP architecture, see the main repository README and `AGENTS.md`.
