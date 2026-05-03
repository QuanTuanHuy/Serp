# PM Core - Use Case Implementation Tracking

**Last Updated:** 2026-05-03  
**Total Use Cases:** 183 (specified in PM_USECASE_SPEC.md)  
**Implemented:** 64 use cases (35% coverage)  
**Pending:** 119 use cases (65%)

---

## 📊 Implementation Overview

| Module | Total Use Cases | Implemented | Pending | Coverage |
|--------|----------------|-------------|---------|----------|
| **Module 01: Projects & Configuration** | 38 | 15 | 23 | 39% |
| **Module 02: Issues & Work Items** | 37 | 28 | 9 | 76% |
| **Module 03: Workflow Engine** | 36 | 21 | 15 | 58% |
| **Module 04: Fields & Screens** | 51 | 0 | 51 | 0% |
| **Module 05: Permissions & Security** | 21 | 0 | 21 | 0% |

---

## Module 01: Projects & Configuration

### ✅ Project Management (4/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-001 | Create Project | ✅ Implemented | `CreateProjectCommandHandler` |
| UC-PM-002 | Update Project | ✅ Implemented | `UpdateProjectCommandHandler` |
| UC-PM-003 | Get Project Details | ✅ Implemented | `GetProjectByIdQueryHandler`, `GetProjectByKeyQueryHandler` |
| UC-PM-004 | List Projects | ✅ Implemented | `ListProjectsQueryHandler` |
| UC-PM-005 | Delete Project | ❌ Pending | - |
| UC-PM-006 | Archive/Unarchive Project | ✅ Implemented | `ArchiveProjectCommandHandler`, `UnarchiveProjectCommandHandler` |
| UC-PM-007 | Update Project Scheme Bindings | ❌ Pending | - |

### ✅ Project Category (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-011 | Create Project Category | ✅ Implemented | `CreateProjectCategoryCommandHandler` |
| UC-PM-012 | Update Project Category | ✅ Implemented | `UpdateProjectCategoryCommandHandler` |
| UC-PM-013 | Get Project Category | ✅ Implemented | `GetProjectCategoryByIdQueryHandler` |
| UC-PM-014 | List Project Categories | ✅ Implemented | `ListProjectCategoriesQueryHandler` |
| UC-PM-015 | Delete Project Category | ✅ Implemented | `DeleteProjectCategoryCommandHandler` |

### ✅ Project Blueprint (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-016 | Create Project Blueprint | ✅ Implemented | `CreateProjectBlueprintCommandHandler` |
| UC-PM-017 | Update Project Blueprint | ✅ Implemented | `UpdateProjectBlueprintCommandHandler` |
| UC-PM-018 | Get Project Blueprint | ✅ Implemented | `GetProjectBlueprintByIdQueryHandler` |
| UC-PM-019 | List Project Blueprints | ✅ Implemented | `ListProjectBlueprintsQueryHandler` |
| UC-PM-020 | Delete Project Blueprint | ✅ Implemented | `DeleteProjectBlueprintCommandHandler` |
| UC-PM-021 | Manage Blueprint Scheme Defaults | ❌ Pending | - |

### ❌ Project Component (0/5 implemented) - HIGH PRIORITY

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-026 | Create Project Component | ❌ Pending | - |
| UC-PM-027 | Update Project Component | ❌ Pending | - |
| UC-PM-028 | Get Project Component | ❌ Pending | - |
| UC-PM-029 | List Project Components | ❌ Pending | - |
| UC-PM-030 | Delete Project Component | ❌ Pending | - |

### ❌ Project Version (0/7 implemented) - HIGH PRIORITY

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-031 | Create Project Version | ❌ Pending | - |
| UC-PM-032 | Update Project Version | ❌ Pending | - |
| UC-PM-033 | Get Project Version | ❌ Pending | - |
| UC-PM-034 | List Project Versions | ❌ Pending | - |
| UC-PM-035 | Delete Project Version | ❌ Pending | - |
| UC-PM-036 | Release Version | ❌ Pending | - |
| UC-PM-037 | Archive Version | ❌ Pending | - |

### ✅ Project Role (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-041 | Create Project Role | ✅ Implemented | `CreateProjectRoleCommandHandler` |
| UC-PM-042 | Update Project Role | ✅ Implemented | `UpdateProjectRoleCommandHandler` |
| UC-PM-043 | Get Project Role | ✅ Implemented | `GetProjectRoleByIdQueryHandler` |
| UC-PM-044 | List Project Roles | ✅ Implemented | `ListProjectRoleQueryHandler` |
| UC-PM-045 | Delete Project Role | ✅ Implemented | `DeleteProjectRoleCommandHandler` |

### ✅ Project Role Actor (3/3 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-046 | Add Project Role Actor | ✅ Implemented | `AddProjectRoleActorCommandHandler` |
| UC-PM-047 | Remove Project Role Actor | ✅ Implemented | `RemoveProjectRoleActorCommandHandler` |
| UC-PM-048 | List Project Role Actors | ✅ Implemented | `ListProjectRoleActorsQueryHandler` |

---

## Module 02: Issues & Work Items

### ⚠️ Work Item (7/12 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-101 | Create Work Item | ✅ Implemented | `CreateWorkItemCommandHandler` |
| UC-PM-102 | Update Work Item | ✅ Implemented | `UpdateWorkItemCommandHandler` |
| UC-PM-103 | Get Work Item Details | ✅ Implemented | `GetWorkItemByIdQueryHandler` |
| UC-PM-104 | List/Search Work Items | ✅ Implemented | `SearchWorkItemsQueryHandler` |
| UC-PM-105 | Delete Work Item | ✅ Implemented | `DeleteWorkItemCommandHandler` |
| UC-PM-106 | Transition Work Item Status | ✅ Implemented | `TransitionWorkItemCommandHandler` |
| UC-PM-107 | Assign Work Item | ✅ Implemented | `AssignWorkItemCommandHandler` |
| UC-PM-108 | Re-rank Work Item (Lexorank) | ❌ Pending | - |
| UC-PM-109 | Bulk Update Work Items | ❌ Pending | - |
| UC-PM-110 | Clone Work Item | ❌ Pending | - |
| UC-PM-111 | Manage Work Item Components | ❌ Pending | - |
| UC-PM-116 | Manage Work Item Fix Versions | ❌ Pending | - |

### ❌ Sprint Management (0/2 implemented) - HIGH PRIORITY

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-121 | Move Work Item to Sprint | ❌ Pending | - |
| UC-PM-122 | Remove Work Item from Sprint | ❌ Pending | - |

### ✅ Issue Type (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-131 | Create Issue Type | ✅ Implemented | `CreateIssueTypeCommandHandler` |
| UC-PM-132 | Update Issue Type | ✅ Implemented | `UpdateIssueTypeCommandHandler` |
| UC-PM-133 | Get Issue Type | ✅ Implemented | `GetIssueTypeByIdQueryHandler` |
| UC-PM-134 | List Issue Types | ✅ Implemented | `ListIssueTypesQueryHandler` |
| UC-PM-135 | Delete Issue Type | ✅ Implemented | `DeleteIssueTypeCommandHandler` |

### ✅ Issue Type Scheme (6/6 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-136 | Create Issue Type Scheme | ✅ Implemented | `CreateIssueTypeSchemeCommandHandler` |
| UC-PM-137 | Update Issue Type Scheme | ✅ Implemented | `UpdateIssueTypeSchemeCommandHandler` |
| UC-PM-138 | Get Issue Type Scheme | ✅ Implemented | `GetIssueTypeSchemeByIdQueryHandler` |
| UC-PM-139 | List Issue Type Schemes | ✅ Implemented | `ListIssueTypeSchemesQueryHandler` |
| UC-PM-140 | Delete Issue Type Scheme | ✅ Implemented | `DeleteIssueTypeSchemeCommandHandler` |
| UC-PM-141 | Manage Issue Type Scheme Items | ✅ Implemented | `ManageIssueTypeSchemeItemsCommandHandler` |

### ✅ Priority (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-146 | Create Priority | ✅ Implemented | `CreatePriorityCommandHandler` |
| UC-PM-147 | Update Priority | ✅ Implemented | `UpdatePriorityCommandHandler` |
| UC-PM-148 | Get Priority | ✅ Implemented | `GetPriorityByIdQueryHandler` |
| UC-PM-149 | List Priorities | ✅ Implemented | `ListPrioritiesQueryHandler` |
| UC-PM-150 | Delete Priority | ✅ Implemented | `DeletePriorityCommandHandler` |

### ✅ Priority Scheme (6/6 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-151 | Create Priority Scheme | ✅ Implemented | `CreatePrioritySchemeCommandHandler` |
| UC-PM-152 | Update Priority Scheme | ✅ Implemented | `UpdatePrioritySchemeCommandHandler` |
| UC-PM-153 | Get Priority Scheme | ✅ Implemented | `GetPrioritySchemeByIdQueryHandler` |
| UC-PM-154 | List Priority Schemes | ✅ Implemented | `ListPrioritySchemesQueryHandler` |
| UC-PM-155 | Delete Priority Scheme | ✅ Implemented | `DeletePrioritySchemeCommandHandler` |
| UC-PM-156 | Manage Priority Scheme Items | ✅ Implemented | `ManagePrioritySchemeItemsCommandHandler` |

### ❌ Resolution (0/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-161 | Create Resolution | ❌ Pending | - |
| UC-PM-162 | Update Resolution | ❌ Pending | - |
| UC-PM-163 | Get Resolution | ❌ Pending | - |
| UC-PM-164 | List Resolutions | ❌ Pending | - |
| UC-PM-165 | Delete Resolution | ❌ Pending | - |

### ✅ Issue Link Type (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-171 | Create Issue Link Type | ✅ Implemented | `CreateIssueLinkTypeCommandHandler` |
| UC-PM-172 | Update Issue Link Type | ✅ Implemented | `UpdateIssueLinkTypeCommandHandler` |
| UC-PM-173 | Get Issue Link Type | ✅ Implemented | `GetIssueLinkTypeByIdQueryHandler` |
| UC-PM-174 | List Issue Link Types | ✅ Implemented | `ListIssueLinkTypesQueryHandler` |
| UC-PM-175 | Delete Issue Link Type | ✅ Implemented | `DeleteIssueLinkTypeCommandHandler` |

### ✅ Issue Link (3/3 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-176 | Create Issue Link | ✅ Implemented | `CreateIssueLinkCommandHandler` |
| UC-PM-177 | Delete Issue Link | ✅ Implemented | `DeleteIssueLinkCommandHandler` |
| UC-PM-178 | List Issue Links | ✅ Implemented | `ListIssueLinksQueryHandler` |

### ✅ Worklog (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-181 | Create Worklog | ✅ Implemented | `CreateWorklogCommandHandler` |
| UC-PM-182 | Update Worklog | ✅ Implemented | `UpdateWorklogCommandHandler` |
| UC-PM-183 | Delete Worklog | ✅ Implemented | `DeleteWorklogCommandHandler` |
| UC-PM-184 | List Worklogs | ✅ Implemented | `ListWorklogsQueryHandler` |
| UC-PM-185 | Get Worklog | ✅ Implemented | `GetWorklogByIdQueryHandler` |

---

## Module 03: Workflow Engine

### ✅ Status Category (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-201 | Create Status Category | ✅ Implemented | `CreateStatusCategoryCommandHandler` |
| UC-PM-202 | Update Status Category | ✅ Implemented | `UpdateStatusCategoryCommandHandler` |
| UC-PM-203 | Get Status Category | ✅ Implemented | `GetStatusCategoryByIdQueryHandler` |
| UC-PM-204 | List Status Categories | ✅ Implemented | `ListStatusCategoriesQueryHandler` |
| UC-PM-205 | Delete Status Category | ✅ Implemented | `DeleteStatusCategoryCommandHandler` |

### ✅ Status (5/5 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-211 | Create Status | ✅ Implemented | `CreateStatusCommandHandler` |
| UC-PM-212 | Update Status | ✅ Implemented | `UpdateStatusCommandHandler` |
| UC-PM-213 | Get Status | ✅ Implemented | `GetStatusByIdQueryHandler` |
| UC-PM-214 | List Statuses | ✅ Implemented | `ListStatusesQueryHandler` |
| UC-PM-215 | Delete Status | ✅ Implemented | `DeleteStatusCommandHandler` |

### ⚠️ Workflow (6/8 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-221 | Create Workflow | ✅ Implemented | `CreateWorkflowCommandHandler` |
| UC-PM-222 | Update Workflow | ✅ Implemented | `UpdateWorkflowCommandHandler` |
| UC-PM-223 | Get Workflow | ✅ Implemented | `GetWorkflowByIdQueryHandler` |
| UC-PM-224 | List Workflows | ✅ Implemented | `ListWorkflowsQueryHandler` |
| UC-PM-225 | Delete Workflow | ❌ Pending | - |
| UC-PM-226 | Publish Workflow | ✅ Implemented | `PublishWorkflowCommandHandler` |
| UC-PM-227 | Clone Workflow | ❌ Pending | - |
| UC-PM-228 | Validate Workflow | ✅ Implemented | `ValidateWorkflowQueryHandler` |

### ✅ Workflow Step (3/3 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-231 | Add Workflow Step | ✅ Implemented | `AddWorkflowStepCommandHandler` |
| UC-PM-232 | Remove Workflow Step | ✅ Implemented | `RemoveWorkflowStepCommandHandler` |
| UC-PM-233 | Reorder Workflow Steps | ✅ Implemented | `ReorderWorkflowStepsCommandHandler` |

### ⚠️ Workflow Transition (4/7 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-236 | Add Workflow Transition | ✅ Implemented | `AddWorkflowTransitionCommandHandler` |
| UC-PM-237 | Update Workflow Transition | ✅ Implemented | `UpdateWorkflowTransitionCommandHandler` |
| UC-PM-238 | Remove Workflow Transition | ✅ Implemented | `RemoveWorkflowTransitionCommandHandler` |
| UC-PM-239 | List Workflow Transitions | ✅ Implemented | `ListWorkflowTransitionsQueryHandler` |
| UC-PM-241 | Add Workflow Transition Rule | ❌ Pending | - |
| UC-PM-242 | Update Workflow Transition Rule | ❌ Pending | - |
| UC-PM-243 | Remove Workflow Transition Rule | ❌ Pending | - |

### ❌ Workflow Scheme (0/6 implemented) - HIGH PRIORITY

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-251 | Create Workflow Scheme | ❌ Pending | - |
| UC-PM-252 | Update Workflow Scheme | ❌ Pending | - |
| UC-PM-253 | Get Workflow Scheme | ❌ Pending | - |
| UC-PM-254 | List Workflow Schemes | ❌ Pending | - |
| UC-PM-255 | Delete Workflow Scheme | ❌ Pending | - |
| UC-PM-256 | Manage Workflow Scheme Items | ❌ Pending | - |

---

## Module 04: Fields & Screens (0/51 implemented)

### ❌ Custom Field (0/11 implemented) - HIGH PRIORITY

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-301 | Create Custom Field | ❌ Pending | - |
| UC-PM-302 | Update Custom Field | ❌ Pending | - |
| UC-PM-303 | Get Custom Field | ❌ Pending | - |
| UC-PM-304 | List Custom Fields | ❌ Pending | - |
| UC-PM-305 | Delete Custom Field | ❌ Pending | - |
| UC-PM-306 | Create Custom Field Context | ❌ Pending | - |
| UC-PM-307 | Update Custom Field Context | ❌ Pending | - |
| UC-PM-308 | Delete Custom Field Context | ❌ Pending | - |
| UC-PM-309 | Set Custom Field Default Value | ❌ Pending | - |
| UC-PM-310 | Add Custom Field Option | ❌ Pending | - |
| UC-PM-311 | Remove Custom Field Option | ❌ Pending | - |

### ❌ Field Configuration (0/6 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-321 | Create Field Configuration | ❌ Pending | - |
| UC-PM-322 | Update Field Configuration | ❌ Pending | - |
| UC-PM-323 | Get Field Configuration | ❌ Pending | - |
| UC-PM-324 | List Field Configurations | ❌ Pending | - |
| UC-PM-325 | Delete Field Configuration | ❌ Pending | - |
| UC-PM-326 | Manage Field Config Items | ❌ Pending | - |

### ❌ Field Configuration Scheme (0/6 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-331 | Create Field Config Scheme | ❌ Pending | - |
| UC-PM-332 | Update Field Config Scheme | ❌ Pending | - |
| UC-PM-333 | Get Field Config Scheme | ❌ Pending | - |
| UC-PM-334 | List Field Config Schemes | ❌ Pending | - |
| UC-PM-335 | Delete Field Config Scheme | ❌ Pending | - |
| UC-PM-336 | Manage Field Config Scheme Items | ❌ Pending | - |

### ❌ Screen (0/9 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-341 | Create Screen | ❌ Pending | - |
| UC-PM-342 | Update Screen | ❌ Pending | - |
| UC-PM-343 | Get Screen | ❌ Pending | - |
| UC-PM-344 | List Screens | ❌ Pending | - |
| UC-PM-345 | Delete Screen | ❌ Pending | - |
| UC-PM-346 | Add Screen Tab | ❌ Pending | - |
| UC-PM-347 | Update Screen Tab | ❌ Pending | - |
| UC-PM-348 | Remove Screen Tab | ❌ Pending | - |
| UC-PM-349 | Manage Screen Tab Fields | ❌ Pending | - |

### ❌ Screen Scheme (0/6 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-356 | Create Screen Scheme | ❌ Pending | - |
| UC-PM-357 | Update Screen Scheme | ❌ Pending | - |
| UC-PM-358 | Get Screen Scheme | ❌ Pending | - |
| UC-PM-359 | List Screen Schemes | ❌ Pending | - |
| UC-PM-360 | Delete Screen Scheme | ❌ Pending | - |
| UC-PM-361 | Manage Screen Scheme Items | ❌ Pending | - |

### ❌ Issue Type Screen Scheme (0/6 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-366 | Create Issue Type Screen Scheme | ❌ Pending | - |
| UC-PM-367 | Update Issue Type Screen Scheme | ❌ Pending | - |
| UC-PM-368 | Get Issue Type Screen Scheme | ❌ Pending | - |
| UC-PM-369 | List Issue Type Screen Schemes | ❌ Pending | - |
| UC-PM-370 | Delete Issue Type Screen Scheme | ❌ Pending | - |
| UC-PM-371 | Manage Issue Type Screen Scheme Items | ❌ Pending | - |

---

## Module 05: Permissions & Security (0/21 implemented)

### ❌ Permission Definition (0/2 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-401 | Get Permission Definition | ❌ Pending | - |
| UC-PM-402 | List Permission Definitions | ❌ Pending | - |

### ❌ Permission Scheme (0/9 implemented) - HIGH PRIORITY

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-411 | Create Permission Scheme | ❌ Pending | - |
| UC-PM-412 | Update Permission Scheme | ❌ Pending | - |
| UC-PM-413 | Get Permission Scheme | ❌ Pending | - |
| UC-PM-414 | List Permission Schemes | ❌ Pending | - |
| UC-PM-415 | Delete Permission Scheme | ❌ Pending | - |
| UC-PM-416 | Grant Permission | ❌ Pending | - |
| UC-PM-417 | Revoke Permission | ❌ Pending | - |
| UC-PM-418 | List Permission Grants | ❌ Pending | - |
| UC-PM-419 | Check User Permission | ❌ Pending | - |

### ❌ Issue Security Scheme (0/8 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-421 | Create Issue Security Scheme | ❌ Pending | - |
| UC-PM-422 | Update Issue Security Scheme | ❌ Pending | - |
| UC-PM-423 | Get Issue Security Scheme | ❌ Pending | - |
| UC-PM-424 | List Issue Security Schemes | ❌ Pending | - |
| UC-PM-425 | Delete Issue Security Scheme | ❌ Pending | - |
| UC-PM-426 | Add Security Level | ❌ Pending | - |
| UC-PM-427 | Update Security Level | ❌ Pending | - |
| UC-PM-428 | Remove Security Level | ❌ Pending | - |

### ❌ Issue Security Level Member (0/3 implemented)

| Use Case ID | Name | Status | Handler/Controller |
|------------|------|--------|-------------------|
| UC-PM-431 | Add Security Level Member | ❌ Pending | - |
| UC-PM-432 | Remove Security Level Member | ❌ Pending | - |
| UC-PM-433 | List Security Level Members | ❌ Pending | - |

---

## 🎯 Implementation Roadmap

### Phase 1: Complete Core Features (Q2 2026)

**Priority: HIGH - Complete existing modules**

1. **Project Components & Versions** (12 use cases)
   - UC-PM-026 to UC-PM-037
   - Required for: Release management, component tracking
   - Estimated effort: 2-3 weeks

2. **Resolution Management** (5 use cases)
   - UC-PM-161 to UC-PM-165
   - Required for: Work item closure workflow
   - Estimated effort: 1 week

3. **Workflow Scheme** (6 use cases)
   - UC-PM-251 to UC-PM-256
   - Required for: Multi-project workflow configuration
   - Estimated effort: 2 weeks

4. **Missing Core Features**
   - UC-PM-005: Delete Project
   - UC-PM-225: Delete Workflow
   - UC-PM-227: Clone Workflow
   - Estimated effort: 1 week

**Total Phase 1: 6-7 weeks**

---

### Phase 2: Advanced Configuration (Q3 2026)

**Priority: HIGH - Enable flexible data model**

1. **Custom Fields** (11 use cases)
   - UC-PM-301 to UC-PM-311
   - Required for: Flexible work item attributes
   - Estimated effort: 4-5 weeks

2. **Field Configuration** (12 use cases)
   - UC-PM-321 to UC-PM-336
   - Required for: Field visibility and behavior control
   - Estimated effort: 3 weeks

3. **Screen Configuration** (15 use cases)
   - UC-PM-341 to UC-PM-361
   - Required for: UI customization per issue type
   - Estimated effort: 4 weeks

**Total Phase 2: 11-12 weeks**

---

### Phase 3: Security & Permissions (Q4 2026)

**Priority: HIGH - Production-ready security**

1. **Permission Schemes** (9 use cases)
   - UC-PM-411 to UC-PM-419
   - Required for: Fine-grained access control
   - Estimated effort: 4 weeks

2. **Issue Security** (11 use cases)
   - UC-PM-421 to UC-PM-433
   - Required for: Row-level security
   - Estimated effort: 3 weeks

**Total Phase 3: 7 weeks**

---

### Phase 4: Agile Features (Q1 2027)

**Priority: MEDIUM - Agile workflow enhancements**

1. **Sprint Management** (2 use cases)
   - UC-PM-121 to UC-PM-122
   - Required for: Scrum workflow
   - Estimated effort: 2 weeks

2. **Work Item Advanced Operations** (5 use cases)
   - UC-PM-108: Re-rank (Lexorank)
   - UC-PM-109: Bulk Update
   - UC-PM-110: Clone
   - UC-PM-111: Manage Components
   - UC-PM-116: Manage Fix Versions
   - Estimated effort: 3 weeks

3. **Workflow Transition Rules** (3 use cases)
   - UC-PM-241 to UC-PM-243
   - Required for: Conditional workflow logic
   - Estimated effort: 2 weeks

**Total Phase 4: 7 weeks**

---

## 📝 Notes

### Implementation Quality
- All implemented handlers follow Clean Architecture patterns
- Command handlers use CQRS with separate read/write models
- Authorization checks via `ProjectPermissionEvaluationService`
- Outbox pattern for event publishing (Kafka integration)
- Comprehensive test coverage for implemented features

### Technical Debt
1. No delete operation for Projects (UC-PM-005)
2. Workflow Scheme not implemented (blocks multi-project workflow reuse)
3. Custom Fields not implemented (blocks flexible data model)
4. Permission runtime not implemented (authorization model incomplete)

### Dependencies
- **Phase 2 depends on Phase 1**: Custom fields need project components/versions
- **Phase 3 independent**: Can be developed in parallel with Phase 2
- **Phase 4 depends on Phase 1**: Sprint/ranking features need components/versions

### Testing Strategy
- Unit tests: JUnit 5 + Mockito (existing pattern)
- Integration tests: Spring Boot Test + Testcontainers
- Current test coverage: ~80% for implemented features
- Target coverage: 85%+ for new features

---

**Document maintained by:** PM Core Development Team  
**Review frequency:** Weekly during active development  
**Last review:** 2026-05-03
