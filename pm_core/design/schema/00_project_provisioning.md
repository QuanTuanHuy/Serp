# Module 00: Project Provisioning & Scheme Materialization

**Design Philosophy:** Jira company-managed parity is not a single "shared-by-default" rule. Template-based project creation usually materializes project-scoped scheme rows for core work configuration, while explicit shared configuration reuses schemes from an existing project. Across both paths, reusable dictionaries such as issue types, statuses, priorities, and custom fields stay shared references rather than cloned copies. Workflow drafts never become project-effective until published, and any rebinding that changes active behavior must be validated and applied transactionally.

## Shared Base Columns (applies to optional helper tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## Provisioning Goals

1. Match Jira company-managed behavior where template-based creation and shared-configuration creation are distinct provisioning paths.
2. Keep blueprint and system defaults reusable as source templates for new project setup.
3. Keep reusable dictionaries shared across schemes/projects instead of cloning them per project.
4. Materialize project-scoped scheme rows only for the families that need isolated project behavior.
5. Enforce compatibility validation when associating, materializing, cloning, or re-associating schemes.
6. Keep project creation and scheme binding updates atomic with full rollback on failure.
7. Treat workflow publication and workflow-scheme migration as explicit lifecycle events, not implicit config edits.

## Provisioning Inputs

- `project` creation payload (name/key/type/lead/etc.)
- optional `blueprint_id`
- optional explicit scheme overrides by type
- optional `provisioning_mode` (`TEMPLATE_DEFAULT`, `SHARED_FROM_EXISTING`)
- optional per-scheme strategy overrides when a blueprint needs behavior different from the family default

Resolution precedence for each `scheme_type`:

1. Explicit override from request
2. Blueprint default from `blueprint_scheme_defaults`
3. Tenant system default template or tenant shared default, depending on scheme family

## Reusable Global Entities (never project-cloned by default)

These entities remain reusable tenant/site dictionaries even when scheme rows are project-scoped:

- `issue_types`
- `statuses`
- `status_categories`
- `priorities`
- `custom_fields`

Project isolation in Jira-like company-managed setup is achieved at the scheme/context layer, not by copying these dictionaries per project. Renaming or changing one of these reusable entities can affect every workflow, scheme, or project that references it.

For system-owned seeds, tenant provisioning should materialize these dictionary rows into tenant scope once when needed, then reuse the tenant-local rows across all future projects and schemes. They should not be recreated per project.

## Provisioning Paths (Jira Parity)

1. `TEMPLATE_DEFAULT` (default)
   - Create a company-managed project from a template/blueprint without shared configuration from an existing project.
   - Materialize project-scoped scheme rows for the families that Jira typically isolates per project.
   - Reuse tenant shared/default schemes for families that are commonly shared across spaces.
2. `SHARED_FROM_EXISTING` (opt-in)
   - Create a project with shared configuration from an existing project or reusable tenant scheme.
   - Project scheme columns point directly to existing reusable scheme rows.
   - Updating a shared scheme can affect all projects associated with it.
3. `CLONE_FROM_SHARED` (optional rebinding action)
   - Use when a project currently points to a reusable shared scheme but now needs local isolation.
   - Clone only scheme trees and project-scoped contexts; keep reusable global entities as references.

## System Scheme Materialization (Tenant Isolation)

When a resolved source artifact belongs to system tenant (`tenant_id=0`), tenant projects should not bind directly to mutable system-owned rows.

Recommended strategy:

1. For reusable shared schemes, check if tenant already has a shared materialized copy for `(scheme_type, source_scheme_id)`.
2. If yes, bind project to that tenant copy.
3. If no, clone the system scheme once into tenant scope, persist mapping, then bind project.
4. For project-scoped families, materialize the needed rows directly into tenant/project scope during provisioning and keep lineage metadata.

Suggested helper table:

- `tenant_scheme_mappings(tenant_id, scheme_type, source_scheme_id, tenant_scheme_id, ...)`
- `tenant_scheme_defaults(tenant_id, scheme_type, scheme_id, ...)`

This allows tenants to customize both their resolved default source schemes and their shared materialized copies without impacting other tenants.

## Scheme Families Covered

| scheme_type | Scheme root table | Project binding column | Typical template-based default |
|---|---|---|---|
| ISSUE_TYPE | issue_type_schemes | projects.issue_type_scheme_id | Materialize project-scoped scheme/items, reuse global `issue_types` |
| WORKFLOW | workflow_schemes | projects.workflow_scheme_id | Materialize project-scoped workflows/schemes, reuse global `statuses` + `status_categories` |
| FIELD_CONFIG | field_config_schemes | projects.field_config_scheme_id | Materialize project-scoped field config family |
| SCREEN | issue_type_screen_schemes | projects.issue_type_screen_scheme_id | Materialize project-scoped screen family |
| PERMISSION | permission_schemes | projects.permission_scheme_id | Bind tenant shared/default scheme unless blueprint requires a dedicated copy |
| ISSUE_SECURITY | issue_security_schemes | projects.issue_security_scheme_id | Bind tenant shared/default scheme unless blueprint requires a dedicated copy |
| NOTIFICATION | notification_schemes | projects.notification_scheme_id | Bind tenant shared/default notification scheme |
| PRIORITY | priority_schemes | projects.priority_scheme_id | Bind tenant shared/default priority scheme |

There is no single universal provisioning default across all scheme families.

## Default Template Provisioning Flow (`TEMPLATE_DEFAULT`)

All steps run in one DB transaction.

1. Create `projects` row first (scheme columns nullable until validation passes).
2. Resolve source templates/schemes by precedence (override -> blueprint default -> tenant default).
3. Materialize project-scoped families:
   - ISSUE_TYPE: `issue_type_schemes`, `issue_type_scheme_items`; keep `issue_type_id` references to reusable `issue_types`
   - WORKFLOW: `workflows`, `workflow_versions`, `workflow_steps`, `workflow_transitions`, `workflow_transition_rules`, `workflow_schemes`, `workflow_scheme_items`; keep `status_id` references to reusable `statuses`
   - FIELD_CONFIG: `field_configurations`, `field_configuration_items`, `field_config_schemes`, `field_config_scheme_items`
   - SCREEN: `screens`, `screen_tabs`, `screen_tab_fields`, `screen_schemes`, `screen_scheme_items`, `issue_type_screen_schemes`, `issue_type_screen_scheme_items`
4. Bind shared/default families:
   - PRIORITY and NOTIFICATION normally bind tenant shared/default schemes
   - PERMISSION and ISSUE_SECURITY may bind tenant shared/default schemes unless the blueprint explicitly requires dedicated copies
5. Resolve each mapped workflow root to its `current_published_version_id`; drafts never bind directly to projects.
6. Validate cross-scheme compatibility gates against the effective IDs.
7. Update scheme columns on `projects` with the effective IDs.
8. Commit transaction, then publish `PROJECT_CREATED`.

## Explicit Shared Configuration Flow (`SHARED_FROM_EXISTING`)

Use this when the admin intentionally creates a project with shared configuration from an existing project or existing reusable scheme.

1. Create `projects` row first (scheme columns nullable until finalized).
2. Resolve target reusable scheme IDs by precedence (explicit shared source -> blueprint default -> tenant shared default).
3. For system-owned reusable sources, materialize one tenant-scoped shared copy first if needed.
4. Resolve each mapped workflow root to its `current_published_version_id`; drafts never bind directly to projects.
5. Validate cross-scheme compatibility gates.
6. Update scheme columns on `projects` with the resolved shared IDs.
7. Commit transaction, then publish `PROJECT_CREATED`.

## Optional Isolation Flow (`CLONE_FROM_SHARED`)

Use this only when a project is currently using reusable shared schemes and explicit local isolation is required.

1. Create `projects` row first (scheme columns nullable until finalized).
2. Clone ISSUE_TYPE scheme tree only:
   - `issue_type_schemes`
   - `issue_type_scheme_items`
   - patch `issue_type_schemes.default_issue_type_id`
   - keep `issue_type_id` references pointed at reusable `issue_types`
3. Clone PRIORITY scheme tree only if a dedicated priority set is explicitly required:
   - `priority_schemes`
   - `priority_scheme_items`
   - patch `priority_schemes.default_priority_id`
   - keep `priority_id` references pointed at reusable `priorities`
4. Clone SCREEN tree:
   - `screens`
   - `screen_tabs`
   - `screen_tab_fields`
   - `screen_schemes`
   - `screen_scheme_items`
   - `issue_type_screen_schemes`
   - `issue_type_screen_scheme_items` (reuse `issue_type_id` references)
5. Clone WORKFLOW tree:
   - `workflows`
   - `workflow_versions`
   - `workflow_steps`
   - keep `workflow_steps.status_id` references pointed at reusable `statuses`
   - `workflow_transitions` (map `screen_id` to cloned screen ids)
   - `workflow_transition_rules`
   - `workflow_schemes`
   - `workflow_scheme_items` (reuse `issue_type_id`, map `workflow_id`)
   - patch `workflows.current_published_version_id` and `workflows.draft_version_id`
6. Clone FIELD_CONFIG tree:
   - `field_configurations`
   - `field_configuration_items`
   - `field_config_schemes`
   - `field_config_scheme_items` (reuse `issue_type_id` references)
7. If isolation must also cover project-specific field contexts, clone matching custom-field context trees:
   - `custom_field_contexts`
   - `custom_field_context_projects`
   - `custom_field_context_issue_types` (reuse `issue_type_id` references)
   - `custom_field_options`
   - `custom_field_context_default_values`
   - patch `project_id`; keep `custom_field_id` and `issue_type_id` as reusable references unless a deeper field-model fork is explicitly required
8. Clone PERMISSION tree:
   - `permission_schemes`
   - `permission_scheme_entries`
9. Clone ISSUE_SECURITY tree (2-pass):
   - insert `issue_security_schemes` with `default_level_id=NULL`
   - clone `issue_security_levels`
   - clone `issue_security_level_members`
   - patch `issue_security_schemes.default_level_id`
10. Clone NOTIFICATION tree:
   - `notification_schemes`
   - `notification_scheme_entries`
   - keep `notification_events` tenant-shared unless project-specific events are required
11. Update scheme columns on `projects` with cloned IDs.
12. Validate cross-scheme compatibility gates.
13. Commit transaction, then publish `PROJECT_CREATED`.

Do not clone `issue_types`, `priorities`, `statuses`, `status_categories`, or `custom_fields` as part of normal project isolation. Jira-like isolation happens by cloning scheme/context rows, not these reusable dictionaries.

## Required ID Mapping Contract (`CLONE_FROM_SHARED` only)

Maintain in-memory maps for every cloned root/child ID used by FK remapping:

- `issue_type_scheme_map`, `priority_scheme_map`
- `screen_map`, `screen_scheme_map`, `issue_type_screen_scheme_map`
- `workflow_map`, `workflow_version_map`, `workflow_step_map`, `workflow_transition_map`, `workflow_scheme_map`
- `field_configuration_map`, `field_config_scheme_map`
- `custom_field_context_map`, `custom_field_option_map`, `custom_field_context_default_map`
- `permission_scheme_map`
- `issue_security_scheme_map`, `issue_security_level_map`
- `notification_scheme_map`

Never insert child records with source IDs.

## Compatibility Gates Before Commit

1. Workflow coverage: every issue type in the effective issue type scheme has a workflow mapping.
2. Workflow publication: every mapped workflow root has one valid `current_published_version_id`.
3. Workflow initial step: each published workflow version has exactly one `is_initial=true` step.
4. Field config coverage: every issue type in the effective issue type scheme has a field configuration mapping.
5. Screen coverage: every issue type in the effective issue type scheme has an issue-type-to-screen-scheme mapping for CREATE, EDIT, and VIEW operations.
6. Transition screens: any `workflow_transitions.screen_id` must reference a valid screen in tenant scope.
7. Custom field context resolution: no field may have two effective contexts with the same specificity for the same `(project_id, issue_type_id)` pair.
8. Default IDs (`default_*_id`) must belong to the same target scheme.
9. Reusable global entity references (`issue_type_id`, `status_id`, `priority_id`, `custom_field_id`) must resolve inside the same tenant scope as the target project.

## Lifecycle Rules

1. Blueprint default updates affect only future project creation unless explicit rebinding is performed.
2. Template-based company-managed creation does not imply one universal shared default across all scheme families.
3. Creating a project with shared configuration from an existing project is explicit opt-in.
4. Changes to reusable global entities (`issue_types`, `statuses`, `status_categories`, `priorities`, `custom_fields`) can affect multiple projects even when scheme rows are isolated.
5. Changes to shared schemes propagate to all associated projects.
6. Changes to materialized/cloned project-scoped schemes remain local to that project.
7. Project scheme rebinding must validate compatibility and apply atomically.
8. Rebinding from shared to project-scoped (or project-scoped to shared) must preserve current work item integrity and migration constraints.
9. Workflow draft publication and workflow-scheme rebinding may require explicit status/step migration plans for in-flight work items.

## Optional Metadata for Traceability

If lineage tracing is required, add metadata fields on scheme root tables:

- `source_scheme_id BIGINT NULL`
- `provisioning_mode VARCHAR(40)` (`TEMPLATE_DEFAULT`, `SHARED_FROM_EXISTING`, `CLONE_FROM_SHARED`, `PROJECT_REBIND`)
- `provision_source_type VARCHAR(20)` (`SYSTEM_DEFAULT`, `BLUEPRINT`, `EXPLICIT_OVERRIDE`, `PROJECT_REBIND`)
- `provision_source_ref_id BIGINT NULL` (e.g. blueprint ID)

These fields are optional but strongly recommended for debugging and audit.
