# UC-PM-002 - Update Project

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.1
> Last Updated: 2026-04-12

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Projects schema: `schema/01_projects.md`
- Permissions and security schema: `schema/05_permissions_security.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-002 |
| **Use Case Name** | Update Project |
| **Module** | PM Core |
| **Version** | 1.1 |
| **Last Updated** | 2026-04-12 |
| **Priority** | High |
| **Complexity** | Medium |

### Description

Update mutable project metadata for an existing project within tenant scope. UC-PM-002 supports partial updates for `name`, `key`, `description`, `lead_user_id`, `project_category_id`, `url`, and `avatar_id`. Scheme bindings, archive state, and `project_type_key` are explicitly out of scope and are handled by separate use cases.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Project Lead | Primary | Updates a project they can administer |
| PM Admin | Secondary | May update project metadata when they satisfy project-scoped administration rules |
| System | System | Validates tenant scope, project administration permission, mutable fields, external references, persists update, and writes outbox event |

### Authorization (Jira Project Permissions)

- This use case uses Jira-style project authorization, not legacy `PM.PROJECT.UPDATE`
- Caller must be authenticated in the current tenant scope
- Caller must satisfy `ADMINISTER_PROJECTS` for the target project
- Permission resolution is grant-only through the project's `permission_scheme_id` and may resolve via `PROJECT_ROLE`, `GROUP`, `USER`, `PROJECT_LEAD`, `REPORTER`, and `ASSIGNEE`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Target project exists in tenant scope and is not soft-deleted
4. Caller satisfies `ADMINISTER_PROJECTS` for the target project
5. Target project is not archived

### Postconditions

#### Success Postconditions

1. Requested mutable project fields are updated on the project row
2. `updated_at` and `updated_by` are updated
3. A `PROJECT_UPDATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.project.events`
4. Response returns the updated project payload

#### Failure Postconditions

1. No partial update is committed
2. No outbox event is committed
3. Existing project data remains unchanged
4. Error response is returned with validation, authorization, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends `PUT /api/v1/projects/{projectId}` with partial metadata updates |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId`, validates it exists in tenant scope and is not archived |
| 4 | System | Evaluates `ADMINISTER_PROJECTS` for the caller on the target project |
| 5 | System | Validates request payload and rejects immutable field updates |
| 6 | System | If `key` is supplied and changed, validates format and uniqueness within tenant scope |
| 7 | System | If `lead_user_id` is supplied and changed, validates the user exists via Account service |
| 8 | System | If `project_category_id` is supplied and non-null, validates the category exists in tenant scope |
| 9 | System | Applies requested metadata changes, including explicit clearing of nullable fields when `null` is supplied |
| 10 | System | Persists the project update and `PROJECT_UPDATED` outbox event in one transaction |
| 11 | System | Returns HTTP 200 with updated project payload |

### Alternative Flows

#### AF-1: Partial Metadata Update

**Branches from**: Main Flow Step 5-9  
**Condition**: Request updates only a subset of mutable fields

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Treats omitted mutable fields as unchanged |
| 9.1 | System | Persists only supplied mutable values |

**Rejoins**: Main Flow Step 10

#### AF-2: Clear Optional Metadata

**Branches from**: Main Flow Step 9  
**Condition**: Request explicitly sends `null` for `description`, `project_category_id`, `url`, or `avatar_id`

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.1 | System | Clears the corresponding persisted value to `NULL` |

**Rejoins**: Main Flow Step 10

### Exception Flows

#### EF-1: Project Not Found

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `PROJECT_NOT_FOUND` |

#### EF-2: Project Archived

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 409 with error: `PROJECT_ARCHIVED` |

#### EF-3: Project Permission Denied

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission detail `ADMINISTER_PROJECTS` |

#### EF-4: Duplicate Project Key

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `PROJECT_KEY_ALREADY_EXISTS` |

#### EF-5: Lead User Not Found

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 404 with error: `USER_NOT_FOUND` |

#### EF-6: Category Not Found

**Triggered at**: Main Flow Step 8

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.E1 | System | Returns HTTP 404 with error: `CATEGORY_NOT_FOUND` |

#### EF-7: Immutable Field Update Rejected

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 400 with validation details for immutable or unsupported fields |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-002-01 | Updating project metadata requires `ADMINISTER_PROJECTS` in the target project | Authorization layer |
| BR-PM-002-02 | Archived projects cannot be updated through UC-PM-002 | Service layer |
| BR-PM-002-03 | `key` may be changed, but when changed it must remain unique within tenant scope and satisfy the canonical project-key format | Service layer + DB constraint |
| BR-PM-002-04 | `project_type_key`, scheme-binding fields, archive flags, and tenant ownership are immutable in UC-PM-002 | UseCase layer |
| BR-PM-002-05 | Optional metadata fields explicitly supplied as `null` are cleared; omitted fields remain unchanged | UseCase + Service layer |
| BR-PM-002-06 | If `lead_user_id` is changed, the target user must exist in Account service before commit | UseCase layer |
| BR-PM-002-07 | If `project_category_id` is supplied and non-null, the category must exist in the same tenant scope | UseCase layer |
| BR-PM-002-08 | Domain events use the outbox pattern: `PROJECT_UPDATED` is stored in the same transaction as the project update and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| projectId | int64 | Yes | min:1 | Project identifier from path |
| name | string | No | min:1, max:255 | Updated project name |
| key | string | No | regex: `^[A-Z][A-Z0-9]{1,9}$`; unique in tenant when changed | Updated project key |
| description | string | No | max:10000; `null` clears value | Updated project description |
| lead_user_id | int64 | No | target user must exist | Updated project lead |
| project_category_id | int64 | No | category must exist in tenant when non-null; `null` clears value | Updated category |
| url | string | No | valid URL, max:255; `null` clears value | Updated external URL |
| avatar_id | int64 | No | valid asset ID; `null` clears value | Updated avatar |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Project ID |
| key | string | Updated project key |
| name | string | Updated project name |
| description | string | Updated project description |
| url | string | Updated external URL |
| lead_user_id | int64 | Updated project lead |
| avatar_id | int64 | Updated avatar |
| project_category_id | int64 | Updated category |
| project_type_key | string | Existing immutable project type |
| archived | bool | Existing archive state |
| issue_type_scheme_id | int64 | Existing bound issue type scheme |
| workflow_scheme_id | int64 | Existing bound workflow scheme |
| field_config_scheme_id | int64 | Existing bound field config scheme |
| issue_type_screen_scheme_id | int64 | Existing bound issue type screen scheme |
| permission_scheme_id | int64 | Existing bound permission scheme |
| notification_scheme_id | int64 | Existing bound notification scheme |
| priority_scheme_id | int64 | Existing bound priority scheme |
| issue_security_scheme_id | int64 | Existing bound issue security scheme |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the update |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by project permission evaluation |
