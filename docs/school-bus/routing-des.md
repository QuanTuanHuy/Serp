# School Bus Routing & Timeline Calculation Design

This document details the configuration-driven routing architecture, straight-line fallback calculations, the N x N routing matrix generation service, and route stop timeline calculation logic for the School Bus operations module.

---

## 1. Dynamic Routing Configuration (`school_bus_app_config`)

To allow live tuning and prevent code modifications during simulations or operations, all routing parameters are dynamically fetched globally from a shared configuration table, rather than being restricted per tenant.

### Configuration Parameters

| Config Code | Data Type | Default Value | Description |
|---|---|---|---|
| `ROUTING_AVERAGE_SPEED_KMPH` | `DECIMAL` | `25.0` | Average vehicle speed in km/h for travel time estimations. |
| `ROUTING_DWELL_TIME_MINUTES` | `INTEGER` | `2` | Default stop dwell time in minutes at each pickup/drop-off. |
| `ROUTING_ROAD_FACTOR` | `DECIMAL` | `1.3` | Multiplier applied to straight-line distance to estimate actual road network distance. |
| `ROUTING_OSRM_ENABLED` | `BOOLEAN` | `true` | Toggle to turn OSRM routing on or off. If disabled or failing, fallback is used. |
| `ROUTING_WEIGHT_DISTANCE` | `DECIMAL` | `1.0` | Weight coefficient for distance cost in the objective function. |
| `ROUTING_WEIGHT_DURATION` | `DECIMAL` | `1.0` | Weight coefficient for duration cost in the objective function. |
| `ROUTING_WEIGHT_ROUTE_COUNT` | `DECIMAL` | `10.0` | Weight penalty per active route in the solution objective function. |
| `ROUTING_WEIGHT_UNASSIGNED` | `DECIMAL` | `1000.0` | Weight penalty per unassigned student in the solution objective function. |
| `ROUTING_WEIGHT_WAIT_TIME` | `DECIMAL` | `0.5` | Weight coefficient for student wait time cost in the objective function. |
| `ROUTING_WEIGHT_BLOCKING_ISSUE` | `DECIMAL` | `10000.0` | Penalty weight for occurrences of blocking issues. |
| `ROUTING_WEIGHT_WARNING_ISSUE` | `DECIMAL` | `50.0` | Penalty weight for occurrences of warning issues. |
| `ROUTING_WEIGHT_CAPACITY_EXCESS` | `DECIMAL` | `10000.0` | Penalty weight per student exceeding bus capacity constraints. |
| `ROUTING_WEIGHT_LOAD_BALANCE` | `DECIMAL` | `2.0` | Weight penalty for student load imbalance among routes. |

> [!NOTE]
> All weights are fully configurable at run-time in the `school_bus_app_config` table, allowing the optimization engines to adapt dynamically to operational goals.

---

## 2. Configuration Resolver (`RoutingConfigResolver`)

The config resolver serves as a centralized provider of routing configuration settings:

- Luồng đọc config:
  `Routing service` $\to$ `RoutingConfigResolver` $\to$ `AppConfigService` $\to$ `SchoolBusAppConfigRepository.findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(...)`
- Standardized access keys are defined as constant fields inside `AppConfigCode` class.
- Safely converts database values to target types and logs warnings on parsing errors.
- Automatically falls back to safe, hardcoded defaults (25.0 km/h speed, 2 minutes dwell, 1.3 road factor, OSRM enabled) to keep routing pipelines active.

---

## 3. Fallback Straight-Line Routing

When OSRM is disabled via config or becomes unreachable, the fallback routing engine (`StraightLineFallbackRoutingEngineServiceImpl`) estimates distances and durations:

1. **Distance Estimation**: Calculates the Haversine distance between sequential coordinates, then applies the `ROUTING_ROAD_FACTOR`:
   $$\text{Estimated Road Distance} = \text{Haversine Distance} \times \text{Road Factor}$$
2. **Duration Estimation**: Translates the estimated distance into duration using `ROUTING_AVERAGE_SPEED_KMPH`:
   $$\text{Duration (minutes)} = \frac{\text{Estimated Road Distance}}{\text{Average Speed}} \times 60$$

---

## 4. N x N Routing Matrix Service

To facilitate multi-stop distance/duration matrix computation for the frontend and routing algorithms, a dedicated matrix API endpoint is exposed.

### API Endpoint: `POST /routes/matrix`

- **Request Body**: `List<RoutingPointRequest>`
  Each point has `latitude`, `longitude`, and an optional `id`.
- **Response Body**: `RoutingMatrixResponse`
  Contains `durations` (2D double array of transit times in minutes) and `distances` (2D double array of distances in kilometers) from point $i$ to point $j$.

### Resolution Strategy
- Uses OSRM Table API if `ROUTING_OSRM_ENABLED` is true.
- Falls back to pairwise Haversine distance computations if OSRM is disabled or fails.

---

## 5. Timeline Calculation & Issue Validation (`TimelineCalculatorService`)

The timeline calculator dynamically builds stop arrival/departure times, updates the route planning start/end boundaries, and evaluates business validations to generate planning issues.

### Stop Dwell Rules
- **Terminal Stops (`DEPOT`, `SCHOOL`)**: Dwell time is `0`.
- **Pickup & Drop-off Stops**: Dwell time equals the configured value (`ROUTING_DWELL_TIME_MINUTES`).

### Workflow Directions

#### A. Outbound Routes (To School)
Outbound routes must drop students off at school before the schedule's `arrivalDeadline`. The calculation works **backward** (time-inversion):
1. **End Terminal (SCHOOL)**:
   - $\text{Planned Arrival Time} = \text{Arrival Deadline}$
   - $\text{Planned Departure Time} = \text{Arrival Deadline}$
2. **Iterating Backward (from school to depot)**:
   - For each stop $i$ (going from $N-1$ down to $0$):
     - $\text{Planned Departure Time}_i = \text{Planned Arrival Time}_{i+1} - \text{Travel Time}_{i \to i+1}$
     - $\text{Planned Arrival Time}_i = \text{Planned Departure Time}_i - \text{Dwell Time}_i$
3. **Route Timeline**:
   - $\text{Planned Start Time} = \text{Planned Departure Time}_0$ (Depot departure)
   - $\text{Planned End Time} = \text{Arrival Deadline}$

#### B. Return Routes (From School)
Return routes pick students up from school starting at the schedule's `departureTime`. The calculation works **forward**:
1. **Start Terminal (SCHOOL)**:
   - $\text{Planned Arrival Time} = \text{Departure Time}$
   - $\text{Planned Departure Time} = \text{Departure Time}$
2. **Iterating Forward (from school to depot)**:
   - For each stop $i$ (going from $1$ up to $N$):
     - $\text{Planned Arrival Time}_i = \text{Planned Departure Time}_{i-1} + \text{Travel Time}_{i-1 \to i}$
     - $\text{Planned Departure Time}_i = \text{Planned Arrival Time}_i + \text{Dwell Time}_i$
3. **Route Timeline**:
   - $\text{Planned Start Time} = \text{Departure Time}$
   - $\text{Planned End Time} = \text{Planned Arrival Time}_N$ (Depot arrival)

### Routing Validation & Issues

During the timeline calculation, the route is audited against operational constraints. When violations occur, planning issues are created and persisted.

| Issue Code | Severity | Description | Trigger Condition |
|---|---|---|---|
| `MISSING_COORDINATES` | `BLOCKING` | Stop coordinates missing | Stop latitude/longitude is null. |
| `MATRIX_CELL_MISSING` | `BLOCKING` | Missing travel matrix data | Distance/travel time from previous stop is null. |
| `MISSING_TIME_WINDOW` | `BLOCKING` | Missing pickup/drop-off time window | No time window is configured for the stop according to the route direction. |
| `TIME_WINDOW_LATE` | `BLOCKING` | Late window arrival | Stop arrival time exceeds pickup point window end time. |
| `SCHOOL_ARRIVAL_DEADLINE_MISSED` | `BLOCKING` | Missed school deadline | Outbound planned arrival time at school is after schedule arrival deadline. |
| `OSRM_FALLBACK_USED` | `INFO` | Straight-line fallback used | OSRM was unavailable or disabled; straight-line estimate used for route geometry. |

#### Duplicate Issue Cleanup
To prevent duplicate issue logs when routes are re-calculated or stops are modified, the system queries and soft-deletes (`is_deleted = true`, `is_active = false`) all prior issues linked to the route before generating new ones.

### Feasibility Output in Phase 3

Phase 3 does not calculate objective or quality score.

It only produces:
- planned stop timeline
- routing issues
- issueCount
- blockingIssueCount
- route feasibility status if available

Formal objective scoring is intentionally deferred to Phase 6, where weighted criteria such as distance, duration, route count, unassigned students, and time-window violations will be configured through `school_bus_app_config`.

---

## 6. Example Timeline Calculation

### OUTBOUND

```txt
Input:
  Depot → Pickup A → Pickup B → School
  Arrival deadline: 07:00
  Dwell time: 2 minutes
  Travel times:
    Depot → A = 8 min
    A → B = 6 min
    B → School = 12 min

Backward calculation steps:
  1. School (End Terminal): Planned Arrival = 07:00, Planned Departure = 07:00
  2. Pickup B:
     - Planned Departure = School Arrival (07:00) - Travel (12 min) = 06:48
     - Planned Arrival = Departure (06:48) - Dwell (2 min) = 06:46
  3. Pickup A:
     - Planned Departure = B Arrival (06:46) - Travel (6 min) = 06:40
     - Planned Arrival = Departure (06:40) - Dwell (2 min) = 06:38
  4. Depot (Start Terminal):
     - Planned Departure = A Arrival (06:38) - Travel (8 min) = 06:30
     - Planned Arrival = 06:30

Output timeline:
  School arrival = 07:00
  Pickup B departure = 06:48, arrival = 06:46
  Pickup A departure = 06:40, arrival = 06:38
  Depot departure = 06:30
```

### RETURN

```txt
Input:
  School → Drop-off A → Depot
  Departure time: 11:00
  Dwell time: 2 minutes
  Travel times:
    School → A = 10 min
    A → Depot = 15 min

Forward calculation steps:
  1. School (Start Terminal): Planned Arrival = 11:00, Planned Departure = 11:00
  2. Drop-off A:
     - Planned Arrival = School Departure (11:00) + Travel (10 min) = 11:10
     - Planned Departure = Arrival (11:10) + Dwell (2 min) = 11:12
  3. Depot (End Terminal):
     - Planned Arrival = A Departure (11:12) + Travel (15 min) = 11:27
     - Planned Departure = 11:27

Output timeline:
  School departure = 11:00
  Drop-off A arrival = 11:10, departure = 11:12
  Depot arrival = 11:27
```

---

## 7. Route Calculation Trace Persistence

To facilitate technical audit, algorithm debugging, demonstration of matrix calculations, and to provide data for route export/benchmarking, the system persists a snapshot trace of every route calculation run.

### Database Table: `school_bus_route_calculation_trace`

Unlike operational objects (such as `RoutePlanningIssue` which are soft-deleted and regenerated on each run to reflect the current state), calculation traces preserve historical runs for auditing and benchmarking.

The trace record captures:
- **Operational IDs**: Link to `route_plan_id`, `planning_session_id`, and `tenant_id`.
- **Calculation Type & Status**: Type `MATRIX_AND_TIMELINE` and status `SUCCESS`, `PARTIAL`, or `FAILED`.
- **input_json**: Context parameter snapshot (route ID, direction, stops list with sequence, depot, service date).
- **matrix_json**: Pairwise distance and duration matrix, including engine source (`OSRM` or `FALLBACK`).
- **timeline_json**: The calculated stop-level planned arrival/departure times, distance/duration from previous stops, and dwell settings.
- **issues_json**: Serialized list of issues generated during this specific calculation run.
- **config_snapshot_json**: Snapshot of `school_bus_app_config` parameters active during calculation.
- **source_summary**: Provider string (e.g. `OSRM` or `STRAIGHT_LINE_FALLBACK`).

> [!NOTE]
> **Trace Retention Policy**: Currently, every recalculation appends a new trace row. A retention policy may be configured in the future to prune old audit logs (TODO).

### 7.1. Export Routing Trace to Excel (Phase 3.2)

To enable debugging and external auditing, users can export calculation traces directly to an Excel sheet. 

> [!IMPORTANT]
> **Phase 3.2 Scope Boundary**:
> Phase 3.2 exports only route calculation traces. A route calculation trace is created after a route already exists and compute path/timeline is executed. This export is not the planning matrix export before route generation. Planning-context full matrix export is deferred to Phase 5/7.
>
> **TODO Phase 5/7**:
> Persist and export planning-context full N x N matrix before route generation. This will be used by greedy route generation and experiment benchmark.

- **Orchestration Flow**:
  - `ExportController / RouteController` receives the export request.
  - Resolves the specific `ExportHandler` from `ExportHandlerResolver` using `ExportCode.ROUTING_TRACE`.
  - Reads data directly from `RouteCalculationTraceEntity` without recalculating matrices or timelines.
  - Invokes `ExcelTemplateEngine` to load template at `export-templates/routing-trace-export-template.xlsx`.
  - Replaces scalar placeholders (e.g., `${trace.id}`, `${routePlan.code}`) and loops table-backed rows (e.g., `${timeline.stops[].plannedArrivalTime}`).
  - Generates the attachment file name: `routing-trace-route-{routePlanId}-{traceId}.xlsx`.
- **Formatting Matrix Data**:
  - If the trace contains a full $N \times N$ matrix, it renders it as a grid.
  - If the trace contains leg-segment durations/distances, the sheet dynamically reformats the grid into a structured leg table (`From` $\rightarrow$ `To` $\rightarrow$ `Value`) to preserve clarity and accuracy.

---

## 8. Files and Classes

| File/Class | Type | Responsibility | Important Methods |
|---|---|---|---|
| `ExportCode` | Constants Class | Defines constant keys for export formats. | N/A |
| `ExportRequest` | DTO Class | Holds request details (code, routePlanId, traceId). | N/A |
| `ExportResult` | DTO Class | Holds generated file name, content type, and byte content. | N/A |
| `ExportHandler` | Interface | Interface for handling exports. | `export(...)` |
| `ExportHandlerResolver` | Component | Resolves handlers by code. | `resolve(...)` |
| `IExportService` / `ExportServiceImpl` | Application Service | Orchestrates export processing. | `export(...)` |
| `ExcelTemplateEngine` | Domain Component | Loads template and replaces placeholders / renders matrices. | `render(...)` |
| `RoutingTraceExportHandler` | Domain Exporter | Fetches trace snapshot and populates Excel placeholders. | `export(...)` |
|---|---|---|---|
| `SchoolBusAppConfigEntity` | JPA Entity | Holds global configurations. | N/A |
| `AppConfigCode` | Constants Class | Defines constant keys for configuration parameters. | N/A |
| `RoutingConfigResolverImpl` | Domain Service | Resolves configs with safe defaults globally. | `resolve()` |
| `SchoolPickupPointWindowRepository` | Repository | Fetches schedule-specific pickup/dropoff windows. | `findWindow(...)` |
| `SchoolPickupPointWindowServiceImpl` | Application Service | Implementation for pickup window lookup. | `findWindow(...)` |
| `IRoutingMatrixService` | Domain Service Interface | Defines interface for N x N matrix calculations. | `generateMatrix(...)` |
| `RoutingMatrixServiceImpl` | Domain Service | Computes distance/duration matrix (OSRM/Fallback). | `generateMatrix(...)` |
| `TimelineCalculatorServiceImpl` | Domain Service | Computes timeline boundaries & audits constraints. | `calculateTimeline(...)`, `validateAndGenerateIssues(...)` |
| `RouteController` | REST Controller | Exposes endpoints for route operations and trace retrieval. | `getRoutingMatrix(...)`, `getLatestCalculationTrace(...)`, `getCalculationTraceHistory(...)` |
| `RouteCalculationTraceEntity` | JPA Entity | Holds calculation trace snapshot history. | N/A |
| `RouteCalculationTraceRepository` | Repository | Direct persistence query access for trace snapshots. | `findFirstByRoutePlanIdAndCalculationType...` |
| `RouteCalculationTraceServiceImpl` | Application Service | Stores new trace and fetches latest trace history. | `saveTrace(...)`, `findLatestByRoutePlanId(...)` |
| `V23__refactor_school_bus_app_config.sql` | Flyway Migration | Removes tenant_id and defines unique configuration index. | N/A |
| `V24__create_route_calculation_trace.sql` | Flyway Migration | Creates trace table, constraints and partial indexes. | N/A |

---

## 9. Future Phase TODOs

- **Phase 7**: Simulation Benchmark Runner. Execute and compare multiple dispatch scenarios side-by-side to analyze computation runtime, feasibility status, and final objective value performance.

---

## 10. Phase 5 — Greedy Route Generation Algorithm

The greedy route generation engine (`GreedyRouteGenerationServiceImpl.java`) builds an initial feasible set of routes starting from a designated planning session.

### Heuristic Search Workflow

1. **Input Parameters**:
   - `planningSessionId` (provides school, schedule, service date, direction).
   - `defaultBusCapacity` (bus capacity constraint, default 30).
   - `depotId` (starting/ending depot location).
2. **Student Aggregation**:
   - Eligible students are queried based on school schedule, service date, and route direction constraints.
   - Students are grouped together into `PointAggregate` clusters based on their exact pickup/drop-off point.
   - The clusters are sorted descending by the number of students assigned to them.
3. **Nearest Feasible Insertion**:
   - For each cluster, the algorithm attempts a simulated stop insertion across all positions of all active routes.
   - A simulation triggers **Phase 3/4 (TimelineCalculatorService)** to temporarily build stops, assign students, and recompute travel times, distance, duration, and safety issues.
   - **Rejection criteria**: A candidate insertion position is rejected if it introduces any `BLOCKING` issues (e.g. late time windows, school deadline missed) or if the total student count exceeds the bus capacity constraint.
   - **Cost Selection**: The candidate insertion that minimizes the local incremental cost is selected:
     $$\Delta \text{Cost} = w_{\text{distance}} \times \Delta \text{Distance} + w_{\text{duration}} \times \Delta \text{Duration} + w_{\text{waitTime}} \times \Delta \text{WaitTime}$$
   - If no feasible insertion exists, a new route is opened. If the cluster still cannot fit in the new empty route, the students are pushed to the **Unassigned list**.
4. **Output and Database Mutation**:
   - Commits actual route plans, stops, and student mappings into the database.
   - Persists a complete calculation trace in `RouteCalculationTraceEntity` under the `GREEDY_GENERATION` type.
   - *Greedy heuristic is design-constrained to be fast and explainable, but it does not guarantee a global optimum.*

---

## 11. Phase 6 — Objective Function Scoring

The objective scoring engine (`RouteObjectiveScoringServiceImpl.java`) evaluates the quality of both individual routes and full solution sessions.

### Core Formulation

#### A. Session/Solution Objective Value ($Z_{\text{session}}$)
The total objective value is calculated as the sum of operational costs and constraint violation penalties. **A lower value represents a better solution.**

$$Z_{\text{session}} = \sum_{r \in R} \left( C_{\text{distance}}(r) + C_{\text{duration}}(r) + C_{\text{waitTime}}(r) + C_{\text{blocking}}(r) + C_{\text{warning}}(r) + C_{\text{excess}}(r) \right) + C_{\text{route\_count}} + C_{\text{unassigned}} + C_{\text{imbalance}}$$

Where:
- $C_{\text{distance}}(r) = w_{\text{distance}} \times \text{distanceKm}(r)$
- $C_{\text{duration}}(r) = w_{\text{duration}} \times \text{durationMin}(r)$
- $C_{\text{waitTime}}(r) = w_{\text{waitTime}} \times \text{totalStudentWaitTimeMin}(r)$
- $C_{\text{blocking}}(r) = w_{\text{blocking}} \times \text{blockingIssueCount}(r)$
- $C_{\text{warning}}(r) = w_{\text{warning}} \times \text{warningIssueCount}(r)$
- $C_{\text{excess}}(r) = w_{\text{capacity\_excess}} \times \max(0, \text{studentCount}(r) - \text{busCapacity}(r))$
- $C_{\text{route\_count}} = w_{\text{route\_count}} \times |R|$
- $C_{\text{unassigned}} = w_{\text{unassigned}} \times \text{totalUnassignedStudents}$
- $C_{\text{imbalance}} = w_{\text{load\_balance}} \times \left( \max_{r \in R} \text{studentCount}(r) - \min_{r \in R} \text{studentCount}(r) \right)$

#### B. Normalized Display Score
To present a human-friendly metric on the user interface, the objective value is normalized into a score range of $[0, 100]$:

$$\text{Display Score} = \frac{100}{1 + \frac{Z}{500}}$$

- An objective value of $0$ maps to a score of $100.00$.
- As the objective value increases due to high costs or severe constraint violations, the display score asymptotes towards $0$.
- **Interpretation**: This score is a relative quality metric under the current configuration weights. It does not represent an absolute mathematical optimum.

