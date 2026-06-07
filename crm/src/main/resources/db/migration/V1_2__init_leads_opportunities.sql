CREATE TABLE IF NOT EXISTS leads (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    company VARCHAR(255),
    industry VARCHAR(100),
    company_size VARCHAR(50),
    website VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    job_title VARCHAR(100),
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    address_zip_code VARCHAR(20),
    address_country VARCHAR(100),
    territory_code VARCHAR(50),
    lead_source VARCHAR(50) NOT NULL,
    lead_status VARCHAR(50) NOT NULL,
    assigned_to BIGINT,
    estimated_value NUMERIC(15, 2),
    lead_score INTEGER,
    follow_up_date DATE,
    notes TEXT,
    converted_opportunity_id BIGINT,
    converted_account_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT chk_leads_lead_source
        CHECK (lead_source IN ('WEBSITE', 'SOCIAL_MEDIA', 'REFERRAL', 'COLD_CALL', 'EMAIL_CAMPAIGN')),
    CONSTRAINT chk_leads_lead_status
        CHECK (lead_status IN ('NEW', 'CONTACTED', 'NURTURING', 'QUALIFIED', 'DISQUALIFIED', 'CONVERTED'))
);

CREATE INDEX IF NOT EXISTS idx_leads_tenant_id
    ON leads (tenant_id);

CREATE INDEX IF NOT EXISTS idx_leads_lead_status
    ON leads (lead_status);

CREATE INDEX IF NOT EXISTS idx_leads_assigned_to
    ON leads (assigned_to);

CREATE INDEX IF NOT EXISTS idx_leads_tenant_territory_code
    ON leads (tenant_id, territory_code);

CREATE TABLE IF NOT EXISTS opportunities (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    lead_id BIGINT,
    account_id BIGINT NOT NULL,
    stage VARCHAR(50) NOT NULL,
    estimated_value NUMERIC(15, 2) NOT NULL,
    actual_value NUMERIC(15, 2),
    probability INTEGER,
    expected_close_date DATE,
    actual_close_date DATE,
    assigned_to BIGINT,
    notes TEXT,
    loss_reason TEXT,
    reopen_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_opportunities_lead
        FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_opportunities_account
        FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT chk_opportunities_stage
        CHECK (stage IN ('PROSPECTING', 'QUALIFICATION', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'))
);

CREATE INDEX IF NOT EXISTS idx_opportunities_tenant_id
    ON opportunities (tenant_id);

CREATE INDEX IF NOT EXISTS idx_opportunities_account_id
    ON opportunities (account_id);

CREATE INDEX IF NOT EXISTS idx_opportunities_lead_id
    ON opportunities (lead_id);

CREATE INDEX IF NOT EXISTS idx_opportunities_stage
    ON opportunities (stage);

CREATE INDEX IF NOT EXISTS idx_opportunities_assigned_to
    ON opportunities (assigned_to);
