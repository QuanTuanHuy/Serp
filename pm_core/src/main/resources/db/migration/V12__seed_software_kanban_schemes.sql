INSERT INTO issue_type_schemes (
    id,
    tenant_id,
    name,
    description,
    default_issue_type_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    700,
    0,
    'Software Kanban Issue Type Scheme',
    'System issue type scheme for the Software Kanban blueprint.',
    302,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO issue_type_scheme_items (
    tenant_id,
    scheme_id,
    issue_type_id,
    sequence,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 700, 300, 1, NOW(), 0, NOW(), 0),
    (0, 700, 301, 2, NOW(), 0, NOW(), 0),
    (0, 700, 302, 3, NOW(), 0, NOW(), 0),
    (0, 700, 303, 4, NOW(), 0, NOW(), 0),
    (0, 700, 304, 5, NOW(), 0, NOW(), 0);

INSERT INTO priority_schemes (
    id,
    tenant_id,
    name,
    description,
    default_priority_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    720,
    0,
    'Software Kanban Priority Scheme',
    'System priority scheme for the Software Kanban blueprint.',
    402,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO priority_scheme_items (
    tenant_id,
    scheme_id,
    priority_id,
    sequence,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 720, 400, 1, NOW(), 0, NOW(), 0),
    (0, 720, 401, 2, NOW(), 0, NOW(), 0),
    (0, 720, 402, 3, NOW(), 0, NOW(), 0),
    (0, 720, 403, 4, NOW(), 0, NOW(), 0),
    (0, 720, 404, 5, NOW(), 0, NOW(), 0);

INSERT INTO field_configurations (
    id,
    tenant_id,
    name,
    description,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    730,
    0,
    'Software Kanban Default Field Configuration',
    'System field configuration for the Software Kanban blueprint.',
    TRUE,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO field_configuration_items (
    tenant_id,
    field_configuration_id,
    field_ref_type,
    field_ref,
    is_required,
    is_hidden,
    renderer_key,
    sequence,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 730, 'SYSTEM', 'issue_type_id', FALSE, FALSE, NULL, 1, NOW(), 0, NOW(), 0),
    (0, 730, 'SYSTEM', 'summary', TRUE, FALSE, NULL, 2, NOW(), 0, NOW(), 0),
    (0, 730, 'SYSTEM', 'description', FALSE, FALSE, 'markdown', 3, NOW(), 0, NOW(), 0),
    (0, 730, 'SYSTEM', 'priority_id', FALSE, FALSE, NULL, 4, NOW(), 0, NOW(), 0),
    (0, 730, 'SYSTEM', 'assignee_id', FALSE, FALSE, NULL, 5, NOW(), 0, NOW(), 0),
    (0, 730, 'SYSTEM', 'parent_id', FALSE, FALSE, NULL, 6, NOW(), 0, NOW(), 0),
    (0, 730, 'SYSTEM', 'due_date', FALSE, FALSE, NULL, 7, NOW(), 0, NOW(), 0),
    (0, 730, 'SYSTEM', 'time_original_estimate', FALSE, FALSE, NULL, 8, NOW(), 0, NOW(), 0),
    (0, 730, 'SYSTEM', 'security_level_id', FALSE, FALSE, NULL, 9, NOW(), 0, NOW(), 0);

INSERT INTO field_config_schemes (
    id,
    tenant_id,
    name,
    description,
    default_field_config_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    740,
    0,
    'Software Kanban Field Configuration Scheme',
    'System field configuration scheme for the Software Kanban blueprint.',
    730,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO screens (
    id,
    tenant_id,
    name,
    description,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    750,
    0,
    'Software Kanban Work Item Screen',
    'System work item screen for create, edit, and view operations.',
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO screen_tabs (
    id,
    tenant_id,
    screen_id,
    name,
    sequence,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    751,
    0,
    750,
    'Details',
    1,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO screen_tab_fields (
    tenant_id,
    screen_tab_id,
    field_ref_type,
    field_ref,
    sequence,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 751, 'SYSTEM', 'issue_type_id', 1, NOW(), 0, NOW(), 0),
    (0, 751, 'SYSTEM', 'summary', 2, NOW(), 0, NOW(), 0),
    (0, 751, 'SYSTEM', 'description', 3, NOW(), 0, NOW(), 0),
    (0, 751, 'SYSTEM', 'priority_id', 4, NOW(), 0, NOW(), 0),
    (0, 751, 'SYSTEM', 'assignee_id', 5, NOW(), 0, NOW(), 0),
    (0, 751, 'SYSTEM', 'parent_id', 6, NOW(), 0, NOW(), 0),
    (0, 751, 'SYSTEM', 'due_date', 7, NOW(), 0, NOW(), 0),
    (0, 751, 'SYSTEM', 'time_original_estimate', 8, NOW(), 0, NOW(), 0),
    (0, 751, 'SYSTEM', 'security_level_id', 9, NOW(), 0, NOW(), 0);

INSERT INTO screen_schemes (
    id,
    tenant_id,
    name,
    description,
    default_screen_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    770,
    0,
    'Software Kanban Screen Scheme',
    'System screen scheme for the Software Kanban blueprint.',
    750,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO screen_scheme_items (
    tenant_id,
    screen_scheme_id,
    operation_key,
    screen_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 770, 'CREATE', 750, NOW(), 0, NOW(), 0),
    (0, 770, 'EDIT', 750, NOW(), 0, NOW(), 0),
    (0, 770, 'VIEW', 750, NOW(), 0, NOW(), 0);

INSERT INTO issue_type_screen_schemes (
    id,
    tenant_id,
    name,
    description,
    default_screen_scheme_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    780,
    0,
    'Software Kanban Issue Type Screen Scheme',
    'System issue type screen scheme for the Software Kanban blueprint.',
    770,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO workflows (
    id,
    tenant_id,
    workflow_key,
    name,
    description,
    current_published_version_id,
    draft_version_id,
    lifecycle_state,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    790,
    0,
    'software_kanban_workflow',
    'Software Kanban Workflow',
    'System workflow for the Software Kanban blueprint.',
    NULL,
    NULL,
    'ACTIVE',
    TRUE,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO workflow_versions (
    id,
    tenant_id,
    workflow_id,
    version_no,
    version_state,
    base_version_id,
    published_at,
    published_by,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    791,
    0,
    790,
    1,
    'PUBLISHED',
    NULL,
    NOW(),
    0,
    NOW(),
    0,
    NOW(),
    0
);

UPDATE workflows
SET current_published_version_id = 791
WHERE id = 790;

INSERT INTO workflow_steps (
    id,
    tenant_id,
    workflow_version_id,
    step_key,
    name,
    status_id,
    step_order,
    is_initial,
    is_terminal,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (792, 0, 791, 'backlog', 'Backlog', 200, 1, TRUE, FALSE, NOW(), 0, NOW(), 0),
    (793, 0, 791, 'selected_for_development', 'Selected for Development', 201, 2, FALSE, FALSE, NOW(), 0, NOW(), 0),
    (794, 0, 791, 'in_progress', 'In Progress', 202, 3, FALSE, FALSE, NOW(), 0, NOW(), 0),
    (795, 0, 791, 'done', 'Done', 203, 4, FALSE, TRUE, NOW(), 0, NOW(), 0);

INSERT INTO workflow_transitions (
    tenant_id,
    workflow_version_id,
    name,
    from_step_id,
    to_step_id,
    screen_id,
    sequence,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 791, 'Select for Development', 792, 793, NULL, 1, NOW(), 0, NOW(), 0),
    (0, 791, 'Start Progress', 793, 794, NULL, 2, NOW(), 0, NOW(), 0),
    (0, 791, 'Complete Work', 794, 795, NULL, 3, NOW(), 0, NOW(), 0),
    (0, 791, 'Reopen', 795, 793, NULL, 4, NOW(), 0, NOW(), 0);

INSERT INTO workflow_schemes (
    id,
    tenant_id,
    name,
    description,
    default_workflow_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    810,
    0,
    'Software Kanban Workflow Scheme',
    'System workflow scheme for the Software Kanban blueprint.',
    790,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO issue_security_schemes (
    id,
    tenant_id,
    name,
    description,
    default_level_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    830,
    0,
    'Software Kanban Issue Security Scheme',
    'System issue security scheme for the Software Kanban blueprint.',
    NULL,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO issue_security_levels (
    id,
    tenant_id,
    scheme_id,
    name,
    description,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    831,
    0,
    830,
    'Internal',
    'Optional security level for work items visible to standard project roles.',
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO issue_security_level_members (
    tenant_id,
    level_id,
    subject_type,
    subject_ref,
    custom_field_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 831, 'PROJECT_ROLE', 'Administrators', NULL, NOW(), 0, NOW(), 0),
    (0, 831, 'PROJECT_ROLE', 'Developers', NULL, NOW(), 0, NOW(), 0),
    (0, 831, 'PROJECT_ROLE', 'Users', NULL, NOW(), 0, NOW(), 0);

INSERT INTO notification_schemes (
    id,
    tenant_id,
    name,
    description,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    850,
    0,
    'Software Kanban Notification Scheme',
    'System notification scheme for the Software Kanban blueprint.',
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO notification_scheme_entries (
    tenant_id,
    scheme_id,
    event_id,
    recipient_type,
    recipient_ref,
    custom_field_id,
    channel,
    template_id,
    is_enabled,
    conditions_json,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 850, 840, 'PROJECT_LEAD', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 840, 'PROJECT_ROLE', 'Developers', NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 841, 'ASSIGNEE', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 841, 'REPORTER', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 842, 'ASSIGNEE', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 843, 'REPORTER', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 843, 'WATCHERS', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 844, 'REPORTER', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 845, 'ASSIGNEE', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 845, 'WATCHERS', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 846, 'REPORTER', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0),
    (0, 850, 846, 'ASSIGNEE', NULL, NULL, 'EMAIL', NULL, TRUE, NULL, NOW(), 0, NOW(), 0);

SELECT setval(
    pg_get_serial_sequence('issue_type_schemes', 'id'),
    (SELECT MAX(id) FROM issue_type_schemes)
);

SELECT setval(
    pg_get_serial_sequence('priority_schemes', 'id'),
    (SELECT MAX(id) FROM priority_schemes)
);

SELECT setval(
    pg_get_serial_sequence('field_configurations', 'id'),
    (SELECT MAX(id) FROM field_configurations)
);

SELECT setval(
    pg_get_serial_sequence('field_config_schemes', 'id'),
    (SELECT MAX(id) FROM field_config_schemes)
);

SELECT setval(
    pg_get_serial_sequence('screens', 'id'),
    (SELECT MAX(id) FROM screens)
);

SELECT setval(
    pg_get_serial_sequence('screen_tabs', 'id'),
    (SELECT MAX(id) FROM screen_tabs)
);

SELECT setval(
    pg_get_serial_sequence('screen_schemes', 'id'),
    (SELECT MAX(id) FROM screen_schemes)
);

SELECT setval(
    pg_get_serial_sequence('issue_type_screen_schemes', 'id'),
    (SELECT MAX(id) FROM issue_type_screen_schemes)
);

SELECT setval(
    pg_get_serial_sequence('workflows', 'id'),
    (SELECT MAX(id) FROM workflows)
);

SELECT setval(
    pg_get_serial_sequence('workflow_versions', 'id'),
    (SELECT MAX(id) FROM workflow_versions)
);

SELECT setval(
    pg_get_serial_sequence('workflow_steps', 'id'),
    (SELECT MAX(id) FROM workflow_steps)
);

SELECT setval(
    pg_get_serial_sequence('workflow_schemes', 'id'),
    (SELECT MAX(id) FROM workflow_schemes)
);

SELECT setval(
    pg_get_serial_sequence('issue_security_schemes', 'id'),
    (SELECT MAX(id) FROM issue_security_schemes)
);

SELECT setval(
    pg_get_serial_sequence('issue_security_levels', 'id'),
    (SELECT MAX(id) FROM issue_security_levels)
);

SELECT setval(
    pg_get_serial_sequence('notification_schemes', 'id'),
    (SELECT MAX(id) FROM notification_schemes)
);
