/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create channels table
*/

CREATE TABLE channels (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    
    -- Tenant & Ownership
    tenant_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    
    -- Channel Info
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Channel Type
    type VARCHAR(50) NOT NULL, 
    -- Values: 'DIRECT', 'GROUP', 'TOPIC'
    
    -- Entity Linking (for TOPIC channels)
    entity_type VARCHAR(100),
    -- Values: 'customer', 'task', 'order', 'lead', etc.
    entity_id BIGINT,
    
    -- Settings
    is_private BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    
    -- Stats (denormalized for performance)
    member_count INT DEFAULT 0,
    message_count INT DEFAULT 0,
    last_message_at TIMESTAMP,
    
    -- Metadata
    metadata JSONB,
    -- Example: {"color": "#FF5733", "icon": "👥", "pinned": true}
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_channel_type CHECK (type IN ('DIRECT', 'GROUP', 'TOPIC')),
    CONSTRAINT topic_has_entity CHECK (
        (type = 'TOPIC' AND entity_type IS NOT NULL AND entity_id IS NOT NULL) OR
        (type != 'TOPIC')
    )
);

-- Basic indexes
CREATE INDEX idx_channels_tenant_type 
    ON channels(tenant_id, type, is_archived);

CREATE INDEX idx_channels_entity 
    ON channels(tenant_id, entity_type, entity_id) 
    WHERE entity_type IS NOT NULL;

CREATE INDEX idx_channels_created_by 
    ON channels(created_by, tenant_id);

CREATE INDEX idx_channels_last_message 
    ON channels(tenant_id, last_message_at DESC NULLS LAST) 
    WHERE is_archived = FALSE;

-- Unique constraint for DIRECT channels
-- Ensures one-to-one direct messaging between users
CREATE UNIQUE INDEX idx_direct_channel_unique 
    ON channels(tenant_id, LEAST(created_by, entity_id), GREATEST(created_by, entity_id))
    WHERE type = 'DIRECT';
