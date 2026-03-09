# PM Core - Use Case Specification

> **Version**: 1.0
> **Date**: 2026-02-20
> **Module Code**: PM
> **Tech Stack**: Go (Gin + FX) + PostgreSQL + Kafka
> **Soft Delete**: `deleted_at TIMESTAMP NULL`

---

## 1. Module Overview

### Purpose

PM Core is a JIRA-like project management module that provides comprehensive work item tracking, configurable workflows, custom fields, screen management, and fine-grained permission control. It enables teams to plan, track, and deliver work through customizable processes and agile methodologies.

### Scope

**In Scope (Phase 1+2)**:
- **Module 00**: Project Provisioning & Scheme Cloning (template resolution, deep clone, clone-and-swap rebinding)
- **Module 01**: Projects & Configuration (projects, categories, blueprints, components, versions, roles)
- **Module 02**: Issues & Work Items (work items, issue types, priorities, resolutions, links, worklogs, custom field values)
- **Module 03**: Workflow Engine (statuses, workflows, transitions, rules, workflow schemes)
- **Module 04**: Fields & Screens (custom fields, field configurations, screens, screen schemes)
- **Module 05**: Permissions & Security (permission definitions, permission schemes, issue security)

**Out of Scope (Phase 2+)**:
- Module 06: Notifications (event-driven delivery)
- Module 07: Agile & Planning (boards, sprints, ranking)
- Module 08: Search & Reporting (saved filters, dashboards)
- Module 09: Collaboration & Audit (comments, attachments, watchers, change logs)

### Integration Points

| Service | Type | Purpose |
|---------|------|---------|
| Account Service | Sync (REST) | User authentication, user/group lookup, RBAC |
| API Gateway | Sync (REST) | JWT validation, routing |
| Notification Service | Async (Kafka) | Send alerts on work item changes, assignments, mentions |
| Discuss Service | Async (Kafka) | Activity feed events for projects and work items |
| serp_llm | Async (Kafka) | AI-assisted summarization, description generation |

---

## 2. Actors

| Actor | Type | Description | Key Permissions |
|-------|------|-------------|-----------------|
| PM Admin | Primary | Configures global schemes, workflows, fields, permissions, issue types, priorities, resolutions | `PM.PROJECT.ADMIN`, `PM.WORKFLOW.MANAGE`, `PM.FIELD.MANAGE`, `PM.PERMISSION.READ`, `PM.PERMISSION_SCHEME.MANAGE` |
| Project Lead | Primary | Creates/manages projects, assigns roles, manages components and versions | `PM.PROJECT.CREATE`, `PM.PROJECT.UPDATE`, `PM.COMPONENT.MANAGE`, `PM.VERSION.MANAGE` |
| Team Member | Primary | Creates/updates work items, logs work, transitions statuses, manages links | `PM.WORK_ITEM.CREATE`, `PM.WORK_ITEM.UPDATE`, `PM.WORK_ITEM.TRANSITION`, `PM.WORKLOG.MANAGE` |
| Viewer | Secondary | Read-only access to projects, work items, and configurations | `PM.PROJECT.READ`, `PM.WORK_ITEM.READ` |
| System (Kafka) | System | Handles async events from other services, triggers automation | N/A |

---

## 3. Use Case Summary

### 3.1. Module 01: Projects & Configuration

| UC ID | Name | Actor | Priority | Complexity | Entity |
|-------|------|-------|----------|------------|--------|
| UC-PM-001 | Create Project | Project Lead | High | Complex | Project |
| UC-PM-002 | Update Project | Project Lead | High | Medium | Project |
| UC-PM-003 | Get Project by ID | Team Member | High | Simple | Project |
| UC-PM-004 | List Projects with Filters | Team Member | High | Simple | Project |
| UC-PM-005 | Delete Project | PM Admin | Medium | Medium | Project |
| UC-PM-006 | Archive/Unarchive Project | Project Lead | Medium | Simple | Project |
| UC-PM-007 | Update Project Scheme Bindings | PM Admin | Medium | Complex | Project |
| UC-PM-011 | Create Project Category | PM Admin | Medium | Simple | Project Category |
| UC-PM-012 | Update Project Category | PM Admin | Medium | Simple | Project Category |
| UC-PM-013 | Get Project Category by ID | PM Admin | Low | Simple | Project Category |
| UC-PM-014 | List Project Categories | PM Admin | Medium | Simple | Project Category |
| UC-PM-015 | Delete Project Category | PM Admin | Low | Simple | Project Category |
| UC-PM-016 | Create Project Blueprint | PM Admin | Medium | Medium | Project Blueprint |
| UC-PM-017 | Update Project Blueprint | PM Admin | Medium | Simple | Project Blueprint |
| UC-PM-018 | Get Project Blueprint by ID | PM Admin | Low | Simple | Project Blueprint |
| UC-PM-019 | List Project Blueprints | PM Admin | Medium | Simple | Project Blueprint |
| UC-PM-020 | Delete Project Blueprint | PM Admin | Low | Simple | Project Blueprint |
| UC-PM-021 | Manage Blueprint Scheme Defaults | PM Admin | Medium | Medium | Blueprint Scheme Defaults |
| UC-PM-026 | Create Project Component | Project Lead | High | Simple | Project Component |
| UC-PM-027 | Update Project Component | Project Lead | Medium | Simple | Project Component |
| UC-PM-028 | Get Project Component by ID | Team Member | Low | Simple | Project Component |
| UC-PM-029 | List Project Components | Team Member | Medium | Simple | Project Component |
| UC-PM-030 | Delete Project Component | Project Lead | Low | Simple | Project Component |
| UC-PM-031 | Create Project Version | Project Lead | High | Simple | Project Version |
| UC-PM-032 | Update Project Version | Project Lead | Medium | Simple | Project Version |
| UC-PM-033 | Get Project Version by ID | Team Member | Low | Simple | Project Version |
| UC-PM-034 | List Project Versions | Team Member | Medium | Simple | Project Version |
| UC-PM-035 | Delete Project Version | Project Lead | Low | Simple | Project Version |
| UC-PM-036 | Release Project Version | Project Lead | High | Medium | Project Version |
| UC-PM-037 | Archive Project Version | Project Lead | Low | Simple | Project Version |
| UC-PM-041 | Create Project Role | PM Admin | Medium | Simple | Project Role |
| UC-PM-042 | Update Project Role | PM Admin | Medium | Simple | Project Role |
| UC-PM-043 | Get Project Role by ID | PM Admin | Low | Simple | Project Role |
| UC-PM-044 | List Project Roles | PM Admin | Medium | Simple | Project Role |
| UC-PM-045 | Delete Project Role | PM Admin | Low | Simple | Project Role |
| UC-PM-046 | Add Project Role Actor | Project Lead | High | Simple | Project Role Actor |
| UC-PM-047 | Remove Project Role Actor | Project Lead | Medium | Simple | Project Role Actor |
| UC-PM-048 | List Project Role Actors | Project Lead | Medium | Simple | Project Role Actor |

### 3.2. Module 02: Issues & Work Items

| UC ID | Name | Actor | Priority | Complexity | Entity |
|-------|------|-------|----------|------------|--------|
| UC-PM-101 | Create Work Item | Team Member | High | Complex | Work Item |
| UC-PM-102 | Update Work Item | Team Member | High | Medium | Work Item |
| UC-PM-103 | Get Work Item by ID | Team Member | High | Simple | Work Item |
| UC-PM-104 | List Work Items with Filters | Team Member | High | Medium | Work Item |
| UC-PM-105 | Delete Work Item | Project Lead | Medium | Medium | Work Item |
| UC-PM-106 | Transition Work Item Status | Team Member | High | Complex | Work Item |
| UC-PM-107 | Assign Work Item | Team Member | High | Simple | Work Item |
| UC-PM-108 | Re-rank Work Item (Lexorank) | Team Member | Medium | Medium | Work Item |
| UC-PM-109 | Bulk Update Work Items | Project Lead | Medium | Complex | Work Item |
| UC-PM-110 | Clone Work Item | Team Member | Medium | Medium | Work Item |
| UC-PM-111 | Manage Work Item Components | Team Member | Medium | Simple | Work Item Component |
| UC-PM-116 | Manage Work Item Fix Versions | Team Member | Medium | Simple | Work Item Fix Version |
| UC-PM-121 | Move Work Item to Sprint | Team Member | High | Medium | Work Item Sprint |
| UC-PM-122 | Remove Work Item from Sprint | Team Member | Medium | Simple | Work Item Sprint |
| UC-PM-131 | Create Issue Type | PM Admin | High | Simple | Issue Type |
| UC-PM-132 | Update Issue Type | PM Admin | Medium | Simple | Issue Type |
| UC-PM-133 | Get Issue Type by ID | PM Admin | Low | Simple | Issue Type |
| UC-PM-134 | List Issue Types | PM Admin | Medium | Simple | Issue Type |
| UC-PM-135 | Delete Issue Type | PM Admin | Low | Simple | Issue Type |
| UC-PM-136 | Create Issue Type Scheme | PM Admin | Medium | Simple | Issue Type Scheme |
| UC-PM-137 | Update Issue Type Scheme | PM Admin | Medium | Simple | Issue Type Scheme |
| UC-PM-138 | Get Issue Type Scheme by ID | PM Admin | Low | Simple | Issue Type Scheme |
| UC-PM-139 | List Issue Type Schemes | PM Admin | Medium | Simple | Issue Type Scheme |
| UC-PM-140 | Delete Issue Type Scheme | PM Admin | Low | Simple | Issue Type Scheme |
| UC-PM-141 | Manage Issue Type Scheme Items | PM Admin | Medium | Medium | Issue Type Scheme Item |
| UC-PM-146 | Create Priority | PM Admin | Medium | Simple | Priority |
| UC-PM-147 | Update Priority | PM Admin | Medium | Simple | Priority |
| UC-PM-148 | Get Priority by ID | PM Admin | Low | Simple | Priority |
| UC-PM-149 | List Priorities | PM Admin | Medium | Simple | Priority |
| UC-PM-150 | Delete Priority | PM Admin | Low | Simple | Priority |
| UC-PM-151 | Create Priority Scheme | PM Admin | Medium | Simple | Priority Scheme |
| UC-PM-152 | Update Priority Scheme | PM Admin | Medium | Simple | Priority Scheme |
| UC-PM-153 | Get Priority Scheme by ID | PM Admin | Low | Simple | Priority Scheme |
| UC-PM-154 | List Priority Schemes | PM Admin | Medium | Simple | Priority Scheme |
| UC-PM-155 | Delete Priority Scheme | PM Admin | Low | Simple | Priority Scheme |
| UC-PM-156 | Manage Priority Scheme Items | PM Admin | Medium | Medium | Priority Scheme Item |
| UC-PM-161 | Create Resolution | PM Admin | Medium | Simple | Resolution |
| UC-PM-162 | Update Resolution | PM Admin | Medium | Simple | Resolution |
| UC-PM-163 | Get Resolution by ID | PM Admin | Low | Simple | Resolution |
| UC-PM-164 | List Resolutions | PM Admin | Medium | Simple | Resolution |
| UC-PM-165 | Delete Resolution | PM Admin | Low | Simple | Resolution |
| UC-PM-171 | Create Issue Link Type | PM Admin | Medium | Simple | Issue Link Type |
| UC-PM-172 | Update Issue Link Type | PM Admin | Medium | Simple | Issue Link Type |
| UC-PM-173 | Get Issue Link Type by ID | PM Admin | Low | Simple | Issue Link Type |
| UC-PM-174 | List Issue Link Types | PM Admin | Medium | Simple | Issue Link Type |
| UC-PM-175 | Delete Issue Link Type | PM Admin | Low | Simple | Issue Link Type |
| UC-PM-176 | Create Issue Link | Team Member | High | Simple | Issue Link |
| UC-PM-177 | Delete Issue Link | Team Member | Medium | Simple | Issue Link |
| UC-PM-178 | List Issue Links for Work Item | Team Member | Medium | Simple | Issue Link |
| UC-PM-181 | Create Worklog | Team Member | High | Simple | Worklog |
| UC-PM-182 | Update Worklog | Team Member | Medium | Simple | Worklog |
| UC-PM-183 | Delete Worklog | Team Member | Medium | Simple | Worklog |
| UC-PM-184 | List Worklogs for Work Item | Team Member | Medium | Simple | Worklog |
| UC-PM-185 | Get Worklog by ID | Team Member | Low | Simple | Worklog |

### 3.3. Module 03: Workflow Engine

| UC ID | Name | Actor | Priority | Complexity | Entity |
|-------|------|-------|----------|------------|--------|
| UC-PM-201 | Create Status Category | PM Admin | Medium | Simple | Status Category |
| UC-PM-202 | Update Status Category | PM Admin | Low | Simple | Status Category |
| UC-PM-203 | Get Status Category by ID | PM Admin | Low | Simple | Status Category |
| UC-PM-204 | List Status Categories | PM Admin | Medium | Simple | Status Category |
| UC-PM-205 | Delete Status Category | PM Admin | Low | Simple | Status Category |
| UC-PM-211 | Create Status | PM Admin | High | Simple | Status |
| UC-PM-212 | Update Status | PM Admin | Medium | Simple | Status |
| UC-PM-213 | Get Status by ID | PM Admin | Low | Simple | Status |
| UC-PM-214 | List Statuses | PM Admin | Medium | Simple | Status |
| UC-PM-215 | Delete Status | PM Admin | Low | Medium | Status |
| UC-PM-221 | Create Workflow | PM Admin | High | Medium | Workflow |
| UC-PM-222 | Update Workflow | PM Admin | High | Medium | Workflow |
| UC-PM-223 | Get Workflow by ID | PM Admin | Medium | Simple | Workflow |
| UC-PM-224 | List Workflows | PM Admin | Medium | Simple | Workflow |
| UC-PM-225 | Delete Workflow | PM Admin | Medium | Medium | Workflow |
| UC-PM-226 | Publish Workflow | PM Admin | High | Complex | Workflow |
| UC-PM-227 | Clone Workflow | PM Admin | Medium | Medium | Workflow |
| UC-PM-228 | Validate Workflow | PM Admin | High | Complex | Workflow |
| UC-PM-231 | Add Workflow Step | PM Admin | High | Simple | Workflow Step |
| UC-PM-232 | Remove Workflow Step | PM Admin | Medium | Medium | Workflow Step |
| UC-PM-233 | Reorder Workflow Steps | PM Admin | Low | Simple | Workflow Step |
| UC-PM-236 | Add Workflow Transition | PM Admin | High | Medium | Workflow Transition |
| UC-PM-237 | Update Workflow Transition | PM Admin | Medium | Simple | Workflow Transition |
| UC-PM-238 | Remove Workflow Transition | PM Admin | Medium | Simple | Workflow Transition |
| UC-PM-239 | List Workflow Transitions | PM Admin | Medium | Simple | Workflow Transition |
| UC-PM-241 | Add Workflow Transition Rule | PM Admin | Medium | Medium | Workflow Transition Rule |
| UC-PM-242 | Update Workflow Transition Rule | PM Admin | Medium | Simple | Workflow Transition Rule |
| UC-PM-243 | Remove Workflow Transition Rule | PM Admin | Medium | Simple | Workflow Transition Rule |
| UC-PM-251 | Create Workflow Scheme | PM Admin | Medium | Simple | Workflow Scheme |
| UC-PM-252 | Update Workflow Scheme | PM Admin | Medium | Simple | Workflow Scheme |
| UC-PM-253 | Get Workflow Scheme by ID | PM Admin | Low | Simple | Workflow Scheme |
| UC-PM-254 | List Workflow Schemes | PM Admin | Medium | Simple | Workflow Scheme |
| UC-PM-255 | Delete Workflow Scheme | PM Admin | Low | Simple | Workflow Scheme |
| UC-PM-256 | Manage Workflow Scheme Items | PM Admin | Medium | Medium | Workflow Scheme Item |

### 3.4. Module 04: Fields & Screens

| UC ID | Name | Actor | Priority | Complexity | Entity |
|-------|------|-------|----------|------------|--------|
| UC-PM-301 | Create Custom Field | PM Admin | High | Medium | Custom Field |
| UC-PM-302 | Update Custom Field | PM Admin | Medium | Simple | Custom Field |
| UC-PM-303 | Get Custom Field by ID | PM Admin | Low | Simple | Custom Field |
| UC-PM-304 | List Custom Fields | PM Admin | Medium | Simple | Custom Field |
| UC-PM-305 | Delete Custom Field | PM Admin | Medium | Medium | Custom Field |
| UC-PM-306 | Manage Custom Field Options | PM Admin | Medium | Medium | Custom Field Option |
| UC-PM-311 | Manage Custom Field Contexts | PM Admin | Medium | Medium | Custom Field Context |
| UC-PM-321 | Create Field Configuration | PM Admin | Medium | Simple | Field Configuration |
| UC-PM-322 | Update Field Configuration | PM Admin | Medium | Simple | Field Configuration |
| UC-PM-323 | Get Field Configuration by ID | PM Admin | Low | Simple | Field Configuration |
| UC-PM-324 | List Field Configurations | PM Admin | Medium | Simple | Field Configuration |
| UC-PM-325 | Delete Field Configuration | PM Admin | Low | Simple | Field Configuration |
| UC-PM-326 | Manage Field Configuration Items | PM Admin | Medium | Medium | Field Configuration Item |
| UC-PM-331 | Create Field Config Scheme | PM Admin | Medium | Simple | Field Config Scheme |
| UC-PM-332 | Update Field Config Scheme | PM Admin | Medium | Simple | Field Config Scheme |
| UC-PM-333 | Get Field Config Scheme by ID | PM Admin | Low | Simple | Field Config Scheme |
| UC-PM-334 | List Field Config Schemes | PM Admin | Medium | Simple | Field Config Scheme |
| UC-PM-335 | Delete Field Config Scheme | PM Admin | Low | Simple | Field Config Scheme |
| UC-PM-336 | Manage Field Config Scheme Items | PM Admin | Medium | Medium | Field Config Scheme Item |
| UC-PM-341 | Create Screen | PM Admin | Medium | Simple | Screen |
| UC-PM-342 | Update Screen | PM Admin | Medium | Simple | Screen |
| UC-PM-343 | Get Screen by ID | PM Admin | Low | Simple | Screen |
| UC-PM-344 | List Screens | PM Admin | Medium | Simple | Screen |
| UC-PM-345 | Delete Screen | PM Admin | Low | Simple | Screen |
| UC-PM-346 | Manage Screen Tabs | PM Admin | Medium | Medium | Screen Tab |
| UC-PM-349 | Manage Screen Tab Fields | PM Admin | Medium | Medium | Screen Tab Field |
| UC-PM-356 | Create Screen Scheme | PM Admin | Medium | Simple | Screen Scheme |
| UC-PM-357 | Update Screen Scheme | PM Admin | Medium | Simple | Screen Scheme |
| UC-PM-358 | Get Screen Scheme by ID | PM Admin | Low | Simple | Screen Scheme |
| UC-PM-359 | List Screen Schemes | PM Admin | Medium | Simple | Screen Scheme |
| UC-PM-360 | Delete Screen Scheme | PM Admin | Low | Simple | Screen Scheme |
| UC-PM-361 | Manage Screen Scheme Items | PM Admin | Medium | Medium | Screen Scheme Item |
| UC-PM-366 | Create Issue Type Screen Scheme | PM Admin | Medium | Simple | Issue Type Screen Scheme |
| UC-PM-367 | Update Issue Type Screen Scheme | PM Admin | Medium | Simple | Issue Type Screen Scheme |
| UC-PM-368 | Get Issue Type Screen Scheme by ID | PM Admin | Low | Simple | Issue Type Screen Scheme |
| UC-PM-369 | List Issue Type Screen Schemes | PM Admin | Medium | Simple | Issue Type Screen Scheme |
| UC-PM-370 | Delete Issue Type Screen Scheme | PM Admin | Low | Simple | Issue Type Screen Scheme |
| UC-PM-371 | Manage Issue Type Screen Scheme Items | PM Admin | Medium | Medium | Issue Type Screen Scheme Item |

### 3.5. Module 05: Permissions & Security

| UC ID | Name | Actor | Priority | Complexity | Entity |
|-------|------|-------|----------|------------|--------|
| UC-PM-401 | Get Permission Definition by Key | PM Admin | Low | Simple | Permission Definition |
| UC-PM-402 | List Permission Definitions | PM Admin | Medium | Simple | Permission Definition |
| UC-PM-411 | Create Permission Scheme | PM Admin | High | Simple | Permission Scheme |
| UC-PM-412 | Update Permission Scheme | PM Admin | Medium | Simple | Permission Scheme |
| UC-PM-413 | Get Permission Scheme by ID | PM Admin | Low | Simple | Permission Scheme |
| UC-PM-414 | List Permission Schemes | PM Admin | Medium | Simple | Permission Scheme |
| UC-PM-415 | Delete Permission Scheme | PM Admin | Low | Simple | Permission Scheme |
| UC-PM-416 | Add Permission Scheme Entry | PM Admin | High | Medium | Permission Scheme Entry |
| UC-PM-417 | Update Permission Scheme Entry | PM Admin | Medium | Simple | Permission Scheme Entry |
| UC-PM-418 | Remove Permission Scheme Entry | PM Admin | Medium | Simple | Permission Scheme Entry |
| UC-PM-419 | List Permission Scheme Entries | PM Admin | Medium | Simple | Permission Scheme Entry |
| UC-PM-421 | Create Issue Security Scheme | PM Admin | Medium | Simple | Issue Security Scheme |
| UC-PM-422 | Update Issue Security Scheme | PM Admin | Medium | Simple | Issue Security Scheme |
| UC-PM-423 | Get Issue Security Scheme by ID | PM Admin | Low | Simple | Issue Security Scheme |
| UC-PM-424 | List Issue Security Schemes | PM Admin | Medium | Simple | Issue Security Scheme |
| UC-PM-425 | Delete Issue Security Scheme | PM Admin | Low | Simple | Issue Security Scheme |
| UC-PM-426 | Add Issue Security Level | PM Admin | Medium | Simple | Issue Security Level |
| UC-PM-427 | Update Issue Security Level | PM Admin | Medium | Simple | Issue Security Level |
| UC-PM-428 | Remove Issue Security Level | PM Admin | Medium | Simple | Issue Security Level |
| UC-PM-431 | Add Issue Security Level Member | PM Admin | Medium | Simple | Issue Security Level Member |
| UC-PM-432 | Remove Issue Security Level Member | PM Admin | Medium | Simple | Issue Security Level Member |
| UC-PM-433 | List Issue Security Level Members | PM Admin | Medium | Simple | Issue Security Level Member |

---

## 4. Entity Status Workflows

### 4.1. Work Item Status Flow

Work item statuses are fully configurable via the Workflow Engine. The default workflow provides:

```
[To Do] --Start Progress--> [In Progress] --Submit for Review--> [In Review]
                                           \--Done--> [Done]
[In Review] --Approve--> [Done]
[In Review] --Request Changes--> [In Progress]
[Done] --Reopen--> [To Do]
```

Status categories map statuses into three logical groups for reporting:
- **To Do** (status_category: `new`): Open, Backlog, To Do
- **In Progress** (status_category: `indeterminate`): In Progress, In Review, Testing
- **Done** (status_category: `done`): Done, Closed, Resolved

**Default Workflow Transition Rules**:

| From | To | Action | Condition | Actor |
|------|----|--------|-----------|-------|
| * (any) | To Do | Reopen | Work item is in Done category | Team Member |
| To Do | In Progress | Start Progress | None | Team Member (assignee or has TRANSITION perm) |
| In Progress | In Review | Submit for Review | None | Team Member |
| In Progress | Done | Mark Done | Resolution must be set | Team Member |
| In Review | Done | Approve | Resolution must be set | Team Member |
| In Review | In Progress | Request Changes | None | Team Member |

### 4.2. Sprint State Flow

```
[FUTURE] --Activate Sprint--> [ACTIVE] --Complete Sprint--> [CLOSED]
```

**Transition Rules**:

| From | To | Action | Condition | Actor |
|------|----|--------|-----------|-------|
| FUTURE | ACTIVE | Activate Sprint | No other active sprint on same board; start_date set | Project Lead |
| ACTIVE | CLOSED | Complete Sprint | Must have at least one work item; complete_date set | Project Lead |

### 4.3. Project Version State Flow

```
[Unreleased] --Release--> [Released]
[Unreleased/Released] --Archive--> [Archived]
[Archived] --Unarchive--> [Unreleased/Released (restore previous state)]
```

**Transition Rules**:

| From | To | Action | Condition | Actor |
|------|----|--------|-----------|-------|
| released=false | released=true | Release Version | release_date set | Project Lead |
| released=true | released=false | Unrelease Version | No constraint | Project Lead |
| archived=false | archived=true | Archive Version | No constraint | Project Lead |
| archived=true | archived=false | Unarchive Version | No constraint | Project Lead |

### 4.4. Project Archive Flow

```
[Active] --Archive--> [Archived] --Unarchive--> [Active]
```

| From | To | Action | Condition | Actor |
|------|----|--------|-----------|-------|
| archived=false | archived=true | Archive Project | Sets archived_at timestamp | Project Lead |
| archived=true | archived=false | Unarchive Project | Clears archived_at | Project Lead |

### 4.5. Workflow Lifecycle

```
[Draft] --Publish--> [Active (v1)] --Edit--> [Draft (v2)] --Publish--> [Active (v2)]
```

Only one active version per workflow. Publishing increments `version_no` and swaps `is_active`.

### 4.6. Notification Outbox Status Flow (reference)

```
[PENDING] --Pick up--> [PROCESSING] --Deliver--> [SENT]
                                     \--Fail--> [FAILED] --Retry--> [PROCESSING]
                                                         \--Max retries--> [DEAD]
```

---

## 5. Detailed Use Cases

### 5.1. Project Management (Module 01)

---

#### UC-PM-001: Create Project

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-001 |
| **Use Case Name** | Create Project |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-20 |
| **Priority** | High |
| **Complexity** | Complex |

##### Description

Allow a Project Lead to create a new project, optionally based on a blueprint template. The project is the central container that binds multiple configuration schemes (issue types, workflows, fields, screens, permissions, notifications, priorities). Creating a project provisions project-owned scheme bindings by deep-cloning template schemes resolved from explicit overrides, blueprint defaults, or system defaults.

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Project Lead | Primary | Initiates project creation |
| PM Admin | Secondary | May also create projects |
| System | System | Resolves template defaults, deep-clones project schemes, publishes event |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.PROJECT.CREATE`
3. User belongs to an active tenant

##### Postconditions

###### Success Postconditions
1. Project record persisted in `projects` table with `deleted_at=NULL`
2. Project-owned scheme bindings provisioned via deep clone from resolved templates
3. Kafka event `PROJECT_CREATED` published to topic `serp.pm.project.events`
4. Audit fields set: `created_at`, `updated_at`, `created_by`, `updated_by`

###### Failure Postconditions
1. No data changes committed (transaction rolled back)
2. Error response returned with appropriate HTTP status and error details

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends POST `/api/v1/projects` with project data |
| 2 | System | Validates JWT token and extracts `userId`, `tenantId` |
| 3 | System | Checks user has `PM.PROJECT.CREATE` permission |
| 4 | System | Validates input data: required fields (name, key, project_type_key) |
| 5 | System | Validates project key format: uppercase alphanumeric, 2-10 chars |
| 6 | System | Checks for duplicate project key within tenant |
| 7 | System | Validates `lead_user_id` exists (via Account Service REST call) |
| 8 | System | If `project_category_id` provided, validates it exists in tenant |
| 9 | System | Begins database transaction |
| 10 | System | Creates Project entity with `archived=false`, sets audit fields |
| 11 | System | Resolves template schemes (explicit override > blueprint default > system default), deep-clones scheme graphs, and binds cloned scheme IDs to the project |
| 12 | System | Commits transaction |
| 13 | System | Publishes `PROJECT_CREATED` event to Kafka topic `serp.pm.project.events` |
| 14 | System | Returns HTTP 201 with created project data including resolved scheme names |

##### Alternative Flows

###### AF-1: Project from Blueprint

**Branches from**: Main Flow Step 11
**Condition**: Request includes `blueprint_id`

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.1 | System | Validates blueprint exists and belongs to tenant |
| 11.2 | System | Loads all `blueprint_scheme_defaults` for the blueprint |
| 11.3 | System | Deep-clones each resolved scheme graph in FK-safe order and records cloned IDs |
| 11.4 | System | Assigns cloned scheme IDs to the new project (issue_type_scheme_id, workflow_scheme_id, etc.) |

**Rejoins**: Main Flow Step 12

###### AF-2: Explicit Scheme Overrides

**Branches from**: Main Flow Step 11
**Condition**: Request includes explicit scheme IDs (e.g., `workflow_scheme_id`, `permission_scheme_id`)

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.1 | System | Validates each provided scheme ID exists and belongs to tenant |
| 11.2 | System | Uses provided scheme IDs as template sources before deep-clone provisioning |

**Rejoins**: Main Flow Step 12

##### Exception Flows

###### EF-1: Validation Error

**Triggered at**: Main Flow Step 4-5
**Condition**: Input data fails validation

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Collects all validation errors |
| 4.E2 | System | Returns HTTP 400 with validation error details |

###### EF-2: Duplicate Project Key

**Triggered at**: Main Flow Step 6
**Condition**: Project with same key already exists in tenant

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `PROJECT_KEY_ALREADY_EXISTS` |

###### EF-3: Lead User Not Found

**Triggered at**: Main Flow Step 7
**Condition**: `lead_user_id` does not exist in Account Service

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 404 with error: `USER_NOT_FOUND` |

###### EF-4: Blueprint Not Found

**Triggered at**: AF-1, Step 11.1
**Condition**: Referenced `blueprint_id` does not exist

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.1.E1 | System | Returns HTTP 404 with error: `BLUEPRINT_NOT_FOUND` |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-001-01 | Project key must be unique within tenant (case-insensitive, uppercase) | Service layer + DB unique constraint `(tenant_id, key)` |
| BR-PM-001-02 | Project key format: 2-10 uppercase alphanumeric characters | DTO validation |
| BR-PM-001-03 | Project name is required, 1-255 characters | DTO validation |
| BR-PM-001-04 | If no blueprint and no explicit schemes, use system default template schemes as deep-clone sources | Service layer |
| BR-PM-001-05 | `project_type_key` must be one of: `software`, `business`, `service_desk` | DTO validation |
| BR-PM-001-06 | New projects are always created with `archived=false` | Service layer |
| BR-PM-001-07 | Project creation must provision project-owned scheme clones; mutable schemes are not shared across projects by default | Service layer |

##### Data Requirements

###### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Project name |
| key | string | Yes | regex: `^[A-Z][A-Z0-9]{1,9}$` | Unique project key |
| description | string | No | max:10000 | Project description |
| project_type_key | string | Yes | software, business, service_desk | Project type |
| lead_user_id | int64 | Yes | must exist in Account Service | Project lead user |
| project_category_id | int64 | No | must exist in tenant | Category classification |
| blueprint_id | int64 | No | must exist in tenant | Blueprint template |
| url | string | No | valid URL, max:255 | External project URL |
| avatar_id | int64 | No | valid asset ID | Project avatar |
| issue_type_scheme_id | int64 | No | must exist in tenant | Explicit override |
| workflow_scheme_id | int64 | No | must exist in tenant | Explicit override |
| field_config_scheme_id | int64 | No | must exist in tenant | Explicit override |
| issue_type_screen_scheme_id | int64 | No | must exist in tenant | Explicit override |
| permission_scheme_id | int64 | No | must exist in tenant | Explicit override |
| notification_scheme_id | int64 | No | must exist in tenant | Explicit override |
| priority_scheme_id | int64 | No | must exist in tenant | Explicit override |
| issue_security_scheme_id | int64 | No | must exist in tenant | Explicit override |

###### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated project ID |
| name | string | Project name |
| key | string | Project key |
| description | string | Project description |
| project_type_key | string | Project type |
| lead_user_id | int64 | Project lead |
| project_category_id | int64 | Category (nullable) |
| archived | bool | Archive state (false) |
| issue_type_scheme_id | int64 | Bound project-owned issue type scheme |
| workflow_scheme_id | int64 | Bound project-owned workflow scheme |
| field_config_scheme_id | int64 | Bound project-owned field config scheme |
| issue_type_screen_scheme_id | int64 | Bound project-owned screen scheme |
| permission_scheme_id | int64 | Bound project-owned permission scheme |
| notification_scheme_id | int64 | Bound project-owned notification scheme |
| priority_scheme_id | int64 | Bound project-owned priority scheme |
| issue_security_scheme_id | int64 | Bound project-owned security scheme |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |

###### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the action |
| tenantId | JWT token | Tenant context for multi-tenancy isolation |

---

#### UC-PM-002: Update Project

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-002 |
| **Use Case Name** | Update Project |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Medium |

##### Description

Allow a Project Lead or PM Admin to update project metadata such as name, description, lead, category, and URL. Scheme bindings are updated via a separate use case (UC-PM-007).

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Project Lead | Primary | Updates project they lead |
| PM Admin | Secondary | Can update any project |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.PROJECT.UPDATE`
3. Project exists and is not soft-deleted

##### Postconditions

###### Success Postconditions
1. Project record updated in database
2. Kafka event `PROJECT_UPDATED` published to topic `serp.pm.project.events`
3. `updated_at`, `updated_by` fields updated

###### Failure Postconditions
1. No changes committed
2. Error response returned

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends PUT `/api/v1/projects/{projectId}` with updated fields |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Checks user has `PM.PROJECT.UPDATE` permission |
| 4 | System | Fetches project by ID, validates it exists and belongs to tenant |
| 5 | System | Validates input data |
| 6 | System | If `key` changed, validates new key is unique within tenant |
| 7 | System | If `lead_user_id` changed, validates user exists via Account Service |
| 8 | System | If `project_category_id` changed, validates category exists in tenant |
| 9 | System | Updates project entity, sets `updated_by=userId` |
| 10 | System | Persists changes within transaction |
| 11 | System | Publishes `PROJECT_UPDATED` event to Kafka |
| 12 | System | Returns HTTP 200 with updated project |

##### Exception Flows

###### EF-1: Project Not Found

**Triggered at**: Main Flow Step 4
**Condition**: Project does not exist or is soft-deleted

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `PROJECT_NOT_FOUND` |

###### EF-2: Duplicate Key on Update

**Triggered at**: Main Flow Step 6
**Condition**: New key conflicts with existing project in tenant

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `PROJECT_KEY_ALREADY_EXISTS` |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-002-01 | Cannot update an archived project (must unarchive first) | Service layer |
| BR-PM-002-02 | Project key change is allowed but must remain unique | Service layer + DB |
| BR-PM-002-03 | All queries must filter by tenantId | Repository layer |

##### Data Requirements

###### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | No | min:1, max:255 | Project name |
| key | string | No | regex: `^[A-Z][A-Z0-9]{1,9}$` | Project key |
| description | string | No | max:10000 | Description |
| lead_user_id | int64 | No | must exist | Project lead |
| project_category_id | int64 | No | must exist or null | Category |
| url | string | No | valid URL, max:255 | External URL |
| avatar_id | int64 | No | valid asset ID | Avatar |

###### Output Data

Same as UC-PM-001 output.

---

#### UC-PM-003: Get Project by ID

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-003 |
| **Use Case Name** | Get Project by ID |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Simple |

##### Description

Retrieve detailed project information including scheme binding names, lead user details, and category information.

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Views project details |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.PROJECT.READ`

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends GET `/api/v1/projects/{projectId}` |
| 2 | System | Validates JWT and permissions |
| 3 | System | Fetches project by ID with `tenant_id` filter and `deleted_at IS NULL` |
| 4 | System | Enriches response with scheme names, lead user name (optional expand) |
| 5 | System | Returns HTTP 200 with project details |

##### Exception Flows

###### EF-1: Project Not Found

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `PROJECT_NOT_FOUND` |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-003-01 | Only return non-deleted projects (deleted_at IS NULL) | Repository layer |
| BR-PM-003-02 | Filter by tenantId from JWT | Repository layer |

---

#### UC-PM-004: List Projects with Filters

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-004 |
| **Use Case Name** | List Projects with Filters |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Simple |

##### Description

Retrieve a paginated list of projects with optional filters for name/key search, category, type, and archive status.

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Browses project list |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.PROJECT.READ`

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends GET `/api/v1/projects` with query parameters |
| 2 | System | Validates JWT and permissions |
| 3 | System | Parses and validates query parameters |
| 4 | System | Builds query with `tenant_id` filter and `deleted_at IS NULL` |
| 5 | System | Applies optional filters (search, category_id, project_type_key, archived) |
| 6 | System | Executes count query for total records |
| 7 | System | Executes paginated query with sorting |
| 8 | System | Returns HTTP 200 with paginated response |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-004-01 | Results always filtered by tenantId | Repository layer |
| BR-PM-004-02 | Only non-deleted records (deleted_at IS NULL) | Repository layer |
| BR-PM-004-03 | Default pagination: page=0, pageSize=10, max pageSize=100 | Controller layer |
| BR-PM-004-04 | Default sort: created_at DESC | Repository layer |
| BR-PM-004-05 | Search applies to both name and key fields (case-insensitive) | Repository layer |

##### Data Requirements

###### Input Data (Query Parameters)

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| page | int | No | min:0, default:0 | Page number (0-indexed) |
| pageSize | int | No | min:1, max:100, default:10 | Items per page |
| search | string | No | min:1 | Search in name and key |
| project_category_id | int64 | No | must exist | Filter by category |
| project_type_key | string | No | software, business, service_desk | Filter by type |
| archived | bool | No | default: false | Include archived projects |
| lead_user_id | int64 | No | valid user ID | Filter by lead |
| sortBy | string | No | name, key, created_at, updated_at | Sort field |
| sortOrder | string | No | ASC, DESC | Sort direction |

###### Output Data

| Field | Type | Description |
|-------|------|-------------|
| data | Project[] | Array of project objects |
| meta.page | int | Current page |
| meta.pageSize | int | Items per page |
| meta.totalItems | int64 | Total matching records |
| meta.totalPages | int | Total pages |

---

#### UC-PM-005: Delete Project (Soft Delete)

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-005 |
| **Use Case Name** | Delete Project |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | Medium |
| **Complexity** | Medium |

##### Description

Soft-delete a project by setting `deleted_at` timestamp. This operation does not cascade-delete child entities (work items, components, versions) -- they become inaccessible via the project but are preserved for data recovery.

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Only admins can delete projects |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.PROJECT.DELETE`
3. Project exists and is not already deleted

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends DELETE `/api/v1/projects/{projectId}` |
| 2 | System | Validates JWT and permissions |
| 3 | System | Fetches project by ID with tenant_id filter |
| 4 | System | Validates project is not already soft-deleted |
| 5 | System | Sets `deleted_at=NOW()`, `updated_by=userId` within transaction |
| 6 | System | Publishes `PROJECT_DELETED` event to Kafka topic `serp.pm.project.events` |
| 7 | System | Returns HTTP 200 with success confirmation |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-005-01 | Soft delete sets `deleted_at` timestamp; does not physically remove data | Service layer |
| BR-PM-005-02 | Only PM Admin role can delete projects | Authorization layer |
| BR-PM-005-03 | Deleted projects are excluded from all list/search queries | Repository layer |

---

#### UC-PM-006: Archive/Unarchive Project

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-006 |
| **Use Case Name** | Archive/Unarchive Project |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | Medium |
| **Complexity** | Simple |

##### Description

Toggle the archive state of a project. Archived projects are visible but read-only (no new work items, no modifications to existing items).

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Project Lead | Primary | Archives/unarchives projects they lead |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.PROJECT.UPDATE`
3. Project exists and is not soft-deleted

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends POST `/api/v1/projects/{projectId}/archive` or `/unarchive` |
| 2 | System | Validates JWT and permissions |
| 3 | System | Fetches project, validates it exists |
| 4 | System | For archive: sets `archived=true`, `archived_at=NOW()` |
| 5 | System | For unarchive: sets `archived=false`, clears `archived_at` |
| 6 | System | Publishes `PROJECT_ARCHIVED` or `PROJECT_UNARCHIVED` event |
| 7 | System | Returns HTTP 200 with updated project |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-006-01 | Cannot archive an already archived project (idempotent check) | Service layer |
| BR-PM-006-02 | Archived projects reject all write operations on child entities | Service layer |

---

#### UC-PM-007: Update Project Scheme Bindings

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-007 |
| **Use Case Name** | Update Project Scheme Bindings |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-20 |
| **Priority** | Medium |
| **Complexity** | Complex |

##### Description

Update the scheme associations for a project (issue type scheme, workflow scheme, field config scheme, screen scheme, permission scheme, notification scheme, priority scheme, issue security scheme). To preserve isolation, the system performs clone-and-swap: selected template schemes are deep-cloned and then rebound to the target project atomically.

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Only admins can rebind schemes |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.PROJECT.ADMIN`
3. Project exists and is not archived or deleted

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends PUT `/api/v1/projects/{projectId}/schemes` with scheme binding updates |
| 2 | System | Validates JWT and permissions |
| 3 | System | Fetches project, validates it exists and is not archived |
| 4 | System | For each provided scheme ID, validates the template scheme exists and belongs to tenant |
| 5 | System | Begins transaction and deep-clones provided template scheme graphs in FK-safe order |
| 6 | System | Validates compatibility: cloned workflow scheme covers all issue types in the resulting cloned issue type scheme |
| 7 | System | Updates project scheme binding fields to cloned scheme IDs within transaction |
| 8 | System | Commits transaction |
| 9 | System | Publishes `PROJECT_SCHEMES_UPDATED` event to Kafka |
| 10 | System | Returns HTTP 200 with updated project |

##### Exception Flows

###### EF-1: Scheme Compatibility Error

**Triggered at**: Main Flow Step 6
**Condition**: New workflow scheme does not cover all issue types

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 422 with error: `SCHEME_INCOMPATIBLE` and list of uncovered issue types |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-007-01 | Cannot change schemes on archived projects | Service layer |
| BR-PM-007-02 | New workflow scheme must provide workflow mappings for all issue types in the project's resulting issue type scheme | UseCase layer |
| BR-PM-007-03 | Existing work items retain their current status; statuses not present in new workflow are flagged for migration | Service layer |
| BR-PM-007-04 | Scheme rebinding uses clone-and-swap; mutable schemes are not directly shared across projects by default | Service layer |

##### Data Requirements

###### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| issue_type_scheme_id | int64 | No | must exist in tenant | Issue type scheme |
| workflow_scheme_id | int64 | No | must exist in tenant | Workflow scheme |
| field_config_scheme_id | int64 | No | must exist in tenant | Field config scheme |
| issue_type_screen_scheme_id | int64 | No | must exist in tenant | Screen scheme |
| permission_scheme_id | int64 | No | must exist in tenant | Permission scheme |
| notification_scheme_id | int64 | No | must exist in tenant | Notification scheme |
| priority_scheme_id | int64 | No | must exist in tenant | Priority scheme |
| issue_security_scheme_id | int64 | No | must exist in tenant | Security scheme |

---

#### UC-PM-011 to UC-PM-015: Project Category CRUD

##### UC-PM-011: Create Project Category

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-011 |
| **Use Case Name** | Create Project Category |
| **Priority** | Medium |
| **Complexity** | Simple |

**Description**: Create a new project category for organizing projects within a tenant.

**Permission**: `PM.PROJECT_CATEGORY.CREATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/project-categories` with `{ name, description }` |
| 2 | System | Validates JWT, permissions, input data |
| 3 | System | Checks name is unique within tenant |
| 4 | System | Persists category, publishes `PROJECT_CATEGORY_CREATED` event |
| 5 | System | Returns HTTP 201 with created category |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-011-01 | Category name must be unique within tenant | Service + DB `UNIQUE(tenant_id, name)` |
| BR-PM-011-02 | Name required, 1-255 characters | DTO validation |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255, unique per tenant | Category name |
| description | string | No | max:2000 | Description |

##### UC-PM-012: Update Project Category

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-012 |
| **Use Case Name** | Update Project Category |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.PROJECT_CATEGORY.UPDATE`

**Main Flow**: Fetch by ID -> validate -> update name/description -> publish `PROJECT_CATEGORY_UPDATED` -> return 200.

**Exception**: If name conflicts with existing category -> HTTP 409.

##### UC-PM-013: Get Project Category by ID

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-013 |
| **Use Case Name** | Get Project Category by ID |
| **Priority** | Low |
| **Complexity** | Simple |

**Permission**: `PM.PROJECT_CATEGORY.READ`

**Main Flow**: GET `/api/v1/project-categories/{id}` -> validate JWT -> fetch with tenant_id + deleted_at IS NULL -> return 200.

##### UC-PM-014: List Project Categories

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-014 |
| **Use Case Name** | List Project Categories |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.PROJECT_CATEGORY.READ`

**Main Flow**: GET `/api/v1/project-categories` with pagination -> return paginated list filtered by tenant_id.

##### UC-PM-015: Delete Project Category

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-015 |
| **Use Case Name** | Delete Project Category |
| **Priority** | Low |
| **Complexity** | Simple |

**Permission**: `PM.PROJECT_CATEGORY.DELETE`

**Main Flow**: Soft-delete (set `deleted_at`) -> publish `PROJECT_CATEGORY_DELETED` event -> return 200.

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-015-01 | Cannot delete category if projects are still assigned to it | Service layer |

---

#### UC-PM-016 to UC-PM-021: Project Blueprint Management

##### UC-PM-016: Create Project Blueprint

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-016 |
| **Use Case Name** | Create Project Blueprint |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Create a project template (blueprint) that pre-configures scheme defaults for new projects.

**Permission**: `PM.BLUEPRINT.CREATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/project-blueprints` with blueprint data |
| 2 | System | Validates JWT, permissions, input |
| 3 | System | Validates `project_type_key` is valid |
| 4 | System | Persists blueprint |
| 5 | System | Publishes `BLUEPRINT_CREATED` event to `serp.pm.blueprint.events` |
| 6 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Blueprint name |
| description | string | No | max:2000 | Description |
| project_type_key | string | Yes | software, business, service_desk | Project type |
| avatar_url | string | No | valid URL | Icon URL |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-016-01 | Only PM Admin can create tenant-defined blueprints; `is_system=false` | Service layer |
| BR-PM-016-02 | System blueprints (`is_system=true`) cannot be created via API | Service layer |

##### UC-PM-017 to UC-PM-020: Blueprint Update, Get, List, Delete

Standard CRUD pattern. Permission: `PM.BLUEPRINT.MANAGE`.

- **UC-PM-017**: Update blueprint metadata (name, description, avatar_url). Cannot modify `is_system` blueprints.
- **UC-PM-018**: Get by ID with scheme defaults expanded.
- **UC-PM-019**: List blueprints with filters (project_type_key, is_system).
- **UC-PM-020**: Soft-delete. Cannot delete system blueprints. Existing projects are not affected because blueprint schemes are only used as provisioning templates.

##### UC-PM-021: Manage Blueprint Scheme Defaults

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-021 |
| **Use Case Name** | Manage Blueprint Scheme Defaults |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Set or update the default scheme bindings for a blueprint. When a project is created from this blueprint, these scheme IDs are resolved as template sources and then deep-cloned into project-owned schemes.

**Permission**: `PM.BLUEPRINT.MANAGE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends PUT `/api/v1/project-blueprints/{blueprintId}/scheme-defaults` with list of scheme mappings |
| 2 | System | Validates JWT, permissions |
| 3 | System | Validates blueprint exists |
| 4 | System | For each entry: validates `scheme_type` is valid enum, validates `scheme_id` exists for that type |
| 5 | System | Replaces all existing `blueprint_scheme_defaults` for the blueprint within transaction |
| 6 | System | Returns HTTP 200 with updated defaults |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| defaults[] | array | Yes | min: 1 entry | List of scheme defaults |
| defaults[].scheme_type | string | Yes | ISSUE_TYPE, WORKFLOW, FIELD_CONFIG, SCREEN, PERMISSION, ISSUE_SECURITY, NOTIFICATION, PRIORITY | Scheme type |
| defaults[].scheme_id | int64 | Yes | must exist for given type | Scheme ID |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-021-01 | Each scheme_type can appear at most once per blueprint | DB `UNIQUE(tenant_id, blueprint_id, scheme_type)` |
| BR-PM-021-02 | Cannot modify defaults for system blueprints | Service layer |

---

#### UC-PM-026 to UC-PM-030: Project Component CRUD

##### UC-PM-026: Create Project Component

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-026 |
| **Use Case Name** | Create Project Component |
| **Priority** | High |
| **Complexity** | Simple |

**Description**: Create a component within a project for categorizing work items (e.g., "Backend", "Frontend", "Database").

**Permission**: `PM.COMPONENT.CREATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends POST `/api/v1/projects/{projectId}/components` with component data |
| 2 | System | Validates JWT, permissions |
| 3 | System | Validates project exists and is not archived |
| 4 | System | Validates component name is unique within project |
| 5 | System | If `lead_user_id` provided, validates user exists |
| 6 | System | Persists component, publishes `COMPONENT_CREATED` to `serp.pm.component.events` |
| 7 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255, unique per project | Component name |
| description | string | No | max:2000 | Description |
| lead_user_id | int64 | No | must exist | Component lead |
| assignee_type | string | No | PROJECT_DEFAULT, COMPONENT_LEAD, PROJECT_LEAD, UNASSIGNED; default: PROJECT_DEFAULT | Auto-assign rule |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-026-01 | Component name unique within project | DB `UNIQUE(tenant_id, project_id, name)` |
| BR-PM-026-02 | Cannot add components to archived projects | Service layer |

##### UC-PM-027 to UC-PM-030: Component Update, Get, List, Delete

Standard project-scoped CRUD. Permission: `PM.COMPONENT.MANAGE` / `PM.COMPONENT.READ`.

- **UC-PM-027**: Update name, description, lead, assignee_type.
- **UC-PM-028**: Get by ID.
- **UC-PM-029**: List components for a project.
- **UC-PM-030**: Soft-delete. Dissociates from work items but preserves history.

---

#### UC-PM-031 to UC-PM-037: Project Version Management

##### UC-PM-031: Create Project Version

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-031 |
| **Use Case Name** | Create Project Version |
| **Priority** | High |
| **Complexity** | Simple |

**Description**: Create a version (release) for a project to track fix targets and release planning.

**Permission**: `PM.VERSION.CREATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends POST `/api/v1/projects/{projectId}/versions` with version data |
| 2 | System | Validates JWT, permissions |
| 3 | System | Validates project exists and is not archived |
| 4 | System | Validates version name is unique within project |
| 5 | System | Sets `released=false`, `archived=false`, calculates next `sequence` |
| 6 | System | Persists version, publishes `VERSION_CREATED` to `serp.pm.version.events` |
| 7 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255, unique per project | Version name |
| description | string | No | max:2000 | Description |
| start_date | date | No | valid date | Planned start |
| release_date | date | No | valid date, >= start_date | Planned release |

##### UC-PM-032 to UC-PM-035: Version Update, Get, List, Delete

Standard project-scoped CRUD. Permission: `PM.VERSION.MANAGE` / `PM.VERSION.READ`.

##### UC-PM-036: Release Project Version

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-036 |
| **Use Case Name** | Release Project Version |
| **Priority** | High |
| **Complexity** | Medium |

**Description**: Mark a version as released. Optionally move unresolved issues to a target version.

**Permission**: `PM.VERSION.MANAGE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends POST `/api/v1/projects/{projectId}/versions/{versionId}/release` |
| 2 | System | Validates JWT, permissions |
| 3 | System | Fetches version, validates it exists and is not already released |
| 4 | System | Sets `released=true`, `release_date` if not already set |
| 5 | System | If `move_unresolved_to_version_id` provided, moves unresolved work item fix versions |
| 6 | System | Publishes `VERSION_RELEASED` event |
| 7 | System | Returns HTTP 200 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| release_date | date | No | valid date | Override release date |
| move_unresolved_to_version_id | int64 | No | must exist, unreleased | Target for unresolved items |

##### UC-PM-037: Archive Project Version

**Permission**: `PM.VERSION.MANAGE`

Simple toggle: set `archived=true` or `archived=false`. Publish `VERSION_ARCHIVED` / `VERSION_UNARCHIVED` event.

---

#### UC-PM-041 to UC-PM-048: Project Role Management

##### UC-PM-041: Create Project Role

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-041 |
| **Use Case Name** | Create Project Role |
| **Priority** | Medium |
| **Complexity** | Simple |

**Description**: Create a new project role (e.g., "Developer", "QA Lead", "Scrum Master"). Roles are global per tenant and can be assigned per project.

**Permission**: `PM.ROLE.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255, unique per tenant | Role name |
| description | string | No | max:2000 | Description |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-041-01 | Role name unique per tenant | DB `UNIQUE(tenant_id, name)` |
| BR-PM-041-02 | `is_system=false` for API-created roles | Service layer |

##### UC-PM-042 to UC-PM-045: Role Update, Get, List, Delete

Standard CRUD. Cannot delete system roles. Cannot delete if role is used in permission schemes.

##### UC-PM-046: Add Project Role Actor

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-046 |
| **Use Case Name** | Add Project Role Actor |
| **Priority** | High |
| **Complexity** | Simple |

**Description**: Assign a user, group, or service account to a project role within a specific project.

**Permission**: `PM.ROLE.ASSIGN`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends POST `/api/v1/projects/{projectId}/roles/{roleId}/actors` |
| 2 | System | Validates JWT, permissions |
| 3 | System | Validates project and role exist |
| 4 | System | Validates subject (user/group) exists via Account Service |
| 5 | System | Checks for duplicate assignment |
| 6 | System | Persists actor assignment |
| 7 | System | Publishes `ROLE_ACTOR_ADDED` event |
| 8 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| subject_type | string | Yes | USER, GROUP, SERVICE_ACCOUNT | Actor type |
| subject_id | string | Yes | must be valid for type | Actor identifier |

##### UC-PM-047: Remove Project Role Actor

**Permission**: `PM.ROLE.ASSIGN`

Soft-delete the actor assignment. Publish `ROLE_ACTOR_REMOVED` event.

##### UC-PM-048: List Project Role Actors

**Permission**: `PM.ROLE.READ`

GET `/api/v1/projects/{projectId}/roles/{roleId}/actors` -> return list of actors for a role in a project. Optionally list all roles with actors for a project.

---

### 5.2. Issues & Work Items (Module 02)

---

#### UC-PM-101: Create Work Item

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-101 |
| **Use Case Name** | Create Work Item |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Complex |

##### Description

Create a new work item (issue) within a project. The work item must conform to the project's issue type scheme (valid issue type), and is initialized with the workflow's initial status. Custom field values are validated against the project's field configuration and screen scheme.

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Creates work item |
| System | System | Resolves workflow initial status, generates issue key |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.WORK_ITEM.CREATE`
3. Project exists, is not archived, and is not deleted
4. Issue type belongs to the project's issue type scheme

##### Postconditions

###### Success Postconditions
1. Work item persisted with auto-generated `key` (`{PROJECT_KEY}-{issue_no}`) and initial status from workflow
2. Custom field values persisted in `work_item_custom_field_values`
3. Kafka event `WORK_ITEM_CREATED` published to topic `serp.pm.workitem.events`
4. `time_spent=0`, `resolution_id=NULL`

###### Failure Postconditions
1. No data changes committed (transaction rolled back)
2. Error response returned

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends POST `/api/v1/projects/{projectId}/work-items` with work item data |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Checks user has `PM.WORK_ITEM.CREATE` permission |
| 4 | System | Validates project exists, is not archived/deleted |
| 5 | System | Validates `issue_type_id` is in the project's issue type scheme |
| 6 | System | Resolves the workflow for this issue type via workflow scheme |
| 7 | System | Determines initial status from workflow (step where `is_initial=true`) |
| 8 | System | Validates `priority_id` is in the project's priority scheme |
| 9 | System | If `assignee_id` provided, validates user exists |
| 10 | System | If `parent_id` provided, validates parent work item exists in same project and hierarchy is valid |
| 11 | System | Validates required fields per field configuration for CREATE screen |
| 12 | System | Begins transaction |
| 13 | System | Generates next `issue_no` for project, computes `key = PROJECT_KEY + "-" + issue_no` |
| 14 | System | Creates Work Item entity with initial status, `reporter_id=userId`, generates Lexorank for `rank` |
| 15 | System | If custom field values provided, persists to `work_item_custom_field_values` |
| 16 | System | Commits transaction |
| 17 | System | Publishes `WORK_ITEM_CREATED` event to `serp.pm.workitem.events` |
| 18 | System | Returns HTTP 201 with created work item |

##### Alternative Flows

###### AF-1: Subtask Creation

**Branches from**: Main Flow Step 10
**Condition**: `parent_id` is provided and issue type has `hierarchy_level=0` (subtask)

| Step | Actor/System | Action |
|------|-------------|--------|
| 10.1 | System | Validates parent exists in same project |
| 10.2 | System | Validates parent issue type has `hierarchy_level >= 1` (standard or epic) |
| 10.3 | System | Sets `parent_id` on work item |

**Rejoins**: Main Flow Step 11

###### AF-2: Epic Child Creation

**Branches from**: Main Flow Step 10
**Condition**: `parent_id` is provided and parent is an epic (`hierarchy_level=2`)

| Step | Actor/System | Action |
|------|-------------|--------|
| 10.1 | System | Validates parent is an epic |
| 10.2 | System | Validates child issue type has `hierarchy_level=1` (standard) |

**Rejoins**: Main Flow Step 11

##### Exception Flows

###### EF-1: Invalid Issue Type for Project

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 400 with error: `ISSUE_TYPE_NOT_IN_SCHEME` |

###### EF-2: Invalid Hierarchy

**Triggered at**: Main Flow Step 10

| Step | Actor/System | Action |
|------|-------------|--------|
| 10.E1 | System | Returns HTTP 400 with error: `INVALID_PARENT_HIERARCHY` with details |

###### EF-3: Required Field Missing

**Triggered at**: Main Flow Step 11

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.E1 | System | Returns HTTP 400 with list of missing required fields |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-101-01 | Work item key format: `{PROJECT_KEY}-{issue_no}`, auto-generated, immutable | Service layer |
| BR-PM-101-02 | Issue type must belong to the project's issue type scheme | UseCase layer |
| BR-PM-101-03 | Initial status determined by the workflow's `is_initial=true` step | UseCase layer |
| BR-PM-101-04 | Subtask hierarchy: subtask (level 0) -> standard (level 1) -> epic (level 2) | Service layer |
| BR-PM-101-05 | `issue_no` is auto-incremented per project (sequence) | DB sequence or app-level lock |
| BR-PM-101-06 | Reporter defaults to the authenticated user | Service layer |
| BR-PM-101-07 | `summary` is always required regardless of field configuration | DTO validation |
| BR-PM-101-08 | Priority defaults to the priority scheme's `default_priority_id` if not provided | Service layer |
| BR-PM-101-09 | Cannot create work items in archived projects | Service layer |

##### Data Requirements

###### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| summary | string | Yes | min:1, max:512 | Work item title |
| description | string | No | max:50000 | Markdown/JSON description |
| issue_type_id | int64 | Yes | must be in project's scheme | Issue type |
| priority_id | int64 | No | must be in project's priority scheme, defaults to scheme default | Priority |
| assignee_id | int64 | No | must exist | Assignee user |
| parent_id | int64 | No | must exist in same project, valid hierarchy | Parent work item |
| due_date | timestamp | No | valid date | Due date |
| time_original_estimate | int64 | No | min:0 | Original estimate in seconds |
| security_level_id | int64 | No | must be in project's security scheme | Security level |
| custom_fields | map | No | validated per field config | Custom field values |

###### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated work item ID |
| key | string | Human key (e.g., SERP-123) |
| summary | string | Title |
| description | string | Description |
| issue_type | object | Issue type details |
| status | object | Initial status |
| priority | object | Priority details |
| assignee_id | int64 | Assignee (nullable) |
| reporter_id | int64 | Reporter (creator) |
| parent_id | int64 | Parent (nullable) |
| rank | string | Lexorank value |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |

---

#### UC-PM-102: Update Work Item

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-102 |
| **Use Case Name** | Update Work Item |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Medium |

##### Description

Update work item fields (summary, description, priority, assignee, due date, estimates, custom fields). Status transitions use a separate use case (UC-PM-106).

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Updates work item fields |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.WORK_ITEM.UPDATE`
3. Work item exists and is not deleted
4. Project is not archived

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends PUT `/api/v1/work-items/{workItemId}` with updated fields |
| 2 | System | Validates JWT and permissions |
| 3 | System | Fetches work item, validates it exists and belongs to tenant |
| 4 | System | Validates project is not archived |
| 5 | System | Validates editable fields per field configuration for EDIT screen |
| 6 | System | If `issue_type_id` changed, validates new type is in project's scheme and re-evaluates workflow |
| 7 | System | If `assignee_id` changed, validates user exists |
| 8 | System | Updates work item, sets `updated_by=userId` |
| 9 | System | Records field changes in `change_groups` / `change_items` (Module 09, deferred) |
| 10 | System | Commits transaction |
| 11 | System | Publishes `WORK_ITEM_UPDATED` event to `serp.pm.workitem.events` |
| 12 | System | Returns HTTP 200 with updated work item |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-102-01 | `key` and `issue_no` are immutable | Service layer |
| BR-PM-102-02 | `status_id` cannot be changed via update; must use transition (UC-PM-106) | Service layer |
| BR-PM-102-03 | Cannot update work items in archived projects | Service layer |
| BR-PM-102-04 | Hidden fields (per field configuration) cannot be updated | Service layer |

---

#### UC-PM-103: Get Work Item by ID

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-103 |
| **Use Case Name** | Get Work Item by ID |
| **Priority** | High |
| **Complexity** | Simple |

**Permission**: `PM.WORK_ITEM.READ`

**Main Flow**: GET `/api/v1/work-items/{workItemId}` or GET `/api/v1/work-items/key/{key}` -> validate JWT -> fetch with tenant_id + deleted_at IS NULL -> enrich with issue type, status, priority, assignee details -> check issue security level access -> return 200.

**Alternative Flow**: Support lookup by `key` (e.g., `SERP-123`) in addition to numeric ID.

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-103-01 | If work item has a security_level_id, user must be a member of that security level | UseCase layer |

---

#### UC-PM-104: List Work Items with Filters

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-104 |
| **Use Case Name** | List Work Items with Filters |
| **Priority** | High |
| **Complexity** | Medium |

**Permission**: `PM.WORK_ITEM.READ`

**Main Flow**: GET `/api/v1/work-items` with query parameters -> validate JWT -> build query with tenant_id -> apply filters -> paginate -> return 200.

**Input Data (Query Parameters)**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| page | int | No | min:0, default:0 | Page number |
| pageSize | int | No | min:1, max:100, default:10 | Items per page |
| project_id | int64 | No | must exist | Filter by project |
| issue_type_id | int64 | No | must exist | Filter by type |
| status_id | int64 | No | must exist | Filter by status |
| status_category | string | No | new, indeterminate, done | Filter by category |
| priority_id | int64 | No | must exist | Filter by priority |
| assignee_id | int64 | No | valid user | Filter by assignee |
| reporter_id | int64 | No | valid user | Filter by reporter |
| parent_id | int64 | No | valid work item | Filter by parent |
| resolution_id | int64 | No | valid resolution | Filter by resolution |
| search | string | No | min:1 | Search in summary and key |
| due_date_from | timestamp | No | valid date | Due date range start |
| due_date_to | timestamp | No | valid date | Due date range end |
| created_from | timestamp | No | valid date | Created range start |
| created_to | timestamp | No | valid date | Created range end |
| sortBy | string | No | key, summary, status, priority, assignee, created_at, updated_at, rank, due_date | Sort field |
| sortOrder | string | No | ASC, DESC | Sort direction |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-104-01 | Results filtered by tenantId and deleted_at IS NULL | Repository layer |
| BR-PM-104-02 | Work items with security levels are filtered based on user's security level access | UseCase layer |
| BR-PM-104-03 | Default sort: rank ASC (board order) | Repository layer |
| BR-PM-104-04 | Default pagination: page=0, pageSize=10, max pageSize=100 | Controller layer |

---

#### UC-PM-105: Delete Work Item (Soft Delete)

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-105 |
| **Use Case Name** | Delete Work Item |
| **Priority** | Medium |
| **Complexity** | Medium |

**Permission**: `PM.WORK_ITEM.DELETE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends DELETE `/api/v1/work-items/{workItemId}` |
| 2 | System | Validates JWT and permissions |
| 3 | System | Fetches work item, validates it exists |
| 4 | System | Sets `deleted_at=NOW()` on work item |
| 5 | System | Cascades soft-delete to subtasks (children with `parent_id = workItemId`) |
| 6 | System | Removes issue links referencing this work item |
| 7 | System | Publishes `WORK_ITEM_DELETED` event |
| 8 | System | Returns HTTP 200 |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-105-01 | Deleting a parent cascades soft-delete to all subtasks | Service layer |
| BR-PM-105-02 | Issue links pointing to/from deleted items are also soft-deleted | Service layer |

---

#### UC-PM-106: Transition Work Item Status

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-106 |
| **Use Case Name** | Transition Work Item Status |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Complex |

##### Description

Execute a workflow transition to change a work item's status. The system validates the transition is allowed by the workflow, evaluates conditions and validators, executes the transition, and runs post-functions.

##### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Initiates status transition |
| System | System | Evaluates workflow rules |

##### Preconditions

1. User is authenticated with valid JWT token
2. User has permission `PM.WORK_ITEM.TRANSITION`
3. Work item exists and is not deleted
4. Project is not archived

##### Postconditions

###### Success Postconditions
1. Work item `status_id` updated to target status
2. If transition is to a "done" category status, `resolution_id` is set
3. Transition rules (post-functions) executed
4. Kafka event `WORK_ITEM_STATUS_CHANGED` published to `serp.pm.workitem.events`
5. Change recorded in history (change_groups/change_items)

###### Failure Postconditions
1. Status unchanged, transaction rolled back
2. Error response with reason

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends POST `/api/v1/work-items/{workItemId}/transitions` with `{ transition_id, resolution_id?, fields? }` |
| 2 | System | Validates JWT and permissions |
| 3 | System | Fetches work item, validates it exists |
| 4 | System | Resolves the applicable workflow for this work item (via project's workflow scheme + issue type) |
| 5 | System | Validates `transition_id` exists in workflow and is valid from current status |
| 6 | System | Evaluates CONDITION rules on the transition (e.g., `user_is_assignee`) |
| 7 | System | Evaluates VALIDATOR rules (e.g., `field_required` for resolution) |
| 8 | System | If transition has an associated screen, validates provided field values |
| 9 | System | Begins transaction |
| 10 | System | Updates `status_id` to the transition's `to_status_id` |
| 11 | System | If resolution provided, sets `resolution_id` |
| 12 | System | Executes POST_FUNCTION rules (e.g., `fire_event`, `update_field`) |
| 13 | System | Records status change in change history |
| 14 | System | Commits transaction |
| 15 | System | Publishes `WORK_ITEM_STATUS_CHANGED` event to Kafka |
| 16 | System | Returns HTTP 200 with updated work item |

##### Alternative Flows

###### AF-1: Global Transition

**Branches from**: Main Flow Step 5
**Condition**: Transition has `from_status_id=NULL` (global, available from any status)

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Transition is valid regardless of current status |

**Rejoins**: Main Flow Step 6

##### Exception Flows

###### EF-1: Invalid Transition

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 400 with error: `INVALID_TRANSITION` and list of available transitions |

###### EF-2: Condition Not Met

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 403 with error: `TRANSITION_CONDITION_FAILED` and condition details |

###### EF-3: Validator Failed

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 400 with error: `TRANSITION_VALIDATION_FAILED` and validator details |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-106-01 | Only transitions defined in the workflow from current status are allowed | UseCase layer |
| BR-PM-106-02 | Global transitions (from_status_id=NULL) are available from any status | UseCase layer |
| BR-PM-106-03 | CONDITIONS are evaluated before VALIDATORS; if condition fails, transition is rejected | UseCase layer |
| BR-PM-106-04 | Transition to "done" category status requires resolution_id to be set | Service layer |
| BR-PM-106-05 | POST_FUNCTION rules execute within the same transaction | UseCase layer |
| BR-PM-106-06 | Kafka event published AFTER transaction commit | UseCase layer |

##### Data Requirements

###### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| transition_id | int64 | Yes | must be valid for current status | Workflow transition |
| resolution_id | int64 | No | required if target is "done" category | Resolution |
| fields | map | No | per transition screen requirements | Screen field values |

###### Output Data

| Field | Type | Description |
|-------|------|-------------|
| work_item | object | Updated work item with new status |
| transition | object | Executed transition details |

---

#### UC-PM-107: Assign Work Item

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-107 |
| **Use Case Name** | Assign Work Item |
| **Priority** | High |
| **Complexity** | Simple |

**Permission**: `PM.WORK_ITEM.ASSIGN`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends PUT `/api/v1/work-items/{workItemId}/assign` with `{ assignee_id }` |
| 2 | System | Validates JWT and permissions |
| 3 | System | Fetches work item, validates it exists |
| 4 | System | Validates `assignee_id` exists (null to unassign) |
| 5 | System | Updates `assignee_id` |
| 6 | System | Publishes `WORK_ITEM_ASSIGNED` event |
| 7 | System | Returns HTTP 200 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| assignee_id | int64 | No | must exist or null to unassign | New assignee |

---

#### UC-PM-108: Re-rank Work Item (Lexorank)

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-108 |
| **Use Case Name** | Re-rank Work Item |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Update the rank of a work item for ordering in boards and backlogs using Lexorank algorithm.

**Permission**: `PM.WORK_ITEM.UPDATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends PUT `/api/v1/work-items/{workItemId}/rank` with `{ rank_before_id, rank_after_id }` |
| 2 | System | Validates JWT and permissions |
| 3 | System | Computes new Lexorank value between `rank_before` and `rank_after` items |
| 4 | System | If Lexorank space exhausted, triggers rebalancing |
| 5 | System | Updates `rank` field |
| 6 | System | Publishes `WORK_ITEM_RANKED` event |
| 7 | System | Returns HTTP 200 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| rank_before_id | int64 | No | valid work item or null (top) | Item to rank after |
| rank_after_id | int64 | No | valid work item or null (bottom) | Item to rank before |

---

#### UC-PM-109: Bulk Update Work Items

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-109 |
| **Use Case Name** | Bulk Update Work Items |
| **Priority** | Medium |
| **Complexity** | Complex |

**Description**: Update multiple work items at once with the same field values (e.g., bulk assign, bulk change priority).

**Permission**: `PM.WORK_ITEM.BULK_UPDATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends POST `/api/v1/work-items/bulk-update` with `{ work_item_ids[], fields }` |
| 2 | System | Validates JWT and permissions |
| 3 | System | Validates all work item IDs exist and belong to tenant |
| 4 | System | Validates field values |
| 5 | System | Updates each work item within a single transaction |
| 6 | System | Publishes `WORK_ITEM_BULK_UPDATED` event with list of affected IDs |
| 7 | System | Returns HTTP 200 with success/failure count |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-109-01 | Maximum 100 work items per bulk operation | Controller layer |
| BR-PM-109-02 | All updates in single transaction; partial failure rolls back all | UseCase layer |
| BR-PM-109-03 | Cannot bulk-update `status_id` (use individual transitions) | Service layer |

---

#### UC-PM-110: Clone Work Item

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-110 |
| **Use Case Name** | Clone Work Item |
| **Priority** | Medium |
| **Complexity** | Medium |

**Permission**: `PM.WORK_ITEM.CREATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends POST `/api/v1/work-items/{workItemId}/clone` with `{ include_subtasks?, include_links?, include_attachments? }` |
| 2 | System | Fetches source work item |
| 3 | System | Creates new work item with copied fields, initial status, new key |
| 4 | System | If `include_subtasks`, clones subtasks recursively |
| 5 | System | If `include_links`, creates "clones" links to original |
| 6 | System | Creates issue link: cloned -> original (link type: "Clones") |
| 7 | System | Publishes `WORK_ITEM_CREATED` events for each cloned item |
| 8 | System | Returns HTTP 201 with cloned work item |

---

#### UC-PM-111: Manage Work Item Components

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-111 |
| **Use Case Name** | Manage Work Item Components |
| **Priority** | Medium |
| **Complexity** | Simple |

**Description**: Add or remove component associations for a work item.

**Permission**: `PM.WORK_ITEM.UPDATE`

**Main Flow (Add)**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends POST `/api/v1/work-items/{workItemId}/components` with `{ component_ids[] }` |
| 2 | System | Validates components exist in the same project |
| 3 | System | Creates `work_item_components` entries (ignoring duplicates) |
| 4 | System | Publishes `WORK_ITEM_UPDATED` event |
| 5 | System | Returns HTTP 200 |

**Main Flow (Remove)**: DELETE `/api/v1/work-items/{workItemId}/components/{componentId}` -> soft-delete entry.

**Main Flow (List)**: GET `/api/v1/work-items/{workItemId}/components` -> return list.

---

#### UC-PM-116: Manage Work Item Fix Versions

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-116 |
| **Use Case Name** | Manage Work Item Fix Versions |
| **Priority** | Medium |
| **Complexity** | Simple |

**Description**: Add or remove version associations (FIX or AFFECTS) for a work item.

**Permission**: `PM.WORK_ITEM.UPDATE`

**Input Data (Add)**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| version_id | int64 | Yes | must exist in same project | Version |
| relation_type | string | Yes | FIX, AFFECTS | Relationship type |

---

#### UC-PM-121: Move Work Item to Sprint

##### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-121 |
| **Use Case Name** | Move Work Item to Sprint |
| **Priority** | High |
| **Complexity** | Medium |

**Permission**: `PM.WORK_ITEM.UPDATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends POST `/api/v1/work-items/{workItemId}/sprints` with `{ sprint_id }` |
| 2 | System | Validates sprint exists and belongs to same project's board |
| 3 | System | If work item already in an active sprint, marks old entry `is_active=false`, sets `removed_at=NOW()` |
| 4 | System | Creates new `work_item_sprints` entry with `is_active=true`, `added_at=NOW()` |
| 5 | System | Publishes `WORK_ITEM_SPRINT_CHANGED` event |
| 6 | System | Returns HTTP 200 |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-121-01 | Work item can only be actively in one sprint at a time | Service layer |
| BR-PM-121-02 | Sprint history is preserved (old entries remain with `removed_at` set) | Service layer |
| BR-PM-121-03 | Cannot move to a CLOSED sprint | Service layer |

---

#### UC-PM-122: Remove Work Item from Sprint

**Permission**: `PM.WORK_ITEM.UPDATE`

Set `is_active=false` and `removed_at=NOW()` on current sprint entry. Publish `WORK_ITEM_SPRINT_CHANGED` event.

---

#### UC-PM-131 to UC-PM-135: Issue Type CRUD

##### UC-PM-131: Create Issue Type

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-131 |
| **Use Case Name** | Create Issue Type |
| **Priority** | High |
| **Complexity** | Simple |

**Permission**: `PM.ISSUE_TYPE.CREATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/issue-types` with issue type data |
| 2 | System | Validates JWT, permissions, input |
| 3 | System | Validates `type_key` is unique within tenant |
| 4 | System | Persists issue type with `is_system=false` |
| 5 | System | Publishes `ISSUE_TYPE_CREATED` to `serp.pm.issuetype.events` |
| 6 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| type_key | string | Yes | min:1, max:100, unique per tenant | Stable key |
| name | string | Yes | min:1, max:255 | Display name |
| description | string | No | max:2000 | Description |
| icon_url | string | No | valid URL | Icon |
| hierarchy_level | int | Yes | 0=subtask, 1=standard, 2=epic | Hierarchy level |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-131-01 | `type_key` is unique per tenant and immutable after creation | DB + Service |
| BR-PM-131-02 | System issue types cannot be created via API | Service layer |

##### UC-PM-132 to UC-PM-135: Issue Type Update, Get, List, Delete

Standard CRUD. System issue types (`is_system=true`) cannot be deleted or have their `type_key` changed.

---

#### UC-PM-136 to UC-PM-141: Issue Type Scheme Management

##### UC-PM-136: Create Issue Type Scheme

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-136 |
| **Use Case Name** | Create Issue Type Scheme |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.ISSUE_TYPE_SCHEME.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Scheme name |
| description | string | No | max:2000 | Description |
| default_issue_type_id | int64 | Yes | must exist | Default issue type |

##### UC-PM-137 to UC-PM-140: Scheme Update, Get, List, Delete

Standard CRUD.

##### UC-PM-141: Manage Issue Type Scheme Items

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-141 |
| **Use Case Name** | Manage Issue Type Scheme Items |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Add, remove, or reorder issue types within a scheme.

**Permission**: `PM.ISSUE_TYPE_SCHEME.MANAGE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends PUT `/api/v1/issue-type-schemes/{schemeId}/items` with ordered list of issue type IDs |
| 2 | System | Validates all issue types exist |
| 3 | System | Validates default issue type is in the list |
| 4 | System | Replaces all scheme items within transaction (delete old, insert new with sequence) |
| 5 | System | Returns HTTP 200 |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-141-01 | Scheme's `default_issue_type_id` must always be in the items list | UseCase layer |
| BR-PM-141-02 | Cannot remove an issue type from scheme if projects using this scheme have work items of that type | UseCase layer (warning/flag) |

---

#### UC-PM-146 to UC-PM-156: Priority & Priority Scheme Management

##### UC-PM-146: Create Priority

**Permission**: `PM.PRIORITY.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:50 | Priority label |
| description | string | No | max:2000 | Description |
| icon_url | string | No | valid URL | Icon |
| color | string | No | hex color | Display color |
| sequence | int | Yes | min:0 | Display order |

##### UC-PM-147 to UC-PM-150: Priority Update, Get, List, Delete

Standard CRUD. System priorities cannot be deleted.

##### UC-PM-151 to UC-PM-155: Priority Scheme CRUD

Same pattern as Issue Type Scheme. Includes `default_priority_id`.

##### UC-PM-156: Manage Priority Scheme Items

Same pattern as UC-PM-141. Manages ordered list of priorities within a scheme.

---

#### UC-PM-161 to UC-PM-165: Resolution CRUD

##### UC-PM-161: Create Resolution

**Permission**: `PM.RESOLUTION.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:50 | Resolution label (e.g., "Done", "Won't Fix", "Duplicate") |
| description | string | No | max:2000 | Description |
| sequence | int | Yes | min:0 | Display order |

##### UC-PM-162 to UC-PM-165: Resolution Update, Get, List, Delete

Standard CRUD. System resolutions cannot be deleted.

---

#### UC-PM-171 to UC-PM-178: Issue Link Management

##### UC-PM-171: Create Issue Link Type

**Permission**: `PM.ISSUE_LINK_TYPE.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:100 | Link type name (Blocks, Clones, Relates) |
| outward_desc | string | Yes | min:1, max:100 | Outward description (e.g., "blocks") |
| inward_desc | string | Yes | min:1, max:100 | Inward description (e.g., "is blocked by") |

##### UC-PM-172 to UC-PM-175: Link Type Update, Get, List, Delete

Standard CRUD. System link types cannot be deleted.

##### UC-PM-176: Create Issue Link

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-176 |
| **Use Case Name** | Create Issue Link |
| **Priority** | High |
| **Complexity** | Simple |

**Permission**: `PM.ISSUE_LINK.CREATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends POST `/api/v1/issue-links` with `{ source_id, target_id, link_type_id }` |
| 2 | System | Validates both work items exist and belong to tenant |
| 3 | System | Validates link type exists |
| 4 | System | Checks for duplicate link (same source, target, type) |
| 5 | System | Validates no self-link (source_id != target_id) |
| 6 | System | Persists link |
| 7 | System | Publishes `ISSUE_LINK_CREATED` to `serp.pm.issuelink.events` |
| 8 | System | Returns HTTP 201 |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-176-01 | Cannot link an item to itself | Service layer |
| BR-PM-176-02 | Duplicate links (same source, target, type) are rejected | Service + DB |
| BR-PM-176-03 | Work items can be from different projects (cross-project links allowed) | Service layer |

##### UC-PM-177: Delete Issue Link

Soft-delete. Publish `ISSUE_LINK_DELETED` event.

##### UC-PM-178: List Issue Links for Work Item

GET `/api/v1/work-items/{workItemId}/links` -> return both inward and outward links with resolved work item summaries.

---

#### UC-PM-181 to UC-PM-185: Worklog Management

##### UC-PM-181: Create Worklog

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-181 |
| **Use Case Name** | Create Worklog |
| **Priority** | High |
| **Complexity** | Simple |

**Permission**: `PM.WORKLOG.CREATE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends POST `/api/v1/work-items/{workItemId}/worklogs` with worklog data |
| 2 | System | Validates JWT and permissions |
| 3 | System | Validates work item exists |
| 4 | System | Persists worklog with `author_id=userId` |
| 5 | System | Updates `time_spent` on work item (increment) and recalculates `time_remaining_estimate` |
| 6 | System | Publishes `WORKLOG_CREATED` to `serp.pm.worklog.events` |
| 7 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| time_spent | int64 | Yes | min:60 (at least 1 minute in seconds) | Time logged in seconds |
| start_date | timestamp | Yes | not in future | When work started |
| comment | string | No | max:5000 | Work description |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-181-01 | Logging work updates the work item's `time_spent` (sum of all worklogs) | Service layer |
| BR-PM-181-02 | `time_remaining_estimate` is reduced by logged time (floor at 0) | Service layer |

##### UC-PM-182: Update Worklog

**Permission**: `PM.WORKLOG.MANAGE` (own worklogs) or `PM.WORKLOG.MANAGE_ALL`

Can update `time_spent`, `start_date`, `comment`. Recalculates work item `time_spent` totals.

##### UC-PM-183: Delete Worklog

Soft-delete. Recalculates work item `time_spent`. Only author or admin.

##### UC-PM-184: List Worklogs for Work Item

GET `/api/v1/work-items/{workItemId}/worklogs` with pagination.

##### UC-PM-185: Get Worklog by ID

GET `/api/v1/worklogs/{worklogId}`.

---

### 5.3. Workflow Engine (Module 03)

---

#### UC-PM-201 to UC-PM-205: Status Category CRUD

##### UC-PM-201: Create Status Category

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-201 |
| **Use Case Name** | Create Status Category |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.STATUS_CATEGORY.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:50 | Category name (To Do, In Progress, Done) |
| key | string | Yes | min:1, max:50, unique per tenant | Stable key (new, indeterminate, done) |
| color_name | string | No | max:50 | Display color name |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-201-01 | System status categories cannot be created via API | Service layer |
| BR-PM-201-02 | Key must be unique per tenant | DB `UNIQUE(tenant_id, key)` |

##### UC-PM-202 to UC-PM-205: Status Category Update, Get, List, Delete

Standard CRUD. System categories cannot be deleted.

---

#### UC-PM-211 to UC-PM-215: Status CRUD

##### UC-PM-211: Create Status

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-211 |
| **Use Case Name** | Create Status |
| **Priority** | High |
| **Complexity** | Simple |

**Permission**: `PM.STATUS.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| status_key | string | Yes | min:1, max:100, unique per tenant | Stable key |
| name | string | Yes | min:1, max:255 | Display name |
| description | string | No | max:2000 | Description |
| icon_url | string | No | valid URL | Icon |
| status_category_id | int64 | Yes | must exist | Parent category |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-211-01 | `status_key` unique per tenant | DB `UNIQUE(tenant_id, status_key)` |
| BR-PM-211-02 | Every status must belong to a status category | DTO validation |

##### UC-PM-212 to UC-PM-214: Status Update, Get, List

Standard CRUD. System statuses cannot have their `status_key` changed.

##### UC-PM-215: Delete Status

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-215-01 | Cannot delete a status that is used in any workflow step | Service layer |
| BR-PM-215-02 | Cannot delete a status if any work item currently has this status | Service layer |

---

#### UC-PM-221 to UC-PM-228: Workflow Management

##### UC-PM-221: Create Workflow

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-221 |
| **Use Case Name** | Create Workflow |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Medium |

##### Description

Create a new workflow definition with initial steps and transitions. Workflows define the allowed status transitions for work items.

**Permission**: `PM.WORKFLOW.CREATE`

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/workflows` with workflow data |
| 2 | System | Validates JWT, permissions, input |
| 3 | System | Creates workflow with `version_no=1`, `is_active=false` (draft) |
| 4 | System | Publishes `WORKFLOW_CREATED` to `serp.pm.workflow.events` |
| 5 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Workflow name |
| description | string | No | max:2000 | Description |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-221-01 | New workflows start as drafts (`is_active=false`, `version_no=1`) | Service layer |
| BR-PM-221-02 | Steps and transitions are added via separate use cases (UC-PM-231, UC-PM-236) | API design |

##### UC-PM-222: Update Workflow

Update metadata (name, description). Cannot update while active unless creating a new draft version.

##### UC-PM-223: Get Workflow by ID

Returns workflow with all steps, transitions, and rules.

##### UC-PM-224: List Workflows

List with optional filters: `is_active`, `is_system`, search by name.

##### UC-PM-225: Delete Workflow

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-225-01 | Cannot delete a workflow used in any workflow scheme | Service layer |
| BR-PM-225-02 | Cannot delete system workflows | Service layer |

##### UC-PM-226: Publish Workflow

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-226 |
| **Use Case Name** | Publish Workflow |
| **Priority** | High |
| **Complexity** | Complex |

**Description**: Validate and activate a draft workflow. Increments `version_no` and sets `is_active=true`.

**Permission**: `PM.WORKFLOW.MANAGE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/workflows/{workflowId}/publish` |
| 2 | System | Validates JWT, permissions |
| 3 | System | Validates workflow (see UC-PM-228 validation rules) |
| 4 | System | Deactivates current active version if exists (`is_active=false`) |
| 5 | System | Increments `version_no`, sets `is_active=true` |
| 6 | System | Publishes `WORKFLOW_PUBLISHED` event |
| 7 | System | Returns HTTP 200 |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-226-01 | Only one active version per workflow at a time | Service layer |
| BR-PM-226-02 | Workflow must pass validation before publishing | UseCase layer |

##### UC-PM-227: Clone Workflow

Create a copy of an existing workflow (all steps, transitions, rules) with a new name. Useful for creating variants.

##### UC-PM-228: Validate Workflow

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-228 |
| **Use Case Name** | Validate Workflow |
| **Priority** | High |
| **Complexity** | Complex |

**Description**: Validate workflow integrity without publishing.

**Permission**: `PM.WORKFLOW.MANAGE`

**Validation Rules** (returned as list of errors/warnings):

| Rule | Severity | Description |
|------|----------|-------------|
| V-001 | Error | Must have exactly one initial step (`is_initial=true`) |
| V-002 | Error | Must have at least one final step (`is_final=true`) |
| V-003 | Error | All non-initial steps must be reachable from the initial step via transitions |
| V-004 | Warning | Orphan steps with no incoming or outgoing transitions |
| V-005 | Error | All statuses referenced in steps must exist and not be deleted |
| V-006 | Warning | Transitions to the same status (self-loop) |

---

#### UC-PM-231 to UC-PM-233: Workflow Step Management

##### UC-PM-231: Add Workflow Step

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-231 |
| **Use Case Name** | Add Workflow Step |
| **Priority** | High |
| **Complexity** | Simple |

**Permission**: `PM.WORKFLOW.MANAGE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/workflows/{workflowId}/steps` with `{ status_id, is_initial, is_final }` |
| 2 | System | Validates workflow is not active (must be draft to edit) |
| 3 | System | Validates status exists |
| 4 | System | Validates status is not already a step in this workflow |
| 5 | System | If `is_initial=true`, ensures no other initial step exists |
| 6 | System | Persists step with next `step_order` |
| 7 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| status_id | int64 | Yes | must exist, unique per workflow | Status |
| is_initial | bool | No | default: false; only one per workflow | Initial step marker |
| is_final | bool | No | default: false | Terminal step marker |
| step_order | int | No | auto-calculated if omitted | Display order |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-231-01 | Can only modify draft workflows (`is_active=false`) | Service layer |
| BR-PM-231-02 | Each status can appear at most once per workflow | DB `UNIQUE(tenant_id, workflow_id, status_id)` |
| BR-PM-231-03 | Only one initial step per workflow | Service layer |

##### UC-PM-232: Remove Workflow Step

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-232-01 | Removing a step also removes all transitions from/to that step | Service layer (cascade within transaction) |
| BR-PM-232-02 | Can only modify draft workflows | Service layer |

##### UC-PM-233: Reorder Workflow Steps

Update `step_order` values. Draft only.

---

#### UC-PM-236 to UC-PM-239: Workflow Transition Management

##### UC-PM-236: Add Workflow Transition

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-236 |
| **Use Case Name** | Add Workflow Transition |
| **Priority** | High |
| **Complexity** | Medium |

**Permission**: `PM.WORKFLOW.MANAGE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Transition label |
| from_status_id | int64 | No | must be a step in this workflow, null for global | Source status |
| to_status_id | int64 | Yes | must be a step in this workflow | Target status |
| screen_id | int64 | No | must exist | Transition screen |
| sequence | int | No | auto-calculated | UI order |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-236-01 | Both from and to statuses must be steps in the workflow | Service layer |
| BR-PM-236-02 | `from_status_id=NULL` creates a global transition (available from any status) | Service layer |
| BR-PM-236-03 | Can only modify draft workflows | Service layer |

##### UC-PM-237: Update Workflow Transition

Update name, screen_id, sequence.

##### UC-PM-238: Remove Workflow Transition

Also removes all associated transition rules.

##### UC-PM-239: List Workflow Transitions

List all transitions for a workflow, optionally filtered by `from_status_id`.

---

#### UC-PM-241 to UC-PM-243: Workflow Transition Rule Management

##### UC-PM-241: Add Workflow Transition Rule

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-241 |
| **Use Case Name** | Add Workflow Transition Rule |
| **Priority** | Medium |
| **Complexity** | Medium |

**Permission**: `PM.WORKFLOW.MANAGE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| transition_id | int64 | Yes | must exist | Parent transition |
| rule_stage | string | Yes | CONDITION, VALIDATOR, POST_FUNCTION | Rule type |
| rule_key | string | Yes | known rule key | Rule identifier |
| config_json | json | No | valid JSON | Rule configuration |
| sequence | int | No | auto-calculated | Execution order |
| is_enabled | bool | No | default: true | Runtime toggle |

**Known Rule Keys**:

| Stage | Rule Key | Description |
|-------|----------|-------------|
| CONDITION | `user_is_assignee` | Only assignee can execute |
| CONDITION | `user_in_role` | User must be in specified project role |
| CONDITION | `field_has_value` | Specified field must have a value |
| VALIDATOR | `field_required` | Specified field is required for transition |
| VALIDATOR | `field_changed` | Specified field must have been changed |
| VALIDATOR | `resolution_required` | Resolution must be set |
| POST_FUNCTION | `fire_event` | Fire a custom notification event |
| POST_FUNCTION | `update_field` | Set a field to a specified value |
| POST_FUNCTION | `assign_to_lead` | Assign to project lead |
| POST_FUNCTION | `clear_resolution` | Clear resolution field |

##### UC-PM-242: Update Workflow Transition Rule

Update `config_json`, `is_enabled`, `sequence`.

##### UC-PM-243: Remove Workflow Transition Rule

Soft-delete the rule.

---

#### UC-PM-251 to UC-PM-256: Workflow Scheme Management

##### UC-PM-251: Create Workflow Scheme

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-251 |
| **Use Case Name** | Create Workflow Scheme |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.WORKFLOW_SCHEME.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Scheme name |
| description | string | No | max:2000 | Description |
| default_workflow_id | int64 | Yes | must exist and be active | Default workflow for unmapped issue types |

##### UC-PM-252 to UC-PM-255: Scheme Update, Get, List, Delete

Standard CRUD.

##### UC-PM-256: Manage Workflow Scheme Items

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-256 |
| **Use Case Name** | Manage Workflow Scheme Items |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Map issue types to specific workflows within a scheme. Issue types not explicitly mapped use the scheme's default workflow.

**Permission**: `PM.WORKFLOW_SCHEME.MANAGE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends PUT `/api/v1/workflow-schemes/{schemeId}/items` with list of mappings |
| 2 | System | Validates all issue types and workflows exist |
| 3 | System | Validates all referenced workflows are active (`is_active=true`) |
| 4 | System | Replaces all scheme items within transaction |
| 5 | System | Returns HTTP 200 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| items[] | array | Yes | | List of mappings |
| items[].issue_type_id | int64 | Yes | must exist | Issue type |
| items[].workflow_id | int64 | Yes | must exist and be active | Workflow |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-256-01 | Each issue type can only appear once per scheme | DB `UNIQUE(tenant_id, scheme_id, issue_type_id)` |
| BR-PM-256-02 | Only active workflows can be assigned to schemes | Service layer |

---

### 5.4. Fields & Screens (Module 04)

---

#### UC-PM-301 to UC-PM-306: Custom Field Management

##### UC-PM-301: Create Custom Field

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-301 |
| **Use Case Name** | Create Custom Field |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Medium |

##### Description

Create a custom field definition that extends the work item data model. Custom fields are typed and support contextual scoping (specific projects/issue types or global).

**Permission**: `PM.CUSTOM_FIELD.CREATE`

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/custom-fields` with field definition |
| 2 | System | Validates JWT, permissions, input |
| 3 | System | Generates unique `field_key` (format: `customfield_{sequence}`) |
| 4 | System | Validates `type_key` is supported |
| 5 | System | Persists custom field |
| 6 | System | If `is_global=true`, creates a global context entry |
| 7 | System | Publishes `CUSTOM_FIELD_CREATED` to `serp.pm.customfield.events` |
| 8 | System | Returns HTTP 201 |

##### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Display name |
| description | string | No | max:2000 | Description |
| type_key | string | Yes | text, number, date, user, select, multiselect, url, datetime | Field type |
| search_template | string | No | text_search, range_search, user_search, option_search | Search behavior |
| is_global | bool | No | default: true | Available in all contexts |
| schema_json | json | No | valid JSON | Plugin-specific settings |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-301-01 | `field_key` is auto-generated and immutable | Service layer |
| BR-PM-301-02 | `type_key` is immutable after creation (cannot change field type) | Service layer |
| BR-PM-301-03 | For `select`/`multiselect` types, options are managed via UC-PM-306 | API design |

##### UC-PM-302 to UC-PM-305: Custom Field Update, Get, List, Delete

- **UC-PM-302**: Update name, description, search_template, schema_json. Cannot change type_key.
- **UC-PM-303**: Get by ID with options and contexts.
- **UC-PM-304**: List with filters (type_key, is_system, is_global, search by name).
- **UC-PM-305**: Soft-delete. Cannot delete if work items have values for this field (warning, force option).

##### UC-PM-306: Manage Custom Field Options

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-306 |
| **Use Case Name** | Manage Custom Field Options |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Add, update, reorder, and disable options for `select`/`multiselect` custom fields.

**Permission**: `PM.CUSTOM_FIELD.MANAGE`

**Operations**:
- **Add**: POST `/api/v1/custom-fields/{fieldId}/options` with `{ value, sequence }`
- **Update**: PUT `/api/v1/custom-fields/{fieldId}/options/{optionId}` with `{ value, sequence, is_disabled }`
- **Remove**: Soft-delete (or set `is_disabled=true` to preserve existing data)

**Input Data (Add)**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| option_key | string | Yes | unique per field | Stable key |
| value | string | Yes | min:1, max:255 | Display value |
| sequence | int | No | auto-calculated | Order |
| parent_option_id | int64 | No | must exist | Cascade parent |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-306-01 | Only applicable to select/multiselect field types | Service layer |
| BR-PM-306-02 | Disabling an option preserves existing data but hides from new selections | Service layer |
| BR-PM-306-03 | `option_key` unique per field | DB `UNIQUE(tenant_id, custom_field_id, option_key)` |

---

#### UC-PM-311: Manage Custom Field Contexts

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-311 |
| **Use Case Name** | Manage Custom Field Contexts |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Define where a custom field is applicable (global, specific project, specific issue type, or combination).

**Permission**: `PM.CUSTOM_FIELD.MANAGE`

**Operations**:
- **Add Context**: POST `/api/v1/custom-fields/{fieldId}/contexts` with `{ project_id?, issue_type_id?, is_global_context }`
- **Remove Context**: DELETE `/api/v1/custom-fields/{fieldId}/contexts/{contextId}`
- **List Contexts**: GET `/api/v1/custom-fields/{fieldId}/contexts`

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-311-01 | If `is_global_context=true`, `project_id` and `issue_type_id` must be null | Service layer |
| BR-PM-311-02 | A field can have multiple non-global contexts for different project/issue type combinations | Service layer |

---

#### UC-PM-321 to UC-PM-326: Field Configuration Management

##### UC-PM-321: Create Field Configuration

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-321 |
| **Use Case Name** | Create Field Configuration |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.FIELD_CONFIG.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Config name |
| description | string | No | max:2000 | Description |

##### UC-PM-322 to UC-PM-325: Field Config Update, Get, List, Delete

Standard CRUD.

##### UC-PM-326: Manage Field Configuration Items

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-326 |
| **Use Case Name** | Manage Field Configuration Items |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Configure individual field behaviors within a field configuration (required, hidden, renderer).

**Permission**: `PM.FIELD_CONFIG.MANAGE`

**Operations**:
- **Set Item**: PUT `/api/v1/field-configurations/{configId}/items` with list of items
- **Get Items**: GET `/api/v1/field-configurations/{configId}/items`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| items[] | array | Yes | | List of field items |
| items[].field_ref_type | string | Yes | SYSTEM, CUSTOM | Field type |
| items[].field_ref | string | Yes | valid field reference | Field identifier |
| items[].is_required | bool | No | default: false | Required flag |
| items[].is_hidden | bool | No | default: false | Hidden flag |
| items[].renderer_key | string | No | text, wiki, markdown | Renderer |
| items[].sequence | int | No | auto-calculated | Display/evaluation order |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-326-01 | System field `summary` cannot be hidden | Service layer |
| BR-PM-326-02 | A field cannot be both required and hidden | Service layer validation |

---

#### UC-PM-331 to UC-PM-336: Field Config Scheme Management

##### UC-PM-331 to UC-PM-335: CRUD

Standard scheme CRUD with `default_field_configuration_id`.

**Permission**: `PM.FIELD_CONFIG_SCHEME.CREATE` / `PM.FIELD_CONFIG_SCHEME.MANAGE`

##### UC-PM-336: Manage Field Config Scheme Items

Maps issue types to field configurations within a scheme. Issue types not explicitly mapped use the scheme's default field configuration.

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| items[].issue_type_id | int64 | Yes | must exist | Issue type |
| items[].field_configuration_id | int64 | Yes | must exist | Field configuration |

---

#### UC-PM-341 to UC-PM-349: Screen Management

##### UC-PM-341: Create Screen

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-341 |
| **Use Case Name** | Create Screen |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.SCREEN.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Screen name |
| description | string | No | max:2000 | Description |

##### UC-PM-342 to UC-PM-345: Screen Update, Get, List, Delete

Standard CRUD.

##### UC-PM-346: Manage Screen Tabs

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-346 |
| **Use Case Name** | Manage Screen Tabs |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Add, update, remove, and reorder tabs within a screen.

**Permission**: `PM.SCREEN.MANAGE`

**Operations**:
- **Add Tab**: POST `/api/v1/screens/{screenId}/tabs` with `{ name, sequence }`
- **Update Tab**: PUT `/api/v1/screens/{screenId}/tabs/{tabId}` with `{ name, sequence }`
- **Remove Tab**: DELETE `/api/v1/screens/{screenId}/tabs/{tabId}`
- **List Tabs**: GET `/api/v1/screens/{screenId}/tabs`

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-346-01 | Removing a tab also removes all field references in that tab | Service layer |
| BR-PM-346-02 | A screen must have at least one tab | Service layer |

##### UC-PM-349: Manage Screen Tab Fields

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-349 |
| **Use Case Name** | Manage Screen Tab Fields |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Add, remove, and reorder field references within a screen tab.

**Permission**: `PM.SCREEN.MANAGE`

**Operations**:
- **Add Field**: POST `/api/v1/screens/{screenId}/tabs/{tabId}/fields` with `{ field_ref_type, field_ref, sequence }`
- **Remove Field**: DELETE `/api/v1/screens/{screenId}/tabs/{tabId}/fields/{fieldId}`
- **Reorder Fields**: PUT `/api/v1/screens/{screenId}/tabs/{tabId}/fields/reorder` with ordered list

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-349-01 | A field can only appear once per screen (across all tabs) | Service layer |
| BR-PM-349-02 | `field_ref` must be a valid system field or existing custom field | Service layer |

---

#### UC-PM-356 to UC-PM-361: Screen Scheme Management

##### UC-PM-356 to UC-PM-360: CRUD

Standard scheme CRUD with `default_screen_id`.

**Permission**: `PM.SCREEN_SCHEME.CREATE` / `PM.SCREEN_SCHEME.MANAGE`

##### UC-PM-361: Manage Screen Scheme Items

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-361 |
| **Use Case Name** | Manage Screen Scheme Items |
| **Priority** | Medium |
| **Complexity** | Medium |

**Description**: Map operations (CREATE, EDIT, VIEW, TRANSITION) to specific screens within a scheme.

**Permission**: `PM.SCREEN_SCHEME.MANAGE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| items[] | array | Yes | | Operation-to-screen mappings |
| items[].operation_key | string | Yes | CREATE, EDIT, VIEW, TRANSITION | Operation |
| items[].screen_id | int64 | Yes | must exist | Screen |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-361-01 | Each operation can only appear once per scheme | DB `UNIQUE(tenant_id, screen_scheme_id, operation_key)` |
| BR-PM-361-02 | Unmapped operations fall back to the scheme's default screen | Service layer |

---

#### UC-PM-366 to UC-PM-371: Issue Type Screen Scheme Management

##### UC-PM-366 to UC-PM-370: CRUD

Standard scheme CRUD with `default_screen_scheme_id`.

**Permission**: `PM.ISSUE_TYPE_SCREEN_SCHEME.CREATE` / `PM.ISSUE_TYPE_SCREEN_SCHEME.MANAGE`

##### UC-PM-371: Manage Issue Type Screen Scheme Items

Maps issue types to screen schemes. Issue types not explicitly mapped use the default screen scheme.

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| items[].issue_type_id | int64 | Yes | must exist | Issue type |
| items[].screen_scheme_id | int64 | Yes | must exist | Screen scheme |

---

### 5.5. Permissions & Security (Module 05)

---

#### UC-PM-401 to UC-PM-402: Permission Definition Catalog (Read-only)

##### UC-PM-401: Get Permission Definition by Key

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-401 |
| **Use Case Name** | Get Permission Definition by Key |
| **Priority** | Low |
| **Complexity** | Simple |

**Description**: Retrieve one permission definition from the system-seeded permission catalog by `permission_key`.

**Permission**: `PM.PERMISSION.READ`

**Main Flow**: GET `/api/v1/permission-definitions/{permissionKey}` -> validate JWT + permission -> fetch by `tenant_id` + `permission_key` + `deleted_at IS NULL` -> return 200.

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-401-01 | Permission catalog keys are immutable | Service layer + migration seed |
| BR-PM-401-02 | Permission definitions are read-only via PM API in v1 | Service layer |

##### UC-PM-402: List Permission Definitions

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-402 |
| **Use Case Name** | List Permission Definitions |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.PERMISSION.READ`

**Main Flow**: GET `/api/v1/permission-definitions` with pagination + optional `category` filter -> validate JWT + permission -> query by `tenant_id` -> return paginated result.

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| category | string | No | PROJECT, ISSUE, COMMENT, ADMIN, AGILE | Category filter |

**Note**: Create/update/delete permission definition APIs are intentionally not exposed in v1. Catalog seeding/sync is handled by system bootstrap/migrations.

---

#### UC-PM-411 to UC-PM-419: Permission Scheme Management

##### UC-PM-411: Create Permission Scheme

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-411 |
| **Use Case Name** | Create Permission Scheme |
| **Priority** | High |
| **Complexity** | Simple |

**Permission**: `PM.PERMISSION_SCHEME.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Scheme name |
| description | string | No | max:2000 | Description |

##### UC-PM-412 to UC-PM-415: Scheme Update, Get, List, Delete

Standard CRUD.

##### UC-PM-416: Add Permission Scheme Entry

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-416 |
| **Use Case Name** | Add Permission Scheme Entry |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-02-18 |
| **Priority** | High |
| **Complexity** | Medium |

##### Description

Add a permission grant to a permission scheme, mapping a permission to a grantee (project role, group, user, or contextual actor).

**Permission**: `PM.PERMISSION_SCHEME.MANAGE`

##### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/permission-schemes/{schemeId}/entries` with entry data |
| 2 | System | Validates JWT, permissions |
| 3 | System | Validates scheme exists |
| 4 | System | Validates `permission_key` exists in permission definitions |
| 5 | System | Validates `grantee_type` and `grantee_id` (for PROJECT_ROLE: role exists; for GROUP/USER: subject exists) |
| 6 | System | Checks for duplicate entry |
| 7 | System | Persists entry |
| 8 | System | Returns HTTP 201 |

##### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| permission_key | string | Yes | must exist | Permission identifier |
| grantee_type | string | Yes | PROJECT_ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE | Grantee type |
| grantee_id | string | No | required for PROJECT_ROLE/GROUP/USER; null for contextual types | Grantee identifier |

##### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-416-01 | Contextual grantee types (PROJECT_LEAD, REPORTER, ASSIGNEE) don't require grantee_id | Service layer |
| BR-PM-416-02 | Permission evaluation is grant-only (implicit deny when no grant matches) | Service layer (evaluation) |
| BR-PM-416-03 | Duplicate entries (same scheme + permission + grantee) are rejected | DB unique expression index with `COALESCE(grantee_id, '__CTX__')` |
| BR-PM-416-04 | `PROJECT_ROLE` grantee must reference an existing tenant role | Service layer |

##### UC-PM-417: Update Permission Scheme Entry

Update `permission_key`, `grantee_type`, `grantee_id`.

##### UC-PM-418: Remove Permission Scheme Entry

Soft-delete.

##### UC-PM-419: List Permission Scheme Entries

GET `/api/v1/permission-schemes/{schemeId}/entries` with optional filter by `permission_key`, `grantee_type`.

---

#### UC-PM-421 to UC-PM-433: Issue Security Management

##### UC-PM-421: Create Issue Security Scheme

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-421 |
| **Use Case Name** | Create Issue Security Scheme |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.ISSUE_SECURITY.CREATE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Scheme name |
| description | string | No | max:2000 | Description |
| default_level_id | int64 | No | must exist if provided | Default security level |

##### UC-PM-422 to UC-PM-425: Scheme Update, Get, List, Delete

Standard CRUD.

##### UC-PM-426: Add Issue Security Level

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-426 |
| **Use Case Name** | Add Issue Security Level |
| **Priority** | Medium |
| **Complexity** | Simple |

**Permission**: `PM.ISSUE_SECURITY.MANAGE`

**Main Flow**:

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends POST `/api/v1/issue-security-schemes/{schemeId}/levels` with level data |
| 2 | System | Validates scheme exists |
| 3 | System | Persists security level |
| 4 | System | Returns HTTP 201 |

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255 | Level name (e.g., "Confidential", "Internal") |
| description | string | No | max:2000 | Description |

##### UC-PM-427: Update Issue Security Level

Update name, description.

##### UC-PM-428: Remove Issue Security Level

Soft-delete. Cannot remove if work items reference this level (must reassign first).

##### UC-PM-431: Add Issue Security Level Member

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-431 |
| **Use Case Name** | Add Issue Security Level Member |
| **Priority** | Medium |
| **Complexity** | Simple |

**Description**: Define who can view issues tagged with a security level.

**Permission**: `PM.ISSUE_SECURITY.MANAGE`

**Input Data**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| subject_type | string | Yes | PROJECT_ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE | Member type |
| subject_id | string | No | required for PROJECT_ROLE/GROUP/USER; null for contextual types | Member identifier |

**Business Rules**:

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-431-01 | Duplicate members (same level + subject) rejected | DB unique expression index with `COALESCE(subject_id, '__CTX__')` |
| BR-PM-431-02 | Contextual subject types (PROJECT_LEAD, REPORTER, ASSIGNEE) must not carry subject_id | Service layer |

##### UC-PM-432: Remove Issue Security Level Member

Soft-delete.

##### UC-PM-433: List Issue Security Level Members

GET `/api/v1/issue-security-schemes/{schemeId}/levels/{levelId}/members`.

---

## 6. Common Business Rules

Rules that apply across multiple use cases:

| Rule ID | Description | Applies To | Enforcement |
|---------|-------------|------------|-------------|
| BR-PM-G01 | All queries MUST filter by `tenant_id` from JWT | All | Repository layer |
| BR-PM-G02 | Soft delete sets `deleted_at` timestamp; never physically remove data | All delete ops | Service layer |
| BR-PM-G03 | Audit fields: `created_at`, `updated_at` set automatically. `created_by`/`updated_by` set from JWT userId | All CUD ops | Service layer |
| BR-PM-G04 | Kafka events published AFTER transaction commit for all CUD operations | All CUD ops | UseCase layer |
| BR-PM-G05 | Kafka topic naming: `serp.pm.[entity].events` | All events | Constant definition |
| BR-PM-G06 | Kafka event type naming: `ENTITY_ACTION` uppercase (e.g., `WORK_ITEM_CREATED`) | All events | Constant definition |
| BR-PM-G07 | Default pagination: page=0, pageSize=10, max pageSize=100 | All list endpoints | Controller layer |
| BR-PM-G08 | System entities (`is_system=true`) cannot be modified or deleted via API | All system entities | Service layer |
| BR-PM-G09 | All entity references (FKs) are validated for existence and tenant ownership before persistence | All CUD ops | Service layer |
| BR-PM-G10 | Archived projects reject all write operations on child entities | All project-scoped writes | Service layer |
| BR-PM-G11 | Kafka message envelope follows standard format: `{ "meta": { id, type, source, v, ts, traceId, tenantId, entityType, entityId, userId }, "data": { ... } }` | All events | Event publisher |
| BR-PM-G12 | Draft workflows (`is_active=false`) can be modified; active workflows are immutable | Workflow modification UCs | Service layer |

---

## 7. Glossary

| Term | Definition |
|------|------------|
| **Work Item** | The central entity representing a unit of work (issue, task, bug, story, epic). Equivalent to JIRA's "Issue". |
| **Issue Type** | Classification of work items (Bug, Story, Task, Epic, Subtask) with hierarchy levels. |
| **Workflow** | A directed graph of statuses and transitions that defines the lifecycle of a work item. |
| **Workflow Scheme** | Maps issue types to workflows. Projects reference a workflow scheme to determine which workflow applies to each issue type. |
| **Status Category** | Logical grouping of statuses into three buckets: To Do (new), In Progress (indeterminate), Done (done). Used for reporting. |
| **Transition** | A directed edge in a workflow graph from one status to another, with optional conditions, validators, and post-functions. |
| **Screen** | A layout definition specifying which fields to display and in what order, organized into tabs. |
| **Screen Scheme** | Maps operations (CREATE, EDIT, VIEW, TRANSITION) to specific screens. |
| **Issue Type Screen Scheme** | Maps issue types to screen schemes. Projects reference this to determine which screen is shown for each issue type + operation. |
| **Field Configuration** | Defines per-field behavior (required, hidden, renderer) for a set of fields. |
| **Field Config Scheme** | Maps issue types to field configurations. |
| **Custom Field** | A user-defined field that extends the work item data model with typed values. |
| **Custom Field Context** | Scoping rule for a custom field, defining which projects and issue types it applies to. |
| **Permission Scheme** | Maps permissions to grantees (project roles, groups, users, contextual actors) using grant-only rules; missing grant means deny. |
| **Issue Security Level** | Restricts visibility of individual work items to specific project roles, groups, users, or contextual actors. |
| **Project Blueprint** | A template that pre-configures source scheme bindings for new projects; runtime project schemes are provisioned as clones. |
| **Project Component** | A sub-section of a project (e.g., "Backend", "Frontend") used to categorize work items. |
| **Project Version** | A release marker (e.g., "v1.0", "v2.0") used to track fix targets and release planning. |
| **Project Role** | A named role within PM Core (e.g., "Developer", "QA Lead") that actors can be assigned to per project. |
| **Lexorank** | A ranking algorithm that generates string-based rank values allowing insertion between existing items without reindexing. |
| **Worklog** | A time tracking entry recording work performed on a work item. |
| **Issue Link** | A typed relationship between two work items (e.g., "blocks", "is blocked by", "clones"). |
| **Resolution** | The outcome of a work item when it reaches a "done" status (e.g., "Done", "Won't Fix", "Duplicate"). |
| **Scheme Indirection** | The architectural pattern where projects reference scheme roots (not individual configs), with project-owned cloning to preserve isolation by default. |
| **Soft Delete** | Marking a record as deleted by setting `deleted_at` timestamp instead of physically removing it from the database. |
