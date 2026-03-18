# Module 03: Workflow Engine (Process Management)

**Design Philosophy:** Workflow behavior is modeled relationally for validation, queryability, and Jira-like lifecycle control. Workflow roots are reusable, workflow versions capture draft/publish history, and workflow steps remain distinct from statuses so migration and transition semantics stay explicit.

Provisioning note: workflow schemes are shared across projects by default (Jira company-managed parity). Clone-on-associate remains optional for isolated project copies (see Module 00).

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

Projects and schemes bind to workflow roots. Runtime behavior is always resolved through `current_published_version_id`; drafts stay isolated until publish.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| workflow_key | VARCHAR(100) | Stable workflow key |
| name | VARCHAR(255) | Workflow name |
| description | TEXT | Description |
| current_published_version_id | BIGINT | FK -> workflow_versions (nullable until first publish) |
| draft_version_id | BIGINT | FK -> workflow_versions (nullable, at most one open draft) |
| lifecycle_state | VARCHAR(20) | ACTIVE, INACTIVE, ARCHIVED |
| is_system | BOOLEAN | Built-in workflow marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.4. `workflow_versions`

Captures publishable snapshots of a workflow. Historical published versions remain immutable; new edits happen on a draft.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| workflow_id | BIGINT | FK -> workflows |
| version_no | INT | Increment on each publish |
| version_state | VARCHAR(20) | DRAFT, PUBLISHED, ARCHIVED |
| base_version_id | BIGINT | FK -> workflow_versions (nullable; prior published version for a draft) |
| published_at | TIMESTAMP | Publish timestamp (nullable for drafts) |
| published_by | BIGINT | Publisher user/service id (nullable for drafts) |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.5. `workflow_steps`

Explicit workflow-step-to-status mapping. Step identity is distinct from status identity, matching Jira migration semantics.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| workflow_version_id | BIGINT | FK -> workflow_versions |
| step_key | VARCHAR(100) | Stable step key within workflow version |
| name | VARCHAR(255) | Step name |
| status_id | BIGINT | FK -> statuses |
| step_order | INT | Display order |
| is_initial | BOOLEAN | Initial node marker |
| is_terminal | BOOLEAN | Terminal node marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.6. `workflow_transitions`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| workflow_version_id | BIGINT | FK -> workflow_versions |
| name | VARCHAR(255) | Transition action label |
| from_step_id | BIGINT | FK -> workflow_steps (nullable for global transition) |
| to_step_id | BIGINT | FK -> workflow_steps |
| screen_id | BIGINT | FK -> screens (Module 04, nullable; transition screen only) |
| sequence | INT | UI order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.7. `workflow_transition_rules`

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

## 3.8. `workflow_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_workflow_id | BIGINT | FK -> workflows |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.9. `workflow_scheme_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> workflow_schemes |
| issue_type_id | BIGINT | FK -> issue_types (Module 02) |
| workflow_id | BIGINT | FK -> workflows |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.10. `workflow_scheme_migrations`

Tracks explicit status/step migration plans when workflow-scheme association changes for a project.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| project_id | BIGINT | FK -> projects |
| old_workflow_scheme_id | BIGINT | FK -> workflow_schemes |
| new_workflow_scheme_id | BIGINT | FK -> workflow_schemes |
| migration_state | VARCHAR(20) | PLANNING, VALIDATED, APPLIED, CANCELLED, FAILED |
| initiated_by | BIGINT | User/service initiating migration |
| started_at | TIMESTAMP | Migration start |
| completed_at | TIMESTAMP | Migration completion |
| error_message | TEXT | Last validation/application error |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 3.11. `workflow_scheme_migration_mappings`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| migration_id | BIGINT | FK -> workflow_scheme_migrations |
| issue_type_id | BIGINT | FK -> issue_types |
| old_workflow_version_id | BIGINT | FK -> workflow_versions |
| new_workflow_version_id | BIGINT | FK -> workflow_versions |
| old_step_id | BIGINT | FK -> workflow_steps |
| old_status_id | BIGINT | FK -> statuses |
| new_step_id | BIGINT | FK -> workflow_steps |
| new_status_id | BIGINT | FK -> statuses |
| mapping_strategy | VARCHAR(30) | STEP_MATCH, STATUS_MATCH, MANUAL, INITIAL_STEP_FALLBACK |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, status_key)` on `statuses`
- `UNIQUE (tenant_id, workflow_key)` on `workflows`
- `UNIQUE (tenant_id, workflow_id, version_no)` on `workflow_versions`
- `UNIQUE (tenant_id, workflow_version_id, step_key)` on `workflow_steps`
- `INDEX (tenant_id, workflow_version_id, status_id)` on `workflow_steps`
- `INDEX (tenant_id, workflow_version_id, from_step_id, to_step_id)` on `workflow_transitions`
- `UNIQUE (tenant_id, scheme_id, issue_type_id)` on `workflow_scheme_items`
- `INDEX (tenant_id, project_id, migration_state)` on `workflow_scheme_migrations`
- `CHECK lifecycle_state IN ('ACTIVE','INACTIVE','ARCHIVED')` on `workflows`
- `CHECK version_state IN ('DRAFT','PUBLISHED','ARCHIVED')` on `workflow_versions`
- `CHECK migration_state IN ('PLANNING','VALIDATED','APPLIED','CANCELLED','FAILED')` on `workflow_scheme_migrations`
- Projects may only become effective against `workflows.current_published_version_id`; drafts are never project-effective.
- Each published workflow version must have exactly one `is_initial=true` step.
- `from_step_id` and `to_step_id` on each transition must belong to the same `workflow_version_id`.
- All UNIQUE constraints above should be implemented as partial unique indexes filtered by `deleted_at IS NULL`.
- Composite tenant-safe FKs are required for all intra-module and cross-module references.
