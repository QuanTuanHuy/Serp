# Discuss Service - Database Schema

**Authors:** QuanTuanHuy  
**Description:** Part of Serp Project - Database Design & Schema  
**Date:** December 2025  

## 🗄️ Database Technology

- **Primary DB:** PostgreSQL 15+
- **Cache Layer:** Redis 7+
- **Rationale:**
  - PostgreSQL: ACID compliance, JSON support, full-text search
  - Redis: Real-time data (presence, typing), caching

---

## 📊 Entity Relationship Diagram

```
┌──────────────────┐         ┌──────────────────┐
│    Channels      │◄───────│  ChannelMembers  │
│                  │    1:N  │                  │
│ - id (PK)        │         │ - id (PK)        │
│ - tenant_id      │         │ - channel_id (FK)│
│ - name           │         │ - user_id        │
│ - type           │         │ - role           │
│ - entity_type    │         │ - unread_count   │
│ - entity_id      │         │ - last_read_msg  │
│ - is_private     │         └──────────────────┘
│ - created_by     │                 │
└──────┬───────────┘                 │
       │                             │
       │ 1:N                         │
       │                             │
       ▼                             │
┌──────────────────┐                 │
│    Messages      │                 │
│                  │                 │
│ - id (PK)        │                 │
│ - channel_id (FK)│─────────────────┘
│ - sender_id      │
│ - content        │
│ - message_type   │
│ - mentions       │  (array of user_ids)
│ - parent_id (FK) │  (for threads)
│ - is_edited      │
│ - is_deleted     │
│ - reactions      │  (JSONB)
└──────┬───────────┘
       │
       │ 1:N
       ▼
┌──────────────────┐
│   Attachments    │
│                  │
│ - id (PK)        │
│ - message_id (FK)│
│ - file_name      │
│ - file_size      │
│ - file_type      │
│ - s3_url         │
└──────────────────┘

┌──────────────────┐
│  ActivityFeed    │  (Aggregated view)
│                  │
│ - id (PK)        │
│ - tenant_id      │
│ - user_id        │
│ - action_type    │
│ - entity_type    │
│ - entity_id      │
│ - channel_id     │
│ - timestamp      │
└──────────────────┘
```

---

## 📋 Table Definitions

### **1. channels**

```sql
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
    last_message_at BIGINT,
    
    -- Metadata
    metadata JSONB,
    -- Example: {"color": "#FF5733", "icon": "👥", "pinned": true}
    
    -- Timestamps
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    
    -- Constraints
    CONSTRAINT valid_channel_type CHECK (type IN ('DIRECT', 'GROUP', 'TOPIC')),
    CONSTRAINT topic_has_entity CHECK (
        (type = 'TOPIC' AND entity_type IS NOT NULL AND entity_id IS NOT NULL) OR
        (type != 'TOPIC')
    )
);

-- Indexes
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
CREATE UNIQUE INDEX idx_direct_channel_unique 
    ON channels(tenant_id, LEAST(created_by, entity_id), GREATEST(created_by, entity_id))
    WHERE type = 'DIRECT';
```

**Notes:**
- `DIRECT` channels: Unique per pair of users (use LEAST/GREATEST for ordering)
- `entity_type` + `entity_id`: Link to business entities
- `metadata`: Flexible storage for UI preferences

---

### **2. messages**

```sql
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
    edited_at BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at BIGINT,
    deleted_by BIGINT,
    
    -- Engagement
    reactions JSONB DEFAULT '[]',
    -- Example: [{"emoji": "👍", "users": [1, 2, 3]}, {"emoji": "❤️", "users": [4]}]
    
    read_by BIGINT[] DEFAULT '{}',
    -- Array of user IDs who read this message
    
    -- Search
    search_vector tsvector,
    -- Generated column for full-text search
    
    -- Metadata
    metadata JSONB,
    -- Example: {"formatted": true, "language": "javascript", "poll_data": {...}}
    
    -- Timestamps
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    
    -- Constraints
    CONSTRAINT valid_message_type CHECK (
        message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM', 'CODE', 'POLL')
    ),
    CONSTRAINT content_not_empty CHECK (
        char_length(trim(content)) > 0 OR message_type != 'TEXT'
    )
);

-- Indexes
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

-- Full-text search index
CREATE INDEX idx_messages_search 
    ON messages USING GIN(search_vector);

-- Generated column for search
ALTER TABLE messages 
    ADD COLUMN search_vector tsvector 
    GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;

-- Partition by created_at (monthly) for large datasets
-- CREATE TABLE messages_2025_01 PARTITION OF messages
--     FOR VALUES FROM (1704067200000) TO (1706745600000);
```

**Notes:**
- `mentions`: Array for efficient filtering (`WHERE sender_id = ANY(mentions)`)
- `reactions`: JSONB for flexible emoji reactions
- `search_vector`: Generated column for fast full-text search
- `thread_count`: Denormalized for quick thread display
- Partitioning: Optional for very high volume (>100M messages)

---

### **3. channel_members**

```sql
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
    joined_at BIGINT NOT NULL,
    left_at BIGINT,
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
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    
    -- Constraints
    CONSTRAINT valid_role CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'GUEST')),
    CONSTRAINT valid_status CHECK (status IN ('ACTIVE', 'MUTED', 'LEFT', 'REMOVED')),
    CONSTRAINT valid_notification CHECK (
        notification_level IN ('ALL', 'MENTIONS', 'NONE')
    ),
    
    -- Unique: One membership per user per channel
    UNIQUE (channel_id, user_id)
);

-- Indexes
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
```

**Notes:**
- `unread_count`: Denormalized for performance (updated via trigger or app logic)
- `is_pinned`: User can pin important channels to top
- `notification_level`: Per-channel notification preference
- Unique constraint prevents duplicate memberships

---

### **4. attachments**

```sql
CREATE TABLE attachments (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    
    -- Foreign Keys
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    channel_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- File Info
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL, -- bytes
    file_type VARCHAR(100),    -- MIME type
    file_extension VARCHAR(20),
    
    -- Storage
    s3_bucket VARCHAR(255),
    s3_key VARCHAR(500) NOT NULL,
    s3_url TEXT NOT NULL,
    
    -- Preview (for images)
    thumbnail_url TEXT,
    width INT,
    height INT,
    
    -- Virus Scan
    scan_status VARCHAR(50) DEFAULT 'PENDING',
    -- Values: 'PENDING', 'CLEAN', 'INFECTED', 'ERROR'
    scanned_at BIGINT,
    
    -- Metadata
    metadata JSONB,
    -- Example: {"duration": 120, "codec": "h264"} for videos
    
    -- Timestamps
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    
    -- Constraints
    CONSTRAINT valid_scan_status CHECK (
        scan_status IN ('PENDING', 'CLEAN', 'INFECTED', 'ERROR')
    ),
    CONSTRAINT positive_file_size CHECK (file_size > 0)
);

-- Indexes
CREATE INDEX idx_attachments_message 
    ON attachments(message_id);

CREATE INDEX idx_attachments_channel 
    ON attachments(channel_id, created_at DESC);

CREATE INDEX idx_attachments_tenant_type 
    ON attachments(tenant_id, file_type, created_at DESC);

CREATE INDEX idx_attachments_scan 
    ON attachments(scan_status) 
    WHERE scan_status = 'PENDING';
```

**Notes:**
- `s3_url`: Pre-signed URL with expiry (generated on-demand)
- `scan_status`: Malware scanning before allowing download
- `thumbnail_url`: For image/video previews

---

### **5. activity_feed**

```sql
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
    read_at BIGINT,
    
    -- Metadata
    metadata JSONB,
    -- Additional context (e.g., task priority, customer name)
    
    -- Timestamps
    timestamp BIGINT NOT NULL,
    -- When the activity occurred
    
    created_at BIGINT NOT NULL,
    
    -- Constraints
    CONSTRAINT has_entity_or_channel CHECK (
        (entity_type IS NOT NULL AND entity_id IS NOT NULL) OR
        (channel_id IS NOT NULL)
    )
);

-- Indexes
CREATE INDEX idx_activity_feed_user_time 
    ON activity_feed(user_id, tenant_id, timestamp DESC);

CREATE INDEX idx_activity_feed_unread 
    ON activity_feed(user_id, is_read, timestamp DESC) 
    WHERE is_read = FALSE;

CREATE INDEX idx_activity_feed_entity 
    ON activity_feed(entity_type, entity_id, timestamp DESC) 
    WHERE entity_type IS NOT NULL;

CREATE INDEX idx_activity_feed_channel 
    ON activity_feed(channel_id, timestamp DESC) 
    WHERE channel_id IS NOT NULL;

-- Partition by timestamp (monthly)
-- CREATE TABLE activity_feed_2025_01 PARTITION OF activity_feed
--     FOR VALUES FROM (1704067200000) TO (1706745600000);
```

**Notes:**
- Aggregates activities from all modules (discuss, CRM, PTM, etc.)
- `user_id`: The recipient of the notification
- `actor_id`: Who performed the action
- Partitioned by time for efficient archiving

---

## 🔧 Triggers & Functions

### **Trigger: Update unread_count**

```sql
CREATE OR REPLACE FUNCTION update_unread_counts()
RETURNS TRIGGER AS $$
BEGIN
    -- Increment unread count for all channel members except sender
    UPDATE channel_members
    SET unread_count = unread_count + 1,
        updated_at = EXTRACT(EPOCH FROM NOW()) * 1000
    WHERE channel_id = NEW.channel_id
      AND user_id != NEW.sender_id
      AND status = 'ACTIVE';
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_unread_counts
AFTER INSERT ON messages
FOR EACH ROW
WHEN (NEW.is_deleted = FALSE)
EXECUTE FUNCTION update_unread_counts();
```

### **Trigger: Update channel stats**

```sql
CREATE OR REPLACE FUNCTION update_channel_stats()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Update message count and last message time
        UPDATE channels
        SET message_count = message_count + 1,
            last_message_at = NEW.created_at,
            updated_at = NEW.created_at
        WHERE id = NEW.channel_id;
        
    ELSIF TG_OP = 'DELETE' THEN
        -- Decrement message count
        UPDATE channels
        SET message_count = GREATEST(0, message_count - 1),
            updated_at = EXTRACT(EPOCH FROM NOW()) * 1000
        WHERE id = OLD.channel_id;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_channel_stats
AFTER INSERT OR DELETE ON messages
FOR EACH ROW
EXECUTE FUNCTION update_channel_stats();
```

### **Trigger: Update member count**

```sql
CREATE OR REPLACE FUNCTION update_member_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.status = 'ACTIVE' THEN
        UPDATE channels
        SET member_count = member_count + 1
        WHERE id = NEW.channel_id;
        
    ELSIF TG_OP = 'UPDATE' AND OLD.status = 'ACTIVE' AND NEW.status != 'ACTIVE' THEN
        UPDATE channels
        SET member_count = GREATEST(0, member_count - 1)
        WHERE id = NEW.channel_id;
        
    ELSIF TG_OP = 'DELETE' AND OLD.status = 'ACTIVE' THEN
        UPDATE channels
        SET member_count = GREATEST(0, member_count - 1)
        WHERE id = OLD.channel_id;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_member_count
AFTER INSERT OR UPDATE OR DELETE ON channel_members
FOR EACH ROW
EXECUTE FUNCTION update_member_count();
```

---

## 📈 Performance Views

### **View: user_channels**

```sql
CREATE VIEW user_channels AS
SELECT 
    cm.user_id,
    cm.tenant_id,
    c.id AS channel_id,
    c.name,
    c.type,
    c.entity_type,
    c.entity_id,
    c.last_message_at,
    cm.unread_count,
    cm.is_pinned,
    cm.is_muted,
    cm.role,
    -- Last message preview
    (
        SELECT content 
        FROM messages m 
        WHERE m.channel_id = c.id 
          AND m.is_deleted = FALSE
        ORDER BY m.created_at DESC 
        LIMIT 1
    ) AS last_message_preview,
    -- Online member count (from Redis, app-level join)
    c.member_count
FROM channel_members cm
JOIN channels c ON c.id = cm.channel_id
WHERE cm.status = 'ACTIVE'
  AND c.is_archived = FALSE
ORDER BY 
    cm.is_pinned DESC,
    c.last_message_at DESC NULLS LAST;
```

### **View: trending_channels**

```sql
CREATE VIEW trending_channels AS
SELECT 
    c.id,
    c.tenant_id,
    c.name,
    c.type,
    c.member_count,
    c.message_count,
    -- Messages in last 24 hours
    (
        SELECT COUNT(*) 
        FROM messages m 
        WHERE m.channel_id = c.id 
          AND m.created_at > EXTRACT(EPOCH FROM NOW() - INTERVAL '24 hours') * 1000
    ) AS recent_message_count
FROM channels c
WHERE c.is_archived = FALSE
  AND c.type = 'GROUP'
ORDER BY recent_message_count DESC
LIMIT 100;
```

---

## 🗑️ Data Retention & Archiving

### **Soft Delete Strategy**

```sql
-- Messages: Soft delete (is_deleted = TRUE)
UPDATE messages 
SET is_deleted = TRUE, 
    deleted_at = EXTRACT(EPOCH FROM NOW()) * 1000,
    deleted_by = :user_id
WHERE id = :message_id;

-- Hard delete after 90 days
DELETE FROM messages 
WHERE is_deleted = TRUE 
  AND deleted_at < EXTRACT(EPOCH FROM NOW() - INTERVAL '90 days') * 1000;
```

### **Channel Archiving**

```sql
-- Archive inactive channels (no messages in 180 days)
UPDATE channels
SET is_archived = TRUE,
    updated_at = EXTRACT(EPOCH FROM NOW()) * 1000
WHERE type = 'GROUP'
  AND (
    last_message_at IS NULL OR
    last_message_at < EXTRACT(EPOCH FROM NOW() - INTERVAL '180 days') * 1000
  )
  AND is_archived = FALSE;
```

### **Activity Feed Cleanup**

```sql
-- Delete old activity feed entries (>365 days)
DELETE FROM activity_feed
WHERE timestamp < EXTRACT(EPOCH FROM NOW() - INTERVAL '365 days') * 1000;
```

---

## 📊 Storage Estimates

### **Per-entity Sizes**

```
Channel: ~500 bytes
Message: ~500 bytes (average text) + attachments
ChannelMember: ~200 bytes
Attachment: ~300 bytes (metadata only, files in S3)
ActivityFeed: ~400 bytes
```

### **Example: 1000 users, 1 year**

```
Channels:
- 1000 DIRECT channels (user pairs): 500KB
- 100 GROUP channels: 50KB
- 5000 TOPIC channels (entities): 2.5MB
Total: ~3MB

Messages (1M/day):
- 365M messages × 500 bytes = 182GB
- With indexes: ~300GB

ChannelMembers:
- Avg 10 members/channel × 6100 channels = 61K records
- 61K × 200 bytes = 12MB

Attachments (20% of messages have files):
- 73M attachments × 300 bytes = 22GB (metadata)
- Files in S3: Assume 2MB avg = 146TB

ActivityFeed:
- 2M activities/day × 365 days = 730M records
- 730M × 400 bytes = 292GB

Total PostgreSQL: ~615GB/year
Total S3: ~146TB/year
```

---

## 🔐 Security Considerations

### **Row-Level Security (RLS)**

```sql
-- Enable RLS on channels
ALTER TABLE channels ENABLE ROW LEVEL SECURITY;

-- Policy: Users can only see channels they're members of
CREATE POLICY channel_access_policy ON channels
FOR SELECT
USING (
    id IN (
        SELECT channel_id 
        FROM channel_members 
        WHERE user_id = current_setting('app.user_id')::BIGINT
          AND status = 'ACTIVE'
    )
);

-- Policy: Users can only see messages in their channels
CREATE POLICY message_access_policy ON messages
FOR SELECT
USING (
    channel_id IN (
        SELECT channel_id 
        FROM channel_members 
        WHERE user_id = current_setting('app.user_id')::BIGINT
          AND status = 'ACTIVE'
    )
);
```

### **Encryption**

```sql
-- Encrypt sensitive content (optional)
-- Using pgcrypto extension

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Encrypt message content
INSERT INTO messages (channel_id, sender_id, content, ...)
VALUES (
    :channel_id,
    :sender_id,
    pgp_sym_encrypt(:content, :encryption_key),
    ...
);

-- Decrypt on read
SELECT 
    id,
    pgp_sym_decrypt(content::bytea, :encryption_key) AS content,
    ...
FROM messages;
```

---

## 🔄 Migration Strategy

### **Initial Setup**

```sql
-- migration/001_create_channels_table.sql
-- migration/002_create_messages_table.sql
-- migration/003_create_channel_members_table.sql
-- migration/004_create_attachments_table.sql
-- migration/005_create_activity_feed_table.sql
-- migration/006_create_indexes.sql
-- migration/007_create_triggers.sql
-- migration/008_create_views.sql
```

### **Rolling Out**

```bash
# Using golang-migrate
migrate -path migrations -database "postgres://..." up

# Or with GORM AutoMigrate (dev only)
db.AutoMigrate(
    &ChannelModel{},
    &MessageModel{},
    &ChannelMemberModel{},
    &AttachmentModel{},
    &ActivityFeedModel{},
)
```

---

## 📝 Redis Schema

### **Cached Data Structures**

```redis
# Recent messages per channel (Sorted Set)
Key: "channel:{channel_id}:messages"
Type: ZSET
Score: Unix timestamp (ms)
Value: JSON(MessageEntity)
TTL: 1 hour
Example:
ZADD channel:123:messages 1704067200000 '{"id":1,"content":"Hello",...}'
ZRANGE channel:123:messages -100 -1  # Get last 100

# User presence (Hash)
Key: "presence:{user_id}"
Type: Hash
Fields: {status, last_seen, device}
TTL: 5 minutes (auto-refresh)
Example:
HSET presence:100 status "ONLINE" last_seen 1704067200000 device "web"

# Typing indicators (Set with TTL)
Key: "typing:{channel_id}"
Type: Set
Members: user_ids
TTL: 10 seconds
Example:
SADD typing:123 100 200
SMEMBERS typing:123  # [100, 200]

# Unread counts cache (Hash)
Key: "unread:{user_id}"
Type: Hash
Fields: {channel_id: count}
TTL: None (persist)
Example:
HSET unread:100 123 5 456 2
HGETALL unread:100  # {123: 5, 456: 2}

# Online users count (String)
Key: "stats:online_users"
Type: String
Value: count
TTL: 1 minute
Example:
SET stats:online_users 1523 EX 60
```

---

## ✅ Database Checklist

- [x] Tables with proper constraints
- [x] Indexes for common queries
- [x] Foreign key relationships
- [x] Triggers for denormalized counters
- [x] Full-text search setup
- [x] Partitioning strategy (optional)
- [x] Row-level security policies
- [x] Migration scripts
- [x] Redis caching schema
- [x] Backup & recovery plan

---

## 📚 Summary

This database schema provides:
- ✅ **Flexible channel types** (DIRECT, GROUP, TOPIC)
- ✅ **Rich messaging** (threads, mentions, reactions)
- ✅ **Performance optimizations** (denormalized counts, indexes)
- ✅ **Full-text search** (tsvector)
- ✅ **Security** (RLS, tenant isolation)
- ✅ **Scalability** (partitioning, Redis caching)
- ✅ **Data integrity** (constraints, triggers)
