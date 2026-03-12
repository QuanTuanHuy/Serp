# PM Core Jira-Closer Parity Gap Matrix and Plan

**Updated:** 2026-03-04  
**Parity profile:** Jira company-managed style (shared schemes by default, optional clone-on-associate)

## 1) Scope and Baseline

- Design source: `pm_core/design/PM_ERD_INDEX.md`, `pm_core/design/schema/*.md`, `pm_core/design/PM_USECASE_SPEC.md`
- Current DB migrations: `V1` to `V4` only
- Current REST controllers: `ProjectController`, `WorkItemController`
- Current provisioning behavior in code: deep clone for `ISSUE_TYPE`, `PRIORITY`, `WORKFLOW`; stubbed for `FIELD_CONFIG`, `SCREEN`, `PERMISSION`, `ISSUE_SECURITY`, `NOTIFICATION`

## 2) Doc-First Correction Pass

Completed updates in this iteration:

| Item | Status | Files |
|---|---|---|
| Switch default parity from clone-by-default to shared-association-by-default | DONE | `PM_ERD_INDEX.md`, `schema/00_project_provisioning.md`, `schema/01_projects.md`, `schema/02_issues.md`, `schema/03_workflows.md`, `schema/04_fields_screens.md`, `schema/05_permissions_security.md`, `schema/06_notifications.md` |
| Align use case metadata with actual service stack (Java/Spring) | DONE | `PM_USECASE_SPEC.md` |
| Standardize terminology notes (`Work Item` = Jira `Issue`) and parity profile | DONE | `PM_USECASE_SPEC.md` |
| Update scheme binding narratives in UC-PM-001, UC-PM-007, UC-PM-021 | DONE | `PM_USECASE_SPEC.md` |
| Align ERD index wording with scheme association model | DONE | `PM_ERD_INDEX.md` |

Follow-up doc hardening still recommended:

1. Add explicit admin warnings where shared scheme updates affect all associated projects.
2. Add workflow migration behavior notes when statuses are removed or remapped.
3. Add documented limits/guardrails for field configs and issue type mappings (Jira-like operational limits).

## 3) Design vs Current Implementation Gap Matrix

### 3.1 Database Gap Matrix

| Domain | Expected by design | Present in V1-V4 | Gap |
|---|---|---|---|
| Project core and provisioning | `projects`, `project_categories`, `project_blueprints`, `blueprint_scheme_defaults`, `project_issue_counters` | `blueprint_scheme_defaults`: NO, `projects`: NO, others: NO | CRITICAL |
| Work item core | `work_items`, `resolutions`, `issue_link_types`, `issue_links`, `worklogs` | All NO | CRITICAL |
| Work item relation tables | `work_item_components`, `work_item_fix_versions`, `work_item_sprints`, `work_item_custom_field_values` | All NO | HIGH |
| Workflow foundation | `workflows`, `workflow_steps`, `workflow_transitions`, `workflow_transition_rules`, `workflow_schemes`, `workflow_scheme_items`, `statuses`, `status_categories` | First six YES (V2), `statuses`/`status_categories` NO | HIGH |
| Fields and screens | `custom_fields`, `custom_field_options`, `custom_field_contexts`, `field_configurations`, `field_config_schemes`, `screens`, `screen_schemes`, `issue_type_screen_schemes` and items | All NO | HIGH |
| Permissions and issue security | `permission_definitions`, `permission_schemes`, `permission_scheme_entries`, `issue_security_schemes`, `issue_security_levels`, `issue_security_level_members` | All NO | HIGH |
| Notifications | `notification_schemes`, `notification_events`, `notification_templates`, `notification_scheme_entries`, delivery logs | All NO (only generic `outbox_events` exists) | MEDIUM |
| Agile/search/collaboration | Module 07/08/09 tables | All NO | MEDIUM |

### 3.2 Schema/Code Contract Mismatches

| Area | Current code/migration state | Design state | Action |
|---|---|---|---|
| Status category FK naming | `statuses.category_id` in model | `statuses.status_category_id` in design | Pick one canonical name and align model + migration + docs together |
| Workflow step order column | `workflow_steps.sequence` in V2/model | `workflow_steps.step_order` in design | Keep `sequence` or migrate to `step_order`; document final convention |
| Transition screen binding | `workflow_transitions.screen_id` not in V2 | Present in design | Add column in new migration before transition screen APIs |
| Transition rule config type | `workflow_transition_rules.config_json` is `TEXT` (V2) | `JSONB` in design | Migrate to `JSONB` for validator/condition flexibility |

### 3.3 Runtime Risk Notes

1. `spring.jpa.hibernate.ddl-auto` is `none`, so missing migrations are runtime blockers.
2. `ProjectModel`, `WorkItemModel`, and `StatusModel` depend on tables not created by current migrations.
3. Work item search query depends on `work_item_sprints`, `work_item_components`, `work_item_fix_versions` tables, which are not migrated yet.

## 4) Ordered DB Migration Checklist (V5+)

### V5 - Project + Work Item Runtime Foundation (must-have first)

1. Create `project_categories`, `project_blueprints`, `blueprint_scheme_defaults`.
2. Create `projects` with scheme binding columns.
3. Create `project_issue_counters` with `(tenant_id, project_id)` uniqueness.
4. Create `status_categories` and `statuses`.
5. Create `work_items` with tenant-safe FKs to project/scheme dictionaries.
6. Create `resolutions`, `issue_link_types`, `issue_links`, `worklogs`.

### V6 - Existing Scheme/Workflow Hardening

1. Add missing tenant-safe FKs and unique constraints to V1/V2 tables.
2. Add `workflow_transitions.screen_id`.
3. Convert `workflow_transition_rules.config_json` from `TEXT` to `JSONB`.
4. Resolve naming decision: `sequence` vs `step_order`, `category_id` vs `status_category_id`.

### V7 - Fields and Screens Chain

1. Create `custom_fields`, `custom_field_options`, `custom_field_contexts`.
2. Create `field_configurations`, `field_configuration_items`, `field_config_schemes`, `field_config_scheme_items`.
3. Create `screens`, `screen_tabs`, `screen_tab_fields`, `screen_schemes`, `screen_scheme_items`, `issue_type_screen_schemes`, `issue_type_screen_scheme_items`.

### V8 - Permission + Issue Security

1. Create `permission_definitions` (seeded catalog), `permission_schemes`, `permission_scheme_entries`.
2. Create `issue_security_schemes`, `issue_security_levels`, `issue_security_level_members`.
3. Add `work_items.security_level_id` FK once issue security tables exist.

### V9 - Notification Schemes

1. Create `notification_schemes`, `notification_events`, `notification_templates`, `notification_scheme_entries`.
2. Keep existing generic outbox tables; integrate via event mapping instead of duplicating outbox mechanics.

### V10 - Agile Core

1. Create boards/sprints model (`boards`, `board_columns`, `board_column_statuses`, `sprints`).
2. Create issue-sprint relation (`work_item_sprints`) and quick filters.

### V11 - Search/Filter Reporting

1. Create saved filter and sharing model (`search_requests`, `share_permissions`, favorites/subscriptions).
2. Create dashboards/gadgets tables.

### V12 - Collaboration and Audit

1. Create comments, attachments, watchers, change history, and audit event tables.
2. Add indexes for timeline queries and audit trails.

## 5) API Checklist (Implemented / Missing / Mismatch)

| API family | Implemented now | Missing / not exposed | Mismatch / dependency |
|---|---|---|---|
| Projects | `POST /projects`, `GET /projects/{id}`, `GET /projects/key/{key}`, list, update, delete, archive/unarchive, `PUT /projects/{id}/schemes` (shared mode, partial scheme coverage) | categories/blueprints/components/versions/roles APIs | Scheme update currently validates/updates ISSUE_TYPE + WORKFLOW + PRIORITY; other scheme families are intentionally blocked until their infrastructure is added |
| Work items | `POST /work-items` | get/list/update/delete/transition/assign/rank/bulk/clone/links/worklogs/components/fix versions/sprints | Route style mismatch vs spec (`/projects/{projectId}/work-items` expected in spec) |
| Workflow admin | none | statuses, workflows, transitions, transition rules, publish/validate/clone endpoints | Workflow transitions tables exist but no adapter/controller path |
| Issue type/priority/resolution/link type admin | none | full CRUD + scheme item management | Partial table availability only |
| Fields/screens admin | none | full module APIs | Tables absent |
| Permission/security admin | none | full module APIs | Tables absent |
| Notifications/agile/search/collaboration | none | full module APIs | Tables absent |

## 6) Recommended Implementation Sequence (Jira-Closer Parity)

1. **Lock docs and contracts**
   - Finish the remaining parity warning notes and naming decisions (`category_id` vs `status_category_id`, `sequence` vs `step_order`).
2. **Ship V5 and V6 migrations first**
   - Unblock current endpoints and provisioning paths under `ddl-auto: none`.
3. **Close core issue lifecycle APIs**
   - Add work item get/list/update/delete and transition APIs before advanced modules.
4. **Implement scheme association management**
   - Add project scheme rebinding endpoint with compatibility checks and optional clone mode.
5. **Enforce permissions and issue security in read/write paths**
   - Add authorization checks to browse/edit/transition/comment scopes.
6. **Add fields/screens and notifications**
   - Complete form behavior parity and event-recipient policies.
7. **Finish agile/search/collaboration modules**
   - Boards/sprints, saved filters, dashboards, comments, watchers, and changelog.

## 7) Immediate Next Slice (recommended)

1. Expand project scheme rebinding from partial support to full scheme families (FIELD_CONFIG, SCREEN, PERMISSION, NOTIFICATION, ISSUE_SECURITY).
2. Implement clone-on-associate mode for scheme rebinding.
3. Add work item update/delete/transition APIs to complete core lifecycle parity.
