CREATE TABLE IF NOT EXISTS teams (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    manager_user_id BIGINT,
    notes TEXT,
    status VARCHAR(20) NOT NULL,
    last_assigned_member_user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT chk_teams_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_teams_tenant_id
    ON teams (tenant_id);

CREATE INDEX IF NOT EXISTS idx_teams_tenant_manager_user_id
    ON teams (tenant_id, manager_user_id);

CREATE INDEX IF NOT EXISTS idx_teams_tenant_status
    ON teams (tenant_id, status);

CREATE INDEX IF NOT EXISTS idx_teams_tenant_last_assigned_member_user_id
    ON teams (tenant_id, last_assigned_member_user_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_teams_tenant_name_active
    ON teams (tenant_id, LOWER(name), status);

CREATE TABLE IF NOT EXISTS team_members (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_team_members_team
        FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT uk_team_members_tenant_user
        UNIQUE (tenant_id, user_id),
    CONSTRAINT chk_team_members_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_team_members_tenant_id
    ON team_members (tenant_id);

CREATE INDEX IF NOT EXISTS idx_team_members_team_id
    ON team_members (team_id);

CREATE INDEX IF NOT EXISTS idx_team_members_user_id
    ON team_members (user_id);

CREATE INDEX IF NOT EXISTS idx_team_members_email
    ON team_members (email);

CREATE INDEX IF NOT EXISTS idx_team_members_tenant_team_status
    ON team_members (tenant_id, team_id, status);

CREATE INDEX IF NOT EXISTS idx_team_members_tenant_role_status
    ON team_members (tenant_id, role, status);
