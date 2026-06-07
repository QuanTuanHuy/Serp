-- ============================================================
-- V13: Complete Request → Subscription flow
-- ============================================================

-- 1. Upgrade school_bus_request_student with service‑detail columns
ALTER TABLE school_bus_request_student
    ADD COLUMN IF NOT EXISTS dropoff_point_id   BIGINT REFERENCES school_bus_pickup_point(id),
    ADD COLUMN IF NOT EXISTS school_schedule_id  BIGINT REFERENCES school_bus_school_schedule(id),
    ADD COLUMN IF NOT EXISTS trip_option         VARCHAR(30),
    ADD COLUMN IF NOT EXISTS is_monday           BOOLEAN DEFAULT TRUE  NOT NULL,
    ADD COLUMN IF NOT EXISTS is_tuesday          BOOLEAN DEFAULT TRUE  NOT NULL,
    ADD COLUMN IF NOT EXISTS is_wednesday        BOOLEAN DEFAULT TRUE  NOT NULL,
    ADD COLUMN IF NOT EXISTS is_thursday         BOOLEAN DEFAULT TRUE  NOT NULL,
    ADD COLUMN IF NOT EXISTS is_friday           BOOLEAN DEFAULT TRUE  NOT NULL,
    ADD COLUMN IF NOT EXISTS is_saturday         BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN IF NOT EXISTS is_sunday           BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN IF NOT EXISTS subscription_id     BIGINT REFERENCES school_bus_student_subscription(id),
    ADD COLUMN IF NOT EXISTS target_subscription_id BIGINT REFERENCES school_bus_student_subscription(id),
    ADD COLUMN IF NOT EXISTS student_note        TEXT;

ALTER TABLE school_bus_request_student
    ADD CONSTRAINT chk_request_student_trip_option
    CHECK (
        trip_option IS NULL
        OR trip_option IN ('MORNING', 'AFTERNOON', 'ROUND_TRIP')
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_request_student_active
    ON school_bus_request_student (tenant_id, request_id, student_id)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_request_student_request
    ON school_bus_request_student (request_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_request_student_student
    ON school_bus_request_student (tenant_id, student_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_request_student_schedule
    ON school_bus_request_student (school_schedule_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_request_student_subscription
    ON school_bus_request_student (subscription_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_request_student_target_subscription
    ON school_bus_request_student (target_subscription_id, is_deleted);


-- 2. Create school_bus_student_subscription_history
CREATE TABLE IF NOT EXISTS school_bus_student_subscription_history
(
    id                     BIGSERIAL PRIMARY KEY,
    tenant_id              BIGINT       NOT NULL,

    subscription_id        BIGINT REFERENCES school_bus_student_subscription(id),
    source_request_id      BIGINT REFERENCES school_bus_transport_request(id),
    request_student_id     BIGINT REFERENCES school_bus_request_student(id),

    change_type            VARCHAR(40)  NOT NULL,

    old_status             VARCHAR(30),
    new_status             VARCHAR(30),

    old_pickup_point_id    BIGINT REFERENCES school_bus_pickup_point(id),
    new_pickup_point_id    BIGINT REFERENCES school_bus_pickup_point(id),

    old_dropoff_point_id   BIGINT REFERENCES school_bus_pickup_point(id),
    new_dropoff_point_id   BIGINT REFERENCES school_bus_pickup_point(id),

    old_school_schedule_id BIGINT REFERENCES school_bus_school_schedule(id),
    new_school_schedule_id BIGINT REFERENCES school_bus_school_schedule(id),

    old_trip_option        VARCHAR(30),
    new_trip_option        VARCHAR(30),

    old_effective_from     DATE,
    new_effective_from     DATE,
    old_effective_to       DATE,
    new_effective_to       DATE,

    changed_by             BIGINT,
    changed_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    reason                 TEXT,
    notes                  TEXT,

    is_active              BOOLEAN   DEFAULT TRUE  NOT NULL,
    is_deleted             BOOLEAN   DEFAULT FALSE NOT NULL,

    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by             VARCHAR(100),
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by             VARCHAR(100),

    CONSTRAINT chk_subscription_history_change_type
        CHECK (change_type IN (
            'CREATED', 'CHANGED', 'PAUSED', 'RESUMED',
            'STOPPED', 'RENEWED', 'EXPIRED'
        ))
);

CREATE INDEX IF NOT EXISTS idx_subscription_history_subscription
    ON school_bus_student_subscription_history (subscription_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_subscription_history_source_request
    ON school_bus_student_subscription_history (source_request_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_subscription_history_request_student
    ON school_bus_student_subscription_history (request_student_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_subscription_history_changed_at
    ON school_bus_student_subscription_history (tenant_id, changed_at, is_deleted);


-- 3. Create school_bus_subscription_pause_period
CREATE TABLE IF NOT EXISTS school_bus_subscription_pause_period
(
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT  NOT NULL,

    subscription_id     BIGINT  NOT NULL REFERENCES school_bus_student_subscription(id),
    source_request_id   BIGINT  REFERENCES school_bus_transport_request(id),
    request_student_id  BIGINT  REFERENCES school_bus_request_student(id),

    pause_from          DATE    NOT NULL,
    pause_to            DATE,

    status              VARCHAR(30) NOT NULL,

    reason              TEXT,

    is_active           BOOLEAN   DEFAULT TRUE  NOT NULL,
    is_deleted          BOOLEAN   DEFAULT FALSE NOT NULL,

    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by          VARCHAR(100),
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by          VARCHAR(100),

    CONSTRAINT chk_subscription_pause_status
        CHECK (status IN ('SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT chk_subscription_pause_range
        CHECK (pause_to IS NULL OR pause_to >= pause_from)
);

CREATE INDEX IF NOT EXISTS idx_pause_period_subscription
    ON school_bus_subscription_pause_period (subscription_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_pause_period_source_request
    ON school_bus_subscription_pause_period (source_request_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_pause_period_date
    ON school_bus_subscription_pause_period (tenant_id, pause_from, pause_to, status, is_deleted);
