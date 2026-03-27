INSERT INTO status_categories (
    id,
    tenant_id,
    name,
    key,
    color_name,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (100, 0, 'To Do', 'new', 'blue-gray', TRUE, NOW(), 0, NOW(), 0),
    (101, 0, 'In Progress', 'indeterminate', 'yellow', TRUE, NOW(), 0, NOW(), 0),
    (102, 0, 'Done', 'done', 'green', TRUE, NOW(), 0, NOW(), 0);

INSERT INTO statuses (
    id,
    tenant_id,
    status_key,
    name,
    description,
    icon_url,
    category_id,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (200, 0, 'backlog', 'Backlog', 'Work waiting for prioritization or refinement.', NULL, 100, TRUE, NOW(), 0, NOW(), 0),
    (201, 0, 'selected_for_development', 'Selected for Development', 'Work that is ready to be started.', NULL, 100, TRUE, NOW(), 0, NOW(), 0),
    (202, 0, 'in_progress', 'In Progress', 'Work that is actively being implemented.', NULL, 101, TRUE, NOW(), 0, NOW(), 0),
    (203, 0, 'done', 'Done', 'Work that has been completed.', NULL, 102, TRUE, NOW(), 0, NOW(), 0);

INSERT INTO issue_types (
    id,
    tenant_id,
    type_key,
    name,
    description,
    icon_url,
    hierarchy_level,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (300, 0, 'epic', 'Epic', 'A large body of work that can be broken down into smaller items.', NULL, 2, TRUE, NOW(), 0, NOW(), 0),
    (301, 0, 'story', 'Story', 'A user-centric requirement or feature slice.', NULL, 1, TRUE, NOW(), 0, NOW(), 0),
    (302, 0, 'task', 'Task', 'A standard unit of work for the team backlog.', NULL, 1, TRUE, NOW(), 0, NOW(), 0),
    (303, 0, 'bug', 'Bug', 'A defect or production issue that needs resolution.', NULL, 1, TRUE, NOW(), 0, NOW(), 0),
    (304, 0, 'subtask', 'Sub-task', 'A child work item that must belong to a parent item.', NULL, 0, TRUE, NOW(), 0, NOW(), 0);

INSERT INTO priorities (
    id,
    tenant_id,
    priority_key,
    name,
    description,
    icon_url,
    color,
    sequence,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (400, 0, 'highest', 'Highest', 'This problem will block progress.', NULL, '#C9372C', 1, TRUE, NOW(), 0, NOW(), 0),
    (401, 0, 'high', 'High', 'Serious problem that could block progress.', NULL, '#E56910', 2, TRUE, NOW(), 0, NOW(), 0),
    (402, 0, 'medium', 'Medium', 'Has the potential to affect progress.', NULL, '#B38600', 3, TRUE, NOW(), 0, NOW(), 0),
    (403, 0, 'low', 'Low', 'Minor problem or easily worked around.', NULL, '#5E6C84', 4, TRUE, NOW(), 0, NOW(), 0),
    (404, 0, 'lowest', 'Lowest', 'Trivial problem with little or no impact on progress.', NULL, '#7A869A', 5, TRUE, NOW(), 0, NOW(), 0);

INSERT INTO notification_events (
    id,
    tenant_id,
    event_key,
    name,
    description,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (840, 0, 'work_item.created', 'Work item created', 'A work item has been entered into the system.', TRUE, NOW(), 0, NOW(), 0),
    (841, 0, 'work_item.updated', 'Work item updated', 'A work item has had its details changed.', TRUE, NOW(), 0, NOW(), 0),
    (842, 0, 'work_item.assigned', 'Work item assigned', 'A work item has been assigned to a new user.', TRUE, NOW(), 0, NOW(), 0),
    (843, 0, 'work_item.resolved', 'Work item resolved', 'A work item has been resolved.', TRUE, NOW(), 0, NOW(), 0),
    (844, 0, 'work_item.closed', 'Work item closed', 'A work item has been closed.', TRUE, NOW(), 0, NOW(), 0),
    (845, 0, 'work_item.commented', 'Work item commented', 'A comment has been added to a work item.', TRUE, NOW(), 0, NOW(), 0),
    (846, 0, 'work_item.reopened', 'Work item reopened', 'A work item has been reopened.', TRUE, NOW(), 0, NOW(), 0);

SELECT setval(
    pg_get_serial_sequence('status_categories', 'id'),
    (SELECT MAX(id) FROM status_categories)
);

SELECT setval(
    pg_get_serial_sequence('statuses', 'id'),
    (SELECT MAX(id) FROM statuses)
);

SELECT setval(
    pg_get_serial_sequence('issue_types', 'id'),
    (SELECT MAX(id) FROM issue_types)
);

SELECT setval(
    pg_get_serial_sequence('priorities', 'id'),
    (SELECT MAX(id) FROM priorities)
);

SELECT setval(
    pg_get_serial_sequence('notification_events', 'id'),
    (SELECT MAX(id) FROM notification_events)
);
