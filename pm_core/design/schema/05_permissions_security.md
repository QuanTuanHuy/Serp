# Module 05: Permissions & Security (Access Control)

**Design Philosophy:** Jira-like security is layered and explicit: project role assignment -> permission scheme grants -> issue-level security restrictions. Access is default-deny unless a grant matches, and the schema must support Jira-style contextual actors such as project lead, assignee, logged-in users, and custom-field-based user/group resolution.

Provisioning note: permission and issue security schemes are reusable across projects. Depending on template/blueprint policy, a project may bind a tenant shared scheme or a dedicated project-scoped copy; shared reuse is supported, but it should not be modeled as the only default for every provisioning path (see Module 00).

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

Use DB-backed permission catalog to avoid hardcoded enums while keeping Jira-like semantics.
In v1, this catalog is system-seeded and API read-only for tenant admins (no tenant CRUD for permission keys).

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| permission_key | VARCHAR(100) | Stable key (BROWSE_PROJECTS, EDIT_ISSUES, etc.), immutable |
| name | VARCHAR(255) | Display name |
| description | TEXT | Description |
| category | VARCHAR(50) | PROJECT, ISSUE, COMMENT, ADMIN, AGILE |
| is_system | BOOLEAN | Built-in permission marker (v1 should always be true) |
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

Grant-only model (Jira-like): this table defines who is granted each permission in a scheme.
If no matching entry exists, access is denied implicitly.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> permission_schemes |
| permission_key | VARCHAR(100) | FK -> permission_definitions.permission_key (same tenant) |
| grantee_type | VARCHAR(40) | PROJECT_ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE, APPLICATION_ACCESS, ANYONE_ON_WEB, USER_CUSTOM_FIELD_VALUE, GROUP_CUSTOM_FIELD_VALUE |
| grantee_ref | VARCHAR(255) | Role/group/user/application-access identifier (nullable for contextual grantees) |
| custom_field_id | BIGINT | FK -> custom_fields when grantee resolves from a user/group custom field |
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
| subject_type | VARCHAR(40) | PROJECT_ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE, USER_CUSTOM_FIELD_VALUE, GROUP_CUSTOM_FIELD_VALUE |
| subject_ref | VARCHAR(255) | Role/group/user identifier (nullable for contextual subjects) |
| custom_field_id | BIGINT | FK -> custom_fields when member resolves from a user/group custom field |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, permission_key)` on `permission_definitions`
- `UNIQUE (tenant_id, scheme_id, permission_key, grantee_type, COALESCE(grantee_ref, '__CTX__'), COALESCE(custom_field_id, 0))` on `permission_scheme_entries`
- `UNIQUE (tenant_id, level_id, subject_type, COALESCE(subject_ref, '__CTX__'), COALESCE(custom_field_id, 0))` on `issue_security_level_members`
- `CHECK` on `permission_scheme_entries`:
  - `grantee_type IN ('PROJECT_ROLE','GROUP','USER','PROJECT_LEAD','REPORTER','ASSIGNEE','APPLICATION_ACCESS','ANYONE_ON_WEB','USER_CUSTOM_FIELD_VALUE','GROUP_CUSTOM_FIELD_VALUE')`
  - `grantee_ref IS NOT NULL` for `PROJECT_ROLE/GROUP/USER/APPLICATION_ACCESS`
  - `custom_field_id IS NOT NULL` for `USER_CUSTOM_FIELD_VALUE/GROUP_CUSTOM_FIELD_VALUE`
  - `grantee_ref IS NULL` for contextual grantees other than `APPLICATION_ACCESS`
- `CHECK rights-like semantics at service layer`: `ANYONE_ON_WEB` should only be allowed when deployment policy permits anonymous access.
- `CHECK` on `issue_security_level_members`:
  - `subject_type IN ('PROJECT_ROLE','GROUP','USER','PROJECT_LEAD','REPORTER','ASSIGNEE','USER_CUSTOM_FIELD_VALUE','GROUP_CUSTOM_FIELD_VALUE')`
  - `subject_ref IS NOT NULL` for `PROJECT_ROLE/GROUP/USER`
  - `custom_field_id IS NOT NULL` for `USER_CUSTOM_FIELD_VALUE/GROUP_CUSTOM_FIELD_VALUE`
  - `subject_ref IS NULL` for contextual subjects
- Composite tenant-safe FKs are required for all references (`(tenant_id, id)` pattern)
- All UNIQUE constraints above should be implemented as partial unique indexes filtered by `deleted_at IS NULL`.
- `INDEX (tenant_id, scheme_id)` on all scheme child tables
