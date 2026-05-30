CREATE TABLE webhook_events (
    id BIGSERIAL PRIMARY KEY,
    event_key VARCHAR(120) NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    target_url VARCHAR(500) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_attempt_at TIMESTAMP,
    last_error VARCHAR(1000),
    last_http_status INTEGER,
    last_response_body TEXT,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_webhook_event_key ON webhook_events(event_key);
CREATE INDEX idx_webhook_status_next_retry ON webhook_events(status, next_retry_at);
CREATE INDEX idx_webhook_created_at ON webhook_events(created_at);
