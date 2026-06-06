# Admin Organizations and Users Console Design

## Context

`serp_web/src/app/admin/organizations/page.tsx` and `serp_web/src/app/admin/users/page.tsx` already render working list pages, but the UX is still basic:

- Search is inline, while filters are split across plain selects and a combobox.
- Organization actions are limited and partially stubbed.
- User create/edit exists, but access management and details are not surfaced as first-class admin workflows.
- The filter UI does not follow the grouped pattern already used by `PMWorkItemListFilters.tsx`.

The `account` service already supports most of the data needed for a richer admin console:

- Organization list and detail APIs exist.
- User list supports `search`, `status`, `userType`, `roleId`, `departmentId`, `organizationId`, pagination, and sorting.
- User detail returns roles, departments, and module access.
- User status, type, roles, create, and edit endpoints already exist.

The main backend gap for this redesign is organization lifecycle management with cascade behavior.

## Scope

This design covers:

- A modern admin UI for `Organizations` and `Users`.
- A grouped filter dialog pattern that keeps search outside the dialog.
- Organization details drawer.
- User details drawer.
- Create/edit user improvements.
- Suspend/activate organization with cascade to users.
- Suspend/activate user actions.
- Organization-user linking through previews and navigation shortcuts.

This design does not include export.
This design does not add bulk import, audit history, or a new permission model.
This design does not add a full organization edit form unless a dedicated admin update API is introduced later.

## Chosen Approach

Use a shared admin console pattern:

- Table-first pages.
- Search in the top toolbar.
- Filter dialog in a two-column grouped layout.
- Right-side drawer for entity details.
- Confirmation dialogs for destructive status changes.

This matches the existing PM filter pattern, keeps the pages dense but readable, and avoids adding a full new navigation model.

## UI Design

### Shared page shell

Both admin pages should follow the same structure:

- Page header with title and short description.
- Search input in the toolbar.
- Filter button with active-filter badge count.
- Optional quick actions on the right.
- `DataTable` as the primary list surface.
- Active filter chips below the toolbar or directly under search.
- `Clear all` only when filters are active.

The organizations page should remove the current `Export` button.

### Grouped filter dialog

The filter dialog should mirror `PMWorkItemListFilters.tsx`:

- Left column: list of criteria.
- Right column: values for the selected criterion.
- Each criterion shows an active count badge.
- Keep search outside the dialog.

#### Organizations filters

Criteria:

- Status
- Type

Use backend enum values for organization filters:

- Status: `ACTIVE`, `TRIAL`, `SUSPENDED`, `EXPIRED`, `CLOSED`
- Type: `ENTERPRISE`, `SMB`, `STARTUP`, `PERSONAL`, `NON_PROFIT`, `GOVERNMENT`

The current frontend type labels should be aligned to these backend values. The stale `INDIVIDUAL` option should not remain in the new UI.

#### Users filters

Criteria:

- Organization
- Status
- User type
- Role
- Department

Search stays in the toolbar, so the dialog only handles faceted filters.

Department filtering should be enabled only after an organization is selected, because department lookup is organization-scoped.

### Organizations page behavior

The organizations table should support:

- View details
- Suspend organization
- Activate organization
- Jump to users filtered by organization

The row action menu should not expose a fake edit action unless an admin organization update endpoint is added later.

#### Organization details drawer

The drawer should show:

- Overview: name, code, type, status, created date.
- Contact: email, phone, website.
- Business profile: industry, employee count, timezone, currency, language.
- Subscription summary if available from the current organization payload.
- Users preview with a small list and a link to the users page filtered by this organization.

The drawer should also expose a primary action to create a user in this organization.

#### Suspend organization

Suspending an organization must:

- Set the organization status to `SUSPENDED`.
- Suspend all users in that organization.

The confirmation dialog should make the blast radius explicit. If the backend returns user counts, show them before confirmation and in the success toast after the mutation.

#### Activate organization

Activating an organization must:

- Set the organization status to `ACTIVE`.
- Activate all users in that organization whose current status is `SUSPENDED`.

Users suspended for other reasons are not distinguished in this phase. The operation activates every suspended user in the organization.

### Users page behavior

The users table should support:

- View details
- Edit profile
- Manage access
- Suspend user
- Activate user

The create button should always be available. If no organization filter is selected, it should open a create dialog with an organization selector.

#### User details drawer

The drawer should show:

- Profile overview: avatar, name, email, status, user type.
- Organization.
- Last login and created date.
- Timezone and preferred language.
- Roles.
- Departments.
- Module access.

Quick actions should include:

- Edit profile
- Manage roles/type
- Suspend or activate

#### Create user dialog

Improve the current form into a clearer sectioned layout:

- Organization selector.
- Basic identity: first name, last name, email.
- Credentials: password and confirm password.
- Access: user type and roles.

If the dialog is opened from an organization drawer, prefill the organization and keep it locked unless the user explicitly changes context.

#### Edit user dialog

Keep profile editing focused on non-access fields:

- first name
- last name
- phone number
- avatar URL
- timezone
- preferred language
- keycloak user id

Role changes, type changes, and status changes should be managed in separate actions so the form does not mix identity edits with access control.

#### Manage access dialog

This dialog should let admins update:

- user type
- roles

It may submit two separate mutations or a single sequential flow, but the UI should present it as one access-management surface.

## Backend/API

### Existing endpoints reused

Organizations:

- `GET /api/v1/admin/organizations`
- `GET /api/v1/admin/organizations/{organizationId}`

Users:

- `GET /api/v1/users`
- `GET /api/v1/organizations/{organizationId}/users/{userId}/detail`
- `POST /api/v1/organizations/{organizationId}/users`
- `PATCH /api/v1/organizations/{organizationId}/users/{userId}/status`
- `PATCH /api/v1/organizations/{organizationId}/users/{userId}/type`
- `PUT /api/v1/organizations/{organizationId}/users/{userId}/roles`
- `PATCH /api/v1/users/{userId}/info`

### Endpoints to add or align

1. `PATCH /api/v1/admin/organizations/{organizationId}/status`

Request:

```json
{
  "status": "SUSPENDED"
}
```

or

```json
{
  "status": "ACTIVE"
}
```

Behavior:

- `SUSPENDED`: suspend the organization and all users in that organization.
- `ACTIVE`: activate the organization and all users in that organization whose status is `SUSPENDED`.

Response should return the updated organization and a small impact summary:

```json
{
  "organization": { "...": "..." },
  "affectedUsers": 23,
  "activatedUsers": 0,
  "suspendedUsers": 23
}
```

The frontend only depends on the updated organization and the counts.

2. Align `GET /api/v1/admin/organizations` to accept and honor `sortBy` and `sortDir` if the frontend sends them.

The current frontend already passes these params through the admin organizations service, so the backend should either support them or the frontend should stop sending them. The design assumes the backend will support them.

3. If the organization drawer needs live user counts for the confirmation dialog, expose or reuse:

- `GET /api/v1/organizations/{organizationId}/users/stats`

If the current auth guard blocks system admin access in this path, add an admin-safe wrapper endpoint under `/api/v1/admin/organizations/{organizationId}/users/stats`.

## Frontend Architecture

### State and hooks

Keep the current module ownership:

- `src/modules/admin/hooks/useOrganizations.ts`
- `src/modules/admin/hooks/useUsers.ts`
- `src/modules/admin/services/...`
- `src/modules/admin/store/...`

Extend the existing slices rather than introducing route-local feature state:

- Organizations need selected row, detail drawer, and status dialog state.
- Users need selected row, detail drawer, access dialog, and status dialog state.

Keep filter state in the existing admin filter slices.

### RTK Query additions

Organizations:

- add `updateOrganizationStatus` mutation

Users:

- add `getUserDetail` query if the drawer does not reuse an existing detail hook
- add `updateUserStatus` mutation
- add `updateUserType` mutation
- add `updateUserRoles` mutation

Lookup data:

- organizations list for the user create dialog
- roles list for user access management
- departments list for the selected organization

### Component additions

Shared admin:

- `AdminFilterDialog`
- `AdminFilterChips`
- `AdminConfirmStatusDialog`

Organizations:

- `OrganizationDetailsDrawer`
- `OrganizationUsersPreview`
- `OrganizationStatusDialog`

Users:

- `UserDetailsDrawer`
- `UserAccessDialog`
- `UserStatusDialog`
- improve `UserDialog` and `UserForm`

## Data Flow

### Organizations page

1. Load organizations through RTK Query using search and grouped filters.
2. Render the table and active chips.
3. Clicking a row action opens drawer or confirmation dialog.
4. Status mutation invalidates organization list, organization detail, and user list caches.
5. After status mutation, refresh the drawer and the users preview if open.

### Users page

1. Load users through RTK Query using search and grouped filters.
2. User detail drawer fetches the detail payload on demand.
3. Create/edit/status/access actions run as separate mutations.
4. Mutations invalidate user list and user detail caches.
5. If a user is edited from an organization drawer context, invalidate the organization users preview too.

## Error Handling

- Keep search and filter updates optimistic in the UI, but do not fake successful mutations.
- Use shared toast/notification helpers for success and error states.
- For destructive organization status changes, require a confirmation step.
- For organization suspend, show the affected user count when available.
- For user detail drawer, show a page-level fallback if the detail query fails.
- Keep backend response shapes stable and preserve `GeneralResponse<?>` where the service already uses it.

## Verification

### Frontend

- `npm run lint`
- `npm run type-check`
- `npm run format:check`

There is no checked-in frontend test runner in this repo, so no frontend unit test command is assumed here.

### Backend

- Run a focused `account` compile or test pass after the API changes.
- Add regression coverage for organization status cascade if a matching unit test layer exists.
- Verify the new admin status endpoint and any organization stats wrapper compile with the existing auth utilities.

## Implementation Notes

- Keep search outside the filter dialog.
- Use grouped filter criteria instead of a long select row.
- Do not add export.
- Do not introduce an organization edit flow unless a real admin update API is added later.
- Align frontend organization enums to the backend enum names before wiring the new filter dialog.
