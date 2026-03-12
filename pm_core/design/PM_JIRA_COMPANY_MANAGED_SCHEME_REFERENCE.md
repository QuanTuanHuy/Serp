# PM Core - Jira Company-Managed Scheme Design Reference

**Updated:** 2026-03-12  
**Audience:** PM Core backend/frontend engineers implementing Jira company-managed parity  
**Parity target:** Shared scheme association by default, optional clone-on-associate isolation

## 1) Purpose

This document is the implementation reference for Jira-like scheme behavior during project provisioning and scheme rebinding.

It provides:

1. Jira-native semantics for share vs copy/clone.
2. Step-by-step behavior per scheme family.
3. Mapping from Jira scheme families to PM Core project binding columns.
4. Concrete parity rules another agent can directly implement.

## 2) Screenshot-to-Model Mapping

Use this as the canonical mapping when implementing scheme binding endpoints and provisioning logic.

| Jira scheme family | Main Jira object | PM Core binding column |
|---|---|---|
| Issue type scheme (Work type scheme) | `issue_type_schemes` | `projects.issue_type_scheme_id` |
| Workflow scheme | `workflow_schemes` | `projects.workflow_scheme_id` |
| Field configuration scheme | `field_config_schemes` | `projects.field_config_scheme_id` |
| Issue type screen scheme | `issue_type_screen_schemes` | `projects.issue_type_screen_scheme_id` |
| Permission scheme | `permission_schemes` | `projects.permission_scheme_id` |
| Issue security scheme | `issue_security_schemes` | `projects.issue_security_scheme_id` |
| Notification scheme | `notification_schemes` | `projects.notification_scheme_id` |
| Priority scheme | `priority_schemes` | `projects.priority_scheme_id` |

## 3) Jira Native Semantics: Shared Association vs Copy/Clone

### 3.1 Shared association is the default operating model

In Jira company-managed projects, schemes are designed to be reusable. A project is usually associated to an existing scheme, not deep-cloned automatically.

Practical meaning:

1. Multiple projects can point to the same scheme.
2. Editing the shared scheme can affect all associated projects.
3. Admins must treat shared scheme edits as cross-project changes.

### 3.2 Copy/clone is explicit, not implicit

If isolation is needed, admins copy an existing scheme (or create a new one), then associate the project to that copied scheme.

Practical meaning:

1. Isolation is opt-in.
2. Clone/copy happens before association or as part of a guided re-association flow.
3. Jira behavior is closer to "associate by default + copy when needed" than "always clone on project create".

### 3.3 Project template usage does not guarantee same scheme IDs

Two projects created from the same template may still have different effective scheme IDs depending on admin choices, defaults, and later reassociation.

## 4) End-to-End Provisioning Flow (Company-Managed Style)

All steps should run in one transaction, with event publish after commit.

1. Create project row with nullable scheme columns.
2. Resolve target scheme IDs by precedence: request override -> blueprint default -> tenant/system default.
3. For each resolved scheme from system scope, materialize tenant-shared copy if required by tenant isolation policy.
4. Validate compatibility gates across resolved schemes.
5. Persist final scheme IDs to project.
6. Commit.
7. Publish `PROJECT_CREATED` event with effective scheme IDs.

## 5) Step-by-Step by Scheme Family

Each subsection below includes the Jira-like operational flow to mirror in PM Core.

### 5.1 Issue Type Scheme

**Admin path:** Jira settings -> Issues -> Issue type schemes

**Create/update flow:**

1. Create issue types (Story/Task/Bug/etc.) if missing.
2. Create issue type scheme.
3. Add issue types to the scheme.
4. Set default issue type.
5. Save.

**Associate/reassociate flow:**

1. Open project settings or global admin association UI.
2. Select target issue type scheme.
3. Validate current issues against allowed issue types.
4. If removed types exist, block or require remap policy.
5. Save association.

**Share/copy behavior:**

- Shared association: common scheme reused by many projects.
- Isolation: copy scheme, then associate copied scheme.

**PM Core parity checks:**

1. `default_issue_type_id` must belong to selected scheme.
2. Every issue type used by workflow/field/screen mapping must exist in this scheme.

### 5.2 Workflow Scheme

**Admin path:** Jira settings -> Issues -> Workflows -> Workflow schemes

**Create/update flow:**

1. Create or reuse workflows.
2. Create workflow scheme.
3. Map each issue type (and optional default mapping) to a workflow.
4. Validate each workflow has one initial status.
5. Publish/activate as needed.

**Associate/reassociate flow:**

1. Select target workflow scheme for project.
2. Compare current issue statuses with target workflows.
3. Run status migration/remap where statuses do not map.
4. Apply scheme only after migration validation passes.

**Share/copy behavior:**

- Shared association is normal.
- Copy workflow/workflow scheme only when project-local lifecycle is required.

**PM Core parity checks:**

1. Workflow scheme covers all issue types in effective issue type scheme.
2. Unmapped statuses for existing work items must be blocked or remapped.
3. Transition rules stay attached to remapped transition IDs in clone mode.

### 5.3 Field Configuration Scheme

**Admin path:** Jira settings -> Issues -> Field configurations / Field configuration schemes

**Create/update flow:**

1. Create or reuse field configurations.
2. Set field-level hidden/required behavior in each configuration.
3. Create field configuration scheme.
4. Map each issue type to a field configuration.

**Associate/reassociate flow:**

1. Select field configuration scheme for project.
2. Ensure every issue type has a mapping (or default mapping).
3. Validate required fields will not break create/edit flows.
4. Save association.

**Share/copy behavior:**

- Shared scheme edits can change validation behavior across projects.
- Copy when project needs independent required/hidden rules.

**PM Core parity checks:**

1. Full issue-type coverage for effective issue type scheme.
2. Required fields should have safe migration policy for existing work items.

### 5.4 Issue Type Screen Scheme (Screen Chain)

**Admin path:** Jira settings -> Issues -> Screens / Screen schemes / Issue type screen schemes

**Create/update flow:**

1. Build screens (fields on Create/Edit/View).
2. Build screen schemes (map operations to screens).
3. Build issue type screen scheme.
4. Map each issue type to a screen scheme.

**Associate/reassociate flow:**

1. Select issue type screen scheme for project.
2. Validate issue-type coverage.
3. Validate referenced screen schemes/screens exist and are active.
4. Save association.

**Share/copy behavior:**

- Shared association enables standard UI consistency.
- Copy for project-specific forms.

**PM Core parity checks:**

1. Full issue-type coverage.
2. All foreign keys in screen chain must resolve after clone/remap.

### 5.5 Permission Scheme

**Admin path:** Jira settings -> System -> Global permissions / Project permissions (scheme administration)

**Create/update flow:**

1. Create permission scheme.
2. Add permission grants (users/groups/roles).
3. Validate minimum browse/edit/admin access policy.

**Associate/reassociate flow:**

1. Select permission scheme for project.
2. Confirm project role grants are valid for target tenant.
3. Save association.

**Share/copy behavior:**

- Shared permission scheme is common for governance.
- Copy when one project requires unique access model.

**PM Core parity checks:**

1. Critical permissions cannot be left unassigned if policy forbids it.
2. Authorization cache invalidation must happen after reassociation.

### 5.6 Issue Security Scheme

**Admin path:** Jira settings -> Issues -> Issue security schemes

**Create/update flow:**

1. Create issue security scheme.
2. Create security levels.
3. Configure level members (users/groups/roles/reporter/assignee/etc.).
4. Set default level if required.

**Associate/reassociate flow:**

1. Associate issue security scheme to project.
2. Validate current issue `security_level_id` values remain valid.
3. If level IDs become invalid, remap or clear by policy.
4. Save association.

**Share/copy behavior:**

- Shared model for consistent confidentiality policy.
- Copy for project-specific confidentiality model.

**PM Core parity checks:**

1. `default_level_id` must belong to selected scheme.
2. Existing issues with obsolete levels require migration policy.

### 5.7 Notification Scheme

**Admin path:** Jira settings -> System -> Notifications

**Create/update flow:**

1. Create notification scheme.
2. Add event -> recipient rules.
3. Validate recipient types (role/group/user/watchers).

**Associate/reassociate flow:**

1. Select notification scheme for project.
2. Validate required events have recipients if policy requires.
3. Save association.

**Share/copy behavior:**

- Shared scheme keeps notification policy consistent across projects.
- Copy when one project needs unique routing/noise profile.

**PM Core parity checks:**

1. Event catalog keys must be valid.
2. Delivery uses outbox after commit.

### 5.8 Priority Scheme

**Admin path:** Jira settings -> Issues -> Priorities / Priority schemes

**Create/update flow:**

1. Create/reuse priorities.
2. Create priority scheme.
3. Add priorities and ordering.
4. Set default priority.

**Associate/reassociate flow:**

1. Select priority scheme for project.
2. Validate existing issues with priorities outside target scheme.
3. Remap missing priorities or block change.
4. Save association.

**Share/copy behavior:**

- Shared association is common.
- Copy when project requires local priority taxonomy.

**PM Core parity checks:**

1. `default_priority_id` must belong to selected scheme.
2. Existing work item priorities must remain valid after reassociation.

## 6) Cross-Scheme Compatibility Gates (Required Before Commit)

Apply these checks in create-project and update-project-schemes flows:

1. Workflow scheme covers all issue types in effective issue type scheme.
2. Field configuration scheme covers all issue types.
3. Issue type screen scheme covers all issue types.
4. All scheme default IDs (`default_*_id`) belong to their owning scheme.
5. Existing data integrity checks pass for reassociation (statuses, priorities, security levels).

If any check fails, return `422 SCHEME_INCOMPATIBLE` with a structured detail payload.

## 7) Service Contract Rules for PM Core

1. Default `associationMode = SHARED_ASSOCIATION`.
2. `CLONE_ON_ASSOCIATE` must be explicit and auditable.
3. Use tenant-scoped materialization for system defaults (`tenant_id=0` source -> tenant shared copy).
4. Persist mapping lineage via `tenant_scheme_mappings` and `tenant_workflow_mappings`.
5. Keep create/reassociate transactional and publish outbox events after commit.

## 8) Implementation Checklist (Next Agent Ready)

1. Expand scheme rebinding endpoint to all eight scheme families.
2. Implement compatibility error payload model (per scheme type + violating IDs).
3. Implement clone-on-associate full graph clone with FK remapping and lineage persistence.
4. Add migration policy handlers for workflow status remap, priority remap, and security-level remap.
5. Add integration tests for shared-association and clone-on-associate modes.

## 9) Jira Documentation References

Primary references used for this design:

1. Workflows and statuses
   - https://support.atlassian.com/jira-cloud-administration/docs/work-with-issue-workflows/
   - https://support.atlassian.com/jira-cloud-administration/docs/manage-issue-workflows/
   - https://support.atlassian.com/jira-cloud-administration/docs/configure-workflow-schemes/
2. Issue types and issue type schemes
   - https://support.atlassian.com/jira-cloud-administration/docs/what-are-issue-types/
   - https://support.atlassian.com/jira-cloud-administration/docs/what-are-issue-type-schemes/
   - https://support.atlassian.com/jira-cloud-administration/docs/associate-issue-types-with-projects/
3. Screens and screen schemes
   - https://support.atlassian.com/jira-cloud-administration/docs/what-are-screens-in-jira/
   - https://support.atlassian.com/jira-cloud-administration/docs/manage-screen-schemes/
   - https://support.atlassian.com/jira-cloud-administration/docs/manage-issue-type-screens/
4. Field configuration schemes
   - https://support.atlassian.com/jira-cloud-administration/docs/what-are-field-configurations-and-field-configuration-schemes/
   - https://support.atlassian.com/jira-cloud-administration/docs/add-edit-and-delete-a-field-configuration-scheme/
5. Permissions and issue security
   - https://support.atlassian.com/jira-cloud-administration/docs/what-are-permission-schemes-in-jira/
   - https://support.atlassian.com/jira-cloud-administration/docs/what-are-work-item-security-schemes/
6. Notifications and priorities
   - https://support.atlassian.com/jira-cloud-administration/docs/configure-notification-schemes/
   - https://support.atlassian.com/jira-cloud-administration/docs/manage-priority-schemes/
