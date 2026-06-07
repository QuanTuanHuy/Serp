# PM Project People Management Design

## Context

`pm_core` already models project roles and role actors through `ProjectRoleActor`. Existing endpoints are role-centric and support `USER`, `GROUP`, and `SERVICE_ACCOUNT` subjects. The PM web UI needs a user-centric `People` tab inside project detail that shows project members with role chips and supports managing user membership.

## Scope

Build a `People` tab at `/pm/projects/{projectId}/people` for user-only project membership management.

The v1 feature includes:

- User avatar, name, and email.
- Role chips per user.
- Project lead badge.
- Added date.
- Add user.
- Edit user roles.
- Remove user from project.
- Search existing project people.

The v1 feature excludes group and service account management, bulk actions, exports, and visual mockups.

## Backend Design

Add an aggregate `ProjectPeopleController` under `projects/{projectId}/people`.

Endpoints:

- `GET /api/v1/projects/{projectId}/people`
- `PUT /api/v1/projects/{projectId}/people/{userId}/roles`
- `DELETE /api/v1/projects/{projectId}/people/{userId}`

`GET` returns people aggregated by user from `ProjectRoleActor` rows where `subjectType = USER` for the project. It also includes the project lead even when the lead has no role actor rows, so the lead is always visible in the People tab. Each item contains:

- `userId`
- `name`
- `email`
- `avatarUrl`
- `isProjectLead`
- `roles[]` with role id, name, and system flag
- `addedAt`, computed as the earliest role actor creation timestamp for that user in the project, or `null` when the row exists only because the user is the project lead

`PUT` replaces all role memberships for a single user in a project. The request body contains `roleIds`. The handler validates:

- Project exists in current tenant.
- User exists through existing `IUserService` integration.
- Each role exists in current tenant, including allowed system roles.
- Actor has `ADMINISTER_PROJECTS` on the project.

`DELETE` removes all role actor rows for that user in the project. It must not delete the project lead identity from the project itself. If the removed user is the project lead, the person remains visible in `GET /people` with `isProjectLead = true`, an empty `roles` list, and `addedAt = null` unless role membership is added again.

Permissions:

- `GET` requires `BROWSE_PROJECTS`.
- `PUT` and `DELETE` require `ADMINISTER_PROJECTS`.

The existing role-centric actor endpoints remain unchanged and continue to support advanced subject types.

## Frontend Design

Add `People` tab to `PMProjectsTopTabs` with href `/pm/projects/${projectId}/people`.

Add route:

- `serp_web/src/app/pm/projects/[projectId]/(detail)/people/page.tsx`

Add module page/component:

- `PMProjectPeoplePage`

Add RTK Query endpoints in `projectApi.ts`:

- `getPmProjectPeople`
- `replacePmProjectPersonRoles`
- `removePmProjectPerson`

Reuse existing settings organization users API for Add User search:

- `useGetOrganizationUsersQuery`

UI behavior:

- People table shows avatar/name/email, role chips, project lead badge, added date, and row actions.
- Local search filters loaded people by name, email, and role name.
- Add User dialog searches organization users, excludes users already in project people, requires at least one role, then calls role replacement API for selected user.
- Edit Roles dialog updates role ids for an existing person using the same replacement API.
- Remove action uses confirm dialog and calls person removal API.
- Success/failure feedback uses shared toast/error helpers and RTK Query `.unwrap()`.

## Data Flow

1. Page loads project people through PM API.
2. Page loads available roles through existing PM roles API.
3. Add dialog searches org users through settings API.
4. User selects person and roles.
5. Frontend calls `PUT /projects/{projectId}/people/{userId}/roles`.
6. RTK Query invalidates project people cache.
7. Table refreshes aggregate people list.

## Error Handling

Backend uses existing domain exceptions and `GeneralResponse<?>` envelope.

Expected errors:

- Missing auth claims -> existing access denied errors.
- Project not found -> resource not found.
- User not found -> resource not found.
- Role not found -> resource not found.
- Missing permission -> access denied.
- Empty role list for replace -> validation error.

Frontend displays normalized errors with `getErrorMessage(error)` and toast messages.

## Testing

Backend tests:

- Aggregate people query groups multiple role actor rows by user.
- Added date uses earliest actor timestamp.
- Replace roles removes stale roles and adds new roles.
- Replace roles rejects empty role list.
- Delete removes all user role actors from project.
- Permission checks cover read and mutate paths.

Frontend verification:

- No frontend test framework exists in repo.
- Run `npm run lint`, `npm run type-check`, and `npm run format:check` from `serp_web`.

Backend verification:

- Run focused `pm_core` tests for new handlers.
- Run `./mvnw.cmd clean compile` from `pm_core`.

## Open Decisions

None. Scope is user-only hybrid table with aggregate PM APIs and org user search from existing settings API.
