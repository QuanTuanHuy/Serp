/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create messages table
*/

CREATE TABLE messages (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    
    -- Foreign Keys
    channel_id BIGINT NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- Content
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    -- Values: 'TEXT', 'IMAGE', 'FILE', 'SYSTEM', 'CODE', 'POLL'
    
    -- Rich Features
    mentions BIGINT[] DEFAULT '{}',
    -- Array of user IDs mentioned with @
    
    -- Threading (for reply threads)
    parent_id BIGINT REFERENCES messages(id) ON DELETE SET NULL,
    thread_count INT DEFAULT 0,
    -- Number of replies to this message
    
    -- Editing & Deletion
    is_edited BOOLEAN DEFAULT FALSE,
    edited_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    
    -- Engagement
    reactions JSONB DEFAULT '[]',
    -- Example: [{"emoji": "👍", "users": [1, 2, 3]}, {"emoji": "❤️", "users": [4]}]
    
    read_by BIGINT[] DEFAULT '{}',
    -- Array of user IDs who read this message
    
    -- Metadata
    metadata JSONB,
    -- Example: {"formatted": true, "language": "javascript", "poll_data": {...}}
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_message_type CHECK (
        message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM', 'CODE', 'POLL')
    ),
    CONSTRAINT content_not_empty CHECK (
        char_length(trim(content)) > 0 OR message_type != 'TEXT'
    )
);

-- Basic indexes
CREATE INDEX idx_messages_channel_time 
    ON messages(channel_id, created_at DESC) 
    WHERE is_deleted = FALSE;

CREATE INDEX idx_messages_sender 
    ON messages(tenant_id, sender_id, created_at DESC);

CREATE INDEX idx_messages_parent 
    ON messages(parent_id) 
    WHERE parent_id IS NOT NULL;

CREATE INDEX idx_messages_mentions 
    ON messages USING GIN(mentions) 
    WHERE mentions != '{}';
