# Module 01: Projects & Configuration (JIRA-like Core)

**Design Philosophy:** `projects` acts as a stable container that binds project-owned scheme clones provisioned from blueprint/system templates. This preserves template reuse while preventing cross-project configuration side effects.

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL` (row-level multi-tenancy)
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL` (soft delete)

## 1.1. `project_categories` (Phan loai du an)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Category name (unique per tenant) |
| description | TEXT | Description |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 1.2. `project_blueprints` (Project templates)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Template name |
| description | TEXT | Template details |
| project_type_key | VARCHAR(50) | software, business, service_desk, etc. |
| avatar_url | VARCHAR(255) | Template icon |
| is_system | BOOLEAN | System-provided or tenant-defined |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 1.3. `blueprint_scheme_defaults` (Blueprint -> Scheme defaults)

Replace opaque JSON defaults with relational mapping.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| blueprint_id | BIGINT | FK -> project_blueprints |
| scheme_type | VARCHAR(50) | ISSUE_TYPE, WORKFLOW, FIELD_CONFIG, SCREEN, PERMISSION, ISSUE_SECURITY, NOTIFICATION, PRIORITY |
| scheme_id | BIGINT | FK target depends on `scheme_type` (template source scheme, not directly shared runtime config) |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 1.4. `projects` (Core entity)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| key | VARCHAR(20) | Human key (e.g. SERP), unique per tenant |
| name | VARCHAR(255) | Project name |
| url | VARCHAR(255) | Optional project URL |
| description | TEXT | Description |
| lead_user_id | BIGINT | Project lead (Account service reference) |
| avatar_id | BIGINT | Optional avatar asset id |
| project_category_id | BIGINT | FK -> project_categories (nullable) |
| project_type_key | VARCHAR(50) | software, business, service_desk, etc. |
| archived | BOOLEAN | Default false |
| archived_at | TIMESTAMP | Archived timestamp |
| issue_type_scheme_id | BIGINT | FK -> issue_type_schemes (Module 02, project-owned clone) |
| workflow_scheme_id | BIGINT | FK -> workflow_schemes (Module 03, project-owned clone) |
| field_config_scheme_id | BIGINT | FK -> field_config_schemes (Module 04, project-owned clone) |
| issue_type_screen_scheme_id | BIGINT | FK -> issue_type_screen_schemes (Module 04, project-owned clone) |
| issue_security_scheme_id | BIGINT | FK -> issue_security_schemes (Module 05, project-owned clone) |
| permission_scheme_id | BIGINT | FK -> permission_schemes (Module 05, project-owned clone) |
| notification_scheme_id | BIGINT | FK -> notification_schemes (Module 06, project-owned clone) |
| priority_scheme_id | BIGINT | FK -> priority_schemes (Module 02, project-owned clone) |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 1.5. `project_components` (Components)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| project_id | BIGINT | FK -> projects |
| name | VARCHAR(255) | Component name |
| description | TEXT | Description |
| lead_user_id | BIGINT | Component lead |
| assignee_type | VARCHAR(30) | PROJECT_DEFAULT, COMPONENT_LEAD, PROJECT_LEAD, UNASSIGNED |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 1.6. `project_versions` (Releases / Fix versions)

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| project_id | BIGINT | FK -> projects |
| name | VARCHAR(255) | Version name |
| description | TEXT | Description |
| sequence | INT | Display order |
| released | BOOLEAN | Release state |
| archived | BOOLEAN | Archive state |
| release_date | DATE | Planned release date |
| start_date | DATE | Planned start date |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 1.7. `project_roles` (Project roles)

This is the single source of truth for roles in PM core (avoid duplicate definition across modules).

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Role name (unique per tenant) |
| description | TEXT | Description |
| is_system | BOOLEAN | Built-in role marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 1.8. `project_role_actors` (Role assignments)

Polymorphic assignment model for future actor types.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| project_id | BIGINT | FK -> projects |
| project_role_id | BIGINT | FK -> project_roles |
| subject_type | VARCHAR(20) | USER, GROUP, SERVICE_ACCOUNT |
| subject_id | VARCHAR(255) | Actor identifier |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Project Provisioning Invariants

1. On project creation, all scheme bindings should point to project-owned cloned schemes provisioned from blueprint/system template defaults.
2. Updating blueprint defaults does not mutate scheme bindings of existing projects.
3. Project scheme rebinding should use clone-and-swap in one transaction (clone candidate template, validate compatibility, update project binding columns).
4. Directly binding multiple projects to the same mutable scheme is discouraged unless explicitly opting into shared behavior.

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, key)` on `projects`
- `UNIQUE (tenant_id, name)` on `project_categories`, `project_roles`
- `UNIQUE (tenant_id, project_id, name)` on `project_components`, `project_versions`
- `UNIQUE (tenant_id, blueprint_id, scheme_type)` on `blueprint_scheme_defaults`
- Composite tenant-safe FKs are recommended for all scheme bindings on `projects` (e.g., `(tenant_id, permission_scheme_id)` -> `permission_schemes(tenant_id, id)`, `(tenant_id, issue_security_scheme_id)` -> `issue_security_schemes(tenant_id, id)`)
- `INDEX (tenant_id, project_id)` on all project-scoped child tables
