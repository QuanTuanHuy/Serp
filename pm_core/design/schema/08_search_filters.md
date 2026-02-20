# Module 08: Search & Reporting (Filters & Dashboards)

**Design Philosophy:** Search artifacts (filters/dashboards) are first-class entities with explicit sharing and subscription policies.

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 8.1. `search_requests` (Saved filters)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Filter name (unique per owner) |
| description | TEXT | Description |
| author_id | BIGINT | Owner user id |
| query_string | TEXT | JQL-like query |
| is_favorite | BOOLEAN | Owner favorite marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 8.2. `search_request_favorites`

Separate table to support many users favoriting same filter.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| filter_id | BIGINT | FK -> search_requests |
| user_id | BIGINT | User id |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 8.3. `share_permissions`

Who can read/edit a filter or dashboard.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| entity_type | VARCHAR(20) | FILTER, DASHBOARD |
| entity_id | BIGINT | Target id |
| share_type | VARCHAR(20) | GLOBAL, GROUP, PROJECT, ROLE, USER |
| share_param | VARCHAR(255) | Subject id |
| rights | VARCHAR(20) | READ, EDIT |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 8.4. `filter_subscriptions`

Periodic delivery settings for saved filters.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| filter_id | BIGINT | FK -> search_requests |
| user_id | BIGINT | Recipient user id (nullable if group subscription) |
| group_id | BIGINT | Recipient group id (nullable if user subscription) |
| cron_expression | VARCHAR(80) | Quartz cron |
| channel | VARCHAR(20) | EMAIL, IN_APP |
| last_run_at | TIMESTAMP | Last execution |
| next_run_at | TIMESTAMP | Next execution |
| is_active | BOOLEAN | Subscription status |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 8.5. `dashboards`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Dashboard name |
| description | TEXT | Description |
| author_id | BIGINT | Owner user id |
| layout | VARCHAR(20) | AA, AAA, AB, BA |
| is_system | BOOLEAN | System dashboard marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 8.6. `gadgets`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| dashboard_id | BIGINT | FK -> dashboards |
| portlet_id | VARCHAR(255) | Gadget implementation key |
| column_idx | INT | Column index |
| row_idx | INT | Row index |
| color | VARCHAR(20) | Gadget accent color |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 8.7. `gadget_user_prefs`

Keep JSONB only for plugin-specific gadget options.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| gadget_id | BIGINT | FK -> gadgets |
| user_id | BIGINT | Preference owner |
| prefs_json | JSONB | Gadget config payload |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, author_id, name)` on `search_requests`
- `UNIQUE (tenant_id, filter_id, user_id)` on `search_request_favorites`
- `INDEX (tenant_id, entity_type, entity_id)` on `share_permissions`
- `INDEX (tenant_id, next_run_at, is_active)` on `filter_subscriptions`
- `UNIQUE (tenant_id, dashboard_id, column_idx, row_idx)` on `gadgets`
