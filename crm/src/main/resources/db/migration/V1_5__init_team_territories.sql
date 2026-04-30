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
    CONSTRAINT uk_crm_territories_tenant_code
        UNIQUE (tenant_id, territory_code),
    CONSTRAINT chk_crm_territories_level
        CHECK (territory_level IN ('PROVINCE_CITY'))
);

CREATE INDEX IF NOT EXISTS idx_crm_territories_tenant_active
    ON crm_territories (tenant_id, is_active);

CREATE TABLE IF NOT EXISTS crm_team_territories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    territory_code VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_crm_team_territories_team
        FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_crm_team_territories_territory
        FOREIGN KEY (tenant_id, territory_code)
            REFERENCES crm_territories(tenant_id, territory_code)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_team_territories_tenant_code_active
    ON crm_team_territories (tenant_id, territory_code, is_active);

CREATE INDEX IF NOT EXISTS idx_crm_team_territories_team_active
    ON crm_team_territories (team_id, is_active);
