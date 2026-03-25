CREATE TABLE IF NOT EXISTS project_roles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_project_roles_tenant_name
    ON project_roles (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS project_role_actors (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    project_role_id BIGINT NOT NULL,
    subject_type VARCHAR(20) NOT NULL,
    subject_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_project_role_actors_subject_type CHECK (
        subject_type IN ('USER', 'GROUP', 'SERVICE_ACCOUNT')
    )
);

CREATE INDEX IF NOT EXISTS idx_project_role_actors_project_role
    ON project_role_actors (tenant_id, project_id, project_role_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_project_role_actors_subject
    ON project_role_actors (tenant_id, subject_type, subject_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_project_role_actors_assignment
    ON project_role_actors (tenant_id, project_id, project_role_id, subject_type, subject_id)
    WHERE deleted_at IS NULL;

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
