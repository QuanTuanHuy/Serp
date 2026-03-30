# PM Core Project Access Flow Hardening Plan

Date: 2026-03-30
Scope: create project flow, role actor management flow, project permission evaluator

## Objective

Harden and complete the Jira-aligned project access flow so that:

1. New projects always bootstrap a usable admin access path.
2. Role actor management uses consistent authorization and validation rules.
3. Permission evaluation handles seeded and schema-supported grantee types robustly.

## Current Baseline

- System roles and default software permission scheme are seeded in `V10__seed_permission_catalog_and_software_roles.sql`.
- Create project already auto-assigns lead user to `Administrators` role.
- Add/remove/list role actor handlers already check `ADMINISTER_PROJECTS`.
- `ProjectPermissionEvaluationService` supports core grant types but has gaps around robustness and role-name collision handling.

## Phase Breakdown

### Phase 1 - Permission Evaluator Hardening (current implementation target)

#### Goals

- Handle all grantee types declared by schema safely.
- Resolve `PROJECT_ROLE` grants deterministically when both tenant and system roles share the same name.
- Improve context normalization and observability of denied decisions.
- Add dedicated unit tests for evaluator behavior.

#### Changes

1. Extend `ProjectPermissionGranteeType` to include schema-declared types:
   - `APPLICATION_ACCESS`
   - `ANYONE_ON_WEB`
   - `USER_CUSTOM_FIELD_VALUE`
   - `GROUP_CUSTOM_FIELD_VALUE`
2. Update parser in `ProjectPermissionEvaluationService` for the above values.
3. Replace single-role lookup by name with list lookup for `PROJECT_ROLE` grants:
   - check all matching roles in tenant/system scope
   - grant when any role has matching active assignment
4. Normalize and safely handle group keys during permission matching.
5. Add debug logging paths for unsupported and unmatched grant scenarios.
6. Add unit test class for evaluator:
   - user/group/project-lead/reporter/assignee grants
   - project-role grant with multiple roles sharing same name
   - fallback behavior when project has no permission scheme
   - unsupported contextual grantee behavior returns false

#### Deliverables

- `ProjectPermissionEvaluationService` hardened
- Role service/port support for role-name list lookup
- New test suite for evaluator

### Phase 2 - Create Project Access Bootstrap Completion

#### Goals

- Guarantee post-create admin path is valid for project lead.
- Keep flow idempotent and observable.

#### Changes

1. Keep lead -> `Administrators` assignment idempotent after final scheme binding.
2. Add guard assertion: created project lead must satisfy `ADMINISTER_PROJECTS`.
3. Emit `PROJECT_CREATED` outbox event in same transaction.
4. Add unit tests for bootstrap and post-create permission.

### Phase 3 - Role Actor Management Completion

#### Goals

- Enforce consistent validation and domain rules for actor assignment.

#### Changes

1. Add command-level validation service for `AddProjectRoleActor`.
2. Validate subject existence by subject type (user/group/service account).
3. Align archived-project behavior for add/remove/list role actor operations.
4. Emit role actor domain events (`ROLE_ACTOR_ADDED`, `ROLE_ACTOR_REMOVED`) via outbox.
5. Add/expand tests for validation and permission-denied scenarios.

### Phase 4 - Migration and Operational Guardrails

#### Goals

- Ensure data and runtime consistency across existing environments.

#### Changes

1. Add forward-only migration for any required index/seed adjustments.
2. Add startup/health checks for mandatory seeded access artifacts.
3. Validate backward compatibility in existing tenants.

## Test Strategy

- Targeted unit tests first, then handler-level tests.
- Minimum per phase:
  - Phase 1: evaluator tests
  - Phase 2: create-project handler tests
  - Phase 3: role actor handler tests
- Validate with:
  - `./mvnw.cmd -Dtest=ProjectPermissionEvaluationServiceTest test`
  - `./mvnw.cmd -Dtest=CreateProjectCommandHandlerTest,ProjectRoleActorHandlersTest test`
  - `./mvnw.cmd clean compile`

## Risks and Notes

- `APPLICATION_ACCESS` and custom-field grantee types require richer auth/context input than current context object; phase 1 handles them safely without false grants.
- Role-name collision between tenant and system roles is expected; evaluator must not depend on first-match semantics.
- Keep all changes tenant-scoped and non-destructive.
