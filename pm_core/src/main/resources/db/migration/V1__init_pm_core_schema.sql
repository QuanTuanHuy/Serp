-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Initial schema for pm_core: labels, priorities, issue_types,
--   priority_schemes, priority_scheme_items, issue_type_schemes, issue_type_scheme_items

CREATE TABLE IF NOT EXISTS labels (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL,
    project_id  BIGINT      NOT NULL,
    name        VARCHAR(255) NOT NULL,
    color       VARCHAR(20),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP   NULL
);

CREATE INDEX IF NOT EXISTS idx_labels_tenant_project ON labels (tenant_id, project_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS priorities (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL,
    name        VARCHAR(50) NOT NULL,
    description TEXT,
    icon_url    VARCHAR(255),
    color       VARCHAR(20),
    sequence    INT,
    is_system   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP   NULL
);

CREATE INDEX IF NOT EXISTS idx_priorities_tenant ON priorities (tenant_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS issue_types (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    type_key        VARCHAR(100) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    icon_url        VARCHAR(255),
    hierarchy_level INT,
    is_system       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    deleted_at      TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_issue_types_tenant ON issue_types (tenant_id);
CREATE INDEX IF NOT EXISTS idx_issue_types_tenant_key ON issue_types (tenant_id, type_key);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS priority_schemes (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    default_priority_id BIGINT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,
    deleted_at          TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_priority_schemes_tenant ON priority_schemes (tenant_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS priority_scheme_items (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL,
    scheme_id   BIGINT NOT NULL,
    priority_id BIGINT NOT NULL,
    sequence    INT,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_priority_scheme_items_scheme ON priority_scheme_items (tenant_id, scheme_id);
CREATE UNIQUE INDEX IF NOT EXISTS uidx_priority_scheme_items ON priority_scheme_items (tenant_id, scheme_id, priority_id)
    WHERE deleted_at IS NULL;

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS issue_type_schemes (
    id                    BIGSERIAL PRIMARY KEY,
    tenant_id             BIGINT       NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    description           TEXT,
    default_issue_type_id BIGINT,
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    created_by            BIGINT,
    updated_by            BIGINT,
    deleted_at            TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_issue_type_schemes_tenant ON issue_type_schemes (tenant_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS issue_type_scheme_items (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL,
    scheme_id     BIGINT NOT NULL,
    issue_type_id BIGINT NOT NULL,
    sequence      INT,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    BIGINT,
    updated_by    BIGINT,
    deleted_at    TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_issue_type_scheme_items_scheme ON issue_type_scheme_items (tenant_id, scheme_id);
CREATE UNIQUE INDEX IF NOT EXISTS uidx_issue_type_scheme_items ON issue_type_scheme_items (tenant_id, scheme_id, issue_type_id)
    WHERE deleted_at IS NULL;
