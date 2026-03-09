/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create activity_feed table
*/

CREATE TABLE activity_feed (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    
    -- Tenant & User
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    -- User this activity is relevant to (assigned, mentioned, etc.)
    
    -- Activity Info
    action_type VARCHAR(100) NOT NULL,
    -- Examples: 'MESSAGE_SENT', 'TASK_ASSIGNED', 'CUSTOMER_CREATED', 'MENTION_RECEIVED'
    
    actor_id BIGINT NOT NULL,
    -- User who performed the action
    
    -- Entity References
    entity_type VARCHAR(100),
    -- 'channel', 'customer', 'task', 'order', etc.
    entity_id BIGINT,
    
    -- Channel References (if activity is a message)
    channel_id BIGINT REFERENCES channels(id) ON DELETE CASCADE,
    message_id BIGINT REFERENCES messages(id) ON DELETE CASCADE,
    
    -- Display
    description TEXT NOT NULL,
    -- Human-readable description: "John mentioned you in Customer #123"
    
    title VARCHAR(255),
    -- Short title for UI
    
    -- Status
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    
    -- Metadata
    metadata JSONB,
    -- Additional context (e.g., task priority, customer name)
    
    -- Timestamps
    occurred_at TIMESTAMP NOT NULL,
    -- When the activity occurred
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT has_entity_or_channel CHECK (
        (entity_type IS NOT NULL AND entity_id IS NOT NULL) OR
        (channel_id IS NOT NULL)
    )
);

-- Basic indexes
CREATE INDEX idx_activity_feed_user_time 
    ON activity_feed(user_id, tenant_id, occurred_at DESC);

CREATE INDEX idx_activity_feed_unread 
    ON activity_feed(user_id, is_read, occurred_at DESC) 
    WHERE is_read = FALSE;

CREATE INDEX idx_activity_feed_entity 
    ON activity_feed(entity_type, entity_id, occurred_at DESC) 
    WHERE entity_type IS NOT NULL;

CREATE INDEX idx_activity_feed_channel 
    ON activity_feed(channel_id, occurred_at DESC) 
    WHERE channel_id IS NOT NULL;
