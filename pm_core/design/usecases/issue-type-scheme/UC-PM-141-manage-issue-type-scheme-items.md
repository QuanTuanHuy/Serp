# UC-PM-141 - Manage Issue Type Scheme Items

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Project provisioning schema: `schema/00_project_provisioning.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-141 |
| **Use Case Name** | Manage Issue Type Scheme Items |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Medium |
| **Complexity** | Medium |

### Description

Replace the ordered list of issue types assigned to a tenant-owned issue type scheme. This use case supports adding, removing, and reordering issue types within the scheme by replacing all active scheme items inside one transaction.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Manages issue type membership and order within a tenant-owned scheme |
| System | System | Validates tenant scope, read-only system rules, issue type visibility, duplicate entries, default-item invariants, usage guards, and persists the replacement item set |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Write APIs may manage items only for tenant-owned issue type schemes where `issue_type_schemes.tenant_id = tenantId`
- System-owned issue type schemes are read-only and cannot be changed through tenant APIs

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type scheme administration
4. Target scheme exists, is not soft-deleted, and belongs to the current tenant
6. Every requested `issue_type_id` is visible to the tenant
7. The requested item list is non-empty and contains no duplicate `issue_type_id` values
8. The scheme's `default_issue_type_id` is included in the resulting item list

### Postconditions

#### Success Postconditions

1. Existing active scheme items are replaced by the requested ordered item list
2. New `sequence` values are assigned according to request order
3. The scheme row remains unchanged except for `updated_at` and `updated_by`
4. Response returns the updated ordered item list

#### Failure Postconditions

1. No partial item replacement is committed
2. Existing active scheme items remain unchanged
3. Error response is returned with validation, authorization, lookup, or usage-conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/issue-type-schemes/{schemeId}/items` with an ordered list of `issue_type_id` values |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads scheme by `id=schemeId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates request list is non-empty and contains no duplicate `issue_type_id` values |
| 6 | System | Validates every requested `issue_type_id` resolves to a tenant-visible issue type |
| 7 | System | Validates the scheme's `default_issue_type_id` is included in the resulting list |
| 8 | System | Compares the current item set with the requested item set and identifies removed issue types |
| 9 | System | If the scheme is used by active projects, validates removed issue types do not have active work items in those projects |
| 10 | System | Begins database transaction |
| 11 | System | Soft-deletes or removes existing active scheme items for the scheme |
| 12 | System | Inserts the new ordered item set with `sequence` starting from `1` |
| 13 | System | Updates scheme audit fields |
| 14 | System | Commits transaction |
| 15 | System | Returns HTTP 200 with the updated ordered item list |

### Alternative Flows

#### AF-1: Reorder Only

**Branches from**: Main Flow Step 8  
**Condition**: Requested item set matches the current item membership and changes only the order

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Detects there are no added or removed issue types |
| 12.1 | System | Persists updated `sequence` values only |

**Rejoins**: Main Flow Step 13

#### AF-2: Add New Issue Types Without Removals

**Branches from**: Main Flow Step 8  
**Condition**: Requested list adds one or more new issue types and removes none

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Detects the removed-item set is empty |
| 9.1 | System | Skips work-item removal guard validation |

**Rejoins**: Main Flow Step 10

### Exception Flows

#### EF-1: Scheme Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_SCHEME_NOT_FOUND` |

#### EF-2: Invalid Item List

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 400 with validation error for empty or duplicate item list |

#### EF-3: Referenced Issue Type Not Found in Visible Scope

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_NOT_FOUND` |

#### EF-4: Default Issue Type Missing From Resulting Items

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 422 with error: `ISSUE_TYPE_SCHEME_DEFAULT_NOT_IN_ITEMS` |

#### EF-5: Removed Issue Type Still In Use

**Triggered at**: Main Flow Step 9

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.E1 | System | Returns HTTP 409 with error: `ISSUE_TYPE_SCHEME_IN_USE` |

#### EF-6: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-141-01 | Tenant callers may manage items only for issue type schemes owned by their own tenant | Authorization + Repository layer |
| BR-PM-141-02 | Write lookup for manage-items is tenant-only; system-owned schemes remain visible only through read APIs and are not addressable through tenant write paths | Service layer |
| BR-PM-141-03 | The scheme's `default_issue_type_id` must always be included in the active item list | Service layer |
| BR-PM-141-04 | Item replacement is transactional: the active item set is replaced atomically, not incrementally | UseCase layer |
| BR-PM-141-05 | Resulting item order is defined solely by request order and persisted via `sequence` starting at `1` | Service layer |
| BR-PM-141-06 | Duplicate `issue_type_id` values are not allowed in the resulting item list | Service layer + DB constraint |
| BR-PM-141-07 | Issue types removed from a scheme cannot be removed when active projects using that scheme still contain active work items of those types | Service layer |
| BR-PM-141-08 | Managing scheme items does not create, update, or delete issue type dictionary rows | Service layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| schemeId | int64 | Yes | min:1 | Scheme identifier from path |
| issue_type_ids | int64[] | Yes | non-empty; unique items; every ID must be tenant-visible | Ordered issue type IDs for the scheme |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Scheme ID |
| default_issue_type_id | int64 | Scheme default issue type |
| items | IssueTypeSchemeItem[] | Resulting ordered item list |
| items[].issue_type_id | int64 | Issue type ID |
| items[].sequence | int | Persisted order within the scheme |
| items[].issue_type.type_key | string | Stable issue type key |
| items[].issue_type.name | string | Display name |
| items[].issue_type.hierarchy_level | int | Hierarchy level |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the change |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for write isolation |
