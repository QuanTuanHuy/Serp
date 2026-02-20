# PM Core - Database Schema Index

**Author:** Quan Tuan Huy  
**Updated:** 2026-02-20  
**Design Philosophy:** Blueprint-driven Scheme Provisioning (JIRA-like) + Row-level Multi-tenancy

The ERD is split into modules to keep configuration-heavy JIRA-like capabilities maintainable while still supporting long-term extensibility.

## Global Design Rules (Applied To All Modules)

1. **Row-level multi-tenancy**: every business table contains `tenant_id` and all queries must filter by `tenant_id`.
2. **Auditability**: all mutable entities include `created_at`, `updated_at`, `created_by`, `updated_by`.
3. **Soft delete by default**: use `deleted_at` for recoverability and historical integrity.
4. **Extensible config model**: prefer relational tables for query-heavy rules; use JSONB only for plugin-style configs.
5. **Project isolation by default**: project creation provisions project-owned scheme clones from blueprint defaults (or system template defaults) so cross-project changes are avoided.
6. **Template reuse at provisioning time**: blueprints and system schemes are reusable sources; runtime customization happens on project-owned scheme copies.
7. **Cross-module consistency**: names, FK targets, and key formats are standardized.

## Table of Contents

1. **[Project Provisioning & Scheme Cloning](./schema/00_project_provisioning.md)**: Deep clone order, FK-safe provisioning sequence, and validation gates.
2. **[Projects & Configuration](./schema/01_projects.md)**: Projects, Categories, Blueprints, Project Roles, Scheme bindings.
3. **[Issues & Work Items](./schema/02_issues.md)**: Work item core, issue types, priorities, links, worklogs, custom field values.
4. **[Workflow Engine](./schema/03_workflows.md)**: Statuses, workflow steps, transitions, rules, workflow schemes.
5. **[Fields & Screens](./schema/04_fields_screens.md)**: Custom fields, contexts, field configs, screen schemes.
6. **[Permissions & Security](./schema/05_permissions_security.md)**: Permission definitions, schemes, issue security levels.
7. **[Notifications](./schema/06_notifications.md)**: Event-to-recipient mapping, templates, outbox delivery.
8. **[Agile & Planning](./schema/07_agile.md)**: Boards, columns, sprints, quick filters, ranking support.
9. **[Search & Reporting](./schema/08_search_filters.md)**: Saved filters, sharing, subscriptions, dashboards, gadgets.
10. **[Collaboration & Audit](./schema/09_collaboration_audit.md)**: Comments, attachments, watchers, change logs, audit events.
