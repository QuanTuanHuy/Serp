# Module 04: Fields & Screens (UI Configuration)

**Design Philosophy:** Match Jira company-managed behavior by separating globally defined custom fields from context-scoped applicability, defaults, and options. Screen schemes cover only CREATE/EDIT/VIEW operations; transition screens remain attached directly to workflow transitions.

Provisioning note: custom fields remain globally reusable, but template-based company-managed provisioning may materialize project-scoped field configuration and screen families. Shared reuse of these schemes is explicit, not a blanket default for every project (see Module 00).

## Shared Base Columns (applies to tenant-owned mutable tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

Current-phase simplification: `custom_fields`, `custom_field_contexts`,
`custom_field_options`, and `custom_field_context_default_values` are system-owned
read-only catalog tables.
They do not support project-scoped overrides or tenant-managed customization in
this phase.

## 4.1. `custom_fields`

Current phase: custom field definitions are seeded as shared system catalog rows.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| field_key | VARCHAR(100) | Stable key (`customfield_10001`) |
| name | VARCHAR(255) | Display name |
| description | TEXT | Description |
| type_key | VARCHAR(50) | text, number, date, user, group, select, multiselect, url, etc. |
| search_template | VARCHAR(50) | text_search, range_search, user_search, option_search |
| is_system | BOOLEAN | Built-in field marker |
| schema_json | JSONB | Plugin-specific schema/settings |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.2. `custom_field_contexts`

One custom field can have many system-owned contexts, each with its own
issue-type scope, options, and defaults.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| custom_field_id | BIGINT | FK -> system-seeded `custom_fields` |
| name | VARCHAR(255) | Context name |
| description | TEXT | Description |
| issue_type_key | VARCHAR(100) NULL | Exact issue type key this context applies to; `NULL` means global fallback |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.3. `custom_field_options`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| custom_field_context_id | BIGINT | FK -> custom_field_contexts |
| option_key | VARCHAR(100) | Stable option key within context |
| value | VARCHAR(255) | Display value |
| sequence | INT | Order |
| parent_option_id | BIGINT | Self FK for cascade options |
| is_disabled | BOOLEAN | Disable option without data loss |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.4. `custom_field_context_default_values`

Defaults are owned by context. Multi-value defaults use multiple rows with `sort_order`.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| context_id | BIGINT | FK -> custom_field_contexts |
| value_type | VARCHAR(30) | TEXT, NUMBER, DATE, DATETIME, USER, GROUP, OPTION, JSON |
| text_value | TEXT | Default for text-like fields |
| number_value | NUMERIC(20,6) | Default for numeric fields |
| date_value | DATE | Default for date fields |
| datetime_value | TIMESTAMP | Default for datetime fields |
| user_value_id | BIGINT | Default user reference |
| group_value_id | VARCHAR(255) | Default group reference |
| option_value_id | BIGINT | FK -> custom_field_options |
| json_value | JSONB | Fallback for complex defaults |
| sort_order | INT | Multi-value ordering |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Context Resolution Rules

1. Each `(custom_field_id, issue_type_key)` must resolve to exactly one effective context.
2. Resolution order is: exact `issue_type_key` match -> global fallback (`issue_type_key IS NULL`).
3. A field may not have two contexts with the same `issue_type_key`, and may not have more than one global fallback context.
4. `work_item_custom_field_values` must persist the resolved `custom_field_context_id` at write time so historical meaning survives later config changes.
5. Current-phase context resolution is independent of project scope.

## 4.5. `field_configurations`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Config name |
| description | TEXT | Description |
| is_system | BOOLEAN | Built-in config marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.6. `field_configuration_items`

Replaces old JSONB `items` for stronger constraints.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| field_configuration_id | BIGINT | FK -> field_configurations |
| field_ref_type | VARCHAR(20) | SYSTEM, CUSTOM |
| field_ref | VARCHAR(100) | e.g. `summary`, `customfield_10001` |
| is_required | BOOLEAN | Required flag |
| is_hidden | BOOLEAN | Visibility flag |
| renderer_key | VARCHAR(50) | text, wiki, markdown, etc. |
| sequence | INT | Rule evaluation/display order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.7. `field_config_schemes`

Added to satisfy FK target from `projects.field_config_scheme_id`.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_field_configuration_id | BIGINT | FK -> field_configurations |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.8. `field_config_scheme_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> field_config_schemes |
| issue_type_id | BIGINT | FK -> issue_types |
| field_configuration_id | BIGINT | FK -> field_configurations |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.9. `screens`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Screen name |
| description | TEXT | Description |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.10. `screen_tabs`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| screen_id | BIGINT | FK -> screens |
| name | VARCHAR(255) | Tab name |
| sequence | INT | Tab order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.11. `screen_tab_fields`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| screen_tab_id | BIGINT | FK -> screen_tabs |
| field_ref_type | VARCHAR(20) | SYSTEM, CUSTOM |
| field_ref | VARCHAR(100) | e.g. `summary`, `customfield_10001` |
| sequence | INT | Display order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.12. `screen_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_screen_id | BIGINT | FK -> screens |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.13. `screen_scheme_items`

Map operation to screen (`CREATE`, `EDIT`, `VIEW`) only. Transition screens are modeled exclusively on `workflow_transitions.screen_id` in Module 03.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| screen_scheme_id | BIGINT | FK -> screen_schemes |
| operation_key | VARCHAR(30) | CREATE, EDIT, VIEW |
| screen_id | BIGINT | FK -> screens |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.14. `issue_type_screen_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Configuration scheme name |
| description | TEXT | Description |
| default_screen_scheme_id | BIGINT | FK -> screen_schemes |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.15. `issue_type_screen_scheme_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> issue_type_screen_schemes |
| issue_type_id | BIGINT | FK -> issue_types |
| screen_scheme_id | BIGINT | FK -> screen_schemes |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (field_key)` on `custom_fields`
- `UNIQUE (custom_field_id, issue_type_key)` on `custom_field_contexts`
- partial unique index on `custom_field_contexts(custom_field_id)` filtered by `issue_type_key IS NULL AND deleted_at IS NULL` to enforce one global fallback per field
- `UNIQUE (custom_field_context_id, option_key)` on `custom_field_options`
- `UNIQUE (context_id, sort_order)` on `custom_field_context_default_values`
- `UNIQUE (tenant_id, scheme_id, issue_type_id)` on `field_config_scheme_items` and `issue_type_screen_scheme_items`
- `UNIQUE (tenant_id, screen_scheme_id, operation_key)` on `screen_scheme_items`
- `CHECK operation_key IN ('CREATE','EDIT','VIEW')` on `screen_scheme_items`
- All UNIQUE constraints above should be implemented as partial unique indexes filtered by `deleted_at IS NULL`.
- Composite tenant-safe FKs are required for tenant-owned tables. References into the system-owned custom-field context catalog are allowed to use non-tenant FKs in the current phase.
