# Module Bulk Access Design

## Context

The account service already exposes module access APIs through
`ModuleAccessController` and orchestration in `ModuleAccessUseCase`.

Current behavior:

- `POST /api/v1/organizations/{organizationId}/modules/{moduleId}/users/bulk`
  assigns many users to one module, but fails the whole request when requested
  users exceed the remaining module slots.
- `DELETE /api/v1/organizations/{organizationId}/modules/{moduleId}/users/{userId}`
  revokes one user's module access.
- There is no bulk revoke endpoint.
- Bulk assign loops through users and does not use the newer batch-oriented
  helpers already planned for auto-grant.

The goal is to make bulk assign resilient and add bulk revoke for the account
backend first. The frontend integration is intentionally deferred to a later
turn.

## Decisions

- Keep the existing bulk assign route.
- Keep bulk assign and bulk revoke as two separate API commands. Do not merge
  them into one action-flag endpoint.
- Change bulk assign from all-or-error quota handling to partial success.
- Add a new bulk revoke route using `POST`, not `DELETE` with a request body.
- Use one shared summary response shape for bulk assign and bulk revoke.
- Use separate request DTOs: assign can carry optional `roleId`; revoke carries
  only `userIds`.
- Return counts, affected user IDs, skipped users, and skipped reason counts.
- Preserve single assign and single revoke behavior.
- Preserve single revoke role behavior: revoking module access removes that
  module's roles from the user.
- Keep the APIs synchronous. Background jobs are out of scope.
- Focus implementation on the backend. Frontend API/UI changes are out of
  scope for this spec.

## API Design

### Bulk Assign

Route:

```http
POST /api/v1/organizations/{organizationId}/modules/{moduleId}/users/bulk
```

Request:

```json
{
  "userIds": [101, 102, 103],
  "roleId": 77
}
```

The controller continues to set `organizationId` and `moduleId` from the path
onto `BulkAssignUsersRequest`. `roleId` is optional.

Behavior:

- Validate the authenticated user can access the organization.
- Validate the module belongs to the organization's active or pending-upgrade
  subscription.
- If `roleId` is present, validate that it exists in the target module and
  assign that single role to every granted or reactivated user.
- If `roleId` is absent, load the module's default roles using
  `RoleEntity.isAutoAssigned()`.
- Fail the whole request if no assignable module role can be resolved.
- Respect `maxUsersPerModule`.
- Process requested user IDs in request order.
- Grant or reactivate only until remaining slots run out.
- Skip users that cannot be changed and include the reason in the response.
- Use `subscription.endDate` as `expiresAt`, matching single assign.

### Bulk Revoke

Route:

```http
POST /api/v1/organizations/{organizationId}/modules/{moduleId}/users/bulk-revoke
```

Request:

```json
{
  "userIds": [101, 102, 103]
}
```

The revoke request DTO should contain only `userIds`. `organizationId` and
`moduleId` come from the route, not from the request body.

Behavior:

- Validate the authenticated user can access the organization.
- Validate request user IDs are not empty.
- Deactivate active module access for users that currently have it.
- Skip users with no module access or inactive module access.
- Remove module roles from revoked users, matching single revoke behavior.
- Publish user sync and logout only for users that were actually revoked.

## Response Design

Add two backend response DTOs:

- `BulkModuleAccessResponse`
- `BulkModuleAccessSkippedUser`

Response example for assign:

```json
{
  "moduleId": 12,
  "requestedCount": 5,
  "grantedCount": 2,
  "revokedCount": 0,
  "skippedCount": 3,
  "grantedUserIds": [101, 102],
  "revokedUserIds": [],
  "skippedUsers": [
    { "userId": 103, "reason": "ALREADY_HAS_ACCESS" },
    { "userId": 104, "reason": "MAX_USERS_LIMIT_REACHED" },
    { "userId": 105, "reason": "MAX_USERS_LIMIT_REACHED" }
  ],
  "skippedReasons": {
    "ALREADY_HAS_ACCESS": 1,
    "MAX_USERS_LIMIT_REACHED": 2
  }
}
```

Response example for revoke:

```json
{
  "moduleId": 12,
  "requestedCount": 3,
  "grantedCount": 0,
  "revokedCount": 2,
  "skippedCount": 1,
  "grantedUserIds": [],
  "revokedUserIds": [101, 102],
  "skippedUsers": [
    { "userId": 103, "reason": "USER_MODULE_ACCESS_NOT_FOUND" }
  ],
  "skippedReasons": {
    "USER_MODULE_ACCESS_NOT_FOUND": 1
  }
}
```

Skip reasons:

- `ALREADY_HAS_ACCESS`
- `MAX_USERS_LIMIT_REACHED`
- `USER_NOT_FOUND`
- `USER_MODULE_ACCESS_NOT_FOUND`

## Backend Design

`ModuleAccessUseCase.bulkAssignUsersToModule(...)` should own request-level
validation and orchestration:

- Read the active or pending-upgrade subscription once.
- Resolve the plan module from `subscription.getSubscriptionPlanId()`.
- Count current active users once.
- Compute available slots once.
- Load users by requested IDs once.
- Load existing module access for requested users once.
- Build `BulkModuleAccessResponse` while preserving request order.
- Save created/reactivated module access records in bulk.
- Assign default module roles to changed users with
  `ICombineRoleService.assignRolesToUsers(...)`.
- When `BulkAssignUsersRequest.roleId` is present, assign only that validated
  module role to changed users.
- Publish user sync and logout only for users in `grantedUserIds`.

Add `ModuleAccessUseCase.bulkRevokeUsersFromModule(...)`:

- Load existing access records for requested users once.
- Deactivate only active access records.
- Save changed access records in bulk.
- Remove all roles for the module from revoked users, matching single revoke.
- Publish user sync and logout only for users in `revokedUserIds`.

`IUserModuleAccessService` should expose batch methods that keep persistence
details outside the use case:

- `getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(...)`
- `saveAll(List<UserModuleAccessEntity> userModuleAccesses)`
- `bulkRegisterUsersToModuleWithExpiration(...)`

Bulk assign should use `getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(...)`
to classify requested users, then call
`bulkRegisterUsersToModuleWithExpiration(...)` only for users that should be
granted or reactivated. Bulk revoke should deactivate loaded
`UserModuleAccessEntity` objects and persist them with `saveAll(...)`.

`ICombineRoleService` should add a batch removal method:

- `removeRolesFromUsers(List<UserEntity> users, List<RoleEntity> roles)`

The batch removal should use a new port/repository operation:

- `IUserRolePort.deleteUserRolesByUserIdsAndRoleIds(List<Long> userIds,
  List<Long> roleIds)`
- `IUserRoleRepository.deleteByUserIdInAndRoleIdIn(...)`

Keycloak role revocation can still happen per changed user because the existing
Keycloak service operates per user/client.

`ModuleAccessUseCase` should use the existing
`IUserService.getUsersByOrganizationIdAndIds(...)` method for batch user lookup.
Requested IDs not returned by that lookup are skipped as `USER_NOT_FOUND`.

## Quota And Access Rules

Bulk assign:

- Already active access: skip `ALREADY_HAS_ACCESS`; does not consume a slot.
- Inactive or expired access: reactivate; consumes a slot.
- No access: create new active access; consumes a slot.
- No remaining slot: skip `MAX_USERS_LIMIT_REACHED`.
- Unknown user or user outside the organization: skip `USER_NOT_FOUND`.

Bulk revoke:

- Active access: deactivate and count as revoked.
- No access, inactive access, or expired access: skip
  `USER_MODULE_ACCESS_NOT_FOUND`.

Duplicate user IDs in one request should be de-duplicated for writes while
preserving first occurrence order in the response. Later duplicates should be
reported as the same outcome as the first occurrence rather than causing a
second write.

## Transactions And Side Effects

Both bulk assign and bulk revoke should run in a write transaction.

Database writes must complete before side effects that are not part of the
transactional store:

- role writes
- user sync publishing
- Keycloak logout or role updates

The existing code already calls user sync and Keycloak services inside use case
transactions. This design keeps that style for consistency, while limiting the
side effects to users that actually changed.

Notification sending remains out of scope, matching the current bulk assign and
single revoke comments.

## Frontend Scope

No frontend implementation is included in this backend-first change.

The later frontend turn should add or update RTK Query types/mutations for:

- bulk assign summary response
- bulk revoke request and summary response
- cache invalidation for module users and module summaries
- optional bulk selection UI if the settings or admin module access screens need
  multi-user actions

## Testing

Backend tests should cover:

- Controller maps `POST /users/bulk-revoke` to the use case and passes the
  authenticated organization context.
- Bulk assign returns partial success when slots run out.
- Bulk assign skips active existing access as `ALREADY_HAS_ACCESS`.
- Bulk assign reactivates inactive access and counts it as granted.
- Bulk assign skips missing or out-of-organization users as `USER_NOT_FOUND`.
- Bulk assign uses the explicit `roleId` when it is present and belongs to the
  module.
- Bulk assign fails the whole request when `roleId` does not belong to the
  module.
- Bulk assign fails the whole request when no default module role exists.
- Bulk revoke revokes active access and skips missing/inactive access as
  `USER_MODULE_ACCESS_NOT_FOUND`.
- Bulk revoke removes module roles with the same behavior as single revoke.
- Use case tests verify batch service methods are used instead of per-user
  register/revoke loops.

Verification commands:

```bash
cd account
mvnw.cmd -Dtest=ModuleAccessControllerTest,ModuleAccessUseCaseBulkAccessTest test
mvnw.cmd test
```

## Out Of Scope

- Frontend implementation.
- Background job processing for bulk operations.
- A new audit table.
- Notification delivery for bulk assign or bulk revoke.
- Changing single assign or single revoke contracts.
- Merging assign and revoke into one action-flag endpoint.
- Tracking whether a role was granted through module access versus another
  source.
