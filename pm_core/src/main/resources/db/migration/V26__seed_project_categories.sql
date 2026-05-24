INSERT INTO project_categories (
    id,
    tenant_id,
    name,
    description,
    is_system,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (870, 0, 'Software Development', 'Projects for software delivery, engineering work, and product development.', TRUE, NOW(), 0, NOW(), 0),
    (871, 0, 'Business Operations', 'Projects for internal operations, process improvements, and cross-functional business work.', TRUE, NOW(), 0, NOW(), 0),
    (872, 0, 'Customer Success', 'Projects related to customer onboarding, support, and service delivery.', TRUE, NOW(), 0, NOW(), 0),
    (873, 0, 'Infrastructure', 'Projects for platform, DevOps, infrastructure, and reliability initiatives.', TRUE, NOW(), 0, NOW(), 0),
    (874, 0, 'Research and Planning', 'Projects used for discovery, research, planning, and exploratory work.', TRUE, NOW(), 0, NOW(), 0);

SELECT setval(
    pg_get_serial_sequence('project_categories', 'id'),
    (SELECT MAX(id) FROM project_categories)
);
