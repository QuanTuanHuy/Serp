CREATE TABLE permission_definitions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    permission_key VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_permission_definitions_category
        CHECK (
            category IN (
                'ADMINISTRATION',
                'PROJECT',
                'ISSUE',
                'VOTERS_WATCHERS',
                'COMMENTS',
                'ATTACHMENTS',
                'TIME_TRACKING'
            )
        )
);

CREATE TABLE permission_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE permission_scheme_entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    permission_key VARCHAR(100) NOT NULL,
    grantee_type VARCHAR(40) NOT NULL,
    grantee_ref VARCHAR(255),
    custom_field_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_permission_scheme_entries_grantee_type
        CHECK (
            grantee_type IN (
                'PROJECT_ROLE',
                'GROUP',
                'USER',
                'PROJECT_LEAD',
                'REPORTER',
                'ASSIGNEE',
                'APPLICATION_ACCESS',
                'ANYONE_ON_WEB',
                'USER_CUSTOM_FIELD_VALUE',
                'GROUP_CUSTOM_FIELD_VALUE'
            )
        ),
    CONSTRAINT fk_permission_scheme_entries_scheme
        FOREIGN KEY (scheme_id) REFERENCES permission_schemes (id),
    CONSTRAINT fk_permission_scheme_entries_custom_field
        FOREIGN KEY (custom_field_id) REFERENCES custom_fields (id)
);

CREATE TABLE issue_security_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_level_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE issue_security_levels (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_issue_security_levels_scheme
        FOREIGN KEY (scheme_id) REFERENCES issue_security_schemes (id)
);

CREATE TABLE issue_security_level_members (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    level_id BIGINT NOT NULL,
    subject_type VARCHAR(40) NOT NULL,
    subject_ref VARCHAR(255),
    custom_field_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_issue_security_level_members_subject_type
        CHECK (
            subject_type IN (
                'PROJECT_ROLE',
                'GROUP',
                'USER',
                'PROJECT_LEAD',
                'REPORTER',
                'ASSIGNEE',
                'USER_CUSTOM_FIELD_VALUE',
                'GROUP_CUSTOM_FIELD_VALUE'
            )
        ),
    CONSTRAINT fk_issue_security_level_members_level
        FOREIGN KEY (level_id) REFERENCES issue_security_levels (id),
    CONSTRAINT fk_issue_security_level_members_custom_field
        FOREIGN KEY (custom_field_id) REFERENCES custom_fields (id)
);

CREATE TABLE notification_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    event_key VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_system BOOLEAN,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE notification_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE notification_scheme_entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    recipient_type VARCHAR(50) NOT NULL,
    recipient_ref VARCHAR(255),
    custom_field_id BIGINT,
    channel VARCHAR(20) NOT NULL,
    template_id BIGINT,
    is_enabled BOOLEAN,
    conditions_json TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_notification_scheme_entries_recipient_type
        CHECK (
            recipient_type IN (
                'ASSIGNEE',
                'REPORTER',
                'WATCHERS',
                'CURRENT_USER',
                'PROJECT_LEAD',
                'COMPONENT_LEAD',
                'PROJECT_ROLE',
                'GROUP',
                'USER',
                'USER_CUSTOM_FIELD_VALUE',
                'GROUP_CUSTOM_FIELD_VALUE'
            )
        ),
    CONSTRAINT chk_notification_scheme_entries_channel
        CHECK (channel IN ('EMAIL', 'IN_APP', 'WEBHOOK')),
    CONSTRAINT fk_notification_scheme_entries_scheme
        FOREIGN KEY (scheme_id) REFERENCES notification_schemes (id),
    CONSTRAINT fk_notification_scheme_entries_event
        FOREIGN KEY (event_id) REFERENCES notification_events (id),
    CONSTRAINT fk_notification_scheme_entries_custom_field
        FOREIGN KEY (custom_field_id) REFERENCES custom_fields (id)
);

ALTER TABLE issue_security_schemes
    ADD CONSTRAINT fk_issue_security_schemes_default_level
        FOREIGN KEY (default_level_id) REFERENCES issue_security_levels (id);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_permission_scheme
        FOREIGN KEY (permission_scheme_id) REFERENCES permission_schemes (id);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_issue_security_scheme
        FOREIGN KEY (issue_security_scheme_id) REFERENCES issue_security_schemes (id);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_notification_scheme
        FOREIGN KEY (notification_scheme_id) REFERENCES notification_schemes (id);

CREATE UNIQUE INDEX uk_permission_definitions_tenant_key
    ON permission_definitions (tenant_id, permission_key)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_permission_schemes_tenant_name
    ON permission_schemes (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_permission_scheme_entries_grant
    ON permission_scheme_entries (
        tenant_id,
        scheme_id,
        permission_key,
        grantee_type,
        COALESCE(grantee_ref, '__CTX__'),
        COALESCE(custom_field_id, 0)
    )
    WHERE deleted_at IS NULL;

CREATE INDEX idx_permission_scheme_entries_scheme
    ON permission_scheme_entries (tenant_id, scheme_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_issue_security_schemes_tenant_name
    ON issue_security_schemes (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_issue_security_levels_scheme
    ON issue_security_levels (tenant_id, scheme_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_issue_security_level_members_subject
    ON issue_security_level_members (
        tenant_id,
        level_id,
        subject_type,
        COALESCE(subject_ref, '__CTX__'),
        COALESCE(custom_field_id, 0)
    )
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_notification_events_tenant_event_key
    ON notification_events (tenant_id, event_key)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_notification_schemes_tenant_name
    ON notification_schemes (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_notification_scheme_entries_lookup
    ON notification_scheme_entries (tenant_id, scheme_id, event_id)
    WHERE deleted_at IS NULL;
