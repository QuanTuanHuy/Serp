CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(255),
    industry VARCHAR(100),
    company_size VARCHAR(50),
    parent_account_id BIGINT,
    tax_id VARCHAR(50),
    credit_limit NUMERIC(15, 2),
    total_opportunities INTEGER,
    won_opportunities INTEGER,
    total_revenue NUMERIC(15, 2),
    active_status VARCHAR(20) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    notes TEXT,
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    address_zip_code VARCHAR(20),
    address_country VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT chk_accounts_active_status
        CHECK (active_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_accounts_account_type
        CHECK (account_type IN ('PROSPECT', 'CUSTOMER'))
);

CREATE INDEX IF NOT EXISTS idx_accounts_tenant_id
    ON accounts (tenant_id);

CREATE INDEX IF NOT EXISTS idx_accounts_email
    ON accounts (email);

CREATE INDEX IF NOT EXISTS idx_accounts_name
    ON accounts (name);

CREATE TABLE IF NOT EXISTS contacts (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    account_id BIGINT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    job_position VARCHAR(100),
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    address_zip_code VARCHAR(20),
    address_country VARCHAR(100),
    contact_type VARCHAR(50),
    is_primary BOOLEAN,
    linkedin_url VARCHAR(255),
    twitter_handle VARCHAR(100),
    active_status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_contacts_account
        FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT chk_contacts_active_status
        CHECK (active_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_contacts_contact_type
        CHECK (contact_type IS NULL OR contact_type IN ('PRIMARY', 'SECONDARY', 'BILLING', 'TECHNICAL'))
);

CREATE INDEX IF NOT EXISTS idx_contacts_tenant_id
    ON contacts (tenant_id);

CREATE INDEX IF NOT EXISTS idx_contacts_account_id
    ON contacts (account_id);

CREATE INDEX IF NOT EXISTS idx_contacts_email
    ON contacts (email);
