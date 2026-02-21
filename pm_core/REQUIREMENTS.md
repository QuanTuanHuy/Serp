# Project Management (PM) Module - Requirements

## 1. Module Overview

PM (Project Management) la module quan ly du an kieu JIRA-like cho team/organization trong he thong SERP ERP. Khac voi PTM (Personal Task Management) phuc vu ca nhan, PM tap trung vao quan ly cong viec nhom voi day du quy trinh Agile/Scrum/Kanban, configurable workflows, custom fields, screens, va permission schemes.

### Microservices

| Service | Port | Language | Responsibility |
|---------|------|----------|----------------|
| **pm_core** | 8093 | Go (Gin) | Projects, Work Items, Workflows, Fields & Screens, Permissions, Sprints, Boards, Notifications, Comments, Audit |
| **pm_analytics** | 8094 | Java (Spring Boot) | Metrics, Dashboards, Reports, Resource Utilization |

### Architecture

```
                        serp_web /modules/pm/
                              |
                        API Gateway (8080)
                         /pm/api/v1/*
                              |
                  +-----------+-----------+
                  |                       |
           pm_core (Go)          pm_analytics (Java)
           Port: 8093            Port: 8094
           DB: pm_core_db        DB: pm_analytics_db
                  |                       |
                  +----> Kafka <----------+
                  |
      +-----+----+-----+-----+
      |     |          |     |
  account  notif   mail   logging
  (HTTP)  (Kafka) (Kafka) (Kafka)
```

### Module Structure

pm_core duoc to chuc thanh 10 modules noi bo:

| Module | Name | Description |
|--------|------|-------------|
| 00 | Project Provisioning | Template resolution, deep-clone scheme isolation |
| 01 | Projects & Configuration | Projects, categories, blueprints, components, versions, roles |
| 02 | Issues & Work Items | Work items, issue types, priorities, resolutions, links, worklogs |
| 03 | Workflow Engine | Statuses, workflows, transitions, rules, workflow schemes |
| 04 | Fields & Screens | Custom fields, field configurations, screens, screen schemes |
| 05 | Permissions & Security | Permission definitions, permission schemes, issue security |
| 06 | Notifications | Notification schemes, events, templates, outbox, delivery logs |
| 07 | Agile & Planning | Boards, columns, sprints, ranking |
| 08 | Search & Reporting | Saved filters, dashboards, gadgets |
| 09 | Collaboration & Audit | Comments, attachments, watchers, change history, audit events |

---

## 2. Bounded Context & Scope

### PM Module so huu (owns):
- **Project lifecycle** - tao, quan ly, archive du an voi blueprint templates va scheme provisioning
- **Work items** - Configurable issue types (Epic, Story, Task, Bug, Subtask) voi hierarchy, links, va dependencies
- **Team membership** - Project roles (polymorphic: USER, GROUP, SERVICE_ACCOUNT) voi permission schemes
- **Sprint management** - Scrum sprints voi board-scoped lifecycle (FUTURE -> ACTIVE -> CLOSED)
- **Board management** - Kanban/Scrum boards voi configurable columns mapped to workflow statuses
- **Workflow engine** - Configurable statuses, transitions, transition rules (conditions, validators, post-functions)
- **Custom fields** - Typed custom fields voi contexts, options, field configurations, va screen layouts
- **Screen management** - Screens, tabs, field placement cho CREATE/EDIT/VIEW/TRANSITION operations
- **Permission system** - DB-backed permission definitions, permission schemes, issue security levels
- **Notification policies** - Notification schemes mapping events to recipients/channels/templates
- **Comments & Activity** - Threaded comments voi mentions, change history (change groups + change items)
- **Attachments** - File dinh kem (metadata in DB, files on S3/MinIO)
- **Time tracking** - Worklogs voi time spent, original estimate, remaining estimate
- **Search & Filters** - Saved filters (JQL-like), dashboards voi configurable gadgets
- **Audit trail** - Append-only audit events cho compliance

### Out of scope (KHONG nam trong PM):
- **Chat/Discussion giua nguoi dung** -> `discuss_service`
- **Video conferencing** -> 3rd party integration
- **Wiki/Knowledge base** -> service rieng (tuong lai)
- **Email sending** -> `mailservice` (PM chi publish Kafka events / notification outbox)
- **Push notifications** -> `notification_service` (PM chi publish events)
- **AI predictions** -> `serp_llm` (PM cung cap data, LLM xu ly, tuong lai)
- **Calendar sync** -> `ptm_schedule` (tuong lai sync tu PM)
- **Full-text search** -> Elasticsearch infrastructure
- **White-label/Branding** -> infrastructure concern
- **User/Org management** -> `account` service
- **Audit logging (global)** -> `logging_tracker` (PM publish events, also keeps local audit_events)

### PTM vs PM

| Aspect | ptm_task (Personal) | pm_core (Team) |
|--------|---------------------|----------------|
| Ownership | userId (ca nhan) | projectId + role-based assignments (nhom) |
| Entity name | TaskEntity | WorkItemEntity (configurable issue types) |
| Hierarchy | Task -> SubTask | Configurable hierarchy via issue_types.hierarchy_level |
| Assignment | Creator = owner | Single assignee per work item + project role actors |
| Methodology | Khong co | Scrum / Kanban (via board type) |
| Sprint | Khong co | Full sprint lifecycle (FUTURE -> ACTIVE -> CLOSED) |
| Board | Khong co | Kanban + Scrum boards voi column-status mapping |
| Workflow | Fixed statuses | Configurable workflow engine voi schemes |
| Fields | Fixed fields | Custom fields voi contexts, options, field configs |
| Permissions | Owner-based | Scheme-based permissions + issue security levels |
| Database | ptm_task_db | pm_core_db |

**Future sync:** ptm_task da co `ExternalID` va `Source` fields. Khi PM publish `WORK_ITEM_ASSIGNED`, ptm_task co the tao personal task voi `Source: "pm"` de user thay work items duoc assign trong personal task list.

---

## 3. Core Entities & Relationships

> **Chi tiet schema:** Xem [design/schema/](design/schema/) cho column-level definitions, constraints, va indexes.
> **All tables share base columns:** `tenant_id`, `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted_at` (soft delete).

### 3.0. Module 00: Project Provisioning ([schema](design/schema/00_project_provisioning.md))

Khong co tables rieng. Module nay dinh nghia **provisioning philosophy** va **deep clone algorithm** de isolate project configurations:

- **Design principle:** Reuse configuration templates at provisioning time, isolate each project through project-owned scheme clones.
- **Scheme types covered:** ISSUE_TYPE, WORKFLOW, FIELD_CONFIG, SCREEN, PERMISSION, ISSUE_SECURITY, NOTIFICATION, PRIORITY.
- **Deep clone order:** FK-safe, atomic transaction, compatibility gates truoc commit.
- **Resolution precedence:** Explicit override > Blueprint default > Tenant system default template.

### 3.1. Module 01: Projects & Configuration ([schema](design/schema/01_projects.md))

| # | Table | Description |
|---|-------|-------------|
| 1.1 | `project_categories` | Phan loai du an (unique name per tenant) |
| 1.2 | `project_blueprints` | Project templates voi project_type_key, avatar, is_system flag |
| 1.3 | `blueprint_scheme_defaults` | Relational mapping blueprint -> scheme defaults per scheme_type |
| 1.4 | `projects` | **Core entity.** Container binding project-owned scheme clones (issue_type_scheme_id, workflow_scheme_id, field_config_scheme_id, issue_type_screen_scheme_id, permission_scheme_id, issue_security_scheme_id, notification_scheme_id, priority_scheme_id). Key fields: key (unique per tenant), name, lead_user_id, project_type_key, project_category_id, archived |
| 1.5 | `project_components` | Functional components within a project (lead_user_id, assignee_type) |
| 1.6 | `project_versions` | Release/fix versions voi released, archived states, release_date, sequence |
| 1.7 | `project_roles` | Role dictionary per tenant (name unique per tenant, is_system flag) |
| 1.8 | `project_role_actors` | Polymorphic role assignments: USER, GROUP, SERVICE_ACCOUNT per project + role |

### 3.2. Module 02: Issues & Work Items ([schema](design/schema/02_issues.md))

| # | Table | Description |
|---|-------|-------------|
| 2.1 | `work_items` | **Central entity.** FK -> issue_types, statuses, priorities, resolutions. Fields: key (e.g. SERP-123), summary, description, assignee_id, reporter_id, parent_id (hierarchy), security_level_id, due_date, rank (Lexorank), time estimates |
| 2.2 | `work_item_components` | N:N work item <-> project_components |
| 2.3 | `work_item_fix_versions` | N:N work item <-> project_versions voi relation_type (FIX, AFFECTS) |
| 2.4 | `work_item_sprints` | Sprint assignment history (is_active flag, added_at, removed_at) |
| 2.5 | `issue_types` | Issue type dictionary: type_key, hierarchy_level (0=subtask, 1=standard, 2=epic+), is_system |
| 2.6 | `issue_type_schemes` | Groups issue types for project binding, default_issue_type_id |
| 2.7 | `issue_type_scheme_items` | Issue type -> scheme membership voi sequence |
| 2.8 | `priorities` | Priority dictionary: name, color, icon_url, sequence, is_system |
| 2.9 | `priority_schemes` | Groups priorities for project binding, default_priority_id |
| 2.10 | `priority_scheme_items` | Priority -> scheme membership voi sequence |
| 2.11 | `resolutions` | Resolution dictionary: Fixed, Won't Fix, Duplicate, etc. |
| 2.12 | `issue_link_types` | Link type dictionary: Blocks/is blocked by, Clones, Relates, etc. |
| 2.13 | `issue_links` | Directed links between work items (source_id -> target_id voi link_type_id) |
| 2.14 | `worklogs` | Time tracking entries: work_item_id, author_id, time_spent (seconds), start_date, comment |
| 2.15 | `work_item_custom_field_values` | Typed value store: text_value, number_value, date_value, datetime_value, user_value_id, option_value_id, json_value (replaces JSONB blob) |

### 3.3. Module 03: Workflow Engine ([schema](design/schema/03_workflows.md))

| # | Table | Description |
|---|-------|-------------|
| 3.1 | `status_categories` | Logical grouping: To Do (new), In Progress (indeterminate), Done (done) |
| 3.2 | `statuses` | Status dictionary: status_key, name, status_category_id, is_system |
| 3.3 | `workflows` | Workflow definitions: versioned (version_no), is_active flag, is_system |
| 3.4 | `workflow_steps` | Explicit workflow-status mapping: step_order, is_initial, is_final flags |
| 3.5 | `workflow_transitions` | Edges between statuses: from_status_id -> to_status_id, optional screen_id (Module 04) |
| 3.6 | `workflow_transition_rules` | Extensible rules: CONDITION, VALIDATOR, POST_FUNCTION voi rule_key va config_json |
| 3.7 | `workflow_schemes` | Groups workflows for project binding, default_workflow_id |
| 3.8 | `workflow_scheme_items` | Maps issue_type_id -> workflow_id within a scheme |

### 3.4. Module 04: Fields & Screens ([schema](design/schema/04_fields_screens.md))

| # | Table | Description |
|---|-------|-------------|
| 4.1 | `custom_fields` | Field metadata: field_key, type_key (text, number, date, select, etc.), search_template, is_global |
| 4.2 | `custom_field_options` | Select/multiselect options: option_key, value, sequence, parent_option_id (cascade), is_disabled |
| 4.3 | `custom_field_contexts` | Scoping: project_id + issue_type_id (nullable for global context) |
| 4.4 | `field_configurations` | Named config sets for field behavior rules |
| 4.5 | `field_configuration_items` | Per-field rules: is_required, is_hidden, renderer_key (text/wiki/markdown) |
| 4.6 | `field_config_schemes` | Groups field configs for project binding, default_field_configuration_id |
| 4.7 | `field_config_scheme_items` | Maps issue_type_id -> field_configuration_id within a scheme |
| 4.8 | `screens` | Named screen definitions |
| 4.9 | `screen_tabs` | Tabs within a screen, ordered by sequence |
| 4.10 | `screen_tab_fields` | Fields placed on tabs: field_ref_type (SYSTEM/CUSTOM) + field_ref |
| 4.11 | `screen_schemes` | Groups screens by operation, default_screen_id |
| 4.12 | `screen_scheme_items` | Maps operation_key (CREATE, EDIT, VIEW, TRANSITION) -> screen_id |
| 4.13 | `issue_type_screen_schemes` | Top-level screen config for project binding, default_screen_scheme_id |
| 4.14 | `issue_type_screen_scheme_items` | Maps issue_type_id -> screen_scheme_id |

### 3.5. Module 05: Permissions & Security ([schema](design/schema/05_permissions_security.md))

| # | Table | Description |
|---|-------|-------------|
| 5.1 | _(reuses project_roles, project_role_actors from Module 01)_ | Role dictionary + polymorphic role actors |
| 5.2 | `permission_definitions` | DB-backed permission dictionary: permission_key (BROWSE_PROJECTS, EDIT_ISSUES, etc.), category (PROJECT, ISSUE, COMMENT, ADMIN, AGILE) |
| 5.3 | `permission_schemes` | Named permission scheme containers |
| 5.4 | `permission_scheme_entries` | Polymorphic grants: permission_key + grantee_type (ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE) + effect (ALLOW, DENY) + conditions_json |
| 5.5 | `issue_security_schemes` | Named issue security scheme, default_level_id |
| 5.6 | `issue_security_levels` | Security levels within a scheme (name, description) |
| 5.7 | `issue_security_level_members` | Who can view issues at a security level: subject_type (ROLE, GROUP, USER) + subject_id |

### 3.6. Module 06: Notifications ([schema](design/schema/06_notifications.md))

| # | Table | Description |
|---|-------|-------------|
| 6.1 | `notification_schemes` | Named notification policy containers |
| 6.2 | `notification_events` | Event dictionary: event_key (work_item.created, comment.added, etc.), is_system |
| 6.3 | `notification_templates` | Channel-specific templates: channel (EMAIL, IN_APP, WEBHOOK), subject_template, body_template, content_type |
| 6.4 | `notification_scheme_entries` | Maps event -> recipient_type (ASSIGNEE, REPORTER, WATCHERS, PROJECT_LEAD, PROJECT_ROLE, GROUP, USER) -> channel -> template, voi is_enabled toggle va conditions_json |
| 6.5 | `notification_outbox` | Channel-agnostic delivery queue: dedupe_key, status (PENDING, PROCESSING, SENT, FAILED, DEAD), retry_count, next_retry_at |
| 6.6 | `notification_delivery_logs` | Immutable delivery history: provider, provider_message_id, status (SENT, FAILED, BOUNCED, OPENED), detail_json |

### 3.7. Module 07: Agile & Planning ([schema](design/schema/07_agile.md))

| # | Table | Description |
|---|-------|-------------|
| 7.1 | `boards` | Board definitions: type (SCRUM, KANBAN), owner_id, filter_id (FK -> search_requests), location_type (PROJECT, USER) + location_id |
| 7.2 | `board_columns` | Columns within a board: name, sequence, min_wip, max_wip constraints |
| 7.3 | `board_column_statuses` | Maps column -> workflow statuses (N:N) |
| 7.4 | `board_quick_filters` | Per-board shortcut filters: query_string (JQL-like) |
| 7.5 | `sprints` | Sprint lifecycle: board_id, state (FUTURE, ACTIVE, CLOSED), start_date, end_date, complete_date, activated_date |
| 7.6 | `sprint_reports` | Metric snapshots: metric_key (velocity, burndown, etc.) + metric_value (JSONB) |
| 7.7 | `rank_fields` | Ranking dimension config: rank_algorithm (LEXORANK, FRACTIONAL_INDEX), is_default |

### 3.8. Module 08: Search & Reporting ([schema](design/schema/08_search_filters.md))

| # | Table | Description |
|---|-------|-------------|
| 8.1 | `search_requests` | Saved filters: author_id, query_string (JQL-like), is_favorite |
| 8.2 | `search_request_favorites` | Multi-user favorites for shared filters |
| 8.3 | `share_permissions` | Polymorphic sharing: entity_type (FILTER, DASHBOARD), share_type (GLOBAL, GROUP, PROJECT, ROLE, USER), rights (READ, EDIT) |
| 8.4 | `filter_subscriptions` | Periodic delivery: cron_expression, channel (EMAIL, IN_APP), is_active |
| 8.5 | `dashboards` | Dashboard definitions: author_id, layout (AA, AAA, AB, BA), is_system |
| 8.6 | `gadgets` | Dashboard widgets: portlet_id (implementation key), column_idx, row_idx, color |
| 8.7 | `gadget_user_prefs` | Per-user gadget config: prefs_json (JSONB) |

### 3.9. Module 09: Collaboration & Audit ([schema](design/schema/09_collaboration_audit.md))

| # | Table | Description |
|---|-------|-------------|
| 9.1 | `issue_comments` | Comments on work items: author_id, body (markdown/JSON), is_internal flag, edited_at |
| 9.2 | `comment_mentions` | Normalized @mentions: comment_id -> mentioned_user_id |
| 9.3 | `attachments` | File metadata: filename, mime_type, size_bytes, storage_provider (S3, MINIO, LOCAL), storage_key, thumbnail_key, checksum |
| 9.4 | `issue_watchers` | Watch subscriptions: issue_id + user_id (unique) |
| 9.5 | `change_groups` | Logical update actions: issue_id, author_id, source (UI, API, AUTOMATION) |
| 9.6 | `change_items` | Individual field changes within a change group: field, field_type (SYSTEM, CUSTOM), old_value/old_string, new_value/new_string |
| 9.7 | `audit_events` | Generic append-only audit stream: entity_type, entity_id, action, actor_id, payload_json, occurred_at |

### pm_analytics Entities (~6)

> pm_analytics la separate Java service consume Kafka events tu pm_core. Entities duoi day nam trong pm_analytics_db.

| Entity | Description |
|--------|-------------|
| `ProjectMetricsSnapshot` | Daily project metrics snapshots (total/completed work items, story points, cycle time, lead time, on-time delivery rate) |
| `SprintMetrics` | Sprint-level metrics (velocity, planned/completed/carry-over points, burndown data, completion rate) |
| `ResourceUtilization` | User workload per period (assigned/completed items, logged/estimated hours, utilization rate) |
| `Dashboard` | Analytics dashboards (name, isDefault, projectId) |
| `DashboardWidget` | Dashboard widgets (widgetType, config JSON, grid position) |
| `ReportTemplate` | Report templates (templateType, config JSON, outputFormat: PDF/EXCEL/CSV) |

### Entity Relationships (ERD)

```
=== Module 01: Projects ===
project_categories (1) ──── (N) projects
project_blueprints (1) ──── (N) blueprint_scheme_defaults
projects (1) ──── (N) project_components
projects (1) ──── (N) project_versions
projects (1) ──── (N) project_role_actors
project_roles (1) ──── (N) project_role_actors

=== Module 02: Issues ===
projects (1) ──── (N) work_items
work_items (1) ──── (N) work_items (parent_id hierarchy)
work_items (1) ──── (N) work_item_components
work_items (1) ──── (N) work_item_fix_versions
work_items (1) ──── (N) work_item_sprints
work_items (1) ──── (N) work_item_custom_field_values
work_items (1) ──── (N) issue_links (source)
work_items (1) ──── (N) issue_links (target)
work_items (1) ──── (N) worklogs
issue_types (1) ──── (N) work_items
priorities (1) ──── (N) work_items
resolutions (1) ──── (N) work_items
issue_type_schemes (1) ──── (N) issue_type_scheme_items
priority_schemes (1) ──── (N) priority_scheme_items
issue_link_types (1) ──── (N) issue_links

=== Module 03: Workflows ===
status_categories (1) ──── (N) statuses
statuses (1) ──── (N) work_items (status_id)
workflows (1) ──── (N) workflow_steps
workflows (1) ──── (N) workflow_transitions
workflow_transitions (1) ──── (N) workflow_transition_rules
workflow_schemes (1) ──── (N) workflow_scheme_items

=== Module 04: Fields & Screens ===
custom_fields (1) ──── (N) custom_field_options
custom_fields (1) ──── (N) custom_field_contexts
field_configurations (1) ──── (N) field_configuration_items
field_config_schemes (1) ──── (N) field_config_scheme_items
screens (1) ──── (N) screen_tabs
screen_tabs (1) ──── (N) screen_tab_fields
screen_schemes (1) ──── (N) screen_scheme_items
issue_type_screen_schemes (1) ──── (N) issue_type_screen_scheme_items

=== Module 05: Permissions ===
permission_schemes (1) ──── (N) permission_scheme_entries
issue_security_schemes (1) ──── (N) issue_security_levels
issue_security_levels (1) ──── (N) issue_security_level_members

=== Module 06: Notifications ===
notification_schemes (1) ──── (N) notification_scheme_entries
notification_outbox (1) ──── (N) notification_delivery_logs

=== Module 07: Agile ===
boards (1) ──── (N) board_columns
boards (1) ──── (N) board_quick_filters
boards (1) ──── (N) sprints
board_columns (1) ──── (N) board_column_statuses
sprints (1) ──── (N) sprint_reports
sprints (1) ──── (N) work_item_sprints

=== Module 08: Search ===
search_requests (1) ──── (N) search_request_favorites
search_requests (1) ──── (N) filter_subscriptions
dashboards (1) ──── (N) gadgets
gadgets (1) ──── (N) gadget_user_prefs

=== Module 09: Collaboration ===
work_items (1) ──── (N) issue_comments
work_items (1) ──── (N) attachments
work_items (1) ──── (N) issue_watchers
work_items (1) ──── (N) change_groups
issue_comments (1) ──── (N) comment_mentions
change_groups (1) ──── (N) change_items

=== Cross-module Scheme Bindings (projects -> schemes) ===
projects.issue_type_scheme_id ──── issue_type_schemes
projects.workflow_scheme_id ──── workflow_schemes
projects.field_config_scheme_id ──── field_config_schemes
projects.issue_type_screen_scheme_id ──── issue_type_screen_schemes
projects.permission_scheme_id ──── permission_schemes
projects.issue_security_scheme_id ──── issue_security_schemes
projects.notification_scheme_id ──── notification_schemes
projects.priority_scheme_id ──── priority_schemes
```

---

## 4. Functional Requirements (Use Cases)

> **Chi tiet use case specifications:** Xem [design/PM_USECASE_SPEC.md](design/PM_USECASE_SPEC.md) cho main flows, alternative flows, exception flows, business rules, va data requirements cua tung use case.

### Actors

| Actor | Type | Description | Key Permissions |
|-------|------|-------------|-----------------|
| PM Admin | Primary | Configures global schemes, workflows, fields, permissions, issue types, priorities, resolutions | `PM.PROJECT.ADMIN`, `PM.WORKFLOW.MANAGE`, `PM.FIELD.MANAGE`, `PM.PERMISSION.MANAGE` |
| Project Lead | Primary | Creates/manages projects, assigns roles, manages components and versions | `PM.PROJECT.CREATE`, `PM.PROJECT.UPDATE`, `PM.COMPONENT.MANAGE`, `PM.VERSION.MANAGE` |
| Team Member | Primary | Creates/updates work items, logs work, transitions statuses, manages links | `PM.WORK_ITEM.CREATE`, `PM.WORK_ITEM.UPDATE`, `PM.WORK_ITEM.TRANSITION`, `PM.WORKLOG.MANAGE` |
| Viewer | Secondary | Read-only access to projects, work items, and configurations | `PM.PROJECT.READ`, `PM.WORK_ITEM.READ` |
| System (Kafka) | System | Handles async events from other services, triggers automation | N/A |

### Phasing

| Phase | Modules | Description |
|-------|---------|-------------|
| Phase 1 | 00, 01, 02, 03, 04, 05 | Core PM functionality: projects, work items, workflows, fields, screens, permissions |
| Phase 2 | 06, 07, 08, 09 | Notifications, agile boards, search/filters, collaboration/audit |
| Phase 3 | pm_analytics | Analytics, dashboards, reports, resource utilization |

### 4.1. Module 01: Projects & Configuration ([detailed specs](design/PM_USECASE_SPEC.md#31-module-01-projects--configuration))

| UC ID | Name | Actor | Priority | Complexity |
|-------|------|-------|----------|------------|
| UC-PM-001 | Create Project | Project Lead | High | Complex |
| UC-PM-002 | Update Project | Project Lead | High | Medium |
| UC-PM-003 | Get Project by ID | Team Member | High | Simple |
| UC-PM-004 | List Projects with Filters | Team Member | High | Simple |
| UC-PM-005 | Delete Project | PM Admin | Medium | Medium |
| UC-PM-006 | Archive/Unarchive Project | Project Lead | Medium | Simple |
| UC-PM-007 | Update Project Scheme Bindings | PM Admin | Medium | Complex |
| UC-PM-011 | Create Project Category | PM Admin | Medium | Simple |
| UC-PM-012 | Update Project Category | PM Admin | Medium | Simple |
| UC-PM-013 | Get Project Category by ID | PM Admin | Low | Simple |
| UC-PM-014 | List Project Categories | PM Admin | Medium | Simple |
| UC-PM-015 | Delete Project Category | PM Admin | Low | Simple |
| UC-PM-016 | Create Project Blueprint | PM Admin | Medium | Medium |
| UC-PM-017 | Update Project Blueprint | PM Admin | Medium | Simple |
| UC-PM-018 | Get Project Blueprint by ID | PM Admin | Low | Simple |
| UC-PM-019 | List Project Blueprints | PM Admin | Medium | Simple |
| UC-PM-020 | Delete Project Blueprint | PM Admin | Low | Simple |
| UC-PM-021 | Manage Blueprint Scheme Defaults | PM Admin | Medium | Medium |
| UC-PM-026 | Create Project Component | Project Lead | High | Simple |
| UC-PM-027 | Update Project Component | Project Lead | Medium | Simple |
| UC-PM-028 | Get Project Component by ID | Team Member | Low | Simple |
| UC-PM-029 | List Project Components | Team Member | Medium | Simple |
| UC-PM-030 | Delete Project Component | Project Lead | Low | Simple |
| UC-PM-031 | Create Project Version | Project Lead | High | Simple |
| UC-PM-032 | Update Project Version | Project Lead | Medium | Simple |
| UC-PM-033 | Get Project Version by ID | Team Member | Low | Simple |
| UC-PM-034 | List Project Versions | Team Member | Medium | Simple |
| UC-PM-035 | Delete Project Version | Project Lead | Low | Simple |
| UC-PM-036 | Release Project Version | Project Lead | High | Medium |
| UC-PM-037 | Archive Project Version | Project Lead | Low | Simple |
| UC-PM-041 | Create Project Role | PM Admin | Medium | Simple |
| UC-PM-042 | Update Project Role | PM Admin | Medium | Simple |
| UC-PM-043 | Get Project Role by ID | PM Admin | Low | Simple |
| UC-PM-044 | List Project Roles | PM Admin | Medium | Simple |
| UC-PM-045 | Delete Project Role | PM Admin | Low | Simple |
| UC-PM-046 | Add Project Role Actor | Project Lead | High | Simple |
| UC-PM-047 | Remove Project Role Actor | Project Lead | Medium | Simple |
| UC-PM-048 | List Project Role Actors | Project Lead | Medium | Simple |

**Key behaviors:**
- Project creation triggers deep-clone provisioning of all scheme bindings (Module 00)
- Project key: unique per tenant, uppercase alphanumeric, 2-10 chars (e.g., "SERP")
- Blueprint templates define default scheme bindings for new projects
- Project versions support release/archive state machine
- Project roles use polymorphic actor model (USER, GROUP, SERVICE_ACCOUNT)

### 4.2. Module 02: Issues & Work Items ([detailed specs](design/PM_USECASE_SPEC.md#32-module-02-issues--work-items))

| UC ID | Name | Actor | Priority | Complexity |
|-------|------|-------|----------|------------|
| UC-PM-101 | Create Work Item | Team Member | High | Complex |
| UC-PM-102 | Update Work Item | Team Member | High | Medium |
| UC-PM-103 | Get Work Item by ID | Team Member | High | Simple |
| UC-PM-104 | List Work Items with Filters | Team Member | High | Medium |
| UC-PM-105 | Delete Work Item | Project Lead | Medium | Medium |
| UC-PM-106 | Transition Work Item Status | Team Member | High | Complex |
| UC-PM-107 | Assign Work Item | Team Member | High | Simple |
| UC-PM-108 | Re-rank Work Item (Lexorank) | Team Member | Medium | Medium |
| UC-PM-109 | Bulk Update Work Items | Project Lead | Medium | Complex |
| UC-PM-110 | Clone Work Item | Team Member | Medium | Medium |
| UC-PM-111 | Manage Work Item Components | Team Member | Medium | Simple |
| UC-PM-116 | Manage Work Item Fix Versions | Team Member | Medium | Simple |
| UC-PM-121 | Move Work Item to Sprint | Team Member | High | Medium |
| UC-PM-122 | Remove Work Item from Sprint | Team Member | Medium | Simple |
| UC-PM-131 | Create Issue Type | PM Admin | High | Simple |
| UC-PM-132 | Update Issue Type | PM Admin | Medium | Simple |
| UC-PM-133 | Get Issue Type by ID | PM Admin | Low | Simple |
| UC-PM-134 | List Issue Types | PM Admin | Medium | Simple |
| UC-PM-135 | Delete Issue Type | PM Admin | Low | Simple |
| UC-PM-136 | Create Issue Type Scheme | PM Admin | Medium | Simple |
| UC-PM-137 | Update Issue Type Scheme | PM Admin | Medium | Simple |
| UC-PM-138 | Get Issue Type Scheme by ID | PM Admin | Low | Simple |
| UC-PM-139 | List Issue Type Schemes | PM Admin | Medium | Simple |
| UC-PM-140 | Delete Issue Type Scheme | PM Admin | Low | Simple |
| UC-PM-141 | Manage Issue Type Scheme Items | PM Admin | Medium | Medium |
| UC-PM-146 | Create Priority | PM Admin | Medium | Simple |
| UC-PM-147 | Update Priority | PM Admin | Medium | Simple |
| UC-PM-148 | Get Priority by ID | PM Admin | Low | Simple |
| UC-PM-149 | List Priorities | PM Admin | Medium | Simple |
| UC-PM-150 | Delete Priority | PM Admin | Low | Simple |
| UC-PM-151 | Create Priority Scheme | PM Admin | Medium | Simple |
| UC-PM-152 | Update Priority Scheme | PM Admin | Medium | Simple |
| UC-PM-153 | Get Priority Scheme by ID | PM Admin | Low | Simple |
| UC-PM-154 | List Priority Schemes | PM Admin | Medium | Simple |
| UC-PM-155 | Delete Priority Scheme | PM Admin | Low | Simple |
| UC-PM-156 | Manage Priority Scheme Items | PM Admin | Medium | Medium |
| UC-PM-161 | Create Resolution | PM Admin | Medium | Simple |
| UC-PM-162 | Update Resolution | PM Admin | Medium | Simple |
| UC-PM-163 | Get Resolution by ID | PM Admin | Low | Simple |
| UC-PM-164 | List Resolutions | PM Admin | Medium | Simple |
| UC-PM-165 | Delete Resolution | PM Admin | Low | Simple |
| UC-PM-171 | Create Issue Link Type | PM Admin | Medium | Simple |
| UC-PM-172 | Update Issue Link Type | PM Admin | Medium | Simple |
| UC-PM-173 | Get Issue Link Type by ID | PM Admin | Low | Simple |
| UC-PM-174 | List Issue Link Types | PM Admin | Medium | Simple |
| UC-PM-175 | Delete Issue Link Type | PM Admin | Low | Simple |
| UC-PM-176 | Create Issue Link | Team Member | High | Simple |
| UC-PM-177 | Delete Issue Link | Team Member | Medium | Simple |
| UC-PM-178 | List Issue Links for Work Item | Team Member | Medium | Simple |
| UC-PM-181 | Create Worklog | Team Member | High | Simple |
| UC-PM-182 | Update Worklog | Team Member | Medium | Simple |
| UC-PM-183 | Delete Worklog | Team Member | Medium | Simple |
| UC-PM-184 | List Worklogs for Work Item | Team Member | Medium | Simple |
| UC-PM-185 | Get Worklog by ID | Team Member | Low | Simple |

**Key behaviors:**
- Work item key = project key + issue_no (e.g., "SERP-123"), unique per tenant
- Status transitions validated against workflow engine (Module 03)
- Hierarchy enforced via issue_types.hierarchy_level (subtask < standard < epic)
- Sprint assignment tracked with history (work_item_sprints with is_active, added_at, removed_at)
- Custom field values stored in typed columns (text, number, date, user, option, json)
- Worklogs track time_spent in seconds with start_date
- Issue links are directed (source -> target) with typed link types (Blocks, Clones, Relates)
- Lexorank-based ordering for backlog and board views

### 4.3. Module 03: Workflow Engine ([detailed specs](design/PM_USECASE_SPEC.md#33-module-03-workflow-engine))

| UC ID | Name | Actor | Priority | Complexity |
|-------|------|-------|----------|------------|
| UC-PM-201 | Create Status Category | PM Admin | Medium | Simple |
| UC-PM-202 | Update Status Category | PM Admin | Low | Simple |
| UC-PM-203 | Get Status Category by ID | PM Admin | Low | Simple |
| UC-PM-204 | List Status Categories | PM Admin | Medium | Simple |
| UC-PM-205 | Delete Status Category | PM Admin | Low | Simple |
| UC-PM-211 | Create Status | PM Admin | High | Simple |
| UC-PM-212 | Update Status | PM Admin | Medium | Simple |
| UC-PM-213 | Get Status by ID | PM Admin | Low | Simple |
| UC-PM-214 | List Statuses | PM Admin | Medium | Simple |
| UC-PM-215 | Delete Status | PM Admin | Low | Medium |
| UC-PM-221 | Create Workflow | PM Admin | High | Medium |
| UC-PM-222 | Update Workflow | PM Admin | High | Medium |
| UC-PM-223 | Get Workflow by ID | PM Admin | Medium | Simple |
| UC-PM-224 | List Workflows | PM Admin | Medium | Simple |
| UC-PM-225 | Delete Workflow | PM Admin | Medium | Medium |
| UC-PM-226 | Publish Workflow | PM Admin | High | Complex |
| UC-PM-227 | Clone Workflow | PM Admin | Medium | Medium |
| UC-PM-228 | Validate Workflow | PM Admin | High | Complex |
| UC-PM-231 | Add Workflow Step | PM Admin | High | Simple |
| UC-PM-232 | Remove Workflow Step | PM Admin | Medium | Medium |
| UC-PM-233 | Reorder Workflow Steps | PM Admin | Low | Simple |
| UC-PM-236 | Add Workflow Transition | PM Admin | High | Medium |
| UC-PM-237 | Update Workflow Transition | PM Admin | Medium | Simple |
| UC-PM-238 | Remove Workflow Transition | PM Admin | Medium | Simple |
| UC-PM-239 | List Workflow Transitions | PM Admin | Medium | Simple |
| UC-PM-241 | Add Workflow Transition Rule | PM Admin | Medium | Medium |
| UC-PM-242 | Update Workflow Transition Rule | PM Admin | Medium | Simple |
| UC-PM-243 | Remove Workflow Transition Rule | PM Admin | Medium | Simple |
| UC-PM-251 | Create Workflow Scheme | PM Admin | Medium | Simple |
| UC-PM-252 | Update Workflow Scheme | PM Admin | Medium | Simple |
| UC-PM-253 | Get Workflow Scheme by ID | PM Admin | Low | Simple |
| UC-PM-254 | List Workflow Schemes | PM Admin | Medium | Simple |
| UC-PM-255 | Delete Workflow Scheme | PM Admin | Low | Simple |
| UC-PM-256 | Manage Workflow Scheme Items | PM Admin | Medium | Medium |

**Key behaviors:**
- Status categories group statuses into 3 logical buckets: To Do (new), In Progress (indeterminate), Done (done)
- Workflows are versioned (version_no) with is_active flag; publishing increments version
- Workflow steps map statuses to a workflow with is_initial and is_final markers
- Transitions can be global (from_status_id=NULL) or status-specific
- Transition rules support 3 stages: CONDITION (guards), VALIDATOR (pre-checks), POST_FUNCTION (side-effects)
- Transitions can optionally trigger a screen (screen_id FK -> screens in Module 04)
- Workflow schemes map issue_type_id -> workflow_id; default_workflow_id for unmapped types
- Validation ensures: every issue type has a workflow, each workflow has exactly one initial step

### 4.4. Module 04: Fields & Screens ([detailed specs](design/PM_USECASE_SPEC.md#34-module-04-fields--screens))

| UC ID | Name | Actor | Priority | Complexity |
|-------|------|-------|----------|------------|
| UC-PM-301 | Create Custom Field | PM Admin | High | Medium |
| UC-PM-302 | Update Custom Field | PM Admin | Medium | Simple |
| UC-PM-303 | Get Custom Field by ID | PM Admin | Low | Simple |
| UC-PM-304 | List Custom Fields | PM Admin | Medium | Simple |
| UC-PM-305 | Delete Custom Field | PM Admin | Medium | Medium |
| UC-PM-306 | Manage Custom Field Options | PM Admin | Medium | Medium |
| UC-PM-311 | Manage Custom Field Contexts | PM Admin | Medium | Medium |
| UC-PM-321 | Create Field Configuration | PM Admin | Medium | Simple |
| UC-PM-322 | Update Field Configuration | PM Admin | Medium | Simple |
| UC-PM-323 | Get Field Configuration by ID | PM Admin | Low | Simple |
| UC-PM-324 | List Field Configurations | PM Admin | Medium | Simple |
| UC-PM-325 | Delete Field Configuration | PM Admin | Low | Simple |
| UC-PM-326 | Manage Field Configuration Items | PM Admin | Medium | Medium |
| UC-PM-331 | Create Field Config Scheme | PM Admin | Medium | Simple |
| UC-PM-332 | Update Field Config Scheme | PM Admin | Medium | Simple |
| UC-PM-333 | Get Field Config Scheme by ID | PM Admin | Low | Simple |
| UC-PM-334 | List Field Config Schemes | PM Admin | Medium | Simple |
| UC-PM-335 | Delete Field Config Scheme | PM Admin | Low | Simple |
| UC-PM-336 | Manage Field Config Scheme Items | PM Admin | Medium | Medium |
| UC-PM-341 | Create Screen | PM Admin | Medium | Simple |
| UC-PM-342 | Update Screen | PM Admin | Medium | Simple |
| UC-PM-343 | Get Screen by ID | PM Admin | Low | Simple |
| UC-PM-344 | List Screens | PM Admin | Medium | Simple |
| UC-PM-345 | Delete Screen | PM Admin | Low | Simple |
| UC-PM-346 | Manage Screen Tabs | PM Admin | Medium | Medium |
| UC-PM-349 | Manage Screen Tab Fields | PM Admin | Medium | Medium |
| UC-PM-356 | Create Screen Scheme | PM Admin | Medium | Simple |
| UC-PM-357 | Update Screen Scheme | PM Admin | Medium | Simple |
| UC-PM-358 | Get Screen Scheme by ID | PM Admin | Low | Simple |
| UC-PM-359 | List Screen Schemes | PM Admin | Medium | Simple |
| UC-PM-360 | Delete Screen Scheme | PM Admin | Low | Simple |
| UC-PM-361 | Manage Screen Scheme Items | PM Admin | Medium | Medium |
| UC-PM-366 | Create Issue Type Screen Scheme | PM Admin | Medium | Simple |
| UC-PM-367 | Update Issue Type Screen Scheme | PM Admin | Medium | Simple |
| UC-PM-368 | Get Issue Type Screen Scheme by ID | PM Admin | Low | Simple |
| UC-PM-369 | List Issue Type Screen Schemes | PM Admin | Medium | Simple |
| UC-PM-370 | Delete Issue Type Screen Scheme | PM Admin | Low | Simple |
| UC-PM-371 | Manage Issue Type Screen Scheme Items | PM Admin | Medium | Medium |

**Key behaviors:**
- Custom fields have stable keys (customfield_10001), type_key (text, number, date, select, etc.), and search_template
- Field contexts scope fields to specific projects and/or issue types (is_global_context for all)
- Select/multiselect options support cascade (parent_option_id) and soft-disable (is_disabled)
- Field configurations control per-field behavior: is_required, is_hidden, renderer_key
- Field config schemes map issue_type_id -> field_configuration_id
- Screens organize fields into tabs; screen tab fields reference SYSTEM or CUSTOM fields
- Screen schemes map operations (CREATE, EDIT, VIEW, TRANSITION) -> screens
- Issue type screen schemes map issue_type_id -> screen_scheme_id
- 4-layer resolution: project -> issue_type_screen_scheme -> screen_scheme -> screen -> tabs -> fields

### 4.5. Module 05: Permissions & Security ([detailed specs](design/PM_USECASE_SPEC.md#35-module-05-permissions--security))

| UC ID | Name | Actor | Priority | Complexity |
|-------|------|-------|----------|------------|
| UC-PM-401 | Create Permission Definition | PM Admin | Medium | Simple |
| UC-PM-402 | Update Permission Definition | PM Admin | Medium | Simple |
| UC-PM-403 | Get Permission Definition by ID | PM Admin | Low | Simple |
| UC-PM-404 | List Permission Definitions | PM Admin | Medium | Simple |
| UC-PM-405 | Delete Permission Definition | PM Admin | Low | Simple |
| UC-PM-411 | Create Permission Scheme | PM Admin | High | Simple |
| UC-PM-412 | Update Permission Scheme | PM Admin | Medium | Simple |
| UC-PM-413 | Get Permission Scheme by ID | PM Admin | Low | Simple |
| UC-PM-414 | List Permission Schemes | PM Admin | Medium | Simple |
| UC-PM-415 | Delete Permission Scheme | PM Admin | Low | Simple |
| UC-PM-416 | Add Permission Scheme Entry | PM Admin | High | Medium |
| UC-PM-417 | Update Permission Scheme Entry | PM Admin | Medium | Simple |
| UC-PM-418 | Remove Permission Scheme Entry | PM Admin | Medium | Simple |
| UC-PM-419 | List Permission Scheme Entries | PM Admin | Medium | Simple |
| UC-PM-421 | Create Issue Security Scheme | PM Admin | Medium | Simple |
| UC-PM-422 | Update Issue Security Scheme | PM Admin | Medium | Simple |
| UC-PM-423 | Get Issue Security Scheme by ID | PM Admin | Low | Simple |
| UC-PM-424 | List Issue Security Schemes | PM Admin | Medium | Simple |
| UC-PM-425 | Delete Issue Security Scheme | PM Admin | Low | Simple |
| UC-PM-426 | Add Issue Security Level | PM Admin | Medium | Simple |
| UC-PM-427 | Update Issue Security Level | PM Admin | Medium | Simple |
| UC-PM-428 | Remove Issue Security Level | PM Admin | Medium | Simple |
| UC-PM-431 | Add Issue Security Level Member | PM Admin | Medium | Simple |
| UC-PM-432 | Remove Issue Security Level Member | PM Admin | Medium | Simple |
| UC-PM-433 | List Issue Security Level Members | PM Admin | Medium | Simple |

**Key behaviors:**
- Permission definitions are DB-backed (not hardcoded enum) for extensibility
- Permission keys organized by category: PROJECT, ISSUE, COMMENT, ADMIN, AGILE
- Permission scheme entries use polymorphic grantee model: ROLE, GROUP, USER, PROJECT_LEAD, REPORTER, ASSIGNEE
- Entries support ALLOW/DENY effect with optional conditions_json for advanced policies
- Issue security schemes control which users can view specific work items
- Security levels assigned to work items via work_items.security_level_id
- Level members define who can access issues at each security level (by ROLE, GROUP, or USER)

---

## 5. Phase 2 - Enhancement Requirements (Modules 06-09)

> **Schema designed, use case specs pending.** Schema files da duoc hoan thanh cho cac modules nay. Use case specifications se duoc viet khi bat dau Phase 2 implementation.

### 5.1. Module 06: Notifications ([schema](design/schema/06_notifications.md))

Decouple event detection from delivery. PM core stores notification policies and emits delivery jobs via outbox.

- Notification schemes map events (work_item.created, comment.added, etc.) to recipients and channels
- Recipient types: ASSIGNEE, REPORTER, WATCHERS, PROJECT_LEAD, PROJECT_ROLE, GROUP, USER
- Channel support: EMAIL, IN_APP, WEBHOOK
- Template system voi channel-specific templates (subject + body + content_type)
- Outbox pattern for reliable async delivery (PENDING -> PROCESSING -> SENT/FAILED -> DEAD)
- Delivery logs for compliance and troubleshooting (provider, status, detail_json)
- Notification schemes provisioned as project-owned clones from templates

### 5.2. Module 07: Agile & Planning ([schema](design/schema/07_agile.md))

Boards as configurable views on saved filters; sprint planning with history-friendly structures.

- Boards: SCRUM or KANBAN type, backed by a saved filter (filter_id -> search_requests)
- Board columns mapped to workflow statuses (N:N via board_column_statuses)
- WIP constraints: min_wip, max_wip per column
- Quick filters for fast planning (JQL-like query_string)
- Sprints: board-scoped lifecycle (FUTURE -> ACTIVE -> CLOSED)
- Sprint reports: metric snapshots (velocity, burndown, commitment, etc.) stored as JSONB
- Rank fields support multiple ranking dimensions (LEXORANK, FRACTIONAL_INDEX)
- Work item sprint assignment tracked with history (Module 02: work_item_sprints)

### 5.3. Module 08: Search & Reporting ([schema](design/schema/08_search_filters.md))

Search artifacts as first-class entities with explicit sharing and subscription policies.

- Saved filters (search_requests) voi JQL-like query_string
- Multi-user favorites (search_request_favorites)
- Polymorphic share permissions for filters and dashboards (GLOBAL, GROUP, PROJECT, ROLE, USER)
- Filter subscriptions: periodic delivery via cron expression (EMAIL, IN_APP)
- Dashboards voi configurable layouts (AA, AAA, AB, BA)
- Gadgets: pluggable widgets voi implementation key (portlet_id)
- Per-user gadget preferences stored as JSONB

### 5.4. Module 09: Collaboration & Audit ([schema](design/schema/09_collaboration_audit.md))

Collaboration records and audit trails for accountability.

- Issue comments: markdown/JSON body, is_internal flag, edit tracking (edited_at)
- @mentions normalized into comment_mentions table for notification lookup
- Attachments: multi-provider storage (S3, MINIO, LOCAL), thumbnail support, checksum integrity
- Issue watchers: subscribe to updates on specific work items
- Change history: change_groups (one logical action) -> change_items (individual field changes)
- Change items track old/new values for both raw and display representations
- Audit events: generic append-only stream for non-issue entities (projects, sprints, boards, filters)

---

## 6. Phase 3 - Analytics Service (pm_analytics)

> pm_analytics la separate Java service, consume Kafka events tu pm_core.

### 6.1. Metrics Collection
- Consume Kafka events tu pm_core (work item changes, sprint events, etc.)
- Tinh toan va luu metrics snapshots (daily)
- Sprint velocity tracking qua cac sprints
- Burndown/burnup chart data
- Cumulative flow diagram data
- Cycle time va lead time analysis

### 6.2. Dashboards & Reports
- Dashboard tuy chinh voi widgets (velocity chart, burndown, status distribution, resource heatmap)
- Report templates (sprint review, project status, resource report)
- Export PDF, Excel, CSV

### 6.3. Resource Utilization
- Workload per user across projects
- Utilization rate = actual hours / available hours
- Capacity planning

### 6.4. PTM Sync (future)
- PM publish `WORK_ITEM_ASSIGNED` -> ptm_task consume va tao personal task
- Status sync: PM work item status thay doi -> ptm_task cap nhat personal task

### 6.5. Automation Rules (future)
- Trigger actions dua tren events (vd: auto-assign reviewer khi status = "In Review")
- Scheduled actions (vd: noti khi work item sap den due date)

---

## 7. API Specifications

### pm_core API Endpoints

> Base path: `/pm/api/v1`

#### Module 01: Projects & Configuration

```
# Projects
POST   /projects                                     - Create project (with scheme provisioning)
GET    /projects                                     - List projects (filtered, paginated)
GET    /projects/:id                                 - Get project by ID
PUT    /projects/:id                                 - Update project
DELETE /projects/:id                                 - Delete project (soft delete)
POST   /projects/:id/archive                         - Archive project
POST   /projects/:id/unarchive                       - Unarchive project
PUT    /projects/:id/schemes                         - Update project scheme bindings

# Project Categories
POST   /project-categories                           - Create category
GET    /project-categories                           - List categories
GET    /project-categories/:id                       - Get category by ID
PUT    /project-categories/:id                       - Update category
DELETE /project-categories/:id                       - Delete category

# Project Blueprints
POST   /blueprints                                   - Create blueprint
GET    /blueprints                                   - List blueprints
GET    /blueprints/:id                               - Get blueprint by ID
PUT    /blueprints/:id                               - Update blueprint
DELETE /blueprints/:id                               - Delete blueprint
PUT    /blueprints/:id/scheme-defaults               - Manage blueprint scheme defaults

# Project Components
POST   /projects/:projectId/components               - Create component
GET    /projects/:projectId/components               - List components
GET    /components/:id                               - Get component by ID
PUT    /components/:id                               - Update component
DELETE /components/:id                               - Delete component

# Project Versions
POST   /projects/:projectId/versions                 - Create version
GET    /projects/:projectId/versions                 - List versions
GET    /versions/:id                                 - Get version by ID
PUT    /versions/:id                                 - Update version
DELETE /versions/:id                                 - Delete version
POST   /versions/:id/release                         - Release version
POST   /versions/:id/archive                         - Archive version

# Project Roles
POST   /project-roles                                - Create role
GET    /project-roles                                - List roles
GET    /project-roles/:id                            - Get role by ID
PUT    /project-roles/:id                            - Update role
DELETE /project-roles/:id                            - Delete role

# Project Role Actors
POST   /projects/:projectId/roles/:roleId/actors     - Add actor to role
GET    /projects/:projectId/roles/:roleId/actors     - List actors for role
DELETE /projects/:projectId/roles/:roleId/actors/:actorId - Remove actor
```

#### Module 02: Issues & Work Items

```
# Work Items
POST   /projects/:projectId/work-items               - Create work item
GET    /projects/:projectId/work-items               - List work items (filtered, paginated)
GET    /work-items/:id                               - Get work item by ID
PUT    /work-items/:id                               - Update work item
DELETE /work-items/:id                               - Delete work item (soft delete)
POST   /work-items/:id/transitions                   - Transition work item status
PUT    /work-items/:id/assignee                      - Assign work item
PUT    /work-items/:id/rank                          - Re-rank work item (Lexorank)
POST   /work-items/bulk-update                       - Bulk update work items
POST   /work-items/:id/clone                         - Clone work item

# Work Item Components
PUT    /work-items/:id/components                    - Manage work item components

# Work Item Fix Versions
PUT    /work-items/:id/fix-versions                  - Manage work item fix versions

# Work Item Sprints
POST   /work-items/:id/sprints                       - Move to sprint
DELETE /work-items/:id/sprints/:sprintId             - Remove from sprint

# Issue Types
POST   /issue-types                                  - Create issue type
GET    /issue-types                                  - List issue types
GET    /issue-types/:id                              - Get issue type by ID
PUT    /issue-types/:id                              - Update issue type
DELETE /issue-types/:id                              - Delete issue type

# Issue Type Schemes
POST   /issue-type-schemes                           - Create scheme
GET    /issue-type-schemes                           - List schemes
GET    /issue-type-schemes/:id                       - Get scheme by ID
PUT    /issue-type-schemes/:id                       - Update scheme
DELETE /issue-type-schemes/:id                       - Delete scheme
PUT    /issue-type-schemes/:id/items                 - Manage scheme items

# Priorities
POST   /priorities                                   - Create priority
GET    /priorities                                   - List priorities
GET    /priorities/:id                               - Get priority by ID
PUT    /priorities/:id                               - Update priority
DELETE /priorities/:id                               - Delete priority

# Priority Schemes
POST   /priority-schemes                             - Create scheme
GET    /priority-schemes                             - List schemes
GET    /priority-schemes/:id                         - Get scheme by ID
PUT    /priority-schemes/:id                         - Update scheme
DELETE /priority-schemes/:id                         - Delete scheme
PUT    /priority-schemes/:id/items                   - Manage scheme items

# Resolutions
POST   /resolutions                                  - Create resolution
GET    /resolutions                                  - List resolutions
GET    /resolutions/:id                              - Get resolution by ID
PUT    /resolutions/:id                              - Update resolution
DELETE /resolutions/:id                              - Delete resolution

# Issue Link Types
POST   /issue-link-types                             - Create link type
GET    /issue-link-types                             - List link types
GET    /issue-link-types/:id                         - Get link type by ID
PUT    /issue-link-types/:id                         - Update link type
DELETE /issue-link-types/:id                         - Delete link type

# Issue Links
POST   /work-items/:id/links                        - Create issue link
GET    /work-items/:id/links                        - List links for work item
DELETE /issue-links/:id                              - Delete issue link

# Worklogs
POST   /work-items/:id/worklogs                     - Create worklog
GET    /work-items/:id/worklogs                     - List worklogs for work item
GET    /worklogs/:id                                - Get worklog by ID
PUT    /worklogs/:id                                - Update worklog
DELETE /worklogs/:id                                - Delete worklog
```

#### Module 03: Workflow Engine

```
# Status Categories
POST   /status-categories                            - Create status category
GET    /status-categories                            - List status categories
GET    /status-categories/:id                        - Get status category by ID
PUT    /status-categories/:id                        - Update status category
DELETE /status-categories/:id                        - Delete status category

# Statuses
POST   /statuses                                     - Create status
GET    /statuses                                     - List statuses
GET    /statuses/:id                                 - Get status by ID
PUT    /statuses/:id                                 - Update status
DELETE /statuses/:id                                 - Delete status

# Workflows
POST   /workflows                                    - Create workflow
GET    /workflows                                    - List workflows
GET    /workflows/:id                                - Get workflow by ID (with steps, transitions)
PUT    /workflows/:id                                - Update workflow
DELETE /workflows/:id                                - Delete workflow
POST   /workflows/:id/publish                        - Publish workflow (increment version)
POST   /workflows/:id/clone                          - Clone workflow
POST   /workflows/:id/validate                       - Validate workflow

# Workflow Steps
POST   /workflows/:id/steps                          - Add workflow step
DELETE /workflows/:id/steps/:stepId                  - Remove workflow step
PUT    /workflows/:id/steps/reorder                  - Reorder workflow steps

# Workflow Transitions
POST   /workflows/:id/transitions                    - Add transition
PUT    /workflows/:id/transitions/:transId           - Update transition
DELETE /workflows/:id/transitions/:transId           - Remove transition
GET    /workflows/:id/transitions                    - List transitions

# Workflow Transition Rules
POST   /transitions/:transId/rules                   - Add transition rule
PUT    /transition-rules/:ruleId                     - Update transition rule
DELETE /transition-rules/:ruleId                     - Remove transition rule

# Workflow Schemes
POST   /workflow-schemes                             - Create workflow scheme
GET    /workflow-schemes                             - List workflow schemes
GET    /workflow-schemes/:id                         - Get workflow scheme by ID
PUT    /workflow-schemes/:id                         - Update workflow scheme
DELETE /workflow-schemes/:id                         - Delete workflow scheme
PUT    /workflow-schemes/:id/items                   - Manage scheme items
```

#### Module 04: Fields & Screens

```
# Custom Fields
POST   /custom-fields                                - Create custom field
GET    /custom-fields                                - List custom fields
GET    /custom-fields/:id                            - Get custom field by ID
PUT    /custom-fields/:id                            - Update custom field
DELETE /custom-fields/:id                            - Delete custom field
PUT    /custom-fields/:id/options                    - Manage options
PUT    /custom-fields/:id/contexts                   - Manage contexts

# Field Configurations
POST   /field-configurations                         - Create field config
GET    /field-configurations                         - List field configs
GET    /field-configurations/:id                     - Get field config by ID
PUT    /field-configurations/:id                     - Update field config
DELETE /field-configurations/:id                     - Delete field config
PUT    /field-configurations/:id/items               - Manage field config items

# Field Config Schemes
POST   /field-config-schemes                         - Create scheme
GET    /field-config-schemes                         - List schemes
GET    /field-config-schemes/:id                     - Get scheme by ID
PUT    /field-config-schemes/:id                     - Update scheme
DELETE /field-config-schemes/:id                     - Delete scheme
PUT    /field-config-schemes/:id/items               - Manage scheme items

# Screens
POST   /screens                                      - Create screen
GET    /screens                                      - List screens
GET    /screens/:id                                  - Get screen by ID
PUT    /screens/:id                                  - Update screen
DELETE /screens/:id                                  - Delete screen
PUT    /screens/:id/tabs                             - Manage screen tabs
PUT    /screens/:id/tabs/:tabId/fields               - Manage tab fields

# Screen Schemes
POST   /screen-schemes                               - Create screen scheme
GET    /screen-schemes                               - List screen schemes
GET    /screen-schemes/:id                           - Get screen scheme by ID
PUT    /screen-schemes/:id                           - Update screen scheme
DELETE /screen-schemes/:id                           - Delete screen scheme
PUT    /screen-schemes/:id/items                     - Manage scheme items

# Issue Type Screen Schemes
POST   /issue-type-screen-schemes                    - Create ITSS
GET    /issue-type-screen-schemes                    - List ITSS
GET    /issue-type-screen-schemes/:id                - Get ITSS by ID
PUT    /issue-type-screen-schemes/:id                - Update ITSS
DELETE /issue-type-screen-schemes/:id                - Delete ITSS
PUT    /issue-type-screen-schemes/:id/items          - Manage ITSS items
```

#### Module 05: Permissions & Security

```
# Permission Definitions
POST   /permission-definitions                       - Create permission
GET    /permission-definitions                       - List permissions
GET    /permission-definitions/:id                   - Get permission by ID
PUT    /permission-definitions/:id                   - Update permission
DELETE /permission-definitions/:id                   - Delete permission

# Permission Schemes
POST   /permission-schemes                           - Create scheme
GET    /permission-schemes                           - List schemes
GET    /permission-schemes/:id                       - Get scheme by ID
PUT    /permission-schemes/:id                       - Update scheme
DELETE /permission-schemes/:id                       - Delete scheme
POST   /permission-schemes/:id/entries               - Add entry
PUT    /permission-scheme-entries/:id                - Update entry
DELETE /permission-scheme-entries/:id                - Remove entry
GET    /permission-schemes/:id/entries               - List entries

# Issue Security Schemes
POST   /issue-security-schemes                       - Create scheme
GET    /issue-security-schemes                       - List schemes
GET    /issue-security-schemes/:id                   - Get scheme by ID
PUT    /issue-security-schemes/:id                   - Update scheme
DELETE /issue-security-schemes/:id                   - Delete scheme
POST   /issue-security-schemes/:id/levels            - Add security level
PUT    /issue-security-levels/:id                    - Update security level
DELETE /issue-security-levels/:id                    - Remove security level
POST   /issue-security-levels/:id/members            - Add level member
DELETE /issue-security-level-members/:id             - Remove level member
GET    /issue-security-levels/:id/members            - List level members
```

#### Phase 2 Modules (07-09) - API TBD

```
# Module 07: Agile
POST   /boards                                       - Create board
GET    /boards                                       - List boards
GET    /boards/:id                                   - Get board by ID (with columns, statuses)
PUT    /boards/:id                                   - Update board
DELETE /boards/:id                                   - Delete board
PUT    /boards/:id/columns                           - Manage columns
POST   /boards/:id/sprints                           - Create sprint
GET    /boards/:id/sprints                           - List sprints
POST   /sprints/:id/activate                         - Activate sprint
POST   /sprints/:id/complete                         - Complete sprint
GET    /sprints/:id/report                           - Get sprint report

# Module 08: Search & Filters
POST   /filters                                      - Create saved filter
GET    /filters                                      - List filters
GET    /filters/:id                                  - Get filter by ID
PUT    /filters/:id                                  - Update filter
DELETE /filters/:id                                  - Delete filter
POST   /dashboards                                   - Create dashboard
GET    /dashboards                                   - List dashboards

# Module 09: Collaboration
POST   /work-items/:id/comments                     - Add comment
GET    /work-items/:id/comments                     - List comments
PUT    /comments/:id                                - Update comment
DELETE /comments/:id                                - Delete comment
POST   /work-items/:id/attachments                  - Upload attachment
GET    /work-items/:id/attachments                  - List attachments
POST   /work-items/:id/watchers                     - Add watcher
DELETE /work-items/:id/watchers/:userId              - Remove watcher
GET    /work-items/:id/changelog                    - Get change history
```

### pm_analytics API Endpoints (Phase 3)
```
GET    /analytics/projects/:id/velocity              - Velocity chart
GET    /analytics/projects/:id/burndown              - Burndown chart
GET    /analytics/projects/:id/throughput             - Throughput metrics
GET    /analytics/projects/:id/cycle-time             - Cycle time
GET    /analytics/projects/:id/cumulative-flow        - Cumulative flow diagram

GET    /analytics/sprints/:id/metrics                 - Sprint metrics
GET    /analytics/sprints/:id/burndown                - Sprint burndown

GET    /analytics/resources/utilization               - Resource utilization
GET    /analytics/resources/:userId/workload          - User workload

POST   /dashboards                                   - Create dashboard
GET    /dashboards                                   - List dashboards
GET    /dashboards/:id                               - Get dashboard
PATCH  /dashboards/:id                               - Update dashboard
DELETE /dashboards/:id                               - Delete dashboard
POST   /dashboards/:id/widgets                       - Add widget
PATCH  /dashboards/:id/widgets/:widgetId             - Update widget
DELETE /dashboards/:id/widgets/:widgetId             - Delete widget

POST   /reports/generate                             - Generate report
GET    /reports/templates                            - Report templates
```

---

## 8. Kafka Events

> Events follow the **transactional outbox pattern**: pm_core writes events to the `notification_outbox` table (see [Module 06](design/schema/06_notifications.md)) within the same DB transaction as the business operation, then a poller/CDC publishes them to Kafka. This guarantees at-least-once delivery with no dual-write risk.
>
> Audit events are also persisted locally in `audit_events` (see [Module 09](design/schema/09_collaboration_audit.md)) for compliance queries without depending on Kafka consumers.

### Topics

| Topic | Publisher | Consumers | Notes |
|-------|-----------|-----------|-------|
| `pm.project` | pm_core | pm_analytics, logging_tracker | Project lifecycle events |
| `pm.workitem` | pm_core | pm_analytics, notification_service, ptm_task (future) | Issue CRUD + transitions |
| `pm.workflow` | pm_core | pm_analytics | Workflow publish/scheme changes |
| `pm.sprint` | pm_core | pm_analytics | Sprint lifecycle (future, active, closed) |
| `pm.board` | pm_core | pm_analytics | Board configuration changes |
| `pm.comment` | pm_core | notification_service | Comments + mentions |
| `pm.worklog` | pm_core | pm_analytics | Time tracking entries |
| `pm.notification.outbox` | pm_core (poller) | notification_service, mailservice | Channel-specific delivery jobs |
| `pm.scheme` | pm_core | logging_tracker | Scheme CRUD (issue type, permission, etc.) |

### Event Types

**Module 01 — Project events:**
- `PROJECT_CREATED` — project provisioned (includes scheme clone results)
- `PROJECT_UPDATED` — project metadata changed
- `PROJECT_ARCHIVED` — project archived
- `PROJECT_UNARCHIVED` — project restored from archive
- `PROJECT_DELETED` — project soft-deleted
- `PROJECT_SCHEMES_UPDATED` — project scheme bindings re-provisioned
- `VERSION_RELEASED` — project version released
- `VERSION_ARCHIVED` — project version archived
- `ROLE_ACTOR_ADDED` — user/group added to project role
- `ROLE_ACTOR_REMOVED` — user/group removed from project role

**Module 02 — Work item events:**
- `WORK_ITEM_CREATED` — issue created
- `WORK_ITEM_UPDATED` — issue fields updated (includes change_items diff)
- `WORK_ITEM_DELETED` — issue soft-deleted
- `WORK_ITEM_TRANSITIONED` — status changed via workflow transition
- `WORK_ITEM_ASSIGNED` — assignee set or changed
- `WORK_ITEM_RANKED` — Lexorank position changed
- `WORK_ITEM_LINKED` — issue link created
- `WORK_ITEM_UNLINKED` — issue link removed
- `WORK_ITEM_CLONED` — issue cloned
- `WORK_ITEM_BULK_UPDATED` — batch update completed

**Module 03 — Workflow events:**
- `WORKFLOW_PUBLISHED` — workflow version incremented and published
- `WORKFLOW_SCHEME_UPDATED` — workflow scheme items changed

**Module 07 — Sprint events (Phase 2):**
- `SPRINT_CREATED` — sprint created on board
- `SPRINT_ACTIVATED` — sprint started (state: FUTURE -> ACTIVE)
- `SPRINT_COMPLETED` — sprint closed (state: ACTIVE -> CLOSED)
- `SPRINT_ITEMS_MOVED` — work items moved to/from sprint

**Module 07 — Board events (Phase 2):**
- `BOARD_CREATED` — board created
- `BOARD_UPDATED` — board columns/config changed

**Module 09 — Collaboration events (Phase 2):**
- `COMMENT_CREATED` — comment added (includes mentioned_user_ids from `comment_mentions`)
- `COMMENT_UPDATED` — comment body edited
- `COMMENT_DELETED` — comment soft-deleted
- `ATTACHMENT_UPLOADED` — file attached to issue
- `ATTACHMENT_DELETED` — attachment removed
- `WATCHER_ADDED` — user watching an issue
- `WATCHER_REMOVED` — user un-watching an issue

**Module 02 — Worklog events:**
- `WORKLOG_CREATED` — time logged on issue
- `WORKLOG_UPDATED` — worklog entry modified
- `WORKLOG_DELETED` — worklog entry removed

**Module 06 — Notification outbox events:**
- `NOTIFICATION_QUEUED` — delivery job written to outbox (status: PENDING)
- `NOTIFICATION_SENT` — delivery confirmed (status: SENT)
- `NOTIFICATION_FAILED` — delivery failed, scheduled for retry
- `NOTIFICATION_DEAD` — max retries exhausted (status: DEAD)

### Message Envelope

All events share a standard envelope. The `data` payload varies by event type.

```json
{
  "meta": {
    "id": "uuid-v7",
    "type": "WORK_ITEM_TRANSITIONED",
    "source": "pm_core",
    "version": "1.0",
    "timestamp": 1704067200000,
    "traceId": "uuid",
    "tenantId": 1,
    "entityType": "WORK_ITEM",
    "entityId": 123,
    "actorId": 456
  },
  "data": { }
}
```

### Example Payloads

**WORK_ITEM_TRANSITIONED:**
```json
{
  "meta": { "type": "WORK_ITEM_TRANSITIONED", "entityType": "WORK_ITEM", "entityId": 123, "..." : "..." },
  "data": {
    "projectId": 10,
    "itemKey": "PROJ-123",
    "issueTypeId": 2,
    "transitionId": 45,
    "fromStatusId": 1,
    "toStatusId": 3,
    "fromStatusName": "To Do",
    "toStatusName": "In Progress",
    "assigneeId": 456,
    "resolutionId": null
  }
}
```

**COMMENT_CREATED:**
```json
{
  "meta": { "type": "COMMENT_CREATED", "entityType": "COMMENT", "entityId": 789, "..." : "..." },
  "data": {
    "issueId": 123,
    "projectId": 10,
    "authorId": 456,
    "isInternal": false,
    "mentionedUserIds": [111, 222]
  }
}
```

**NOTIFICATION_QUEUED (outbox):**
```json
{
  "meta": { "type": "NOTIFICATION_QUEUED", "entityType": "NOTIFICATION_OUTBOX", "entityId": 500, "..." : "..." },
  "data": {
    "dedupeKey": "work_item.transitioned:123:1704067200",
    "eventKey": "work_item.transitioned",
    "channel": "EMAIL",
    "recipient": "user@example.com",
    "templateId": 15,
    "payloadJson": { "issueKey": "PROJ-123", "newStatus": "In Progress" }
  }
}
```

### Event Ordering & Guarantees

| Guarantee | Implementation |
|-----------|---------------|
| At-least-once delivery | Outbox table + poller with status tracking |
| Ordering per entity | Kafka partition key = `tenantId:entityType:entityId` |
| Idempotency | `dedupe_key` on `notification_outbox`; consumers deduplicate by `meta.id` |
| No dual-write | Events written in same DB transaction as business data |
| Retry with backoff | `retry_count` + `next_retry_at` on outbox; exponential backoff |
| Dead letter | Status = DEAD after max retries; manual review via `notification_delivery_logs` |

---

## 9. Integration Points

| Target Service | Mechanism | Direction | Purpose |
|---|---|---|---|
| **account** (8081) | HTTP (sync) | pm_core -> account | Resolve user profiles, validate org/tenant membership, verify group memberships for permission scheme entries |
| **notification_service** (8090) | Kafka (`pm.notification.outbox`) | pm_core -> notification | Deliver IN_APP notifications via outbox pattern; notification_service consumes delivery jobs |
| **mailservice** (8091) | Kafka (`pm.notification.outbox`) | pm_core -> mail | Deliver EMAIL notifications; mailservice consumes outbox jobs where channel=EMAIL |
| **logging_tracker** (8082) | Kafka (`pm.project`, `pm.scheme`) | pm_core -> logging | Audit trail for compliance; also backed by local `audit_events` table ([Module 09](design/schema/09_collaboration_audit.md)) |
| **ptm_task** (8083) | Kafka (`pm.workitem`) | pm_core -> ptm (future) | Sync assigned work items to personal task list; `WORK_ITEM_ASSIGNED` -> create/update personal task |
| **discuss_service** (8092) | Kafka (optional) | pm_core -> discuss | Auto-create discussion channel per project on `PROJECT_CREATED` |
| **file_storage** (S3/MinIO) | HTTP (sync) | pm_core -> storage | Upload/download attachments; `attachments` table stores `storage_key` ([Module 09](design/schema/09_collaboration_audit.md)) |
| **Keycloak** (8180) | JWKS (async) | api_gateway -> Keycloak | JWT validation; pm_core trusts tokens already validated by API Gateway |

### Integration Notes

1. **Outbox-first delivery**: pm_core never calls notification_service or mailservice directly. All notifications go through the `notification_outbox` table, then a poller publishes to Kafka topics.
2. **Account lookups are cached**: User profile and group membership data from account service is cached in Redis with TTL to avoid hot-path HTTP calls.
3. **PTM sync is eventual**: Work item assignment events are consumed asynchronously. PTM task creation may lag behind PM updates.
4. **File storage is pluggable**: The `storage_provider` column on `attachments` supports S3, MinIO, or LOCAL backends.

---

## 10. Non-functional Requirements

### Performance
- API response time < 200ms for CRUD operations
- Board view (Kanban) load < 500ms for 500 work items
- Backlog load < 300ms for 1000 work items (paginated)
- Workflow transition validation < 50ms (conditions + validators)
- Scheme provisioning (deep clone) < 2s per project creation

### Scalability
- Support 100+ concurrent users per tenant
- Support 1000+ projects per tenant
- Support 50,000+ work items per project
- Support 10+ scheme types with deep-clone isolation per project

### Security
- JWT authentication via API Gateway + Keycloak
- Project-level access control via permission schemes ([Module 05](design/schema/05_permissions_security.md))
- Issue-level security via issue security schemes + levels
- Role-based permissions: project roles with configurable actors (users, groups)
- Tenant isolation: `tenant_id` filter on every query, enforced at adapter layer
- Soft delete: `deleted_at` column on all entities (no permanent data loss)

### Reliability
- Kafka events via transactional outbox (no dual-write risk)
- Idempotent event processing: `dedupe_key` on outbox, `meta.id` for consumers
- Database transactions for all multi-entity operations (scheme provisioning, bulk updates)
- Retry with exponential backoff for failed notification deliveries
- Dead letter tracking: `notification_delivery_logs` for failed deliveries ([Module 06](design/schema/06_notifications.md))
- Graceful error handling: no internal errors exposed to clients

### Auditability
- Change history: `change_groups` + `change_items` for every work item field change ([Module 09](design/schema/09_collaboration_audit.md))
- Generic audit events: `audit_events` table for non-issue entity changes
- Notification delivery logs: immutable `notification_delivery_logs` for compliance

---

## 11. Tech Stack

### pm_core
- **Language:** Go 1.22+
- **Framework:** Gin (HTTP), GORM (ORM)
- **DI:** Uber FX
- **Database:** PostgreSQL (pm_core_db)
- **Cache:** Redis (user profiles, permission lookups, scheme metadata)
- **Message Queue:** Kafka (via transactional outbox pattern)
- **File Storage:** S3/MinIO (attachments — see [Module 09](design/schema/09_collaboration_audit.md))
- **Auth:** JWT via Keycloak JWKS (validated at API Gateway)
- **Architecture:** Clean Architecture (Controller -> UseCase -> Service -> Port -> Adapter)
- **Key Patterns:**
  - Transactional outbox for event publishing
  - Deep-clone scheme provisioning ([Module 00](design/schema/00_project_provisioning.md))
  - Lexorank for issue ordering
  - Change tracking via change_groups/change_items

### pm_analytics (Phase 3)
- **Language:** Java 21
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL (pm_analytics_db)
- **Migrations:** Flyway
- **Message Queue:** Kafka (consumer — subscribes to `pm.*` topics)
- **Auth:** JWT via Keycloak JWKS
- **Architecture:** Clean Architecture (same pattern as account, crm services)
