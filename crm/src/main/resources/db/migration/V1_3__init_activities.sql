CREATE TABLE IF NOT EXISTS activities (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    lead_id BIGINT,
    contact_id BIGINT,
    account_id BIGINT,
    opportunity_id BIGINT,
    activity_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    assigned_to BIGINT NOT NULL,
    activity_date BIGINT,
    due_date BIGINT,
    reminder_date BIGINT,
    duration_minutes INTEGER,
    priority VARCHAR(20),
    progress_percent INTEGER,
    outcome VARCHAR(50),
    notes TEXT,
    attachments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_activities_lead
        FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_activities_contact
        FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_activities_account
        FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_activities_opportunity
        FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    CONSTRAINT chk_activities_activity_type
        CHECK (activity_type IN ('CALL', 'MEETING', 'EMAIL', 'TASK')),
    CONSTRAINT chk_activities_status
        CHECK (status IN ('PLANNED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_activities_priority
        CHECK (priority IS NULL OR priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
    -- CONSTRAINT chk_activities_outcome
    --     CHECK (outcome IS NULL OR outcome IN (
    --         'REACHED',
    --         'VOICEMAIL',
    --         'NO_ANSWER',
    --         'BUSY',
    --         'WRONG_NUMBER',
    --         'OCCURRED',
    --         'NO_SHOW',
    --         'RESCHEDULED',
    --         'CANCELLED_BY_CUSTOMER'
    --     ))
);

CREATE INDEX IF NOT EXISTS idx_activities_tenant_id
    ON activities (tenant_id);

CREATE INDEX IF NOT EXISTS idx_activities_assigned_to
    ON activities (assigned_to);

CREATE INDEX IF NOT EXISTS idx_activities_activity_type
    ON activities (activity_type);

CREATE INDEX IF NOT EXISTS idx_activities_due_date
    ON activities (due_date);

CREATE INDEX IF NOT EXISTS idx_activities_lead_id
    ON activities (lead_id);

CREATE INDEX IF NOT EXISTS idx_activities_account_id
    ON activities (account_id);

CREATE INDEX IF NOT EXISTS idx_activities_opportunity_id
    ON activities (opportunity_id);
