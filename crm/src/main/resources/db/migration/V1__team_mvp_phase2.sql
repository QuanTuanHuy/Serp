ALTER TABLE IF EXISTS teams
    RENAME COLUMN leader_id TO manager_user_id;

ALTER TABLE IF EXISTS teams
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

UPDATE teams
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE IF EXISTS teams
    ALTER COLUMN status SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_teams_tenant_manager_user_id
    ON teams (tenant_id, manager_user_id);

CREATE INDEX IF NOT EXISTS idx_teams_tenant_status
    ON teams (tenant_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_teams_tenant_name_active
    ON teams (tenant_id, LOWER(name), status);

UPDATE team_members
SET status = CASE
    WHEN status IN ('INVITED', 'CONFIRMED', 'ACTIVE') THEN 'ACTIVE'
    WHEN status IN ('ARCHIVED', 'INACTIVE') THEN 'INACTIVE'
    ELSE 'ACTIVE'
END
WHERE status IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_team_members_tenant_team_status
    ON team_members (tenant_id, team_id, status);

CREATE INDEX IF NOT EXISTS idx_team_members_tenant_role_status
    ON team_members (tenant_id, role, status);

CREATE TABLE IF NOT EXISTS crm_territories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    territory_code VARCHAR(50) NOT NULL,
    territory_name VARCHAR(255) NOT NULL,
    territory_level VARCHAR(30) NOT NULL,
    country_code VARCHAR(10) NOT NULL DEFAULT 'VN',
    parent_territory_code VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT uk_crm_territories_tenant_code UNIQUE (tenant_id, territory_code)
);

CREATE INDEX IF NOT EXISTS idx_crm_territories_tenant_active
    ON crm_territories (tenant_id, is_active);

CREATE TABLE IF NOT EXISTS crm_team_territories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    territory_code VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_crm_team_territories_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_crm_team_territories_territory FOREIGN KEY (tenant_id, territory_code)
        REFERENCES crm_territories(tenant_id, territory_code)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_team_territories_tenant_code_active
    ON crm_team_territories (tenant_id, territory_code, is_active);

CREATE INDEX IF NOT EXISTS idx_crm_team_territories_team_active
    ON crm_team_territories (team_id, is_active);

CREATE TABLE IF NOT EXISTS crm_team_quota_snapshots (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    period VARCHAR(7) NOT NULL,
    quota_amount NUMERIC(18, 2) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_crm_team_quota_snapshots_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT chk_crm_team_quota_period_format CHECK (period ~ '^[0-9]{4}-[0-9]{2}$'),
    CONSTRAINT chk_crm_team_quota_amount_non_negative CHECK (quota_amount >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_team_quota_snapshots_tenant_team_period
    ON crm_team_quota_snapshots (tenant_id, team_id, period);

CREATE INDEX IF NOT EXISTS idx_crm_team_quota_snapshots_tenant_period
    ON crm_team_quota_snapshots (tenant_id, period);

ALTER TABLE IF EXISTS leads
    ADD COLUMN IF NOT EXISTS territory_code VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_leads_tenant_territory_code
    ON leads (tenant_id, territory_code);
