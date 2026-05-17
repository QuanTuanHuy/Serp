# Work Item Detail Phase 2 Backend Plan

## Goal

Implement backend support for JIRA-style work item detail phase 2 in `pm_core`.

Phase 2 must support:

- Full work item detail data for sidebar and header.
- Inline edit for common work item fields.
- Subtasks/children list.
- Linked work items list.
- Comments stored in `pm_core`.
- Real activity history from field changes.
- User display names resolved from `account` service.

Phase 2 explicitly excludes:

- Labels.
- Watchers.
- Attachments.

## Current State

Existing endpoint:

```http
GET /projects/{projectId}/work-items/{id}
```

Current response is `WorkItemDetailView`, built from `WorkItemDetailProjection` via `IWorkItemRepository.findWorkItemDetailById`.

Current detail includes core fields:

- `id`, `projectId`, `key`, `summary`, `description`.
- `issueType`, `status`, `priority`, `workflowStep`.
- `assigneeId`, `reporterId`, `parentId`.
- schedule and estimate fields.
- `createdAt`, `createdBy`, `updatedAt`, `updatedBy`.

Known gaps:

- `assignee.displayName` and `reporter.displayName` are always null.
- Detail query scopes by `id + tenantId`, not `projectId`.
- No comments table/API in `pm_core`.
- No persisted work item field history.
- Issue links table exists, but no detail dialog API exposes linked items.
- Worklogs table exists, but phase 2 activity can start with field history + comments.

Relevant existing database tables:

- `work_items`.
- `work_item_custom_field_values`.
- `worklogs`.
- `issue_links`.
- `issue_link_types`.
- `work_item_components`.

## API Contract

### 1. Enriched Work Item Detail

Keep existing route and extend response:

```http
GET /projects/{projectId}/work-items/{id}
```

Response shape:

```json
{
  "id": 10,
  "projectId": 1,
  "issueNo": 10,
  "key": "PM-10",
  "summary": "Build work item detail dialog",
  "description": "Markdown or plain text description",
  "issueType": {
    "id": 1,
    "name": "Task",
    "iconUrl": null,
    "hierarchyLevel": 1
  },
  "status": {
    "id": 2,
    "name": "In Progress"
  },
  "priority": {
    "id": 1,
    "name": "High",
    "color": "#EF4444"
  },
  "workflowStep": {
    "id": 5,
    "name": "In Progress"
  },
  "assignee": {
    "id": 5,
    "displayName": "Huy Nguyen",
    "avatarUrl": null
  },
  "reporter": {
    "id": 2,
    "displayName": "Quan Tran",
    "avatarUrl": null
  },
  "parent": {
    "id": 3,
    "key": "PM-3",
    "summary": "Parent epic"
  },
  "components": [
    {
      "id": 7,
      "name": "Frontend"
    }
  ],
  "customFields": [
    {
      "fieldId": 11,
      "fieldKey": "story_points",
      "name": "Story Points",
      "valueType": "NUMBER",
      "value": 5
    }
  ],
  "subtaskStats": {
    "total": 3,
    "done": 1
  },
  "linkStats": {
    "total": 2
  },
  "commentStats": {
    "total": 4
  },
  "resolutionId": null,
  "parentId": 3,
  "securityLevelId": null,
  "startDate": 1710000000000,
  "dueDate": 1710600000000,
  "rank": "abc",
  "timeOriginalEstimate": 28800,
  "timeRemainingEstimate": 14400,
  "timeSpent": 7200,
  "createdAt": 1710000000000,
  "createdBy": 2,
  "updatedAt": 1710100000000,
  "updatedBy": 5
}
```

Rules:

- Preserve existing fields for frontend compatibility.
- Add fields as nullable where data is missing.
- Query must validate `tenantId`, `projectId`, and `workItemId`.
- User display data must come from `account` service.

### 2. Inline Edit

Add a partial update endpoint rather than overloading full update semantics:

```http
PATCH /projects/{projectId}/work-items/{id}
```

Request:

```json
{
  "summary": "New summary",
  "description": "New description",
  "assigneeId": 5,
  "priorityId": 2,
  "statusId": 3,
  "startDate": 1710000000000,
  "dueDate": 1710600000000,
  "timeOriginalEstimate": 28800,
  "timeRemainingEstimate": 14400,
  "customFields": {
    "story_points": 5
  }
}
```

Response:

```json
{
  "id": 10,
  "updatedAt": 1710100000000,
  "changedFields": ["summary", "assigneeId"]
}
```

Patch semantics:

- Missing field means no change.
- Present field with null means clear value only for nullable fields.
- Non-nullable fields reject null with domain validation error.
- Status changes must reuse existing workflow/transition authorization rules.
- Custom fields must reuse existing custom field validation/mutation service.
- Every changed field must create one work item history entry.

Implementation note:

- Java DTO should use nullable wrapper with presence tracking, not plain nullable fields, to distinguish missing vs explicit null.
- Acceptable options:
  - `JsonNullable<T>` if dependency already acceptable.
  - Custom `PatchField<T>` with Jackson deserializer.
  - `Map<String, Object>` plus typed validator for smaller first pass.

Recommendation: use a typed request with custom presence tracking if repo already has no `JsonNullable` dependency. Avoid adding dependency unless needed.

### 3. Children/Subtasks

```http
GET /projects/{projectId}/work-items/{id}/children
```

Response:

```json
[
  {
    "id": 11,
    "key": "PM-11",
    "summary": "Add loading state",
    "issueType": {
      "id": 4,
      "name": "Sub-task",
      "iconUrl": null,
      "hierarchyLevel": 0
    },
    "status": {
      "id": 3,
      "name": "Done"
    },
    "priority": {
      "id": 2,
      "name": "Medium",
      "color": "#F59E0B"
    },
    "assignee": {
      "id": 5,
      "displayName": "Huy Nguyen",
      "avatarUrl": null
    }
  }
]
```

Rules:

- Scope by `tenantId`, `projectId`, and `parentId`.
- Return lightweight rows only.
- Sort by `rank ASC NULLS LAST, id ASC`.

### 4. Linked Work Items

```http
GET /projects/{projectId}/work-items/{id}/links
```

Response:

```json
[
  {
    "id": 100,
    "direction": "OUTWARD",
    "linkType": {
      "id": 1,
      "name": "Blocks",
      "description": "blocks"
    },
    "workItem": {
      "id": 20,
      "key": "PM-20",
      "summary": "Blocked item",
      "status": {
        "id": 1,
        "name": "To Do"
      },
      "priority": {
        "id": 1,
        "name": "High",
        "color": "#EF4444"
      }
    }
  }
]
```

Rules:

- Include links where `issue_links.source_id = id` or `issue_links.target_id = id`.
- `OUTWARD` means current item is source; use `issue_link_types.outward_desc`.
- `INWARD` means current item is target; use `issue_link_types.inward_desc`.
- Join linked work item by opposite side.
- Scope linked item by same tenant and non-deleted.
- Cross-project links can be shown if current tenant has access; include `projectId` in linked work item if future frontend needs it.

### 5. Comments

Store comments in `pm_core`.

Routes:

```http
GET /projects/{projectId}/work-items/{id}/comments?page=0&size=20
POST /projects/{projectId}/work-items/{id}/comments
PUT /projects/{projectId}/work-items/{id}/comments/{commentId}
DELETE /projects/{projectId}/work-items/{id}/comments/{commentId}
```

Create request:

```json
{
  "body": "Looks good to me."
}
```

Comment view:

```json
{
  "id": 100,
  "body": "Looks good to me.",
  "author": {
    "id": 5,
    "displayName": "Huy Nguyen",
    "avatarUrl": null
  },
  "createdAt": 1710000000000,
  "updatedAt": 1710000000000,
  "edited": false
}
```

Rules:

- Comment body required, trim whitespace.
- Author is current user.
- Edit/delete allowed for author or project admin group.
- Delete is soft delete.
- Comments contribute to activity feed.

### 6. Activity Feed

```http
GET /projects/{projectId}/work-items/{id}/activities?page=0&size=20&type=ALL|COMMENT|HISTORY
```

Response:

```json
[
  {
    "id": "comment-100",
    "type": "COMMENT",
    "actor": {
      "id": 5,
      "displayName": "Huy Nguyen",
      "avatarUrl": null
    },
    "body": "Looks good to me.",
    "createdAt": 1710000000000
  },
  {
    "id": "history-200",
    "type": "HISTORY",
    "actor": {
      "id": 5,
      "displayName": "Huy Nguyen",
      "avatarUrl": null
    },
    "fieldKey": "statusId",
    "fieldName": "Status",
    "fromValue": "To Do",
    "toValue": "In Progress",
    "createdAt": 1710100000000
  }
]
```

Rules:

- Merge comments and history ordered by `createdAt DESC, id DESC`.
- For phase 2, omit worklogs from activity unless worklog API is implemented in same sprint.
- History entries must be created by inline edit and existing full update command if feasible.

## Database Changes

Add Flyway migration after latest version in `src/main/resources/db/migration`.

### work_item_comments

```sql
CREATE TABLE work_item_comments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_item_comments_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id)
);

CREATE INDEX idx_work_item_comments_work_item_created
    ON work_item_comments (tenant_id, work_item_id, created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_comments_author
    ON work_item_comments (tenant_id, author_id)
    WHERE deleted_at IS NULL;
```

### work_item_history

```sql
CREATE TABLE work_item_history (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    field_key VARCHAR(100) NOT NULL,
    field_name VARCHAR(255),
    from_value TEXT,
    to_value TEXT,
    from_display_value TEXT,
    to_display_value TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_item_history_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id)
);

CREATE INDEX idx_work_item_history_work_item_created
    ON work_item_history (tenant_id, work_item_id, created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_history_field
    ON work_item_history (tenant_id, work_item_id, field_key)
    WHERE deleted_at IS NULL;
```

Notes:

- Store raw values and display values. This avoids expensive lookup when rendering old history.
- Keep `updated_at` columns for consistency with `BaseModel`, even if history is append-only.

## Account Service Integration

Need resolve user display names from account service for:

- Detail assignee/reporter.
- Children assignee.
- Comments authors.
- Activity actors.
- History actors.

Recommended abstraction:

```java
public interface IUserSummaryPort {
    Map<Long, UserSummary> getUserSummaries(Long tenantId, Collection<Long> userIds);
}
```

Domain/application should depend on this port, not Feign/client directly.

Infrastructure implementation should call account service batch endpoint.

If account service lacks batch endpoint, add one or implement temporary per-user fallback with caching. Batch endpoint is strongly preferred to avoid N+1 calls.

Expected account response fields:

- `id`.
- `displayName`.
- `avatarUrl` nullable.

Failure policy:

- If account service fails, do not fail work item detail.
- Return user IDs with null display names and log warning.
- Do fail only if security/access check depends on account service; phase 2 display does not.

## Layered Implementation Tasks

### Task 1: Secure and Enrich Detail Query

Files likely touched:

- `ui/rest/workitem/WorkItemController.java`.
- `application/workitem/query/get/GetWorkItemByIdQuery.java`.
- `application/workitem/query/get/GetWorkItemByIdQueryHandler.java`.
- `application/workitem/query/get/WorkItemDetailView.java`.
- `domain/workitem/dto/WorkItemDetailProjection.java`.
- `domain/workitem/port/read/IWorkItemReadPort.java`.
- `infrastructure/store/repository/IWorkItemRepository.java`.
- `infrastructure/store/adapter/WorkItemReadAdapter.java`.

Steps:

1. Change detail read method to include `projectId`.
2. Join parent work item.
3. Add issue type `iconUrl`, `hierarchyLevel` if columns exist.
4. Load components with existing component query or port method.
5. Load custom field values with existing custom field value port.
6. Count children, links, comments.
7. Resolve user summaries via `IUserSummaryPort`.
8. Preserve response envelope `GeneralResponse<WorkItemDetailView>`.

Acceptance:

- Work item from different project in same tenant returns not found/access denied.
- Assignee/reporter display names appear when account service returns users.
- Existing frontend fields still deserialize.

### Task 2: Add Children Query

New classes likely:

- `application/workitem/query/children/ListWorkItemChildrenQuery.java`.
- `application/workitem/query/children/ListWorkItemChildrenQueryHandler.java`.
- `application/workitem/query/children/WorkItemChildView.java`.
- `domain/workitem/dto/WorkItemChildProjection.java`.
- Row mapper if using JDBC.

Controller:

- Add `GET /{id}/children` to `WorkItemController` or separate controller under same base route.

Acceptance:

- Returns children for current work item only.
- Uses tenant/project scope.
- Includes assignee display names via account service batch resolution.

### Task 3: Add Linked Work Items Query

New classes likely:

- `application/workitem/query/links/ListWorkItemLinksQuery.java`.
- `application/workitem/query/links/ListWorkItemLinksQueryHandler.java`.
- `application/workitem/query/links/WorkItemLinkView.java`.
- `domain/workitem/dto/WorkItemLinkProjection.java`.

Implementation:

- Add read port `listWorkItemLinks`.
- Use `NamedParameterJdbcTemplate` in `WorkItemReadAdapter` for native SQL.
- Join `issue_links`, `issue_link_types`, linked `work_items`, `statuses`, `priorities`.

Acceptance:

- Shows inward and outward links.
- Direction and description match current item perspective.
- Soft-deleted links/items excluded.

### Task 4: Add Comments

New persistence:

- `WorkItemCommentModel`.
- `IWorkItemCommentRepository`.
- `WorkItemCommentMapper`.

New domain/application:

- `WorkItemCommentEntity`.
- `IWorkItemCommentReadPort`.
- `IWorkItemCommentWritePort`.
- `ListWorkItemCommentsQueryHandler`.
- `CreateWorkItemCommentCommandHandler`.
- `UpdateWorkItemCommentCommandHandler`.
- `DeleteWorkItemCommentCommandHandler`.

New UI DTOs:

- `CreateWorkItemCommentRequest`.
- `UpdateWorkItemCommentRequest`.
- `WorkItemCommentView`.

Acceptance:

- Create/list/edit/delete comments.
- Soft delete hides comments from list/activity.
- Author display names resolved through account service.

### Task 5: Add History Persistence and Recording

New persistence:

- `WorkItemHistoryModel`.
- `IWorkItemHistoryRepository`.
- `WorkItemHistoryMapper`.

New domain/application:

- `WorkItemHistoryEntity`.
- `IWorkItemHistoryReadPort`.
- `IWorkItemHistoryWritePort`.
- `WorkItemHistoryRecorder` application service.

Recording rules:

- Compare before/after values in patch command.
- Create one history row per changed field.
- Store both raw IDs and display values for catalog/user fields.
- Fields to record in phase 2:
  - `summary`.
  - `description`.
  - `assigneeId`.
  - `priorityId`.
  - `statusId`.
  - `startDate`.
  - `dueDate`.
  - `timeOriginalEstimate`.
  - `timeRemainingEstimate`.
  - supported custom fields.

Display value resolution:

- User fields via account service.
- Status/priority/issue type via existing local catalogs.
- Dates formatted as epoch millis string or ISO display consistently.
- Custom field options via custom field option port when option type.

Acceptance:

- Inline edit creates history rows.
- No history row when value unchanged.
- Multiple field changes create multiple rows in same transaction.

### Task 6: Add Patch Command

New classes likely:

- `ui/rest/workitem/dto/request/PatchWorkItemRequest.java`.
- `application/workitem/command/patch/PatchWorkItemCommand.java`.
- `application/workitem/command/patch/PatchWorkItemCommandHandler.java`.
- `application/workitem/command/patch/PatchWorkItemResult.java`.

Implementation:

1. Load existing work item by `id + tenantId` and validate `projectId`.
2. Authorize edit with existing work item authorization support.
3. Validate field write policy and required-field rules.
4. Apply present fields only.
5. For status, reuse transition validation/authorization.
6. Persist work item.
7. Apply custom field mutation plan.
8. Record history rows.
9. Emit existing work item update event/outbox if update flow already does.

Acceptance:

- Missing fields unchanged.
- Explicit null clears nullable fields.
- Invalid transition rejected.
- History and work item update persist atomically.

### Task 7: Add Activity Feed

New classes likely:

- `application/workitem/query/activity/ListWorkItemActivitiesQuery.java`.
- `application/workitem/query/activity/ListWorkItemActivitiesQueryHandler.java`.
- `application/workitem/query/activity/WorkItemActivityView.java`.

Implementation options:

- Query comments and history separately, merge/page in application for phase 2 simplicity.
- Or use SQL `UNION ALL` for correct DB-level pagination.

Recommendation:

- Use SQL `UNION ALL` in read adapter if page size matters.
- Use application merge only if result sizes are bounded and acceptable.

Acceptance:

- `type=ALL` returns comments and history in reverse chronological order.
- `type=COMMENT` returns only comments.
- `type=HISTORY` returns only history.
- Actor display names resolved through account service.

## Error Handling

Reuse domain exceptions:

- Missing work item: `ResourceNotFoundException`.
- Missing project/tenant/user claims: existing `AccessDeniedException` with `DomainErrorCode`.
- Invalid field values: `DomainValidationException`.
- Invalid transition: existing workflow/domain exception.
- Unauthorized comment edit/delete: `AccessDeniedException`.

Do not add generic `RuntimeException` for expected business failures.

## Tests

Unit tests first, focused by handler.

Recommended test classes:

- `GetWorkItemByIdQueryHandlerTest`.
- `ListWorkItemChildrenQueryHandlerTest`.
- `ListWorkItemLinksQueryHandlerTest`.
- `CreateWorkItemCommentCommandHandlerTest`.
- `UpdateWorkItemCommentCommandHandlerTest`.
- `DeleteWorkItemCommentCommandHandlerTest`.
- `PatchWorkItemCommandHandlerTest`.
- `ListWorkItemActivitiesQueryHandlerTest`.
- `WorkItemHistoryRecorderTest`.

Critical cases:

- Detail rejects wrong project scope.
- Account service failure returns null display names, not request failure.
- Patch missing field means unchanged.
- Patch explicit null clears nullable field.
- Patch explicit null on non-nullable field fails.
- Patch status runs transition authorization.
- Patch unchanged value creates no history.
- Patch multi-field update creates multiple history rows.
- Comment author can edit/delete own comment.
- Non-author cannot edit/delete without admin group.
- Activity feed merges comments/history in correct order.

Verification commands:

```bash
./mvnw.cmd -Dtest=PatchWorkItemCommandHandlerTest test
./mvnw.cmd -Dtest=CreateWorkItemCommentCommandHandlerTest test
./mvnw.cmd -Dtest=ListWorkItemActivitiesQueryHandlerTest test
./mvnw.cmd clean compile
```

Use `./mvnw` instead of `./mvnw.cmd` on Bash.

## Rollout Order

1. Add migrations for comments/history.
2. Add user summary account-service port/client.
3. Secure/enrich detail response.
4. Add children endpoint.
5. Add links endpoint.
6. Add comments CRUD.
7. Add history recorder.
8. Add patch endpoint with history recording.
9. Add activity feed endpoint.
10. Wire frontend phase 2 endpoints.

## Open Technical Checks Before Coding

- Confirm account service has batch user summary endpoint. If not, create one or define temporary fallback.
- Confirm existing `UpdateWorkItemCommandHandler` should also record history, or only new `PATCH` in phase 2.
- Confirm exact project admin group naming for comment edit/delete authorization.
- Confirm custom field value read API exists; if not, add read port for detail custom fields.
- Confirm status transition patch should accept `statusId` only, or `workflowStepId` too.
