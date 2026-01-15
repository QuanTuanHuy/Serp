# PTM Schedule Service

PTM Schedule Service is an intelligent task scheduling microservice in the SERP ERP system. It automatically creates optimized schedules by analyzing user tasks, availability calendars, and applying sophisticated scheduling algorithms to maximize productivity.

## Overview

This service consumes task events from PTM Task Service and generates optimal schedules based on user availability, task priorities, deadlines, and preferences. It supports multiple scheduling strategies including built-in hybrid algorithms and integration with the PTM Optimization Service for advanced optimization.

**Technology Stack:**
- Go 1.25.0
- Gin Web Framework
- GORM (PostgreSQL)
- Kafka (Consumer & Producer)
- Redis (Caching)
- Uber FX (Dependency Injection)
- Integration with PTM Optimization Service

**Port:** 8084  
**Context Path:** `/ptm-schedule`

## Architecture

This service follows **Clean Architecture** with event-driven components:

```
src/
├── main.go                    # Application entry point
├── cmd/
│   └── bootstrap/            # Dependency injection & initialization
│       ├── all.go           # Main module assembly
│       ├── kafka_consumer.go # Kafka consumer setup
│       ├── reschedule_worker.go # Background worker
│       ├── event_cleanup.go # Event cleanup worker
│       └── migrate.go       # Database migrations
├── ui/
│   ├── controller/          # HTTP request handlers
│   │   ├── schedule_plan_controller.go
│   │   ├── schedule_event_controller.go
│   │   ├── schedule_task_controller.go
│   │   ├── schedule_window_controller.go
│   │   └── availability_calendar_controller.go
│   ├── kafka/               # Kafka message handlers
│   │   ├── ptm_task_handler.go
│   │   └── message_processing_middleware.go
│   └── worker/              # Background workers
│       └── reschedule_worker.go
├── core/
│   ├── usecase/             # Business logic orchestration
│   │   ├── schedule_plan_usecase.go
│   │   ├── schedule_event_usecase.go
│   │   ├── schedule_task_usecase.go
│   │   ├── schedule_window_usecase.go
│   │   ├── availability_calendar_usecase.go
│   │   └── optimization_usecase.go
│   ├── service/             # Domain services
│   │   ├── schedule_plan_service.go
│   │   ├── schedule_event_service.go
│   │   ├── schedule_task_service.go
│   │   ├── schedule_window_service.go
│   │   ├── availability_calendar_service.go
│   │   ├── reschedule_strategy_service.go
│   │   ├── reschedule_queue_service.go
│   │   ├── idempotency_service.go
│   │   └── transaction_service.go
│   ├── domain/
│   │   ├── entity/         # Domain entities
│   │   ├── dto/            # Data transfer objects
│   │   ├── enum/           # Enumerations
│   │   ├── algorithm/      # Scheduling algorithms
│   │   │   ├── hybrid_scheduler.go
│   │   │   ├── scoring_service.go
│   │   │   ├── time_slot_utils.go
│   │   │   └── algorithm_mapper.go
│   │   ├── mapper/         # Entity mappers
│   │   └── constant/       # Constants
│   └── port/                # Interfaces
│       ├── store/          # Database port interfaces
│       └── client/         # External client interfaces
├── infrastructure/
│   ├── store/              # Database implementations
│   │   ├── adapter/       # Store adapters
│   │   ├── model/         # Database models
│   │   └── mapper/        # Model-Entity mappers
│   └── client/             # External clients
│       ├── optimization_client_adapter.go
│       └── kafka_producer_adapter.go
├── kernel/
│   ├── properties/         # Configuration properties
│   └── utils/              # Utility functions
└── config/
    ├── default.yaml        # Default configuration
    ├── local.yaml          # Local development config
    └── production.yaml     # Production config
```

## Core Features

### 1. Schedule Plan Management

Manage schedule plans with lifecycle states and versioning.

**Key Capabilities:**
- Create active rolling schedules
- Generate proposed optimization plans
- Maintain historical plans
- Plan transitions (apply, revert, discard)
- Version control for plans
- Track optimization metrics (score, algorithm, duration)

**Plan States:**
- `DRAFT` - Initial state
- `PROCESSING` - Being optimized
- `PROPOSED` - Ready for review
- `ACTIVE` - Currently active
- `ARCHIVED` - Historical record
- `DISCARDED` - Rejected proposal

**Domain Model:**
```go
type SchedulePlanEntity struct {
    UserID, TenantID    int64
    StartDateMs         int64
    EndDateMs           *int64
    PlanName, PlanType  string
    
    AlgorithmUsed             Algorithm
    OptimizationScore         float64
    OptimizationTimestamp     int64
    OptimizationDurationMs    int64
    
    Version       int32
    ParentPlanID  *int64
    Status        PlanStatus
    IsStale       bool
}
```

### 2. Schedule Events (Time Assignments)

Schedule events represent specific time slots assigned to tasks.

**Key Capabilities:**
- Create time block assignments
- Move events to different time slots
- Split events into multiple parts
- Complete or skip events
- Pin events to prevent rescheduling
- Track actual vs planned times
- Linked events (for split tasks)

**Domain Model:**
```go
type ScheduleEventEntity struct {
    SchedulePlanID  int64
    ScheduleTaskID  int64
    
    DateMs           int64
    StartMin, EndMin int    // Minutes from midnight
    Title            string
    
    PartIndex, TotalParts  int
    LinkedEventID          *int64
    
    Status       ScheduleEventStatus
    IsPinned     bool
    UtilityScore *float64
    
    ActualStartMin, ActualEndMin  *int
}
```

### 3. Schedule Tasks

Materialized view of tasks with scheduling-specific metadata.

**Key Capabilities:**
- Sync from PTM Task Service
- Track scheduling requirements
- Maintain scheduling constraints
- Monitor scheduling status

**Domain Model:**
```go
type ScheduleTaskEntity struct {
    OriginalTaskID   int64
    UserID, TenantID int64
    
    Title       string
    Priority    string
    DurationMin int
    
    DeadlineMs          *int64
    PreferredStartMs    *int64
    EarliestStartMs     *int64
    
    IsDeepWork, IsMeeting  bool
    IsFlexible             bool
    
    SchedulingStatus  SchedulingStatus
    LastScheduledMs   *int64
}
```

### 4. Availability Calendar

Manage user availability patterns and working hours.

**Key Capabilities:**
- Define weekly availability patterns
- Set working hours per day
- Block out unavailable times
- Calendar exceptions (holidays, special events)
- Default availability templates

**Domain Model:**
```go
type AvailabilityCalendarEntity struct {
    UserID, TenantID  int64
    DayOfWeek         int  // 0=Sunday, 6=Saturday
    
    IsAvailable       bool
    StartMin, EndMin  *int  // Working hours
    
    Priority          int
    Label             string
}
```

### 5. Schedule Windows

Materialized time slots where tasks can be scheduled.

**Key Capabilities:**
- Generate available time windows from calendar
- Filter by date range
- Exclude busy times
- Prioritize windows
- Efficient slot allocation

### 6. Automatic Rescheduling

Background worker that triggers rescheduling based on task changes.

**Key Capabilities:**
- Listen to task events via Kafka
- Queue reschedule requests
- Debounce frequent changes
- Intelligent strategy selection
- Fallback mechanisms

**Reschedule Strategies:**
- **Quick Reschedule** - Fast built-in hybrid algorithm
- **Deep Optimization** - Call PTM Optimization Service with MILP/CP-SAT
- **Fallback Chain** - Try multiple strategies sequentially

### 7. Scheduling Algorithms

Built-in hybrid scheduling algorithm with multi-factor scoring.

**Algorithm Capabilities:**
- Priority-based scheduling
- Deadline awareness
- Preferred time matching
- Energy level optimization (deep work scheduling)
- Context switching minimization
- Gap filling

**Scoring Factors:**
- Urgency score (deadline proximity)
- Priority score
- Preference score (time of day)
- Energy level score
- Continuity bonus
- Gap penalty

### 8. Optimization Service Integration

Delegate complex scheduling to PTM Optimization Service for optimal solutions.

**Key Capabilities:**
- Request format construction
- Strategy selection (MILP, CP-SAT, Heuristic)
- Timeout and retry handling
- Result parsing and validation
- Fallback to built-in algorithm

### 9. Idempotency & Event Processing

Ensure reliable event processing with idempotency guarantees.

**Key Capabilities:**
- Track processed events
- Detect and skip duplicates
- Failed event retry mechanism
- Event cleanup (retention policy)

## API Routes

All routes require JWT authentication and PTM_ADMIN or PTM_USER role.

### Schedule Plan Routes

```
POST   /ptm-schedule/api/v1/schedule-plans                    # Get or create active plan
GET    /ptm-schedule/api/v1/schedule-plans/active             # Get active plan
GET    /ptm-schedule/api/v1/schedule-plans/active/detail      # Get active plan with events
GET    /ptm-schedule/api/v1/schedule-plans/history            # Get historical plans
POST   /ptm-schedule/api/v1/schedule-plans/reschedule         # Trigger quick reschedule
POST   /ptm-schedule/api/v1/schedule-plans/deep-optimize      # Trigger deep optimization
POST   /ptm-schedule/api/v1/schedule-plans/fallback-chain-optimize  # Try multiple strategies
GET    /ptm-schedule/api/v1/schedule-plans/:id                # Get plan by ID
GET    /ptm-schedule/api/v1/schedule-plans/:id/events         # Get plan with events
POST   /ptm-schedule/api/v1/schedule-plans/:id/apply          # Apply proposed plan
POST   /ptm-schedule/api/v1/schedule-plans/:id/revert         # Revert to previous plan
DELETE /ptm-schedule/api/v1/schedule-plans/:id                # Discard proposed plan
```

### Schedule Event Routes

```
GET    /ptm-schedule/api/v1/schedule-events                   # List events (filtered)
POST   /ptm-schedule/api/v1/schedule-events                   # Save multiple events
POST   /ptm-schedule/api/v1/schedule-events/:id/move          # Manually move event
POST   /ptm-schedule/api/v1/schedule-events/:id/complete      # Mark event complete
POST   /ptm-schedule/api/v1/schedule-events/:id/split         # Split event into parts
```

### Availability Calendar Routes

```
GET    /ptm-schedule/api/v1/availability-calendar             # Get user availability
POST   /ptm-schedule/api/v1/availability-calendar             # Set availability pattern
PUT    /ptm-schedule/api/v1/availability-calendar             # Replace entire calendar
```

### Schedule Window Routes

```
GET    /ptm-schedule/api/v1/schedule-windows                  # List available windows
POST   /ptm-schedule/api/v1/schedule-windows/materialize      # Generate windows for date range
```

### Schedule Task Routes

```
GET    /ptm-schedule/api/v1/schedule-tasks                    # List schedule tasks
```

## Domain Enumerations

### Plan Status
- `DRAFT` - Being created
- `PROCESSING` - Running optimization
- `PROPOSED` - Awaiting approval
- `ACTIVE` - Currently active
- `ARCHIVED` - Historical
- `DISCARDED` - Rejected

### Schedule Event Status
- `PLANNED` - Scheduled but not started
- `DONE` - Completed
- `SKIPPED` - Skipped/cancelled

### Scheduling Status
- `UNSCHEDULED` - Not yet scheduled
- `SCHEDULED` - Has time slots
- `PARTIALLY_SCHEDULED` - Some parts scheduled
- `COMPLETED` - Finished

### Algorithm Type
- `HYBRID` - Built-in hybrid algorithm
- `MILP` - Mixed Integer Linear Programming
- `CP_SAT` - Constraint Programming SAT
- `HEURISTIC` - Heuristic approach
- `LOCAL_SEARCH` - Local search optimization


## Event-Driven Communication

### Consumed Events (from PTM Task Service)

Listens to task events and triggers rescheduling:

```json
{
  "eventType": "TASK_CREATED",
  "eventId": "uuid",
  "timestamp": 1704067200000,
  "userId": 123,
  "tenantId": 1,
  "aggregateId": "task-456",
  "aggregateType": "TASK",
  "payload": { /* task data */ }
}
```

**Kafka Topics:**
- `ptm-task-events` (consumed)

**Consumer Group:** `ptm-schedule-group`

**Event Types Handled:**
- `TASK_CREATED` - New task added
- `TASK_UPDATED` - Task modified (priority, deadline, duration)
- `TASK_DELETED` - Task removed
- `TASK_COMPLETED` - Task finished

### Published Events

Publishes schedule change events:

```json
{
  "eventType": "SCHEDULE_UPDATED",
  "userId": 123,
  "tenantId": 1,
  "planId": 789,
  "timestamp": 1704067200000
}
```

**Topics:**
- `ptm-schedule-events`

## Scheduling Algorithm

### Hybrid Scheduling Algorithm

Built-in algorithm that balances multiple factors for optimal scheduling.

**Process:**
1. **Load Available Windows** - Get time slots from availability calendar
2. **Load Tasks** - Get unscheduled tasks sorted by priority
3. **Score Each Assignment** - Calculate utility score for task-window pairs
4. **Greedy Assignment** - Assign tasks to best-scoring windows
5. **Gap Filling** - Fill small gaps with flexible tasks
6. **Validation** - Ensure constraints satisfied

**Scoring Formula:**
```
Total Score = 
  + Urgency Score (deadline proximity)
  + Priority Score (task priority)
  + Preference Score (preferred time match)
  + Energy Level Score (deep work during peak hours)
  + Continuity Bonus (group similar tasks)
  - Gap Penalty (avoid fragmentation)
```

**Features:**
- Respects task deadlines
- Honors user preferences
- Optimizes for deep work during peak hours
- Minimizes context switching
- Fills gaps efficiently

### Optimization Service Integration

For complex scenarios, delegates to PTM Optimization Service.

**When to Use:**
- Large number of tasks (>50)
- Complex dependencies
- Tight deadlines
- User requests deep optimization

**Request Format:**
```json
{
  "tasks": [
    {
      "id": 1,
      "title": "Task A",
      "durationMin": 60,
      "priority": 5,
      "deadlineMs": 1704153600000,
      "isDeepWork": true
    }
  ],
  "windows": [
    {
      "dateMs": 1704067200000,
      "startMin": 540,
      "endMin": 1020,
      "energyLevel": 0.9
    }
  ],
  "params": {
    "strategy": "CP_SAT",
    "timeLimitSec": 30
  }
}
```

## Background Workers

### Reschedule Worker

Processes queued reschedule requests.

**Functionality:**
- Poll reschedule queue
- Debounce rapid changes
- Select appropriate strategy
- Update schedule plans
- Notify users of changes

**Trigger Conditions:**
- Task created/updated/deleted
- Availability calendar changed
- Manual reschedule request

### Event Cleanup Worker

Maintains event processing tables.

**Functionality:**
- Clean up old processed events
- Archive failed events
- Retention policy enforcement

## Development Guidelines

### Code Style

**Go Conventions:**
- Exported identifiers: `PascalCase`
- Unexported identifiers: `camelCase`
- Interfaces: `I` prefix
- File names: `snake_case`

### Adding New Features

1. **Define Entity**: `core/domain/entity/`
2. **Create DTOs**: `core/domain/dto/`
3. **Define Port**: `core/port/`
4. **Implement Service**: `core/service/`
5. **Create UseCase**: `core/usecase/`
6. **Add Controller**: `ui/controller/`
7. **Implement Adapter**: `infrastructure/`
8. **Register in Bootstrap**: `cmd/bootstrap/all.go`

### Critical Rules

1. **Transaction Management**: Use `TransactionService` for database operations
2. **Idempotency**: Always check `IdempotencyService` before processing events
3. **Event Publishing**: Publish events after successful DB commit
4. **Context Extraction**: Use utils for user/tenant context
5. **Error Handling**: Return errors explicitly

## Related Services

- **API Gateway** (8080) - Routes requests to this service
- **PTM Task Service** (8083) - Provides task data via Kafka events
- **PTM Optimization Service** (8085) - Advanced optimization algorithms
- **Account Service** (8081) - Authentication and authorization

## Contributing

1. Follow Go code style guidelines in `AGENTS.md`
2. Write tests for algorithms and critical logic
3. Test Kafka event handling with mocks
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
