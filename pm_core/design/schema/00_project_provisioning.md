# Module 00: Project Provisioning & Scheme Cloning

**Design Philosophy:** Reuse configuration templates at provisioning time, then isolate each project through project-owned scheme clones.

## Shared Base Columns (applies to optional helper tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## Provisioning Goals

1. Prevent configuration changes in project A from impacting project B.
2. Keep blueprint and system defaults reusable as source templates.
3. Preserve FK integrity when cloning multi-level scheme graphs.
4. Keep provisioning atomic with full rollback on failure.

## Provisioning Inputs

- `project` creation payload (name/key/type/lead/etc.)
- optional `blueprint_id`
- optional explicit scheme overrides by type

Resolution precedence for each `scheme_type`:

1. Explicit override from request
2. Blueprint default from `blueprint_scheme_defaults`
3. Tenant system default template

## Scheme Types Covered

| scheme_type | Template root table | Project binding column |
|---|---|---|
| ISSUE_TYPE | issue_type_schemes | projects.issue_type_scheme_id |
| WORKFLOW | workflow_schemes | projects.workflow_scheme_id |
| FIELD_CONFIG | field_config_schemes | projects.field_config_scheme_id |
| SCREEN | issue_type_screen_schemes | projects.issue_type_screen_scheme_id |
| PERMISSION | permission_schemes | projects.permission_scheme_id |
| ISSUE_SECURITY | issue_security_schemes | projects.issue_security_scheme_id |
| NOTIFICATION | notification_schemes | projects.notification_scheme_id |
| PRIORITY | priority_schemes | projects.priority_scheme_id |

## Deep Clone Order (FK-safe)

All steps run in one DB transaction.

1. Create `projects` row first (minimal fields, scheme columns nullable until finalized).
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
   - keep `notification_events` shared by tenant unless project-specific events are required
10. Update scheme columns on `projects` with cloned ids.
11. Validate cross-scheme compatibility gates.
12. Commit transaction, then publish `PROJECT_CREATED`.

## Required ID Mapping Contract

Maintain in-memory maps for every cloned root/child id used by FK remapping:

- `issue_type_map`, `priority_map`
- `screen_map`, `screen_scheme_map`, `issue_type_screen_scheme_map`
- `status_category_map`, `status_map`, `workflow_map`, `workflow_transition_map`, `workflow_scheme_map`
- `field_configuration_map`, `field_config_scheme_map`
- `permission_scheme_map`
- `issue_security_scheme_map`, `issue_security_level_map`
- `notification_scheme_map`

Never insert child records with source ids.

## Compatibility Gates Before Commit

1. Workflow coverage: every issue type in cloned issue type scheme must have a workflow mapping.
2. Field config coverage: every issue type in cloned issue type scheme must have a field configuration mapping.
3. Screen coverage: every issue type in cloned issue type scheme must have an issue-type-to-screen-scheme mapping.
4. Workflow initial status: each mapped workflow has exactly one `is_initial=true` step.
5. Default ids (`default_*_id`) must belong to the same cloned scheme.

## Isolation and Lifecycle Rules

1. Blueprint updates affect only future project provisioning.
2. Existing projects keep their cloned schemes unless explicitly re-provisioned.
3. Project scheme rebinding should use clone-and-swap (clone candidate template, validate, replace project binding atomically).

## Optional Metadata for Traceability

If lineage tracing is required, add metadata fields on scheme root tables:

- `source_scheme_id BIGINT NULL`
- `provision_source_type VARCHAR(20)` (`SYSTEM_DEFAULT`, `BLUEPRINT`, `EXPLICIT_OVERRIDE`, `PROJECT_REBIND`)
- `provision_source_ref_id BIGINT NULL` (e.g., blueprint id)

These fields are optional but strongly recommended for debugging and audit.
