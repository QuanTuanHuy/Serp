INSERT INTO resolutions (
    id,
    tenant_id,
    name,
    description,
    sequence,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (500, 0, 'Done', 'Work completed as expected.', 1, TRUE, NOW(), 0, NOW(), 0),
    (501, 0, 'Fixed', 'A defect has been corrected.', 2, TRUE, NOW(), 0, NOW(), 0),
    (502, 0, 'Won''t Do', 'Work intentionally not implemented.', 3, TRUE, NOW(), 0, NOW(), 0),
    (503, 0, 'Duplicate', 'Work tracked by another item.', 4, TRUE, NOW(), 0, NOW(), 0),
    (504, 0, 'Cannot Reproduce', 'Issue cannot be reproduced currently.', 5, TRUE, NOW(), 0, NOW(), 0);

SELECT setval(
    pg_get_serial_sequence('resolutions', 'id'),
    (SELECT MAX(id) FROM resolutions)
);
