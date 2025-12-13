# Database Schema

**Module:** notification_service  
**Database:** PostgreSQL  
**Ngày tạo:** 2025-12-13

---

## 1. ER Diagram

```
┌─────────────────────────────────┐
│         notifications           │
├─────────────────────────────────┤
│ id (PK)                         │
│ user_id (FK, indexed)           │
│ tenant_id (indexed)             │
│ title, message                  │
│ type, category, priority        │
│ source_service, source_event_id │
│ action_url, action_type         │
│ entity_type, entity_id          │
│ is_read, read_at                │
│ is_archived                     │
│ delivery_channels (jsonb)       │
│ delivered_at, expires_at        │
│ metadata (jsonb)                │
│ created_at, updated_at          │
└─────────────────────────────────┘
              │
              │ user_id
              ▼
┌─────────────────────────────────┐
│   notification_preferences      │
├─────────────────────────────────┤
│ id (PK)                         │
│ user_id (unique, indexed)       │
│ tenant_id                       │
│ enable_in_app, enable_email     │
│ enable_push                     │
│ category_settings (jsonb)       │
│ quiet_hours_enabled             │
│ quiet_hours_start/end           │
│ timezone                        │
│ created_at, updated_at          │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│    notification_templates       │
├─────────────────────────────────┤
│ id (PK)                         │
│ tenant_id                       │
│ code (unique per tenant)        │
│ name, description               │
│ title_template                  │
│ message_template                │
│ type, category, priority        │
│ channels (jsonb)                │
│ variables_schema (jsonb)        │
│ is_active, active_status        │
│ created_at, updated_at          │
└─────────────────────────────────┘
```

---

## 2. Table Definitions

### 2.1. notifications

```sql
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    
    -- Target
    user_id         BIGINT NOT NULL,
    tenant_id       BIGINT NOT NULL,
    
    -- Content
    title           VARCHAR(200) NOT NULL,
    message         TEXT NOT NULL,
    type            VARCHAR(20) NOT NULL DEFAULT 'INFO',
    category        VARCHAR(50) NOT NULL,
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    
    -- Source
    source_service  VARCHAR(50) NOT NULL,
    source_event_id VARCHAR(100),
    
    -- Action
    action_url      VARCHAR(500),
    action_type     VARCHAR(20),
    
    -- Entity reference
    entity_type     VARCHAR(50),
    entity_id       BIGINT,
    
    -- Status
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMP,
    is_archived     BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Delivery
    delivery_channels JSONB DEFAULT '["IN_APP"]',
    delivered_at      TIMESTAMP,
    expires_at        TIMESTAMP,
    
    -- Metadata
    metadata        JSONB,
    
    -- Timestamps
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_notifications_user_unread 
    ON notifications(user_id, is_read, created_at DESC) 
    WHERE is_archived = FALSE;

CREATE INDEX idx_notifications_user_category 
    ON notifications(user_id, category, created_at DESC);

CREATE INDEX idx_notifications_tenant_created 
    ON notifications(tenant_id, created_at DESC);

CREATE INDEX idx_notifications_entity 
    ON notifications(entity_type, entity_id) 
    WHERE entity_type IS NOT NULL;

CREATE INDEX idx_notifications_expires 
    ON notifications(expires_at) 
    WHERE expires_at IS NOT NULL;

CREATE INDEX idx_notifications_source 
    ON notifications(source_service, source_event_id);
```

### 2.2. notification_preferences

```sql
CREATE TABLE notification_preferences (
    id                   BIGSERIAL PRIMARY KEY,
    
    user_id              BIGINT NOT NULL,
    tenant_id            BIGINT NOT NULL,
    
    -- Channel toggles
    enable_in_app        BOOLEAN NOT NULL DEFAULT TRUE,
    enable_email         BOOLEAN NOT NULL DEFAULT TRUE,
    enable_push          BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Per-category settings
    category_settings    JSONB DEFAULT '{}',
    
    -- Quiet hours
    quiet_hours_enabled  BOOLEAN NOT NULL DEFAULT FALSE,
    quiet_hours_start    TIME,
    quiet_hours_end      TIME,
    timezone             VARCHAR(50) DEFAULT 'UTC',
    
    -- Timestamps
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_preferences_user UNIQUE (user_id, tenant_id)
);

CREATE INDEX idx_preferences_user ON notification_preferences(user_id);
```

### 2.3. notification_templates

```sql
CREATE TABLE notification_templates (
    id               BIGSERIAL PRIMARY KEY,
    
    tenant_id        BIGINT NOT NULL,
    
    -- Template identification
    code             VARCHAR(100) NOT NULL,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    
    -- Content templates
    title_template   VARCHAR(500) NOT NULL,
    message_template TEXT NOT NULL,
    
    -- Defaults
    type             VARCHAR(20) NOT NULL DEFAULT 'INFO',
    category         VARCHAR(50) NOT NULL,
    priority         VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    channels         JSONB DEFAULT '["IN_APP"]',
    
    -- Schema for variables
    variables_schema JSONB,
    
    -- Status
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    active_status    VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Timestamps
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_template_code_tenant UNIQUE (tenant_id, code)
);

CREATE INDEX idx_templates_tenant ON notification_templates(tenant_id, is_active);
CREATE INDEX idx_templates_code ON notification_templates(code);
```

---

## 3. GORM Models

### 3.1. NotificationModel

```go
// infrastructure/store/model/notification_model.go
package model

import (
    "time"
    "gorm.io/datatypes"
)

type NotificationModel struct {
    BaseModel
    
    UserID   int64 `gorm:"not null;index:idx_notif_user_unread,priority:1"`
    TenantID int64 `gorm:"not null;index:idx_notif_tenant"`
    
    Title    string `gorm:"type:varchar(200);not null"`
    Message  string `gorm:"type:text;not null"`
    Type     string `gorm:"type:varchar(20);not null;default:'INFO'"`
    Category string `gorm:"type:varchar(50);not null"`
    Priority string `gorm:"type:varchar(20);not null;default:'MEDIUM'"`
    
    SourceService string  `gorm:"type:varchar(50);not null"`
    SourceEventID *string `gorm:"type:varchar(100)"`
    
    ActionURL  *string `gorm:"type:varchar(500)"`
    ActionType *string `gorm:"type:varchar(20)"`
    
    EntityType *string `gorm:"type:varchar(50);index:idx_notif_entity,priority:1"`
    EntityID   *int64  `gorm:"index:idx_notif_entity,priority:2"`
    
    IsRead     bool       `gorm:"not null;default:false;index:idx_notif_user_unread,priority:2"`
    ReadAt     *time.Time
    IsArchived bool       `gorm:"not null;default:false"`
    
    DeliveryChannels datatypes.JSON `gorm:"type:jsonb;default:'[\"IN_APP\"]'"`
    DeliveredAt      *time.Time
    ExpiresAt        *time.Time `gorm:"index:idx_notif_expires"`
    
    Metadata datatypes.JSON `gorm:"type:jsonb"`
}

func (NotificationModel) TableName() string {
    return "notifications"
}
```

### 3.2. NotificationPreferenceModel

```go
// infrastructure/store/model/notification_preference_model.go
package model

import "gorm.io/datatypes"

type NotificationPreferenceModel struct {
    BaseModel
    
    UserID   int64 `gorm:"not null;uniqueIndex:uq_pref_user_tenant,priority:1"`
    TenantID int64 `gorm:"not null;uniqueIndex:uq_pref_user_tenant,priority:2"`
    
    EnableInApp bool `gorm:"not null;default:true"`
    EnableEmail bool `gorm:"not null;default:true"`
    EnablePush  bool `gorm:"not null;default:false"`
    
    CategorySettings datatypes.JSON `gorm:"type:jsonb;default:'{}'"`
    
    QuietHoursEnabled bool    `gorm:"not null;default:false"`
    QuietHoursStart   *string `gorm:"type:time"`
    QuietHoursEnd     *string `gorm:"type:time"`
    Timezone          string  `gorm:"type:varchar(50);default:'UTC'"`
}

func (NotificationPreferenceModel) TableName() string {
    return "notification_preferences"
}
```

---

## 4. Performance Considerations

### 4.1. Partitioning (cho high volume)

```sql
-- Partition by created_at monthly
CREATE TABLE notifications_partitioned (
    LIKE notifications INCLUDING ALL
) PARTITION BY RANGE (created_at);

-- Create monthly partitions
CREATE TABLE notifications_2025_12 
    PARTITION OF notifications_partitioned
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');
```

### 4.2. Archival Strategy

```sql
-- Move old read notifications to archive
CREATE TABLE notifications_archive (LIKE notifications INCLUDING ALL);

-- Scheduled job: Archive notifications older than 90 days
INSERT INTO notifications_archive
SELECT * FROM notifications 
WHERE is_read = TRUE AND created_at < NOW() - INTERVAL '90 days';

DELETE FROM notifications 
WHERE is_read = TRUE AND created_at < NOW() - INTERVAL '90 days';
```
