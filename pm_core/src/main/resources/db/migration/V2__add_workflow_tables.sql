-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Workflow tables: workflows, workflow_steps, workflow_transitions,
--   workflow_transition_rules, workflow_schemes, workflow_scheme_items

CREATE TABLE IF NOT EXISTS workflows (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    version_no  INT          NOT NULL DEFAULT 1,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_workflows_tenant ON workflows (tenant_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS workflow_steps (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT  NOT NULL,
    workflow_id BIGINT  NOT NULL,
    status_id   BIGINT  NOT NULL,
    sequence    INT,
    is_initial  BOOLEAN NOT NULL DEFAULT FALSE,
    is_final    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_workflow_steps_workflow ON workflow_steps (tenant_id, workflow_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS workflow_transitions (
    id             BIGSERIAL PRIMARY KEY,
    tenant_id      BIGINT       NOT NULL,
    workflow_id    BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    from_status_id BIGINT,
    to_status_id   BIGINT       NOT NULL,
    sequence       INT,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     BIGINT,
    updated_by     BIGINT,
    deleted_at     TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_workflow_transitions_workflow ON workflow_transitions (tenant_id, workflow_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS workflow_transition_rules (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    transition_id BIGINT       NOT NULL,
    rule_stage    VARCHAR(50)  NOT NULL,
    rule_key      VARCHAR(255) NOT NULL,
    config_json   TEXT,
    sequence      INT,
    is_enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    BIGINT,
    updated_by    BIGINT,
    deleted_at    TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_workflow_transition_rules_transition ON workflow_transition_rules (tenant_id, transition_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS workflow_schemes (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    default_workflow_id BIGINT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,
    deleted_at          TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_workflow_schemes_tenant ON workflow_schemes (tenant_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS workflow_scheme_items (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL,
    scheme_id     BIGINT NOT NULL,
    issue_type_id BIGINT NOT NULL,
    workflow_id   BIGINT NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    BIGINT,
    updated_by    BIGINT,
    deleted_at    TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_workflow_scheme_items_scheme ON workflow_scheme_items (tenant_id, scheme_id);
CREATE UNIQUE INDEX IF NOT EXISTS uidx_workflow_scheme_items ON workflow_scheme_items (tenant_id, scheme_id, issue_type_id)
    WHERE deleted_at IS NULL;
