# Module 09: Collaboration & Audit (Comments, Attachments, History)

**Design Philosophy:** Collaboration records and audit trails must be immutable enough for accountability while still supporting rich content and future channels.

## Shared Base Columns (applies to all tables in this module)

- `tenant_id BIGINT NOT NULL`
- `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- `created_by BIGINT`, `updated_by BIGINT`
- `deleted_at TIMESTAMP NULL`

## 9.1. `issue_comments`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| issue_id | BIGINT | FK -> work_items |
| author_id | BIGINT | User id |
| body | TEXT | Markdown/JSON body |
| is_internal | BOOLEAN | Internal-only flag |
| edited_at | TIMESTAMP | Last edit time |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 9.2. `comment_mentions`

Normalize mentions for faster notification lookup and analytics.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| comment_id | BIGINT | FK -> issue_comments |
| mentioned_user_id | BIGINT | Mentioned user id |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 9.3. `attachments`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| issue_id | BIGINT | FK -> work_items |
| uploader_id | BIGINT | User id |
| filename | VARCHAR(255) | Original filename |
| mime_type | VARCHAR(100) | MIME type |
| size_bytes | BIGINT | File size |
| storage_provider | VARCHAR(30) | S3, MINIO, LOCAL |
| storage_key | VARCHAR(512) | Object key/path |
| thumbnail_key | VARCHAR(512) | Thumbnail key/path |
| checksum | VARCHAR(128) | Integrity checksum |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 9.4. `issue_watchers`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| issue_id | BIGINT | FK -> work_items |
| user_id | BIGINT | Watcher user id |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 9.5. `change_groups`

Represents one logical update action that may contain multiple field changes.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| issue_id | BIGINT | FK -> work_items |
| author_id | BIGINT | User who changed the issue |
| source | VARCHAR(30) | UI, API, AUTOMATION |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 9.6. `change_items`

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| group_id | BIGINT | FK -> change_groups |
| field | VARCHAR(100) | Field key |
| field_type | VARCHAR(20) | SYSTEM, CUSTOM |
| old_value | TEXT | Raw old value |
| old_string | TEXT | Display old value |
| new_value | TEXT | Raw new value |
| new_string | TEXT | Display new value |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## 9.7. `audit_events`

Generic, append-friendly audit stream for non-issue entities.

| Column | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| tenant_id | BIGINT | Tenant scope |
| entity_type | VARCHAR(50) | PROJECT, WORK_ITEM, SPRINT, BOARD, FILTER, etc. |
| entity_id | BIGINT | Entity identifier |
| action | VARCHAR(30) | CREATE, UPDATE, DELETE, TRANSITION, ASSIGN |
| actor_id | BIGINT | User/service actor |
| payload_json | JSONB | Structured event payload |
| occurred_at | TIMESTAMP | Business event time |
| created_at, updated_at, created_by, updated_by, deleted_at | TIMESTAMP/BIGINT | Base audit columns |

## Suggested Constraints & Indexes

- `UNIQUE (tenant_id, issue_id, user_id)` on `issue_watchers`
- `UNIQUE (tenant_id, comment_id, mentioned_user_id)` on `comment_mentions`
- `INDEX (tenant_id, issue_id, created_at DESC)` on `issue_comments`, `change_groups`
- `INDEX (tenant_id, entity_type, entity_id, occurred_at DESC)` on `audit_events`
