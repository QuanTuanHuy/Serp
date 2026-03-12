# Module 00: Project Provisioning & Scheme Association

**Design Philosophy:** Jira-closer parity for company-managed projects: associate shared schemes by default, with optional clone-on-associate mode for tenants that require project-level isolation.

## Shared Base Columns (applies to optional helper tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## Provisioning Goals

1. Match Jira company-managed behavior where schemes are reusable across multiple projects.
2. Keep blueprint and system defaults reusable as source templates for new project setup.
3. Enforce compatibility validation when associating or re-associating schemes.
4. Keep project creation and scheme binding updates atomic with full rollback on failure.
5. Support optional isolation via cloning without making it the default behavior.

## Provisioning Inputs

- `project` creation payload (name/key/type/lead/etc.)
- optional `blueprint_id`
- optional explicit scheme overrides by type
- optional `association_mode` (`SHARED_ASSOCIATION`, `CLONE_ON_ASSOCIATE`)

Resolution precedence for each `scheme_type`:

1. Explicit override from request
2. Blueprint default from `blueprint_scheme_defaults`
3. Tenant system default scheme

## Association Modes (Jira Parity)

1. `SHARED_ASSOCIATION` (default)
   - Project scheme columns point directly to existing scheme rows.
   - Updating a shared scheme can affect all projects associated with it.
2. `CLONE_ON_ASSOCIATE` (optional)
   - Candidate schemes are deep-cloned before binding to the project.
   - Changes to cloned schemes stay local to that project.

## System Scheme Materialization (Tenant Isolation)

When a resolved source scheme belongs to system tenant (`tenant_id=0`), project provisioning should not bind tenant projects directly to mutable system-owned rows.

Recommended strategy for shared mode:

1. Check if tenant already has a shared materialized copy for `(scheme_type, source_scheme_id)`.
2. If yes, bind project to that tenant copy.
3. If no, clone system scheme once into tenant scope, persist mapping, then bind project.

Suggested helper table:

- `tenant_scheme_mappings(tenant_id, scheme_type, source_scheme_id, tenant_scheme_id, ...)`

This allows tenants to customize their own shared schemes without impacting other tenants.

## Scheme Types Covered

| scheme_type | Scheme root table | Project binding column |
|---|---|---|
| ISSUE_TYPE | issue_type_schemes | projects.issue_type_scheme_id |
| WORKFLOW | workflow_schemes | projects.workflow_scheme_id |
| FIELD_CONFIG | field_config_schemes | projects.field_config_scheme_id |
| SCREEN | issue_type_screen_schemes | projects.issue_type_screen_scheme_id |
| PERMISSION | permission_schemes | projects.permission_scheme_id |
| ISSUE_SECURITY | issue_security_schemes | projects.issue_security_scheme_id |
| NOTIFICATION | notification_schemes | projects.notification_scheme_id |
| PRIORITY | priority_schemes | projects.priority_scheme_id |

## Default Association Flow (SHARED_ASSOCIATION)

All steps run in one DB transaction.

1. Create `projects` row first (scheme columns nullable until validation passes).
2. Resolve target scheme IDs by precedence (override -> blueprint default -> system default).
3. Validate cross-scheme compatibility gates against resolved IDs.
4. Update scheme columns on `projects` with resolved IDs.
5. Commit transaction, then publish `PROJECT_CREATED`.

## Optional Clone Flow (CLONE_ON_ASSOCIATE)

Use this only when explicit isolation is required.

1. Create `projects` row first (scheme columns nullable until finalized).
2. Clone ISSUE_TYPE tree:
   - `issue_types`
   - `issue_type_schemes`
   - `issue_type_scheme_items`
   - patch `issue_type_schemes.default_issue_type_id`
3. Clone PRIORITY tree:
   - `priorities`
   - `priority_schemes`
   - `priority_scheme_items`
   - patch `priority_schemes.default_priority_id`
4. Clone SCREEN tree:
   - `screens`
   - `screen_tabs`
   - `screen_tab_fields`
   - `screen_schemes`
   - `screen_scheme_items`
   - `issue_type_screen_schemes`
   - `issue_type_screen_scheme_items` (map `issue_type_id`)
5. Clone WORKFLOW tree:
   - `status_categories`
   - `statuses`
   - `workflows`
   - `workflow_steps`
   - `workflow_transitions` (map `screen_id` to cloned screen ids)
   - `workflow_transition_rules`
   - `workflow_schemes`
   - `workflow_scheme_items` (map `issue_type_id` + `workflow_id`)
6. Clone FIELD_CONFIG tree:
   - `field_configurations`
   - `field_configuration_items`
   - `field_config_schemes`
   - `field_config_scheme_items` (map `issue_type_id`)
7. Clone PERMISSION tree:
   - `permission_schemes`
   - `permission_scheme_entries`
8. Clone ISSUE_SECURITY tree (2-pass):
   - insert `issue_security_schemes` with `default_level_id=NULL`
   - clone `issue_security_levels`
   - clone `issue_security_level_members`
   - patch `issue_security_schemes.default_level_id`
9. Clone NOTIFICATION tree:
   - `notification_schemes`
   - `notification_scheme_entries`
   - keep `notification_events` tenant-shared unless project-specific events are required
10. Update scheme columns on `projects` with cloned IDs.
11. Validate cross-scheme compatibility gates.
12. Commit transaction, then publish `PROJECT_CREATED`.

## Required ID Mapping Contract (clone mode only)

Maintain in-memory maps for every cloned root/child ID used by FK remapping:

- `issue_type_map`, `priority_map`
- `screen_map`, `screen_scheme_map`, `issue_type_screen_scheme_map`
- `status_category_map`, `status_map`, `workflow_map`, `workflow_transition_map`, `workflow_scheme_map`
- `field_configuration_map`, `field_config_scheme_map`
- `permission_scheme_map`
- `issue_security_scheme_map`, `issue_security_level_map`
- `notification_scheme_map`

Never insert child records with source IDs.

## Compatibility Gates Before Commit

1. Workflow coverage: every issue type in the effective issue type scheme has a workflow mapping.
2. Field config coverage: every issue type in the effective issue type scheme has a field configuration mapping.
3. Screen coverage: every issue type in the effective issue type scheme has an issue-type-to-screen-scheme mapping.
4. Workflow initial status: each mapped workflow has exactly one `is_initial=true` step.
5. Default IDs (`default_*_id`) must belong to the same target scheme.

## Lifecycle Rules

1. Blueprint default updates affect only future project creation unless explicit rebinding is performed.
2. In `SHARED_ASSOCIATION`, scheme changes propagate to all associated projects.
3. In `CLONE_ON_ASSOCIATE`, scheme changes remain project-local.
4. Project scheme rebinding must validate compatibility and apply atomically.
5. Rebinding from shared to cloned (or cloned to shared) must preserve current work item integrity and migration constraints.

## Optional Metadata for Traceability

If lineage tracing is required, add metadata fields on scheme root tables:

- `source_scheme_id BIGINT NULL`
- `association_mode VARCHAR(30)` (`SHARED_ASSOCIATION`, `CLONE_ON_ASSOCIATE`)
- `provision_source_type VARCHAR(20)` (`SYSTEM_DEFAULT`, `BLUEPRINT`, `EXPLICIT_OVERRIDE`, `PROJECT_REBIND`)
- `provision_source_ref_id BIGINT NULL` (e.g., blueprint ID)

These fields are optional but strongly recommended for debugging and audit.
