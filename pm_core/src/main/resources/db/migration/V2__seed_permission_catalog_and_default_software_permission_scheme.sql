CREATE TABLE IF NOT EXISTS permission_definitions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    permission_key VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_permission_definitions_category CHECK (
        category IN (
            'ADMINISTRATION',
            'PROJECT',
            'ISSUE',
            'VOTERS_WATCHERS',
            'COMMENTS',
            'ATTACHMENTS',
            'TIME_TRACKING'
        )
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_permission_definitions_tenant_key
    ON permission_definitions (tenant_id, permission_key)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS permission_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_permission_schemes_tenant_name
    ON permission_schemes (tenant_id, name);

CREATE TABLE IF NOT EXISTS permission_scheme_entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    permission_key VARCHAR(100) NOT NULL,
    grantee_type VARCHAR(40) NOT NULL,
    grantee_ref VARCHAR(255),
    custom_field_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_permission_scheme_entries_scheme
    ON permission_scheme_entries (tenant_id, scheme_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_permission_scheme_entries_grant
    ON permission_scheme_entries (
        tenant_id,
        scheme_id,
        permission_key,
        grantee_type,
        COALESCE(grantee_ref, '__CTX__'),
        COALESCE(custom_field_id, 0)
    )
    WHERE deleted_at IS NULL;

INSERT INTO permission_definitions (
    tenant_id,
    permission_key,
    name,
    description,
    category,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    0,
    seeded.permission_key,
    seeded.name,
    seeded.description,
    seeded.category,
    TRUE,
    NOW(),
    0,
    NOW(),
    0
FROM (
    VALUES
        ('ADMINISTER_PROJECTS', 'Administer Projects', 'Administer project settings, project role membership, components, versions, and project details.', 'ADMINISTRATION'),
        ('EDIT_WORKFLOWS', 'Edit Workflows', 'Edit project-scoped workflows under guardrails for company-managed projects.', 'ADMINISTRATION'),
        ('MANAGE_WORK_ITEM_LAYOUTS', 'Manage Work Item Layouts', 'Manage project-scoped work item layout and screen arrangements under guardrails.', 'ADMINISTRATION'),
        ('BROWSE_PROJECTS', 'Browse Projects', 'Browse projects, search work items, and view work items subject to issue security.', 'PROJECT'),
        ('MANAGE_SPRINTS', 'Manage Sprints', 'Create, start, complete, reorder, and configure sprints.', 'PROJECT'),
        ('MANAGE_VERSIONS', 'Manage Versions', 'Create, release, archive, edit, merge, and delete project versions.', 'PROJECT'),
        ('VIEW_DEVELOPMENT_TOOLS', 'View Development Tools', 'View development information linked to work items.', 'PROJECT'),
        ('VIEW_READ_ONLY_WORKFLOW', 'View Read-only Workflow', 'View project and work item workflows in read-only mode.', 'PROJECT'),
        ('VIEW_AGGREGATED_DATA', 'View Aggregated Data', 'View aggregated backlog, board, and deployment insights for the project.', 'PROJECT'),
        ('ARCHIVE_ISSUES', 'Archive Issues', 'Archive work items instead of deleting them permanently.', 'ISSUE'),
        ('RESTORE_ARCHIVED_ISSUES', 'Restore Archived Issues', 'Restore archived work items back into active project data.', 'ISSUE'),
        ('ASSIGN_ISSUES', 'Assign Issues', 'Change the assignee on work items.', 'ISSUE'),
        ('ASSIGNABLE_USER', 'Assignable User', 'Allow a user to be selected as an assignee on work items.', 'ISSUE'),
        ('CLOSE_ISSUES', 'Close Issues', 'Close work items when workflow and resolution conditions are satisfied.', 'ISSUE'),
        ('CREATE_ISSUES', 'Create Issues', 'Create work items and subtasks within the project.', 'ISSUE'),
        ('DELETE_ISSUES', 'Delete Issues', 'Delete work items and bundled issue data.', 'ISSUE'),
        ('EDIT_ISSUES', 'Edit Issues', 'Edit work item fields subject to field-level permission rules.', 'ISSUE'),
        ('LINK_ISSUES', 'Link Issues', 'Create and delete links between work items.', 'ISSUE'),
        ('MODIFY_REPORTER', 'Modify Reporter', 'Set or update reporter on behalf of another user.', 'ISSUE'),
        ('MOVE_ISSUES', 'Move Issues', 'Move work items across projects or issue types.', 'ISSUE'),
        ('RESOLVE_ISSUES', 'Resolve Issues', 'Set or clear resolution in conjunction with workflow transitions.', 'ISSUE'),
        ('SCHEDULE_ISSUES', 'Schedule Issues', 'Edit due date and ranking or scheduling-related fields.', 'ISSUE'),
        ('SET_ISSUE_SECURITY', 'Set Issue Security', 'Set or change issue security level on work items.', 'ISSUE'),
        ('TRANSITION_ISSUES', 'Transition Issues', 'Execute workflow transitions on work items.', 'ISSUE'),
        ('MANAGE_WATCHERS', 'Manage Watchers', 'Add and remove watchers on work items.', 'VOTERS_WATCHERS'),
        ('VIEW_VOTERS_AND_WATCHERS', 'View Voters and Watchers', 'View watcher and voter lists on work items.', 'VOTERS_WATCHERS'),
        ('ADD_COMMENTS', 'Add Comments', 'Add comments or internal notes to work items.', 'COMMENTS'),
        ('DELETE_ALL_COMMENTS', 'Delete All Comments', 'Delete any comment from a work item.', 'COMMENTS'),
        ('DELETE_OWN_COMMENTS', 'Delete Own Comments', 'Delete comments authored by the current user.', 'COMMENTS'),
        ('EDIT_ALL_COMMENTS', 'Edit All Comments', 'Edit any comment on a work item.', 'COMMENTS'),
        ('EDIT_OWN_COMMENTS', 'Edit Own Comments', 'Edit comments authored by the current user.', 'COMMENTS'),
        ('CREATE_ATTACHMENTS', 'Create Attachments', 'Upload attachments to work items when attachments are enabled.', 'ATTACHMENTS'),
        ('DELETE_ALL_ATTACHMENTS', 'Delete All Attachments', 'Delete any attachment from a work item.', 'ATTACHMENTS'),
        ('DELETE_OWN_ATTACHMENTS', 'Delete Own Attachments', 'Delete attachments uploaded by the current user.', 'ATTACHMENTS'),
        ('WORK_ON_ISSUES', 'Work On Issues', 'Log work on work items and create worklogs.', 'TIME_TRACKING'),
        ('DELETE_ALL_WORKLOGS', 'Delete All Worklogs', 'Delete any worklog entry.', 'TIME_TRACKING'),
        ('DELETE_OWN_WORKLOGS', 'Delete Own Worklogs', 'Delete worklog entries authored by the current user.', 'TIME_TRACKING'),
        ('EDIT_ALL_WORKLOGS', 'Edit All Worklogs', 'Edit any worklog entry.', 'TIME_TRACKING'),
        ('EDIT_OWN_WORKLOGS', 'Edit Own Worklogs', 'Edit worklog entries authored by the current user.', 'TIME_TRACKING')
) AS seeded(permission_key, name, description, category)
WHERE NOT EXISTS (
    SELECT 1
    FROM permission_definitions existing
    WHERE existing.tenant_id = 0
      AND existing.permission_key = seeded.permission_key
      AND existing.deleted_at IS NULL
);

INSERT INTO permission_schemes (
    tenant_id,
    name,
    description,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    0,
    'Default Software Permission Scheme',
    'System-seeded Jira-aligned default software permission scheme. Project lead receives bootstrap access immediately; project-role grants (Administrators, Developers, Users) expand access once role actors are assigned.',
    NOW(),
    0,
    NOW(),
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM permission_schemes existing
    WHERE existing.tenant_id = 0
      AND existing.name = 'Default Software Permission Scheme'
      AND existing.deleted_at IS NULL
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'project_roles'
    ) THEN
        INSERT INTO project_roles (
            tenant_id,
            name,
            description,
            is_system,
            created_at,
            created_by,
            updated_at,
            updated_by
        )
        SELECT
            0,
            seeded.name,
            seeded.description,
            TRUE,
            NOW(),
            0,
            NOW(),
            0
        FROM (
            VALUES
                ('Administrators', 'System project role for project administrators and elevated project governance.'),
                ('Developers', 'System project role for users who actively work on development issues.'),
                ('Users', 'System project role for general project participants with standard collaboration access.')
        ) AS seeded(name, description)
        WHERE NOT EXISTS (
            SELECT 1
            FROM project_roles existing
            WHERE existing.tenant_id = 0
              AND existing.name = seeded.name
              AND existing.deleted_at IS NULL
        );
    END IF;
END $$;

WITH target_scheme AS (
    SELECT id
    FROM permission_schemes
    WHERE tenant_id = 0
      AND name = 'Default Software Permission Scheme'
      AND deleted_at IS NULL
    ORDER BY id ASC
    LIMIT 1
),
seed_entries(permission_key, grantee_type, grantee_ref, custom_field_id) AS (
    VALUES
        ('ADMINISTER_PROJECTS', 'PROJECT_LEAD', CAST(NULL AS VARCHAR(255)), CAST(NULL AS BIGINT)),
        ('ADMINISTER_PROJECTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('EDIT_WORKFLOWS', 'PROJECT_LEAD', NULL, NULL),
        ('EDIT_WORKFLOWS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('MANAGE_WORK_ITEM_LAYOUTS', 'PROJECT_LEAD', NULL, NULL),
        ('MANAGE_WORK_ITEM_LAYOUTS', 'PROJECT_ROLE', 'Administrators', NULL),

        ('BROWSE_PROJECTS', 'PROJECT_LEAD', NULL, NULL),
        ('BROWSE_PROJECTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('BROWSE_PROJECTS', 'PROJECT_ROLE', 'Developers', NULL),
        ('BROWSE_PROJECTS', 'PROJECT_ROLE', 'Users', NULL),
        ('MANAGE_SPRINTS', 'PROJECT_LEAD', NULL, NULL),
        ('MANAGE_SPRINTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('MANAGE_SPRINTS', 'PROJECT_ROLE', 'Developers', NULL),
        ('MANAGE_VERSIONS', 'PROJECT_LEAD', NULL, NULL),
        ('MANAGE_VERSIONS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('MANAGE_VERSIONS', 'PROJECT_ROLE', 'Developers', NULL),
        ('VIEW_DEVELOPMENT_TOOLS', 'PROJECT_LEAD', NULL, NULL),
        ('VIEW_DEVELOPMENT_TOOLS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('VIEW_DEVELOPMENT_TOOLS', 'PROJECT_ROLE', 'Developers', NULL),
        ('VIEW_DEVELOPMENT_TOOLS', 'PROJECT_ROLE', 'Users', NULL),
        ('VIEW_READ_ONLY_WORKFLOW', 'PROJECT_LEAD', NULL, NULL),
        ('VIEW_READ_ONLY_WORKFLOW', 'PROJECT_ROLE', 'Administrators', NULL),
        ('VIEW_READ_ONLY_WORKFLOW', 'PROJECT_ROLE', 'Developers', NULL),
        ('VIEW_READ_ONLY_WORKFLOW', 'PROJECT_ROLE', 'Users', NULL),
        ('VIEW_AGGREGATED_DATA', 'PROJECT_LEAD', NULL, NULL),
        ('VIEW_AGGREGATED_DATA', 'PROJECT_ROLE', 'Administrators', NULL),
        ('VIEW_AGGREGATED_DATA', 'PROJECT_ROLE', 'Developers', NULL),
        ('VIEW_AGGREGATED_DATA', 'PROJECT_ROLE', 'Users', NULL),

        ('ARCHIVE_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('ARCHIVE_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('RESTORE_ARCHIVED_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('RESTORE_ARCHIVED_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('ASSIGN_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('ASSIGN_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('ASSIGN_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),
        ('ASSIGNABLE_USER', 'PROJECT_LEAD', NULL, NULL),
        ('ASSIGNABLE_USER', 'PROJECT_ROLE', 'Administrators', NULL),
        ('ASSIGNABLE_USER', 'PROJECT_ROLE', 'Developers', NULL),
        ('ASSIGNABLE_USER', 'PROJECT_ROLE', 'Users', NULL),
        ('CLOSE_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('CLOSE_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('CLOSE_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),
        ('CREATE_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('CREATE_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('CREATE_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),
        ('CREATE_ISSUES', 'PROJECT_ROLE', 'Users', NULL),
        ('DELETE_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('DELETE_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('EDIT_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('EDIT_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('EDIT_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),
        ('LINK_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('LINK_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('LINK_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),
        ('LINK_ISSUES', 'PROJECT_ROLE', 'Users', NULL),
        ('MODIFY_REPORTER', 'PROJECT_LEAD', NULL, NULL),
        ('MODIFY_REPORTER', 'PROJECT_ROLE', 'Administrators', NULL),
        ('MOVE_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('MOVE_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('RESOLVE_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('RESOLVE_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('RESOLVE_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),
        ('SCHEDULE_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('SCHEDULE_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('SCHEDULE_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),
        ('SET_ISSUE_SECURITY', 'PROJECT_LEAD', NULL, NULL),
        ('SET_ISSUE_SECURITY', 'PROJECT_ROLE', 'Administrators', NULL),
        ('TRANSITION_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('TRANSITION_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('TRANSITION_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),

        ('MANAGE_WATCHERS', 'PROJECT_LEAD', NULL, NULL),
        ('MANAGE_WATCHERS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('MANAGE_WATCHERS', 'PROJECT_ROLE', 'Developers', NULL),
        ('MANAGE_WATCHERS', 'PROJECT_ROLE', 'Users', NULL),
        ('VIEW_VOTERS_AND_WATCHERS', 'PROJECT_LEAD', NULL, NULL),
        ('VIEW_VOTERS_AND_WATCHERS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('VIEW_VOTERS_AND_WATCHERS', 'PROJECT_ROLE', 'Developers', NULL),
        ('VIEW_VOTERS_AND_WATCHERS', 'PROJECT_ROLE', 'Users', NULL),

        ('ADD_COMMENTS', 'PROJECT_LEAD', NULL, NULL),
        ('ADD_COMMENTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('ADD_COMMENTS', 'PROJECT_ROLE', 'Developers', NULL),
        ('ADD_COMMENTS', 'PROJECT_ROLE', 'Users', NULL),
        ('DELETE_ALL_COMMENTS', 'PROJECT_LEAD', NULL, NULL),
        ('DELETE_ALL_COMMENTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('DELETE_OWN_COMMENTS', 'PROJECT_LEAD', NULL, NULL),
        ('DELETE_OWN_COMMENTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('DELETE_OWN_COMMENTS', 'PROJECT_ROLE', 'Developers', NULL),
        ('DELETE_OWN_COMMENTS', 'PROJECT_ROLE', 'Users', NULL),
        ('EDIT_ALL_COMMENTS', 'PROJECT_LEAD', NULL, NULL),
        ('EDIT_ALL_COMMENTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('EDIT_OWN_COMMENTS', 'PROJECT_LEAD', NULL, NULL),
        ('EDIT_OWN_COMMENTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('EDIT_OWN_COMMENTS', 'PROJECT_ROLE', 'Developers', NULL),
        ('EDIT_OWN_COMMENTS', 'PROJECT_ROLE', 'Users', NULL),

        ('CREATE_ATTACHMENTS', 'PROJECT_LEAD', NULL, NULL),
        ('CREATE_ATTACHMENTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('CREATE_ATTACHMENTS', 'PROJECT_ROLE', 'Developers', NULL),
        ('CREATE_ATTACHMENTS', 'PROJECT_ROLE', 'Users', NULL),
        ('DELETE_ALL_ATTACHMENTS', 'PROJECT_LEAD', NULL, NULL),
        ('DELETE_ALL_ATTACHMENTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('DELETE_OWN_ATTACHMENTS', 'PROJECT_LEAD', NULL, NULL),
        ('DELETE_OWN_ATTACHMENTS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('DELETE_OWN_ATTACHMENTS', 'PROJECT_ROLE', 'Developers', NULL),
        ('DELETE_OWN_ATTACHMENTS', 'PROJECT_ROLE', 'Users', NULL),

        ('WORK_ON_ISSUES', 'PROJECT_LEAD', NULL, NULL),
        ('WORK_ON_ISSUES', 'PROJECT_ROLE', 'Administrators', NULL),
        ('WORK_ON_ISSUES', 'PROJECT_ROLE', 'Developers', NULL),
        ('WORK_ON_ISSUES', 'PROJECT_ROLE', 'Users', NULL),
        ('DELETE_ALL_WORKLOGS', 'PROJECT_LEAD', NULL, NULL),
        ('DELETE_ALL_WORKLOGS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('DELETE_OWN_WORKLOGS', 'PROJECT_LEAD', NULL, NULL),
        ('DELETE_OWN_WORKLOGS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('DELETE_OWN_WORKLOGS', 'PROJECT_ROLE', 'Developers', NULL),
        ('DELETE_OWN_WORKLOGS', 'PROJECT_ROLE', 'Users', NULL),
        ('EDIT_ALL_WORKLOGS', 'PROJECT_LEAD', NULL, NULL),
        ('EDIT_ALL_WORKLOGS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('EDIT_OWN_WORKLOGS', 'PROJECT_LEAD', NULL, NULL),
        ('EDIT_OWN_WORKLOGS', 'PROJECT_ROLE', 'Administrators', NULL),
        ('EDIT_OWN_WORKLOGS', 'PROJECT_ROLE', 'Developers', NULL),
        ('EDIT_OWN_WORKLOGS', 'PROJECT_ROLE', 'Users', NULL)
)
INSERT INTO permission_scheme_entries (
    tenant_id,
    scheme_id,
    permission_key,
    grantee_type,
    grantee_ref,
    custom_field_id,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    0,
    target_scheme.id,
    seed_entries.permission_key,
    seed_entries.grantee_type,
    seed_entries.grantee_ref,
    seed_entries.custom_field_id,
    NOW(),
    0,
    NOW(),
    0
FROM target_scheme
CROSS JOIN seed_entries
WHERE NOT EXISTS (
    SELECT 1
    FROM permission_scheme_entries existing
    WHERE existing.tenant_id = 0
      AND existing.scheme_id = target_scheme.id
      AND existing.permission_key = seed_entries.permission_key
      AND existing.grantee_type = seed_entries.grantee_type
      AND COALESCE(existing.grantee_ref, '__CTX__') = COALESCE(seed_entries.grantee_ref, '__CTX__')
      AND COALESCE(existing.custom_field_id, CAST(0 AS BIGINT)) = COALESCE(seed_entries.custom_field_id, CAST(0 AS BIGINT))
      AND existing.deleted_at IS NULL
);
