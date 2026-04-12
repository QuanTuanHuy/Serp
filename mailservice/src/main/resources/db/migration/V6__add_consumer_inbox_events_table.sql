-- Author: QuanTuanHuy
-- Description: Part of Serp Project

CREATE TABLE consumer_inbox_events (
    id              BIGSERIAL PRIMARY KEY,
    consumer_group  VARCHAR(120) NOT NULL,
    event_id        VARCHAR(120) NOT NULL,
    event_type      VARCHAR(120),
    topic           VARCHAR(255) NOT NULL,
    partition_no    INT NOT NULL,
    offset_no       BIGINT NOT NULL,
    tenant_id       BIGINT,
    payload_hash    VARCHAR(128),
    raw_payload     JSONB,
    status          VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    attempts        INT NOT NULL DEFAULT 1,
    processed_at    TIMESTAMP,
    last_error      TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uidx_consumer_inbox_group_event UNIQUE (consumer_group, event_id),
    CONSTRAINT chk_consumer_inbox_status CHECK (status IN ('PROCESSING', 'PROCESSED', 'FAILED', 'DEAD'))
);
