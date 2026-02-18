# Module 03: Workflow Engine (Process Management)

**Design Philosophy:** Workflow behavior is modeled relationally for validation, queryability, and future extension (plugins, automation, conditional routing).

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 3.1. `status_categories`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(50) | To Do, In Progress, Done |
| key | VARCHAR(50) | new, indeterminate, done |
| color_name | VARCHAR(50) | blue-gray, yellow, green |
| is_system | BOOLEAN | Built-in category marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.2. `statuses`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| status_key | VARCHAR(100) | Stable key (open, in_progress, done) |
| name | VARCHAR(255) | Display name |
| description | TEXT | Description |
| icon_url | VARCHAR(255) | Icon URL |
| status_category_id | BIGINT | FK -> status_categories |
| is_system | BOOLEAN | Built-in status marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.3. `workflows`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Workflow name |
| description | TEXT | Description |
| version_no | INT | Increment on publish |
| is_active | BOOLEAN | Active version marker |
| is_system | BOOLEAN | Built-in workflow marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.4. `workflow_steps`

Explicit workflow-status mapping.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| workflow_id | BIGINT | FK -> workflows |
| status_id | BIGINT | FK -> statuses |
| step_order | INT | Display order |
| is_initial | BOOLEAN | Initial node marker |
| is_final | BOOLEAN | Terminal node marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.5. `workflow_transitions`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| workflow_id | BIGINT | FK -> workflows |
| name | VARCHAR(255) | Transition action label |
| from_status_id | BIGINT | FK -> statuses (nullable for global transition) |
| to_status_id | BIGINT | FK -> statuses |
| screen_id | BIGINT | FK -> screens (Module 04, nullable) |
| sequence | INT | UI order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.6. `workflow_transition_rules`

Stores conditions, validators, and post-functions in a normalized extensible model.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| transition_id | BIGINT | FK -> workflow_transitions |
| rule_stage | VARCHAR(20) | CONDITION, VALIDATOR, POST_FUNCTION |
| rule_key | VARCHAR(100) | user_is_assignee, field_required, fire_event, etc. |
| config_json | JSONB | Rule configuration payload |
| sequence | INT | Execution order |
| is_enabled | BOOLEAN | Runtime toggle |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.7. `workflow_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_workflow_id | BIGINT | FK -> workflows |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.8. `workflow_scheme_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> workflow_schemes |
| issue_type_id | BIGINT | FK -> issue_types (Module 02) |
| workflow_id | BIGINT | FK -> workflows |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, status_key)` on `statuses`
- `UNIQUE (tenant_id, workflow_id, status_id)` on `workflow_steps`
- `INDEX (tenant_id, workflow_id, from_status_id, to_status_id)` on `workflow_transitions`
- `UNIQUE (tenant_id, scheme_id, issue_type_id)` on `workflow_scheme_items`
