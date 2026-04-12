INSERT INTO project_blueprints (
    id,
    tenant_id,
    name,
    description,
    project_type_key,
    avatar_url,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    860,
    0,
    'Software Kanban',
    'System blueprint for company-managed software projects using a Kanban workflow.',
    'software',
    NULL,
    TRUE,
    NOW(),
    0,
    NOW(),
    0
);

INSERT INTO blueprint_scheme_defaults (
    tenant_id,
    blueprint_id,
    scheme_type,
    scheme_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 860, 'ISSUE_TYPE', 700, NOW(), 0, NOW(), 0),
    (0, 860, 'WORKFLOW', 810, NOW(), 0, NOW(), 0),
    (0, 860, 'FIELD_CONFIG', 740, NOW(), 0, NOW(), 0),
    (0, 860, 'SCREEN', 780, NOW(), 0, NOW(), 0),
    (0, 860, 'PERMISSION', 820, NOW(), 0, NOW(), 0),
    (0, 860, 'NOTIFICATION', 850, NOW(), 0, NOW(), 0),
    (0, 860, 'PRIORITY', 720, NOW(), 0, NOW(), 0),
    (0, 860, 'ISSUE_SECURITY', 830, NOW(), 0, NOW(), 0);

INSERT INTO tenant_scheme_defaults (
    tenant_id,
    scheme_type,
    scheme_id,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (0, 'ISSUE_TYPE', 700, NOW(), 0, NOW(), 0),
    (0, 'WORKFLOW', 810, NOW(), 0, NOW(), 0),
    (0, 'FIELD_CONFIG', 740, NOW(), 0, NOW(), 0),
    (0, 'SCREEN', 780, NOW(), 0, NOW(), 0),
    (0, 'PERMISSION', 820, NOW(), 0, NOW(), 0),
    (0, 'NOTIFICATION', 850, NOW(), 0, NOW(), 0),
    (0, 'PRIORITY', 720, NOW(), 0, NOW(), 0),
    (0, 'ISSUE_SECURITY', 830, NOW(), 0, NOW(), 0);

SELECT setval(
    pg_get_serial_sequence('project_blueprints', 'id'),
    (SELECT MAX(id) FROM project_blueprints)
);
