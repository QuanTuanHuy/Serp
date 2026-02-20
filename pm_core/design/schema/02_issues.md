# Module 02: Issues & Work Items (Data Structure)

**Design Philosophy:** Work items remain the central entity, but extensibility is achieved through typed relational sub-structures instead of large opaque JSON blobs.

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 2.1. `work_items` (Core work item)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| project_id | BIGINT | FK -> projects |
| issue_type_id | BIGINT | FK -> issue_types |
| issue_no | BIGINT | Sequence per project (for key generation) |
| key | VARCHAR(30) | Human key (e.g. SERP-123), unique per tenant |
| summary | VARCHAR(512) | Title |
| description | TEXT | Markdown/JSON document |
| status_id | BIGINT | FK -> statuses (Module 03) |
| priority_id | BIGINT | FK -> priorities |
| resolution_id | BIGINT | FK -> resolutions (nullable) |
| assignee_id | BIGINT | User id |
| reporter_id | BIGINT | User id |
| parent_id | BIGINT | Self FK for hierarchy (epic/story/subtask) |
| security_level_id | BIGINT | FK -> issue_security_levels (Module 05) |
| due_date | TIMESTAMP | Due date |
| rank | VARCHAR(255) | Lexorank value |
| time_original_estimate | BIGINT | Seconds |
| time_remaining_estimate | BIGINT | Seconds |
| time_spent | BIGINT | Seconds |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.2. `work_item_components` (N:N work item <-> component)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| work_item_id | BIGINT | FK -> work_items |
| component_id | BIGINT | FK -> project_components |
| sequence | INT | Display order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.3. `work_item_fix_versions` (N:N work item <-> project version)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| work_item_id | BIGINT | FK -> work_items |
| version_id | BIGINT | FK -> project_versions |
| relation_type | VARCHAR(20) | FIX, AFFECTS |
| sequence | INT | Display order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.4. `work_item_sprints` (Sprint assignment history)

Support moving issues across sprints while preserving historical timeline.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| work_item_id | BIGINT | FK -> work_items |
| sprint_id | BIGINT | FK -> sprints (Module 07) |
| is_active | BOOLEAN | Current sprint link marker |
| added_at | TIMESTAMP | Time entered sprint |
| removed_at | TIMESTAMP | Time removed from sprint (nullable) |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.5. `issue_types` (Issue type dictionary)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| type_key | VARCHAR(100) | Stable key (bug, story, epic, task) |
| name | VARCHAR(255) | Display name |
| description | TEXT | Description |
| icon_url | VARCHAR(255) | Icon |
| hierarchy_level | INT | 0=subtask, 1=standard, 2=epic+ |
| is_system | BOOLEAN | Built-in type marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.6. `issue_type_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_issue_type_id | BIGINT | FK -> issue_types |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.7. `issue_type_scheme_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> issue_type_schemes |
| issue_type_id | BIGINT | FK -> issue_types |
| sequence | INT | Display order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.8. `priorities`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(50) | Priority label |
| description | TEXT | Description |
| icon_url | VARCHAR(255) | Icon |
| color | VARCHAR(20) | Hex color |
| sequence | INT | Default order |
| is_system | BOOLEAN | Built-in priority marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.9. `priority_schemes`

Added to resolve missing FK target from `projects.priority_scheme_id`.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_priority_id | BIGINT | FK -> priorities |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.10. `priority_scheme_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> priority_schemes |
| priority_id | BIGINT | FK -> priorities |
| sequence | INT | Order override by scheme |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.11. `resolutions`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(50) | Resolution label |
| description | TEXT | Description |
| sequence | INT | Display order |
| is_system | BOOLEAN | Built-in resolution marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.12. `issue_link_types`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(100) | Blocks, Clones, Relates |
| outward_desc | VARCHAR(100) | e.g. blocks |
| inward_desc | VARCHAR(100) | e.g. is blocked by |
| is_system | BOOLEAN | Built-in link type marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.13. `issue_links`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| source_id | BIGINT | FK -> work_items |
| target_id | BIGINT | FK -> work_items |
| link_type_id | BIGINT | FK -> issue_link_types |
| sequence | INT | Link order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.14. `worklogs`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| work_item_id | BIGINT | FK -> work_items |
| author_id | BIGINT | User id |
| comment | TEXT | Work description |
| start_date | TIMESTAMP | Start time |
| time_spent | BIGINT | Seconds |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 2.15. `work_item_custom_field_values` (Typed value store)

Replaces `work_items.custom_field_values` JSONB for better indexing and validation.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| work_item_id | BIGINT | FK -> work_items |
| custom_field_id | BIGINT | FK -> custom_fields (Module 04) |
| value_type | VARCHAR(30) | TEXT, NUMBER, DATE, DATETIME, USER, OPTION, JSON |
| text_value | TEXT | Value for text-like fields |
| number_value | NUMERIC(20,6) | Value for numeric fields |
| date_value | DATE | Value for date fields |
| datetime_value | TIMESTAMP | Value for datetime fields |
| user_value_id | BIGINT | User reference |
| option_value_id | BIGINT | FK -> custom_field_options (Module 04) |
| json_value | JSONB | Fallback for complex values |
| sort_order | INT | Support multi-value ordering |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, project_id, issue_no)` and `UNIQUE (tenant_id, key)` on `work_items`
- `INDEX (tenant_id, project_id, status_id, assignee_id, rank)` on `work_items`
- `UNIQUE (tenant_id, scheme_id, issue_type_id)` on `issue_type_scheme_items`
- `UNIQUE (tenant_id, scheme_id, priority_id)` on `priority_scheme_items`
- `INDEX (tenant_id, work_item_id, custom_field_id)` on `work_item_custom_field_values`
