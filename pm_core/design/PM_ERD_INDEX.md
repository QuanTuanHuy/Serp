# PM Core - Database Schema Index

**Author:** Quan Tuan Huy  
**Updated:** 2026-03-04  
**Design Philosophy:** Jira-closer parity for company-managed projects: shared scheme association by default, optional clone-on-associate isolation, plus row-level multi-tenancy

The ERD is split into modules to keep configuration-heavy JIRA-like capabilities maintainable while still supporting long-term extensibility.

## Global Design Rules (Applied To All Modules)

1. **Row-level multi-tenancy**: every business table contains `tenant_id` and all queries must filter by `tenant_id`.
2. **Auditability**: all mutable entities include `created_at`, `updated_at`, `created_by`, `updated_by`.
3. **Soft delete by default**: use `deleted_at` for recoverability and historical integrity.
4. **Extensible config model**: prefer relational tables for query-heavy rules; use JSONB only for plugin-style configs.
5. **Shared-scheme default (Jira parity)**: project creation associates schemes directly by default, so one scheme can be reused by multiple projects.
6. **Optional isolation mode**: clone-on-associate is supported for tenants that want project-specific copies and reduced blast radius.
7. **Safe association changes**: scheme reassociation must validate compatibility and provide migration controls before applying.
8. **Cross-module consistency**: names, FK targets, and key formats are standardized.

## Table of Contents

1. **[Project Provisioning & Scheme Association](./schema/00_project_provisioning.md)**: Shared-association defaults, optional clone flow, and compatibility gates.
2. **[Projects & Configuration](./schema/01_projects.md)**: Projects, Categories, Blueprints, Project Roles, Scheme bindings.
3. **[Issues & Work Items](./schema/02_issues.md)**: Work item core, issue types, priorities, links, worklogs, custom field values.
4. **[Workflow Engine](./schema/03_workflows.md)**: Statuses, workflow steps, transitions, rules, workflow schemes.
5. **[Fields & Screens](./schema/04_fields_screens.md)**: Custom fields, contexts, field configs, screen schemes.
6. **[Permissions & Security](./schema/05_permissions_security.md)**: Permission definitions, schemes, issue security levels.
7. **[Notifications](./schema/06_notifications.md)**: Event-to-recipient mapping, templates, outbox delivery.
8. **[Agile & Planning](./schema/07_agile.md)**: Boards, columns, sprints, quick filters, ranking support.
9. **[Search & Reporting](./schema/08_search_filters.md)**: Saved filters, sharing, subscriptions, dashboards, gadgets.
10. **[Collaboration & Audit](./schema/09_collaboration_audit.md)**: Comments, attachments, watchers, change logs, audit events.
