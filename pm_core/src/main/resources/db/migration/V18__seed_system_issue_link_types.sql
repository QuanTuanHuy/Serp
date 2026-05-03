INSERT INTO issue_link_types (
    id,
    tenant_id,
    name,
    outward_desc,
    inward_desc,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (600, 0, 'Blocks', 'blocks', 'is blocked by', TRUE, NOW(), 0, NOW(), 0),
    (601, 0, 'Clones', 'clones', 'is cloned by', TRUE, NOW(), 0, NOW(), 0),
    (602, 0, 'Relates', 'relates to', 'relates to', TRUE, NOW(), 0, NOW(), 0);

SELECT setval(
    pg_get_serial_sequence('issue_link_types', 'id'),
    (SELECT MAX(id) FROM issue_link_types)
);
