# PTM Optimization Service

PTM Optimization Service provides advanced scheduling optimization algorithms for the SERP ERP system. It leverages Google OR-Tools to solve complex task scheduling problems using mathematical optimization techniques, delivering optimal or near-optimal schedules based on multiple constraints and objectives.

## Overview

This service acts as a specialized optimization engine that PTM Schedule Service can delegate to when more sophisticated algorithms are needed beyond the built-in heuristics. It implements multiple optimization strategies ranging from fast heuristics to computationally intensive exact algorithms.

**Technology Stack:**
- Java 21
- Spring Boot 3.5.5
- Google OR-Tools 9.11 (MILP & CP-SAT solvers)
- PostgreSQL (Task Registration)
- Redis (Caching)
- Kafka (Event Consumer)

**Port:** 8085  
**Context Path:** `/ptm-optimization`

## Architecture

This service follows **Clean Architecture** with strategy pattern for algorithm selection:

```
src/main/java/serp/project/ptm_optimization/
├── PtmOptimizationApplication.java    # Application entry point
├── ui/
│   ├── controller/                   # REST API controllers
│   │   ├── OptimizationController.java
│   │   ├── TaskRegistrationController.java
│   │   └── GlobalExceptionHandler.java
│   └── kafkahandler/                 # Kafka message handlers
│       └── TaskMessageHandler.java
├── core/
│   ├── usecase/                      # Business logic orchestration
│   │   ├── OptimizationUseCase.java
│   │   ├── TaskUseCase.java
│   │   └── TaskRegistrationUseCase.java
│   ├── service/                      # Domain services
│   │   ├── impl/
│   │   │   ├── TaskService.java
│   │   │   ├── ParentTaskService.java
│   │   │   └── TaskRegistrationService.java
│   │   ├── ITaskService.java
│   │   ├── IParentTaskService.java
│   │   └── ITaskRegistrationService.java
│   ├── port/                         # Interfaces
│   │   ├── store/                   # Database port interfaces
│   │   ├── strategy/                # Strategy interfaces
│   │   │   └── ISchedulingStrategy.java
│   │   └── factory/
│   │       └── ISchedulingStrategyFactory.java
│   ├── domain/                       # Domain models
│   │   ├── entity/                  # Domain entities
│   │   ├── dto/                     # Data transfer objects
│   │   ├── enums/                   # Enumerations
│   │   ├── mapper/                  # Entity mappers
│   │   ├── constant/                # Constants
│   │   └── callback/                # Event callbacks
│   └── exception/
│       └── AppException.java
├── infrastructure/
│   ├── algorithm/                    # Optimization algorithms
│   │   ├── strategy/                # Strategy implementations
│   │   │   ├── MilpStrategy.java
│   │   │   ├── CpSatStrategy.java
│   │   │   ├── HeuristicStrategy.java
│   │   │   └── LocalSearchStrategy.java
│   │   ├── milp/                    # MILP solver implementations
│   │   │   ├── MilpScheduler.java
│   │   │   └── MilpSchedulerV2.java
│   │   ├── cpsat/                   # CP-SAT solver implementations
│   │   │   ├── CpSatScheduler.java
│   │   │   └── CpSatSchedulerV2.java
│   │   ├── heuristic/               # Heuristic algorithms
│   │   │   └── GapBasedScheduler.java
│   │   ├── localsearch/             # Local search algorithms
│   │   │   ├── LocalSearchScheduler.java
│   │   │   ├── ScheduleState.java
│   │   │   └── Move.java
│   │   ├── base/                    # Base classes
│   │   │   └── AbstractOptimalScheduler.java
│   │   └── dto/                     # Algorithm DTOs
│   │       ├── input/              # Input models
│   │       └── output/             # Output models
│   ├── factory/
│   │   └── SchedulingStrategyFactory.java
│   └── store/                        # Database implementations
│       ├── adapter/                 # Store adapters
│       ├── model/                   # JPA models
│       ├── mapper/                  # Model-Entity mappers
│       └── repository/              # JPA repositories
└── kernel/
    ├── config/                       # Spring configurations
    │   ├── SecurityConfiguration.java
    │   ├── KafkaConsumerConfig.java
    │   ├── KafkaProducerConfig.java
    │   ├── RedisConfig.java
    │   ├── WebClientConfig.java
    │   └── VirtualThreadsConfig.java
    ├── property/                     # Configuration properties
    │   ├── KeycloakProperties.java
    │   └── RequestFilter.java
    └── utils/                        # Utility classes
        ├── SchedulingUtils.java
        ├── UtilityModel.java
        ├── GapManager.java
        └── ...
```

## Core Features

### 1. Multiple Optimization Strategies

The service provides multiple algorithms optimized for different problem sizes and requirements.

#### MILP (Mixed Integer Linear Programming)

**Best For:** Small problems (<30 tasks)  
**Algorithm:** Linear programming with integer constraints  
**Solver:** Google OR-Tools GLOP + Branch and Bound

**Characteristics:**
- Guaranteed optimal solution (if found within time limit)
- Exact mathematical optimization
- Slower for large problems
- Handles complex constraints naturally

**Use Cases:**
- Critical schedules requiring provably optimal solutions
- Small number of high-priority tasks
- Problems with strict constraints

#### CP-SAT (Constraint Programming SAT)

**Best For:** Medium to large problems (30-100 tasks)  
**Algorithm:** Constraint programming with SAT solver  
**Solver:** Google OR-Tools CP-SAT

**Characteristics:**
- Near-optimal to optimal solutions
- Excellent performance on scheduling problems
- Handles complex constraints efficiently
- Scales better than MILP

**Use Cases:**
- Standard daily/weekly scheduling
- Balanced optimization speed and quality
- Complex dependency constraints

#### Heuristic (Gap-Based Greedy)

**Best For:** Large problems (100+ tasks), quick results  
**Algorithm:** Greedy gap-filling heuristic

**Characteristics:**
- Very fast execution (<1 second)
- Good quality solutions
- No optimality guarantees
- Scales to 1000+ tasks

**Use Cases:**
- Real-time scheduling updates
- Fallback when other methods timeout
- Preview/draft schedules

#### Local Search (Simulated Annealing)

**Best For:** Improving existing solutions, large problems  
**Algorithm:** Simulated annealing with custom move operators

**Characteristics:**
- Iterative improvement
- Can escape local optima
- Quality improves with time
- Warm-start from heuristic solution

**Use Cases:**
- Refining heuristic solutions
- Long-running optimization jobs
- Custom quality criteria

### 2. Automatic Strategy Selection

Smart selection based on problem characteristics.

**Selection Logic:**
```
if tasks.size() < 30:
    use MILP (optimal solution)
elif tasks.size() < 100:
    use CP-SAT (near-optimal, fast)
else:
    use HEURISTIC + LOCAL_SEARCH (scalable)
```

### 3. Fallback Chain Optimization

Try multiple strategies sequentially with time limits.

**Process:**
1. Try CP-SAT with 30s timeout
2. If timeout or no solution, try MILP with 20s timeout
3. If still no solution, fall back to Heuristic (always succeeds)

### 4. Task Registration

Register tasks for optimization tracking and analytics.

**Capabilities:**
- Store task metadata
- Track optimization history
- Parent-child task relationships
- Integration with Kafka events

### 5. Optimization Request/Response Model

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
      "earliestStartMs": 1704067200000,
      "isDeepWork": true,
      "isFlexible": false,
      "category": "development"
    }
  ],
  "windows": [
    {
      "dateMs": 1704067200000,
      "startMin": 540,
      "endMin": 1020,
      "energyLevel": 0.9,
      "label": "Morning Focus"
    }
  ],
  "weights": {
    "urgency": 2.0,
    "priority": 1.5,
    "preference": 1.0,
    "energyMatch": 1.0,
    "continuity": 0.5,
    "gapPenalty": 0.3
  },
  "params": {
    "timeLimitSec": 30,
    "requireOptimal": false,
    "allowSplit": true,
    "minSplitDurationMin": 30
  }
}
```

**Response Format:**
```json
{
  "assignments": [
    {
      "taskId": 1,
      "windowId": 1,
      "dateMs": 1704067200000,
      "startMin": 540,
      "endMin": 600,
      "utilityScore": 8.5,
      "partIndex": 1,
      "totalParts": 1
    }
  ],
  "unscheduledTasks": [
    {
      "taskId": 2,
      "reason": "NO_FEASIBLE_WINDOW",
      "details": "Deadline too tight"
    }
  ],
  "utilityBreakdown": {
    "totalUtility": 85.3,
    "urgencyScore": 35.0,
    "priorityScore": 25.0,
    "preferenceScore": 15.3,
    "energyMatchScore": 10.0
  },
  "solutionMetadata": {
    "strategyUsed": "CP_SAT",
    "optimizationTimeMs": 1234,
    "isOptimal": true,
    "scheduledCount": 25,
    "unscheduledCount": 1
  }
}
```

### 6. Event-Driven Task Synchronization

Listen to task events from PTM Task Service via Kafka.

**Consumed Events:**
- `task.created` - Register new task
- `task.updated` - Update task metadata
- `task.deleted` - Remove task registration

**Consumer Group:** `ptm-optimization`  
**Topics:** `ptm-task-events`

## API Routes

### Optimization Routes

```
POST   /ptm-optimization/api/v1/optimization/schedule               # Optimize with specific strategy
POST   /ptm-optimization/api/v1/optimization/schedule-with-fallback # Optimize with fallback chain
GET    /ptm-optimization/api/v1/optimization/strategies             # List available strategies
```

### Task Registration Routes (Protected)

```
POST   /ptm-optimization/api/v1/task-registrations     # Register task
GET    /ptm-optimization/api/v1/task-registrations     # List registered tasks
GET    /ptm-optimization/api/v1/task-registrations/:id # Get task registration
PUT    /ptm-optimization/api/v1/task-registrations/:id # Update registration
DELETE /ptm-optimization/api/v1/task-registrations/:id # Delete registration
```

## Algorithm Details

### MILP Formulation

**Decision Variables:**
- `x[t][w]` - Binary: task t assigned to window w
- `start[t]` - Integer: start minute of task t

**Objective Function:**
```
Maximize: Σ (priority[t] * utility[t][w] * x[t][w])
```

**Constraints:**
- Each task assigned to at most one window
- Tasks fit within window duration
- No overlapping assignments in same window
- Respect deadlines and earliest start times
- Deep work tasks only in high-energy windows

### CP-SAT Formulation

**Interval Variables:**
- `task_interval[t]` - Interval variable for task t
- `window_interval[w]` - Interval variable for window w

**Constraints:**
- `AddNoOverlap` - Tasks in same window don't overlap
- `AddCumulative` - Resource capacity constraints
- `AddElement` - Task-window assignment constraints
- Precedence constraints for dependencies
- Time window constraints (earliest start, deadline)

**Search Strategy:**
- Choose variable: Select highest priority unassigned task
- Choose value: Prefer windows with higher energy match
- Heuristic: Least-regret branching

### Heuristic Algorithm

**Gap-Based Greedy:**
1. Sort tasks by priority and urgency
2. For each task:
   - Find all compatible windows
   - Score each window based on utility function
   - Select highest-scoring window
   - Place task in best gap within window
3. Fill remaining gaps with flexible tasks

**Time Complexity:** O(n * w * g) where n=tasks, w=windows, g=gaps

### Local Search Algorithm

**Simulated Annealing:**
1. Start with heuristic solution
2. Generate neighbor solution via move:
   - Swap two tasks
   - Move task to different window
   - Shift task within window
3. Accept better solutions always
4. Accept worse solutions with probability e^(-ΔE/T)
5. Gradually decrease temperature T
6. Terminate after max iterations or no improvement

## Configuration

### OR-Tools Configuration

**JVM Arguments Required:**
```bash
--enable-native-access=ALL-UNNAMED
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
-XX:+EnableDynamicAgentLoading
-Djava.library.path=/path/to/ortools/lib
-Xmx1g
```

These are configured in `pom.xml` for Maven Surefire plugin.

## Domain Enumerations

### Strategy Type
- `AUTO` - Auto-select based on problem size
- `HEURISTIC` - Greedy + Local Search
- `MILP` - Mixed Integer Linear Programming
- `CPSAT` - Constraint Programming SAT
- `LOCAL_SEARCH` - Simulated Annealing
- `HYBRID` - Heuristic + CPSAT/MILP refinement

### Unscheduled Reason
- `NO_FEASIBLE_WINDOW` - No available time slot
- `DEADLINE_TOO_TIGHT` - Cannot meet deadline
- `INSUFFICIENT_DURATION` - Windows too short
- `CONSTRAINT_CONFLICT` - Conflicting constraints
- `OPTIMIZATION_TIMEOUT` - Solver timeout

## Development Guidelines

### Code Style

**Java Conventions:**
- Classes/Interfaces: `PascalCase`
- Methods/Variables: `camelCase`
- Interfaces: `I` prefix (e.g., `ISchedulingStrategy`)
- Use Lombok annotations
- Constructor injection via `@RequiredArgsConstructor`

### Adding New Strategy

1. **Create Strategy Class**: `infrastructure/algorithm/strategy/`
2. **Implement ISchedulingStrategy**: Define `solve()` method
3. **Register in Factory**: Add to `SchedulingStrategyFactory`
4. **Add to StrategyType Enum**: Define new strategy type
5. **Write Tests**: Test with various problem sizes

### Critical Rules

1. **OR-Tools Initialization**: Always load native library before use
2. **Memory Management**: Set appropriate JVM heap size (-Xmx)
3. **Timeout Handling**: Implement timeout for all solvers
4. **Validation**: Validate input constraints before solving
5. **Error Handling**: Return partial solutions on timeout, not failure

## Performance Characteristics

### Strategy Comparison

| Strategy | Tasks | Time | Quality | Memory |
|----------|-------|------|---------|--------|
| MILP | <30 | 1-30s | Optimal | High |
| CP-SAT | 30-100 | 2-45s | Near-optimal | Medium |
| Heuristic | 100+ | <1s | Good | Low |
| Local Search | 100+ | 5-60s | Very Good | Low |

### Recommended Usage

- **Real-time updates**: Heuristic
- **Daily scheduling**: CP-SAT
- **Critical schedules**: MILP
- **Large backlogs**: Heuristic + Local Search
- **Uncertain**: Auto or Fallback Chain

## Related Services

- **PTM Schedule Service** (8084) - Main consumer of optimization API
- **PTM Task Service** (8083) - Provides task data via Kafka
- **Account Service** (8081) - Authentication and authorization

## Contributing

1. Follow Java code style guidelines in `AGENTS.md`
2. Write tests for optimization algorithms
3. Benchmark performance with various problem sizes
4. Validate solutions for correctness
5. Run tests before committing:
   ```bash
   ./mvnw test
   ./mvnw clean package
   ```

## License
This project is part of the SERP ERP system and is licensed under the MIT License. See the [LICENSE](../LICENSE) file in the root directory for details.

---

For more information about the overall SERP architecture, see the main repository README and `AGENTS.md`.
