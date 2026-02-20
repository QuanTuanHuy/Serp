# Module 06: Notifications (Event-driven Delivery)

**Design Philosophy:** Decouple event detection from delivery. PM core stores notification policies and emits delivery jobs via outbox for downstream channel services.

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 6.1. `notification_schemes`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| name | VARCHAR(255) | Scheme name |
| description | TEXT | Description |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 6.2. `notification_events`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| event_key | VARCHAR(120) | `work_item.created`, `work_item.updated`, `comment.added`, etc. |
| name | VARCHAR(255) | Display name |
| description | TEXT | Description |
| is_system | BOOLEAN | Built-in event marker |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 6.3. `notification_templates`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| template_key | VARCHAR(120) | Stable template key |
| name | VARCHAR(255) | Template name |
| channel | VARCHAR(20) | EMAIL, IN_APP, WEBHOOK |
| subject_template | VARCHAR(255) | Optional subject template |
| body_template | TEXT | Inline template or reference |
| content_type | VARCHAR(20) | TEXT, HTML, JSON |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 6.4. `notification_scheme_entries`

Maps event -> recipient rule -> channel for a scheme.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| scheme_id | BIGINT | FK -> notification_schemes |
| event_id | BIGINT | FK -> notification_events |
| recipient_type | VARCHAR(50) | ASSIGNEE, REPORTER, WATCHERS, PROJECT_LEAD, PROJECT_ROLE, GROUP, USER |
| recipient_id | VARCHAR(255) | Optional subject identifier |
| channel | VARCHAR(20) | EMAIL, IN_APP, WEBHOOK |
| template_id | BIGINT | FK -> notification_templates (nullable) |
| is_enabled | BOOLEAN | Runtime toggle |
| conditions_json | JSONB | Optional conditional trigger config |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 6.5. `notification_outbox`

Channel-agnostic outbox for asynchronous delivery workers.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| dedupe_key | VARCHAR(255) | Idempotency key |
| event_key | VARCHAR(120) | Source event key |
| channel | VARCHAR(20) | EMAIL, IN_APP, WEBHOOK |
| recipient | VARCHAR(255) | Email/user/webhook target |
| payload_json | JSONB | Compiled message payload |
| status | VARCHAR(20) | PENDING, PROCESSING, SENT, FAILED, DEAD |
| retry_count | INT | Retry attempts |
| next_retry_at | TIMESTAMP | Retry schedule |
| sent_at | TIMESTAMP | Successful delivery timestamp |
| error_message | TEXT | Last failure reason |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 6.6. `notification_delivery_logs`

Immutable history for compliance and troubleshooting.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| outbox_id | BIGINT | FK -> notification_outbox |
| provider | VARCHAR(50) | SMTP, FCM, Slack, etc. |
| provider_message_id | VARCHAR(255) | External provider id |
| status | VARCHAR(20) | SENT, FAILED, BOUNCED, OPENED |
| detail_json | JSONB | Raw provider response |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, event_key)` on `notification_events`
- `UNIQUE (tenant_id, template_key, channel)` on `notification_templates`
- `INDEX (tenant_id, scheme_id, event_id)` on `notification_scheme_entries`
- `UNIQUE (tenant_id, dedupe_key)` on `notification_outbox`
- `INDEX (tenant_id, status, next_retry_at)` on `notification_outbox`
