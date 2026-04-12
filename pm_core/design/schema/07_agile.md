# Module 07: Agile & Planning (Scrum/Kanban)

**Design Philosophy:** Boards are configurable views on saved filters, board access inherits from filter access, and agile configuration must preserve Jira-like column/status mapping, done-column semantics, sprint lifecycle, and rank-based backlog ordering.

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 7.1. `boards`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Board name |
| type | VARCHAR(20) | SCRUM, KANBAN |
| owner_id | BIGINT | Owner user id |
| filter_id | BIGINT | FK -> search_requests (Module 08) |
| location_type | VARCHAR(20) | PROJECT, USER |
| location_id | BIGINT | Project id or user id |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

Board visibility is inherited from the underlying saved filter. A board cannot be visible to principals that cannot access `filter_id`.

## 7.2. `board_columns`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| board_id | BIGINT | FK -> boards |
| name | VARCHAR(255) | Column name |
| sequence | INT | Column order |
| is_done_column | BOOLEAN | Marks the board's done column |
| min_wip | INT | Min WIP hint |
| max_wip | INT | Max WIP hint |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

WIP limits are informational/UI-only, not hard workflow enforcement.

## 7.3. `board_column_statuses`

Denormalize `board_id` so a status can be constrained to at most one column per board.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| board_id | BIGINT | FK -> boards |
| column_id | BIGINT | FK -> board_columns |
| status_id | BIGINT | FK -> statuses |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 7.4. `board_quick_filters`

Optional per-board shortcuts for fast planning.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| board_id | BIGINT | FK -> boards |
| name | VARCHAR(255) | Quick filter label |
| query_string | TEXT | JQL-like expression |
| sequence | INT | Display order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 7.5. `sprints`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| board_id | BIGINT | FK -> boards |
| name | VARCHAR(255) | Sprint name |
| goal | TEXT | Sprint goal |
| sequence | INT | Backlog order of the sprint within the board |
| state | VARCHAR(20) | FUTURE, ACTIVE, CLOSED |
| start_date | TIMESTAMP | Planned start |
| end_date | TIMESTAMP | Planned end |
| complete_date | TIMESTAMP | Actual completion |
| activated_date | TIMESTAMP | Actual start |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 7.6. `sprint_reports`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| sprint_id | BIGINT | FK -> sprints |
| metric_key | VARCHAR(50) | velocity, burndown, commitment, completed, punted |
| metric_value | JSONB | Snapshot data |
| generated_at | TIMESTAMP | Snapshot time |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 7.7. `rank_fields`

Supports future multiple ranking dimensions while keeping current Lexorank in `work_items.rank`.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| field_key | VARCHAR(100) | e.g. `rank`, `backlog_rank` |
| name | VARCHAR(255) | Display name |
| rank_algorithm | VARCHAR(50) | LEXORANK, FRACTIONAL_INDEX |
| is_default | BOOLEAN | Default rank field |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, board_id, sequence)` on `board_columns`
- `UNIQUE (tenant_id, board_id, status_id)` on `board_column_statuses`
- `UNIQUE (tenant_id, column_id, status_id)` on `board_column_statuses`
- `UNIQUE (tenant_id, board_id, name)` on `sprints`
- `UNIQUE (tenant_id, board_id, sequence)` on `sprints`
- `INDEX (tenant_id, board_id, state)` on `sprints`
- `CHECK type IN ('SCRUM','KANBAN')` on `boards`
- `CHECK state IN ('FUTURE','ACTIVE','CLOSED')` on `sprints`
- Each board must have exactly one `is_done_column=true`, and it must be the highest `sequence` column.
- `board_column_statuses.board_id` must equal `board_columns.board_id` for the referenced column.
- All UNIQUE constraints above should be implemented as partial unique indexes filtered by `deleted_at IS NULL`.
- Composite tenant-safe FKs are required for all intra-module and cross-module references.
