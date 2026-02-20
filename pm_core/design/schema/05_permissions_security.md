# Module 05: Permissions & Security (Access Control)

**Design Philosophy:** Security is layered and explicit: project role assignment -> permission scheme -> issue-level security restrictions.

Provisioning note: permission and issue security schemes should be cloned per project from template sources during provisioning/rebinding (see Module 00).

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 5.1. `project_roles` and `project_role_actors`

Role entities are defined in Module 01 and reused here.

- `project_roles`: role dictionary per tenant.
- `project_role_actors`: assigns USER/GROUP/SERVICE_ACCOUNT to project roles.

## 5.2. `permission_definitions`

Use DB-backed permission dictionary for extensibility instead of fixed hardcoded enum.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| permission_key | VARCHAR(100) | Stable key (BROWSE_PROJECTS, EDIT_ISSUES, etc.) |
| name | VARCHAR(255) | Display name |
| description | TEXT | Description |
| category | VARCHAR(50) | PROJECT, ISSUE, COMMENT, ADMIN, AGILE |
| is_system | BOOLEAN | Built-in permission marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.3. `permission_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.4. `permission_scheme_entries`

Polymorphic grantee model for future actor types.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> permission_schemes |
| permission_key | VARCHAR(100) | FK-like to permission_definitions.permission_key |
| grantee_type | VARCHAR(30) | ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE |
| grantee_id | VARCHAR(255) | Actor identifier (nullable for contextual grantee) |
| effect | VARCHAR(10) | ALLOW, DENY |
| conditions_json | JSONB | Optional constraints for advanced policy |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.5. `issue_security_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_level_id | BIGINT | FK -> issue_security_levels |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.6. `issue_security_levels`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> issue_security_schemes |
| name | VARCHAR(255) | Level name |
| description | TEXT | Description |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.7. `issue_security_level_members`

Who can view issues tagged with a given security level.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| level_id | BIGINT | FK -> issue_security_levels |
| subject_type | VARCHAR(20) | ROLE, GROUP, USER |
| subject_id | VARCHAR(255) | Actor identifier |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, permission_key)` on `permission_definitions`
- `UNIQUE (tenant_id, scheme_id, permission_key, grantee_type, grantee_id)` on `permission_scheme_entries`
- `UNIQUE (tenant_id, level_id, subject_type, subject_id)` on `issue_security_level_members`
- `INDEX (tenant_id, scheme_id)` on all scheme child tables
