CREATE TABLE IF NOT EXISTS meeting_requests (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    preferred_user_id BIGINT,
    assigned_team_member_id BIGINT,
    assigned_user_id BIGINT,
    scheduled_activity_id BIGINT,
    scheduled_start_time BIGINT,
    account_id BIGINT NOT NULL,
    opportunity_id BIGINT,
    contact_id BIGINT,
    subject VARCHAR(255),
    description TEXT,
    location VARCHAR(255),
    meeting_type VARCHAR(50) NOT NULL,
    preferred_time_slot VARCHAR(50),
    earliest_start BIGINT NOT NULL,
    latest_start BIGINT NOT NULL,
    requested_deadline BIGINT NOT NULL,
    duration_minutes INTEGER,
    status VARCHAR(20) NOT NULL,
    scheduling_attempts INTEGER NOT NULL DEFAULT 0,
    priority_score INTEGER NOT NULL DEFAULT 0,
    failure_reason VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_meeting_requests_team
        FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_meeting_requests_account
        FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_meeting_requests_opportunity
        FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    CONSTRAINT fk_meeting_requests_contact
        FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_meeting_requests_activity
        FOREIGN KEY (scheduled_activity_id) REFERENCES activities(id),
    CONSTRAINT chk_meeting_requests_type
        CHECK (meeting_type IN ('DISCOVERY', 'DEMO', 'PROPOSAL', 'NEGOTIATION', 'QBR')),
    CONSTRAINT chk_meeting_requests_preferred_slot
        CHECK (preferred_time_slot IS NULL OR preferred_time_slot IN ('MORNING', 'AFTERNOON')),
    CONSTRAINT chk_meeting_requests_status
        CHECK (status IN ('PENDING', 'SCHEDULED', 'FAILED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_meeting_requests_tenant_id
    ON meeting_requests (tenant_id);

CREATE INDEX IF NOT EXISTS idx_meeting_requests_status_deadline
    ON meeting_requests (status, requested_deadline);

CREATE INDEX IF NOT EXISTS idx_meeting_requests_team_id
    ON meeting_requests (team_id);
