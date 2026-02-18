# PM Core - Database Schema Index

**Author:** Quan Tuan Huy  
**Updated:** 2026-02-14  
**Design Philosophy:** Scheme-based Architecture (JIRA-like) + Row-level Multi-tenancy

The ERD is split into modules to keep configuration-heavy JIRA-like capabilities maintainable while still supporting long-term extensibility.

## Global Design Rules (Applied To All Modules)

1. **Row-level multi-tenancy**: every business table contains `tenant_id` and all queries must filter by `tenant_id`.
2. **Auditability**: all mutable entities include `created_at`, `updated_at`, `created_by`, `updated_by`.
3. **Soft delete by default**: use `deleted_at` for recoverability and historical integrity.
4. **Extensible config model**: prefer relational tables for query-heavy rules; use JSONB only for plugin-style configs.
5. **Scheme indirection**: projects reference schemes to allow bulk configuration changes without mass updates.
6. **Cross-module consistency**: names, FK targets, and key formats are standardized.

## Table of Contents

1. **[Projects & Configuration](./schema/01_projects.md)**: Projects, Categories, Blueprints, Project Roles, Scheme bindings.
2. **[Issues & Work Items](./schema/02_issues.md)**: Work item core, issue types, priorities, links, worklogs, custom field values.
3. **[Workflow Engine](./schema/03_workflows.md)**: Statuses, workflow steps, transitions, rules, workflow schemes.
4. **[Fields & Screens](./schema/04_fields_screens.md)**: Custom fields, contexts, field configs, screen schemes.
5. **[Permissions & Security](./schema/05_permissions_security.md)**: Permission definitions, schemes, issue security levels.
6. **[Notifications](./schema/06_notifications.md)**: Event-to-recipient mapping, templates, outbox delivery.
7. **[Agile & Planning](./schema/07_agile.md)**: Boards, columns, sprints, quick filters, ranking support.
8. **[Search & Reporting](./schema/08_search_filters.md)**: Saved filters, sharing, subscriptions, dashboards, gadgets.
9. **[Collaboration & Audit](./schema/09_collaboration_audit.md)**: Comments, attachments, watchers, change logs, audit events.
