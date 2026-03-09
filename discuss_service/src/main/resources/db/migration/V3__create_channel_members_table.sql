/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create channel_members table
*/

CREATE TABLE channel_members (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    
    -- Foreign Keys
    channel_id BIGINT NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- Role & Status
    role VARCHAR(50) DEFAULT 'MEMBER',
    -- Values: 'OWNER', 'ADMIN', 'MEMBER', 'GUEST'
    
    status VARCHAR(50) DEFAULT 'ACTIVE',
    -- Values: 'ACTIVE', 'MUTED', 'LEFT', 'REMOVED'
    
    -- Timestamps
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    removed_by BIGINT,
    
    -- Reading Tracking
    last_read_msg_id BIGINT,
    unread_count INT DEFAULT 0,
    -- Denormalized counter, updated on new messages
    
    -- Preferences
    is_muted BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    notification_level VARCHAR(50) DEFAULT 'ALL',
    -- Values: 'ALL', 'MENTIONS', 'NONE'
    
    -- Metadata
    metadata JSONB,
    -- Example: {"color": "#FF5733", "nickname": "Boss"}
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_role CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'GUEST')),
    CONSTRAINT valid_status CHECK (status IN ('ACTIVE', 'MUTED', 'LEFT', 'REMOVED')),
    CONSTRAINT valid_notification CHECK (
        notification_level IN ('ALL', 'MENTIONS', 'NONE')
    ),
    
    -- Unique: One membership per user per channel
    UNIQUE (channel_id, user_id)
);

-- Basic indexes
CREATE INDEX idx_channel_members_user 
    ON channel_members(user_id, tenant_id, is_pinned DESC, updated_at DESC) 
    WHERE status = 'ACTIVE';

CREATE INDEX idx_channel_members_channel 
    ON channel_members(channel_id, status) 
    WHERE status = 'ACTIVE';

CREATE INDEX idx_channel_members_unread 
    ON channel_members(user_id, tenant_id, unread_count DESC) 
    WHERE unread_count > 0 AND status = 'ACTIVE';

CREATE INDEX idx_channel_members_role 
    ON channel_members(channel_id, role) 
    WHERE status = 'ACTIVE';
