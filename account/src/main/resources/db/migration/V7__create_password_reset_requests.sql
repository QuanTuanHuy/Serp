CREATE TABLE password_reset_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_by BIGINT,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_password_reset_status CHECK (status IN ('PENDING', 'USED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_password_reset_user_status ON password_reset_requests(user_id, status);
CREATE INDEX idx_password_reset_token_hash ON password_reset_requests(token_hash);
