# UC-PM-006 - Archive/Unarchive Project

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.1
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Projects schema: `schema/01_projects.md`
- Permissions and security schema: `schema/05_permissions_security.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-006 |
| **Use Case Name** | Archive/Unarchive Project |
| **Module** | PM Core |
| **Version** | 1.1 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Toggle the archive state of a project. Archived projects remain visible but are read-only for project-scoped write operations such as work item changes, worklog changes, and role-actor assignment changes. This use case archives by setting `archived=true` and `archived_at`, and unarchives by clearing them.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Project Lead | Primary | Archives or unarchives a project they can administer |
| PM Admin | Secondary | May archive or unarchive when they satisfy project-scoped administration rules |
| System | System | Validates project scope, project administration permission, toggles archive state, and persists the update |

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

### Postconditions

#### Success Postconditions

1. For archive: `archived=true` and `archived_at` is set to current timestamp
2. For unarchive: `archived=false` and `archived_at` is cleared
3. `updated_at` and `updated_by` are updated
4. Response returns the updated project payload

#### Failure Postconditions

1. No archive-state change is committed
2. Existing project data remains unchanged
3. Error response is returned with authorization, state, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends `POST /api/v1/projects/{projectId}/archive` or `POST /api/v1/projects/{projectId}/unarchive` |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId`, validates it exists in tenant scope |
| 4 | System | Evaluates `ADMINISTER_PROJECTS` for the caller on the target project |
| 5 | System | For archive request: validates project is not already archived |
| 6 | System | For unarchive request: validates project is currently archived |
| 7 | System | Updates archive state and audit fields inside transaction |
| 8 | System | Returns HTTP 200 with updated project payload |

### Alternative Flows

#### AF-1: Archive Project

**Branches from**: Main Flow Step 5-7  
**Condition**: Request targets `/archive`

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Confirms `archived=false` |
| 7.1 | System | Sets `archived=true` and `archived_at=NOW()` |

**Rejoins**: Main Flow Step 8

#### AF-2: Unarchive Project

**Branches from**: Main Flow Step 6-7  
**Condition**: Request targets `/unarchive`

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.1 | System | Confirms `archived=true` |
| 7.1 | System | Sets `archived=false` and clears `archived_at` |

**Rejoins**: Main Flow Step 8

### Exception Flows

#### EF-1: Project Not Found

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `PROJECT_NOT_FOUND` |

#### EF-2: Project Permission Denied

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission detail `ADMINISTER_PROJECTS` |

#### EF-3: Project Already Archived

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `PROJECT_ALREADY_ARCHIVED` |

#### EF-4: Project Not Archived

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `PROJECT_NOT_ARCHIVED` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-006-01 | Archiving and unarchiving require `ADMINISTER_PROJECTS` in the target project | Authorization layer |
| BR-PM-006-02 | Archive request must be rejected when project is already archived | Service layer |
| BR-PM-006-03 | Unarchive request must be rejected when project is not archived | Service layer |
| BR-PM-006-04 | Archived projects reject downstream project-scoped write operations until unarchived | Service layer |
| BR-PM-006-05 | This implementation scope updates project state only and does not publish Kafka or outbox events for archive-state changes | Application layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| projectId | int64 | Yes | min:1 | Project identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Project ID |
| key | string | Project key |
| name | string | Project name |
| archived | bool | Updated archive state |
| archived_at | timestamp | Archive timestamp or `null` when unarchived |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the action |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the action |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by project permission evaluation |
