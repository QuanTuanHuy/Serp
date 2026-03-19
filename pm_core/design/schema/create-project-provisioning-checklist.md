# Create Project Provisioning Checklist

Source of truth: `pm_core/design/schema/00_project_provisioning.md`

## Phase 1 - Foundation

- [x] `CP-01` Replace old association contract with `ProvisioningMode`
- [x] `CP-02` Add typed provisioning request/result objects
- [x] `CP-03` Refactor `CreateProjectCommand` to create shell project, call provisioning, then persist effective bindings
- [~] `CP-04` Start normalizing provisioning error handling and logs

## Phase 2 - Source Resolution

- [x] `CP-05` Implement `explicit override -> blueprint default -> tenant default/shared default` resolution
- [x] `CP-06` Add tenant-default source support for all scheme families
- [x] `CP-07` Enforce tenant materialization for system-owned scheme sources

## Phase 3 - Missing Scheme Family Trees

- [x] `CP-08` Add full `FIELD_CONFIG` child-tree infra and provisioning
- [x] `CP-09` Add full `SCREEN` family infra and provisioning
- [x] `CP-10` Add full `PERMISSION` family infra and provisioning
- [x] `CP-11` Add full `ISSUE_SECURITY` family infra and provisioning
- [x] `CP-12` Add full `NOTIFICATION` family infra and provisioning

## Phase 4 - Align Existing Provisioners

- [x] `CP-13` Stop cloning reusable `issue_types`
- [x] `CP-14` Stop cloning reusable `priorities`
- [x] `CP-15` Stop cloning reusable `statuses` and `status_categories`
- [~] `CP-16` Apply per-family behavior for `TEMPLATE_DEFAULT` vs `SHARED_FROM_EXISTING`

## Phase 5 - Workflow Model Alignment

- [x] `CP-17` Add workflow versioning model (`workflow_versions`, published/draft pointers)
- [x] `CP-18` Make workflow provisioning version-aware and bind only published workflows

## Phase 6 - Compatibility Validation

- [ ] `CP-19` Add `ProjectSchemeCompatibilityValidator` orchestration layer
- [ ] `CP-20` Implement workflow coverage/publication/initial-step gates
- [ ] `CP-21` Implement field-config/screen/transition-screen gates
- [ ] `CP-22` Implement context-conflict/default-id/global-reference gates

## Phase 7 - Full Integration

- [ ] `CP-23` Complete `TEMPLATE_DEFAULT` create-project path
- [ ] `CP-24` Complete `SHARED_FROM_EXISTING` create-project path
- [ ] `CP-25` Extract reusable `CLONE_FROM_SHARED` building blocks for future rebinding

## Phase 8 - Testing

- [ ] `CP-26` Add unit tests for source resolution and provisioners
- [ ] `CP-27` Add integration tests for create-project provisioning paths
- [ ] `CP-28` Add regression tests ensuring reusable global entities are never cloned

## Phase 1 Notes

- Current scope: establish the typed provisioning contract and wire create-project through it safely.
- Done: create-project now routes through `ISchemeProvisioningService` using `ProvisioningMode` and typed provisioning DTOs.
- Done: source resolution now supports explicit overrides, blueprint defaults, and `tenant_scheme_defaults` with tenant-over-system precedence.
- Done: system-owned sources are no longer bound directly; supported families materialize via mappings, and unsupported families require a pre-materialized tenant mapping.
- Done: `FIELD_CONFIG`, `SCREEN`, `PERMISSION`, `ISSUE_SECURITY`, and `NOTIFICATION` now have provisioning trees and infrastructure roots/children required for create-project provisioning.
- Done: reusable `issue_types`, `priorities`, `statuses`, and `status_categories` are no longer cloned per project; the service now reuses tenant-local dictionaries when present, otherwise materializes them once into tenant scope from system-owned seeds.
- Done: workflow roots now carry `currentPublishedVersionId` and `draftVersionId`, and workflow provisioning clones version trees so runtime resolves through published workflow versions instead of mutable draft data.
- Transitional limitation: full Jira-parity family behavior, compatibility gates, and full exception unification will follow in later phases.
