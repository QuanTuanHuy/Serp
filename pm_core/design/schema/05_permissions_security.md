# Module 05: Permissions & Security (Access Control)

**Design Philosophy:** Jira-like security is layered and explicit: project role assignment -> permission scheme grants -> issue-level security restrictions. Access is default-deny unless a grant matches, and the schema must support Jira-style contextual actors such as project lead, assignee, logged-in users, and custom-field-based user/group resolution.

Provisioning note: permission and issue security schemes are reusable across projects. Depending on template/blueprint policy, a project may bind a tenant shared scheme or a dedicated project-scoped copy; shared reuse is supported, but it should not be modeled as the only default for every provisioning path (see Module 00).

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 5.1. `project_roles` and `project_role_actors`

Role entities are defined in Module 01 and reused here.

- `project_roles`: role dictionary per tenant.
- `project_role_actors`: assigns USER/GROUP/SERVICE_ACCOUNT to project roles.

## 5.2. `permission_definitions`

Use DB-backed permission catalog to avoid hardcoded enums while keeping Jira-like semantics.
In v1, this catalog is system-seeded and API read-only for tenant admins (no tenant CRUD for permission keys).

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| permission_key | VARCHAR(100) | Stable key (BROWSE_PROJECTS, EDIT_ISSUES, etc.), immutable |
| name | VARCHAR(255) | Display name |
| description | TEXT | Description |
| category | VARCHAR(50) | ADMINISTRATION, PROJECT, ISSUE, VOTERS_WATCHERS, COMMENTS, ATTACHMENTS, TIME_TRACKING |
| is_system | BOOLEAN | Built-in permission marker (v1 should always be true) |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

### Suggested System Seed Catalog (Jira-aligned draft)

- Group headings below intentionally follow the classic/default Jira Software permission scheme headings for familiarity.
- Atlassian Cloud documentation now uses `space` and `work item` terminology; PM Core maps those directly to `project` and `work item`.
- Recommended bootstrap strategy: seed the same immutable catalog for each tenant because `permission_scheme_entries.permission_key` is modeled as a same-tenant FK.
- Seed all rows with `is_system=true`. Some rows are parity extensions that may be seeded now but only enforced in later phases.

#### Administration Permissions

| permission_key | Name | Seed tier | Notes |
|---|---|---|---|
| ADMINISTER_PROJECTS | Administer Projects | Core | Manage project administration, role membership, components, versions, and project details |
| EDIT_WORKFLOWS | Edit Workflows | Compatibility extension | Cloud company-managed permission for project-scoped workflow editing under guardrails |
| MANAGE_WORK_ITEM_LAYOUTS | Manage Work Item Layouts | Compatibility extension | Cloud company-managed permission for project-scoped issue layout editing |

#### Project Permissions

| permission_key | Name | Seed tier | Notes |
|---|---|---|---|
| BROWSE_PROJECTS | Browse Projects | Core | Prerequisite for most project/work item access |
| MANAGE_SPRINTS | Manage Sprints | Core | Create/start/complete/reorder sprints |
| MANAGE_VERSIONS | Manage Versions | Core | Create/release/archive/edit/delete versions/releases |
| VIEW_DEVELOPMENT_TOOLS | View Development Tools | Core | View development panel/integration data |
| VIEW_READ_ONLY_WORKFLOW | View Read-only Workflow | Core | View workflow from work item/project UI |
| VIEW_AGGREGATED_DATA | View Aggregated Data | Compatibility extension | Cloud permission for backlog/board/deployment insights |

#### Issue Permissions

| permission_key | Name | Seed tier | Notes |
|---|---|---|---|
| ARCHIVE_ISSUES | Archive Issues | Compatibility extension | Cloud permission to archive work items instead of deleting |
| RESTORE_ARCHIVED_ISSUES | Restore Archived Issues | Compatibility extension | Cloud permission to restore archived work items |
| ASSIGN_ISSUES | Assign Issues | Core | Change `assignee_id` on a work item |
| ASSIGNABLE_USER | Assignable User | Core | Candidate can be assigned work items |
| CLOSE_ISSUES | Close Issues | Compatibility extension | Separate close permission used with workflow/resolution semantics |
| CREATE_ISSUES | Create Issues | Core | Create work items/subtasks |
| DELETE_ISSUES | Delete Issues | Core | Delete work items and bundled issue data |
| EDIT_ISSUES | Edit Issues | Core | Edit work item fields except where other permissions narrow access |
| LINK_ISSUES | Link Issues | Core | Create/delete issue links |
| MODIFY_REPORTER | Modify Reporter | Core | Create/edit on behalf of another reporter |
| MOVE_ISSUES | Move Issues | Core | Move between projects or issue types |
| RESOLVE_ISSUES | Resolve Issues | Core | Set resolution / reopen in combination with transitions |
| SCHEDULE_ISSUES | Schedule Issues | Core | Edit due date and ranking/backlog scheduling |
| SET_ISSUE_SECURITY | Set Issue Security | Core | Set or change issue security level |
| TRANSITION_ISSUES | Transition Issues | Core | Execute workflow transitions |

#### Voters & Watchers Permissions

| permission_key | Name | Seed tier | Notes |
|---|---|---|---|
| MANAGE_WATCHERS | Manage Watchers | Core | Add/remove watchers |
| VIEW_VOTERS_AND_WATCHERS | View Voters and Watchers | Core | View watcher/voter lists |

#### Comments Permissions

| permission_key | Name | Seed tier | Notes |
|---|---|---|---|
| ADD_COMMENTS | Add Comments | Core | Add comments/internal notes |
| DELETE_ALL_COMMENTS | Delete All Comments | Core | Delete any comment |
| DELETE_OWN_COMMENTS | Delete Own Comments | Core | Delete only self-authored comments |
| EDIT_ALL_COMMENTS | Edit All Comments | Core | Edit any comment |
| EDIT_OWN_COMMENTS | Edit Own Comments | Core | Edit only self-authored comments |

#### Attachments Permissions

| permission_key | Name | Seed tier | Notes |
|---|---|---|---|
| CREATE_ATTACHMENTS | Create Attachments | Core | Add file attachments when attachments are enabled |
| DELETE_ALL_ATTACHMENTS | Delete All Attachments | Core | Delete any attachment |
| DELETE_OWN_ATTACHMENTS | Delete Own Attachments | Core | Delete only self-uploaded attachments |

#### Time Tracking Permissions

| permission_key | Name | Seed tier | Notes |
|---|---|---|---|
| WORK_ON_ISSUES | Work On Issues | Core | Log work / create worklogs; prerequisite for other worklog permissions |
| DELETE_ALL_WORKLOGS | Delete All Worklogs | Core | Delete any worklog |
| DELETE_OWN_WORKLOGS | Delete Own Worklogs | Core | Delete only self-authored worklogs |
| EDIT_ALL_WORKLOGS | Edit All Worklogs | Core | Edit any worklog |
| EDIT_OWN_WORKLOGS | Edit Own Worklogs | Core | Edit only self-authored worklogs |

### Source Baseline

- Atlassian Jira Cloud: [What are permission schemes in Jira?](https://support.atlassian.com/jira-cloud-administration/docs/what-are-permission-schemes-in-jira/)
- Atlassian Jira Cloud: [Types of permissions you can grant in a space scheme](https://support.atlassian.com/jira-cloud-administration/docs/types-of-permissions-you-can-grant-in-a-space-scheme/)
- Atlassian Jira Cloud: [Space access and configuration permissions](https://support.atlassian.com/jira-cloud-administration/docs/space-access-and-configuration-permissions/)
- Atlassian Jira Cloud: [Work item permissions in a space](https://support.atlassian.com/jira-cloud-administration/docs/work-item-permissions/)
- Atlassian Jira Cloud: [Time tracking permissions](https://support.atlassian.com/jira-cloud-administration/docs/time-tracking-permissions/)
- Atlassian Jira Cloud: [Voters, watchers, comment, and attachment permissions](https://support.atlassian.com/jira-cloud-administration/docs/voters-watchers-comment-and-attachment-permissions/)
- Atlassian Jira Data Center: [Managing project permissions](https://confluence.atlassian.com/adminjiraserver/managing-project-permissions-938847145.html) for classic permission names and dependency notes (`Assign Issues`, `Assignable User`, `Resolve Issues`, `Work On Issues`, etc.)

## 5.3. `permission_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.4. `permission_scheme_entries`

Grant-only model (Jira-like): this table defines who is granted each permission in a scheme.
If no matching entry exists, access is denied implicitly.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> permission_schemes |
| permission_key | VARCHAR(100) | FK -> permission_definitions.permission_key (same tenant) |
| grantee_type | VARCHAR(40) | PROJECT_ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE, APPLICATION_ACCESS, ANYONE_ON_WEB, USER_CUSTOM_FIELD_VALUE, GROUP_CUSTOM_FIELD_VALUE |
| grantee_ref | VARCHAR(255) | Role/group/user/application-access identifier (nullable for contextual grantees) |
| custom_field_id | BIGINT | FK -> custom_fields when grantee resolves from a user/group custom field |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.5. `issue_security_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| default_level_id | BIGINT | FK -> issue_security_levels |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.6. `issue_security_levels`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> issue_security_schemes |
| name | VARCHAR(255) | Level name |
| description | TEXT | Description |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 5.7. `issue_security_level_members`

Who can view issues tagged with a given security level.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| level_id | BIGINT | FK -> issue_security_levels |
| subject_type | VARCHAR(40) | PROJECT_ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE, USER_CUSTOM_FIELD_VALUE, GROUP_CUSTOM_FIELD_VALUE |
| subject_ref | VARCHAR(255) | Role/group/user identifier (nullable for contextual subjects) |
| custom_field_id | BIGINT | FK -> custom_fields when member resolves from a user/group custom field |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, permission_key)` on `permission_definitions`
- `UNIQUE (tenant_id, scheme_id, permission_key, grantee_type, COALESCE(grantee_ref, '__CTX__'), COALESCE(custom_field_id, 0))` on `permission_scheme_entries`
- `UNIQUE (tenant_id, level_id, subject_type, COALESCE(subject_ref, '__CTX__'), COALESCE(custom_field_id, 0))` on `issue_security_level_members`
- `CHECK` on `permission_scheme_entries`:
  - `grantee_type IN ('PROJECT_ROLE','GROUP','USER','PROJECT_LEAD','REPORTER','ASSIGNEE','APPLICATION_ACCESS','ANYONE_ON_WEB','USER_CUSTOM_FIELD_VALUE','GROUP_CUSTOM_FIELD_VALUE')`
  - `grantee_ref IS NOT NULL` for `PROJECT_ROLE/GROUP/USER/APPLICATION_ACCESS`
  - `custom_field_id IS NOT NULL` for `USER_CUSTOM_FIELD_VALUE/GROUP_CUSTOM_FIELD_VALUE`
  - `grantee_ref IS NULL` for contextual grantees other than `APPLICATION_ACCESS`
- `CHECK rights-like semantics at service layer`: `ANYONE_ON_WEB` should only be allowed when deployment policy permits anonymous access.
- `CHECK` on `issue_security_level_members`:
  - `subject_type IN ('PROJECT_ROLE','GROUP','USER','PROJECT_LEAD','REPORTER','ASSIGNEE','USER_CUSTOM_FIELD_VALUE','GROUP_CUSTOM_FIELD_VALUE')`
  - `subject_ref IS NOT NULL` for `PROJECT_ROLE/GROUP/USER`
  - `custom_field_id IS NOT NULL` for `USER_CUSTOM_FIELD_VALUE/GROUP_CUSTOM_FIELD_VALUE`
  - `subject_ref IS NULL` for contextual subjects
- Composite tenant-safe FKs are required for all references (`(tenant_id, id)` pattern)
- All UNIQUE constraints above should be implemented as partial unique indexes filtered by `deleted_at IS NULL`.
- `INDEX (tenant_id, scheme_id)` on all scheme child tables
