# PM Core - Project Scheme Binding API Contract (Draft)

**Updated:** 2026-03-04  
**Status:** Draft for implementation

## Purpose

Define a Jira-closer parity contract for re-associating project schemes.

- Default behavior: shared association to selected schemes.
- Optional behavior: clone-on-associate for isolated project-specific copies.

## Endpoint

- **Method:** `PUT`
- **Path:** `/api/v1/projects/{projectId}/schemes`
- **Auth:** JWT required
- **Permission:** `PM.PROJECT.ADMIN`

## Request Body

All scheme fields are optional; only provided fields are updated.

```json
{
  "issueTypeSchemeId": 101,
  "workflowSchemeId": 205,
  "fieldConfigSchemeId": 309,
  "issueTypeScreenSchemeId": 412,
  "permissionSchemeId": 501,
  "notificationSchemeId": 601,
  "prioritySchemeId": 701,
  "issueSecuritySchemeId": 801,
  "associationMode": "SHARED_ASSOCIATION"
}
```

### associationMode

- `SHARED_ASSOCIATION` (default)
  - Directly binds project to selected scheme IDs.
  - Shared scheme changes can affect all associated projects.
- `CLONE_ON_ASSOCIATE`
  - Clones provided scheme graphs, then binds project to cloned IDs.
  - Later scheme changes remain local to the project.

## Validation Rules

1. Project exists, belongs to tenant, and is not archived.
2. Each provided scheme ID exists and belongs to tenant (or system tenant if supported by service policy).
3. Compatibility checks before commit:
   - Workflow mapping covers all issue types.
   - Field config mapping covers all issue types.
   - Screen mapping covers all issue types.
   - Default IDs belong to target schemes.
4. Existing work item safety:
   - Current statuses that become unmapped must be detected and blocked or migrated per policy.

## Success Response

- **HTTP:** `200 OK`
- **Payload:** Updated project with effective scheme IDs.

```json
{
  "status": "success",
  "code": 200,
  "message": "OK",
  "data": {
    "id": 55,
    "key": "SERP",
    "issueTypeSchemeId": 101,
    "workflowSchemeId": 205,
    "fieldConfigSchemeId": 309,
    "issueTypeScreenSchemeId": 412,
    "permissionSchemeId": 501,
    "notificationSchemeId": 601,
    "prioritySchemeId": 701,
    "issueSecuritySchemeId": 801,
    "updatedAt": 1770000000000,
    "updatedBy": 12
  }
}
```

## Error Mapping

- `401 UNAUTHORIZED`: JWT/tenant context missing.
- `403 FORBIDDEN`: missing `PM.PROJECT.ADMIN`.
- `404 NOT_FOUND`: project or scheme not found.
- `409 CONFLICT`: concurrency/version conflict (if optimistic locking is enabled).
- `422 UNPROCESSABLE_ENTITY`: `SCHEME_INCOMPATIBLE`.
- `400 BAD_REQUEST`: invalid enum/value/empty payload.

## Outbox Event

After commit, publish project event:

- `PROJECT_SCHEMES_UPDATED`
- Includes: `projectId`, previous and new scheme IDs, `associationMode`, `tenantId`, `actorId`.

## Notes for Implementation

1. Keep operation transactional.
2. Apply compatibility validation in the same transaction before final write.
3. For shared mode, expose admin warning in UI/service response metadata that updates can impact multiple projects.
4. For clone mode, return cloned scheme IDs in response.
5. Current backend implementation supports only `SHARED_ASSOCIATION` and validates/updates `issueTypeSchemeId`, `workflowSchemeId`, `prioritySchemeId`; other scheme families are blocked until their ports/tables are completed.
