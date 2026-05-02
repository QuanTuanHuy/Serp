-- Working hours table: stores team member availability schedule
-- Note: Unique constraint allows only one working hours block per day per member
-- If multiple blocks per day are needed (e.g., split shifts), this constraint should be modified
CREATE TABLE IF NOT EXISTS working_hours (
    id BIGSERIAL PRIMARY KEY,
    team_member_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    working_day BOOLEAN NOT NULL,
    start_minute INTEGER,
    end_minute INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_working_hours_team_member
        FOREIGN KEY (team_member_id) REFERENCES team_members(id) ON DELETE CASCADE,
    CONSTRAINT uk_working_hours_member_day
        UNIQUE (team_member_id, day_of_week),
    CONSTRAINT chk_working_hours_day_of_week
        CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT chk_working_hours_minutes
        CHECK (
            (working_day = FALSE AND start_minute IS NULL AND end_minute IS NULL)
            OR (working_day = TRUE AND start_minute IS NOT NULL AND end_minute IS NOT NULL AND start_minute >= 0 AND end_minute <= 1440 AND start_minute < end_minute)
        )
);

CREATE INDEX IF NOT EXISTS idx_working_hours_team_member_id
    ON working_hours (team_member_id);

CREATE TABLE IF NOT EXISTS rep_time_blocks (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    team_member_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT NOT NULL,
    block_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_rep_time_blocks_team_member
        FOREIGN KEY (team_member_id) REFERENCES team_members(id),
    CONSTRAINT fk_rep_time_blocks_activity
        FOREIGN KEY (activity_id) REFERENCES activities(id),
    CONSTRAINT uk_rep_time_blocks_activity
        UNIQUE (tenant_id, activity_id),
    CONSTRAINT chk_rep_time_blocks_type
        CHECK (block_type IN ('MEETING')),
    CONSTRAINT chk_rep_time_blocks_time
        CHECK (start_time < end_time)
);

CREATE INDEX IF NOT EXISTS idx_rep_time_blocks_member_time
    ON rep_time_blocks (team_member_id, start_time, end_time);

CREATE INDEX IF NOT EXISTS idx_rep_time_blocks_tenant_id
    ON rep_time_blocks (tenant_id);
