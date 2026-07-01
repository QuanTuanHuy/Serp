# Module Access Users Design

## Context

Frontend module screens currently need users who are allowed to access a specific module. The current user listing flow can return all users in a tenant, even when those users have not been granted module access.

The account service already has module access data in `user_module_access` and an existing public module users endpoint. The missing piece is a shared, paginated, searchable user query that can filter by active module access and be reused by both public frontend APIs and internal service APIs.

## Decision

Use `user_module_access` as the source of truth for whether a user can access a module.

A user is considered accessible to a module only when there is an active row:

```sql
user_module_access.user_id = users.id
AND user_module_access.organization_id = :organizationId
AND user_module_access.module_id = :moduleId
AND user_module_access.is_active = TRUE
```

Roles are not used to decide module access in this listing. Roles remain assignment side effects and response enrichment where the existing user list already includes them.

## API Contract

### Public API

Extend the existing endpoint:

```http
GET /api/v1/organizations/{organizationId}/modules/{moduleId}/users
```

Supported query parameters:

- `page`
- `pageSize`
- `sortBy`
- `sortDir`
- `search`
- `status`
- `userType`
- `roleId`
- `departmentId`

The controller sets `organizationId` and `moduleId` from the path and uses the same user listing use case as the internal API.

### Internal API

Extend the existing endpoint:

```http
GET /internal/api/v1/users
```

Add optional query parameter:

- `moduleId`

When `moduleId` is present, `organizationId` is required so the module access filter is scoped to one tenant. Existing filters remain supported.

## Response

Both endpoints return the existing paginated `getUsers` response shape:

```json
{
  "totalItems": 0,
  "totalPages": 0,
  "currentPage": 0,
  "items": []
}
```

Each item uses the existing user profile list response assembled by `UserQueryService`.

## Architecture

Add `moduleId` to `GetUserParams`.

Keep the main read flow:

```text
Controller -> UserUseCase -> UserQueryService -> IUserService -> IUserPort -> UserQueryBuilder
```

`ModuleAccessController#getUsersWithAccessToModule` should become a thin wrapper that builds `GetUserParams` with the path `organizationId` and `moduleId`.

`InternalUserController#getUsers` should accept optional `moduleId` and pass it through `GetUserParams`.

`UserQueryBuilder` should append an `EXISTS` filter on `user_module_access` when `moduleId` is present. This keeps pagination, count, search, role filtering, department filtering, and sorting consistent across public and internal endpoints.

## Data Flow

1. Frontend module screen calls the public module users endpoint with pagination and search params.
2. Controller checks `authUtils.canAccessOrganization(organizationId)`.
3. Controller builds `GetUserParams`.
4. User query flow builds one count query and one data query.
5. The query returns only users with active `user_module_access` for the requested module and organization.
6. Existing user profile assembly enriches roles and profile fields as before.

## Error Handling

The public endpoint preserves the existing organization access check and returns forbidden when the caller cannot access the organization.

If `moduleId` has no active assignments for the organization, the endpoint returns an empty paginated result.

If `moduleId` is not included in the organization's subscription, the endpoint also returns an empty paginated result. Subscription validation remains the responsibility of assignment and module access-check flows.

For the internal endpoint, `moduleId` without `organizationId` returns `400 Bad Request`, because module access is tenant-scoped and a cross-tenant module access query would be ambiguous.

## Testing

Add focused tests around `UserQueryBuilder`:

- Without `moduleId`, generated SQL remains compatible with the existing user list behavior.
- With `moduleId` and `organizationId`, generated count and data SQL include an `EXISTS` clause against `user_module_access`.
- The module access filter checks `user_id`, `organization_id`, `module_id`, and `is_active = TRUE`.
- Existing filters such as `status`, `search`, `roleId`, and `departmentId` can coexist with `moduleId`.

If a controller test pattern exists nearby, add a narrow test that verifies public and internal controllers pass `moduleId` into `GetUserParams`.

## Out of Scope

- Changing role assignment behavior.
- Backfilling or migrating `user_module_access`.
- Changing subscription validation for assignment flows.
- Changing frontend screens in this backend design slice.
