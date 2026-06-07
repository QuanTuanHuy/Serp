CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX uk_skills_active_code
    ON skills (tenant_id, code)
    WHERE deleted_at IS NULL;

CREATE TABLE work_item_skills (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    requirement_type VARCHAR(50) NOT NULL,
    min_proficiency VARCHAR(50) NOT NULL,
    weight INTEGER NOT NULL DEFAULT 1,
    source VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_item_skills_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id),
    CONSTRAINT fk_work_item_skills_skill
        FOREIGN KEY (skill_id) REFERENCES skills (id),
    CONSTRAINT chk_work_item_skills_weight
        CHECK (weight > 0)
);

CREATE UNIQUE INDEX uk_work_item_skills_active_work_item_skill
    ON work_item_skills (tenant_id, work_item_id, skill_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_skills_work_item
    ON work_item_skills (tenant_id, project_id, work_item_id)
    WHERE deleted_at IS NULL;

CREATE TABLE user_skills (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency VARCHAR(50) NOT NULL,
    confidence INTEGER,
    source VARCHAR(50) NOT NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_skills_skill
        FOREIGN KEY (skill_id) REFERENCES skills (id),
    CONSTRAINT chk_user_skills_confidence
        CHECK (confidence IS NULL OR (confidence >= 0 AND confidence <= 100))
);

CREATE UNIQUE INDEX uk_user_skills_active_user_skill
    ON user_skills (tenant_id, user_id, skill_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_user_skills_skill_proficiency
    ON user_skills (tenant_id, skill_id, proficiency)
    WHERE deleted_at IS NULL;
