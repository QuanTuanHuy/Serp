# Module 04: Fields & Screens (UI Configuration)

**Design Philosophy:** Separate field metadata, behavioral rules, and UI presentation. Keep configurations relational where runtime filtering/querying is frequent.

Provisioning note: field/screen scheme trees are provisioned as project-owned clones from template sources (see Module 00).

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 4.1. `custom_fields`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| field_key | VARCHAR(100) | Stable key (`customfield_10001`) |
| name | VARCHAR(255) | Display name |
| description | TEXT | Description |
| type_key | VARCHAR(50) | text, number, date, user, select, multiselect, url, etc. |
| search_template | VARCHAR(50) | text_search, range_search, user_search, option_search |
| is_system | BOOLEAN | Built-in field marker |
| is_global | BOOLEAN | Available in all contexts |
| schema_json | JSONB | Plugin-specific schema/settings |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.2. `custom_field_options`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| custom_field_id | BIGINT | FK -> custom_fields |
| option_key | VARCHAR(100) | Stable option key |
| value | VARCHAR(255) | Display value |
| sequence | INT | Order |
| parent_option_id | BIGINT | Self FK for cascade options |
| is_disabled | BOOLEAN | Disable option without data loss |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.3. `custom_field_contexts`

Controls where a custom field is applicable.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| custom_field_id | BIGINT | FK -> custom_fields |
| project_id | BIGINT | FK -> projects (nullable for global) |
| issue_type_id | BIGINT | FK -> issue_types (nullable for all types) |
| is_global_context | BOOLEAN | True if applies globally |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.4. `field_configurations`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Config name |
| description | TEXT | Description |
| is_system | BOOLEAN | Built-in config marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.5. `field_configuration_items`

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

## 4.6. `field_config_schemes`

Added to satisfy FK target from `projects.field_config_scheme_id`.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_field_configuration_id | BIGINT | FK -> field_configurations |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.7. `field_config_scheme_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> field_config_schemes |
| issue_type_id | BIGINT | FK -> issue_types |
| field_configuration_id | BIGINT | FK -> field_configurations |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.8. `screens`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Screen name |
| description | TEXT | Description |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.9. `screen_tabs`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| screen_id | BIGINT | FK -> screens |
| name | VARCHAR(255) | Tab name |
| sequence | INT | Tab order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.10. `screen_tab_fields`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| screen_tab_id | BIGINT | FK -> screen_tabs |
| field_ref_type | VARCHAR(20) | SYSTEM, CUSTOM |
| field_ref | VARCHAR(100) | e.g. `summary`, `customfield_10001` |
| sequence | INT | Display order |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.11. `screen_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_screen_id | BIGINT | FK -> screens |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.12. `screen_scheme_items`

Map operation to screen (`CREATE`, `EDIT`, `VIEW`, `TRANSITION`).

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| screen_scheme_id | BIGINT | FK -> screen_schemes |
| operation_key | VARCHAR(30) | CREATE, EDIT, VIEW, TRANSITION |
| screen_id | BIGINT | FK -> screens |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.13. `issue_type_screen_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Configuration scheme name |
| description | TEXT | Description |
| default_screen_scheme_id | BIGINT | FK -> screen_schemes |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 4.14. `issue_type_screen_scheme_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> issue_type_screen_schemes |
| issue_type_id | BIGINT | FK -> issue_types |
| screen_scheme_id | BIGINT | FK -> screen_schemes |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, field_key)` on `custom_fields`
- `UNIQUE (tenant_id, custom_field_id, option_key)` on `custom_field_options`
- `UNIQUE (tenant_id, scheme_id, issue_type_id)` on `field_config_scheme_items` and `issue_type_screen_scheme_items`
- `UNIQUE (tenant_id, screen_scheme_id, operation_key)` on `screen_scheme_items`
