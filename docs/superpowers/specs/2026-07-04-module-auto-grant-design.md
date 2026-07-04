# Module Auto-Grant Design

## Context

SERP already has organization module access management in the account service and
the settings UI:

- `ModuleAccessUseCase` handles manual user grant, bulk grant, revoke, and
  organization module summaries.
- `OrgModuleAccessResponse` already exposes `isAutoGrantToNewUsers`, but the
  current response always uses the default value.
- The settings modules page already renders an `Auto-grant to new users` switch,
  but it only logs to the console.
- Manual module grants assign module roles where `RoleEntity.isDefault` is true
  through `RoleEntity.isAutoAssigned()`.

The goal is to turn the existing switch into a persisted organization-module
policy and use it when new users are created.

## Decisions

- Store auto-grant as an organization-module policy, not as part of
  `user_module_access` and not as part of subscription plan modules.
- Use a new table, `organization_module_access_settings`, keyed by
  `(organization_id, module_id)`.
- The policy applies to all user types.
- Auto-grant assigns module roles where `RoleEntity.isDefault = true`.
- Turning auto-grant on changes future behavior only. Backfill for existing users
  is a separate confirmed action.
- Turning auto-grant off only disables the policy. Existing module access remains
  unchanged.
- When quota is limited, grant until slots run out, skip the rest, and return a
  clear summary.

## Backend Design

Add a new organization module access setting model:

- Domain entity: `OrganizationModuleAccessSettingEntity`
- JPA model: `OrganizationModuleAccessSettingModel`
- Repository: `IOrganizationModuleAccessSettingRepository`
- Port: `IOrganizationModuleAccessSettingPort`
- Adapter and mapper matching the account module's existing store pattern
- Service: `IOrganizationModuleAccessSettingService`

The table should include:

- `id`
- `organization_id`
- `module_id`
- `auto_grant_to_new_users BOOLEAN NOT NULL DEFAULT FALSE`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`
- unique constraint on `(organization_id, module_id)`

`ModuleAccessUseCase.getAccessibleModulesForOrganization(...)` should read the
settings for the organization and populate
`OrgModuleAccessResponse.isAutoGrantToNewUsers` for active modules. Missing
settings are treated as false.

Add a focused auto-grant service, for example `ModuleAutoGrantService`, that owns
shared grant behavior used by both backfill and new-user provisioning. It should:

- Validate that the organization subscription includes the module.
- Respect `maxUsersPerModule`.
- Reuse `IUserModuleAccessService.registerUserToModuleWithExpiration(...)` so
  access expiration follows the subscription end date.
- Assign default module roles using `RoleEntity.isAutoAssigned()`.
- Publish user sync for users changed by backfill.
- Let the new-user provisioning caller publish one final user sync after
  organization roles and auto-granted module roles are both applied.
- Return structured results for batch operations.

The backfill path must be batch-oriented because it can touch many users:

- Query active users without active access to the module directly from the
  database, limited to the number of available module slots.
- Count the remaining matching users separately so skipped users can be reported
  without loading every user into memory.
- Register module access and insert user-role links in bulk where the existing
  store layer supports it.
- Publish user sync only for users that were actually changed.

The new-user path should stay synchronous but lightweight. It should read enabled
policies once, reuse subscription/role data across modules, collect all default
module roles that apply, assign those roles once, and let the caller publish the
single final user sync.

## API Design

Add two endpoints to `ModuleAccessController`.

`PUT /api/v1/organizations/{organizationId}/modules/{moduleId}/access-settings`

Request:

```json
{
  "autoGrantToNewUsers": true
}
```

Behavior:

- Validates the authenticated user can access the organization.
- Validates the module is included in the current active or pending-upgrade
  subscription.
- Upserts the organization-module setting.
- Does not backfill and does not revoke existing access.

`POST /api/v1/organizations/{organizationId}/modules/{moduleId}/auto-grant/backfill`

Behavior:

- Validates the authenticated user can access the organization.
- Requires the module to be in the organization's subscription.
- Requires `autoGrantToNewUsers=true` for the organization-module setting.
- Requires at least one default module role for the module.
- Grants access to existing active organization users without active access to
  the module.
- Stops granting when quota runs out and reports skipped users.

Response shape:

```json
{
  "moduleId": 12,
  "grantedCount": 18,
  "skippedCount": 5,
  "skippedReasons": {
    "MAX_USERS_LIMIT_REACHED": 5
  }
}
```

## User Creation Flow

Update `UserProvisioningCoordinator.createOrganizationUser(...)` after the
current organization role assignment succeeds:

1. Create the user and Keycloak account as today.
2. Activate the user as today.
3. Resolve and assign organization roles as today.
4. Assign organization-user links as today.
5. Run auto-grant for configured organization modules.
6. Publish one final user sync for the new user.

The auto-grant step should apply to every `UserType`. If a configured module has
no remaining quota or no default module role, skip that module and log a warning.
New-user creation should not fail because optional module auto-grant could not be
applied.

The new-user path should not publish per-module sync events. The final sync after
organization roles and module roles have both been assigned is the only sync
event required for the created user.

Invitation acceptance already calls `UserUseCase.createUserForOrganization(...)`,
so this hook covers accepted invitations without adding invitation-specific
logic.

## Frontend Design

Update the settings modules page and modules API layer:

- Add RTK Query mutation for updating module access settings.
- Add RTK Query mutation for backfill.
- Wire the existing `Auto-grant to new users` switch to the update mutation.
- Add per-module pending state so the switch cannot be double-toggled while a
  request is in flight.
- When enabling succeeds, open a confirmation dialog asking whether to grant the
  module to existing users now.
- If the admin confirms, run backfill and show a toast summary with granted and
  skipped counts.
- When disabling succeeds, show a toast explaining that existing access remains
  unchanged.
- Invalidate the module list and module users tags after update/backfill.
- Add a small badge or status indicator on active module cards when auto-grant is
  enabled.

The UI should not introduce user-type filters or role selectors for auto-grant.
The rule is all user types, with default module roles from the backend.

## Error Handling

- Updating policy fails if the module is not part of the organization's current
  subscription.
- Backfill fails early if the module has no default module role, because this is
  a deliberate admin action and should surface missing role configuration.
- New-user auto-grant skips modules with missing default roles and logs a warning
  instead of failing user creation.
- Backfill and new-user auto-grant respect module user limits.
- Turning off auto-grant does not revoke access.
- Manual grant, bulk grant, and revoke keep their current behavior.

## Testing

Backend focused tests should cover:

- `getAccessibleModulesForOrganization` returns the persisted
  `isAutoGrantToNewUsers` value.
- Updating policy succeeds only for modules included in the organization's
  subscription.
- Backfill grants users without access, assigns default module roles, respects
  quota, and returns skipped counts.
- Backfill uses bounded candidate queries instead of loading every organization
  user and filtering in memory.
- Backfill fails when the module has no default module role.
- New-user provisioning auto-grants configured modules for every user type.
- New-user provisioning assigns collected module roles once and relies on the
  caller's final sync.
- New-user provisioning skips quota-full modules without failing user creation.
- Disabling auto-grant does not revoke existing access.

Frontend verification should use the existing project commands:

- `npm run lint`
- `npm run type-check`

There is currently no checked-in frontend test framework, so no UI test command
is required unless a test framework is added separately.

## Open Scope

This design does not add:

- User-type filters for auto-grant.
- Per-module role selection for auto-grant.
- Automatic revoke when policy is disabled.
- Background jobs for backfill. Backfill runs as an explicit request and returns
  a summary. This can be revisited later if organizations regularly backfill
  thousands of users or if Keycloak/user-sync latency becomes the dominant cost.
