-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Project/work item runtime foundation tables for Jira-closer parity

CREATE TABLE IF NOT EXISTS project_categories (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP    NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_project_categories_tenant_name
    ON project_categories (tenant_id, name)
    WHERE deleted_at IS NULL;

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS project_blueprints (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT       NOT NULL,
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    project_type_key VARCHAR(50),
    avatar_url       VARCHAR(255),
    is_system        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    created_by       BIGINT,
    updated_by       BIGINT,
    deleted_at       TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_project_blueprints_tenant
    ON project_blueprints (tenant_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS blueprint_scheme_defaults (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL,
    blueprint_id BIGINT     NOT NULL,
    scheme_type VARCHAR(50) NOT NULL,
    scheme_id   BIGINT      NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP   NULL
);

CREATE INDEX IF NOT EXISTS idx_blueprint_scheme_defaults_blueprint
    ON blueprint_scheme_defaults (tenant_id, blueprint_id);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_blueprint_scheme_defaults
    ON blueprint_scheme_defaults (tenant_id, blueprint_id, scheme_type)
    WHERE deleted_at IS NULL;

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS projects (
    id                           BIGSERIAL PRIMARY KEY,
    tenant_id                    BIGINT       NOT NULL,
    key                          VARCHAR(20)  NOT NULL,
    name                         VARCHAR(255) NOT NULL,
    description                  TEXT,
    url                          VARCHAR(255),
    lead_user_id                 BIGINT,
    avatar_id                    BIGINT,
    project_category_id          BIGINT,
    project_type_key             VARCHAR(50),
    archived                     BOOLEAN      NOT NULL DEFAULT FALSE,
    archived_at                  TIMESTAMP,
    issue_type_scheme_id         BIGINT,
    workflow_scheme_id           BIGINT,
    field_config_scheme_id       BIGINT,
    issue_type_screen_scheme_id  BIGINT,
    permission_scheme_id         BIGINT,
    notification_scheme_id       BIGINT,
    priority_scheme_id           BIGINT,
    issue_security_scheme_id     BIGINT,
    created_at                   TIMESTAMP,
    updated_at                   TIMESTAMP,
    created_by                   BIGINT,
    updated_by                   BIGINT,
    deleted_at                   TIMESTAMP    NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_projects_tenant_key
    ON projects (tenant_id, key)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_projects_tenant_archived
    ON projects (tenant_id, archived);

CREATE INDEX IF NOT EXISTS idx_projects_tenant_category
    ON projects (tenant_id, project_category_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS project_issue_counters (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL,
    project_id  BIGINT NOT NULL,
    counter     BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_project_issue_counters_tenant_project
    ON project_issue_counters (tenant_id, project_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS status_categories (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL,
    name        VARCHAR(50) NOT NULL,
    key         VARCHAR(50) NOT NULL,
    color_name  VARCHAR(50),
    is_system   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP   NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_status_categories_tenant_key
    ON status_categories (tenant_id, key)
    WHERE deleted_at IS NULL;

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS statuses (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    status_key  VARCHAR(100) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url    VARCHAR(255),
    category_id BIGINT,
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP    NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_statuses_tenant_key
    ON statuses (tenant_id, status_key)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_statuses_tenant_category
    ON statuses (tenant_id, category_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS work_items (
    id                       BIGSERIAL PRIMARY KEY,
    tenant_id                BIGINT       NOT NULL,
    project_id               BIGINT       NOT NULL,
    issue_type_id            BIGINT       NOT NULL,
    issue_no                 BIGINT       NOT NULL,
    key                      VARCHAR(30)  NOT NULL,
    summary                  VARCHAR(512) NOT NULL,
    description              TEXT,
    status_id                BIGINT       NOT NULL,
    priority_id              BIGINT       NOT NULL,
    resolution_id            BIGINT,
    assignee_id              BIGINT,
    reporter_id              BIGINT       NOT NULL,
    parent_id                BIGINT,
    security_level_id        BIGINT,
    due_date                 TIMESTAMP,
    rank                     VARCHAR(255),
    time_original_estimate   BIGINT,
    time_remaining_estimate  BIGINT,
    time_spent               BIGINT,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP,
    created_by               BIGINT,
    updated_by               BIGINT,
    deleted_at               TIMESTAMP    NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_work_items_tenant_key
    ON work_items (tenant_id, key)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_work_items_tenant_project_issue_no
    ON work_items (tenant_id, project_id, issue_no)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_work_items_tenant_project
    ON work_items (tenant_id, project_id);

CREATE INDEX IF NOT EXISTS idx_work_items_tenant_status
    ON work_items (tenant_id, status_id);

CREATE INDEX IF NOT EXISTS idx_work_items_tenant_assignee
    ON work_items (tenant_id, assignee_id);

CREATE INDEX IF NOT EXISTS idx_work_items_tenant_rank
    ON work_items (tenant_id, rank);

CREATE INDEX IF NOT EXISTS idx_work_items_tenant_created_at
    ON work_items (tenant_id, created_at);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS resolutions (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL,
    name        VARCHAR(50) NOT NULL,
    description TEXT,
    sequence    INT,
    is_system   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,
    deleted_at  TIMESTAMP   NULL
);

CREATE INDEX IF NOT EXISTS idx_resolutions_tenant
    ON resolutions (tenant_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS issue_link_types (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    name         VARCHAR(100) NOT NULL,
    outward_desc VARCHAR(100),
    inward_desc  VARCHAR(100),
    is_system    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,
    deleted_at   TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_issue_link_types_tenant
    ON issue_link_types (tenant_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS issue_links (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT NOT NULL,
    source_id    BIGINT NOT NULL,
    target_id    BIGINT NOT NULL,
    link_type_id BIGINT NOT NULL,
    sequence     INT,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,
    deleted_at   TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_issue_links_tenant_source
    ON issue_links (tenant_id, source_id);

CREATE INDEX IF NOT EXISTS idx_issue_links_tenant_target
    ON issue_links (tenant_id, target_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS worklogs (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    author_id    BIGINT NOT NULL,
    comment      TEXT,
    start_date   TIMESTAMP,
    time_spent   BIGINT,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,
    deleted_at   TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_worklogs_tenant_work_item
    ON worklogs (tenant_id, work_item_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tenant_scheme_mappings (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT      NOT NULL,
    scheme_type      VARCHAR(50) NOT NULL,
    source_scheme_id BIGINT      NOT NULL,
    tenant_scheme_id BIGINT      NOT NULL,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    created_by       BIGINT,
    updated_by       BIGINT,
    deleted_at       TIMESTAMP   NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_tenant_scheme_mappings_source
    ON tenant_scheme_mappings (tenant_id, scheme_type, source_scheme_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_tenant_scheme_mappings_target
    ON tenant_scheme_mappings (tenant_id, scheme_type, tenant_scheme_id)
    WHERE deleted_at IS NULL;
