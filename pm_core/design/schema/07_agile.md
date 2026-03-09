# Module 07: Agile & Planning (Scrum/Kanban)

**Design Philosophy:** Boards are configurable views on saved filters; sprint planning and ranking are persisted with history-friendly structures.

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

## 7.2. `board_columns`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| board_id | BIGINT | FK -> boards |
| name | VARCHAR(255) | Column name |
| sequence | INT | Column order |
| min_wip | INT | Min WIP constraint |
| max_wip | INT | Max WIP constraint |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 7.3. `board_column_statuses`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
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
- `UNIQUE (tenant_id, column_id, status_id)` on `board_column_statuses`
- `UNIQUE (tenant_id, board_id, name)` on `sprints`
- `INDEX (tenant_id, board_id, state)` on `sprints`
