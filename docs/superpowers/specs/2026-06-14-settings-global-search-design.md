# Settings Global Search Design

## Context

The settings module has a header search input, but it currently only logs the query. The admin module now has a global search pattern with an account-service endpoint, an RTK Query client, and a results page. Settings needs the same user experience, but the backend boundary is different: this search is for an organization administrator working inside one organization, not for a SERP system administrator.

## Goals

- Add global search for the settings UI.
- Search the most important settings resources only: users, departments, and modules.
- Keep search scoped to the current organization.
- Keep the system-admin endpoint `/api/v1/admin/search` separate from organization-admin search.
- Reuse the admin search response shape so the frontend rendering pattern stays simple.

## Non-Goals

- Search across CRM, sales, PM, logistics, or other business modules.
- Search system-level organizations, roles, menu displays, or subscription plans.
- Add autocomplete or live search in the header.
- Add a visual mockup.

## Backend Design

Add an organization-scoped account-service endpoint:

```http
GET /api/v1/organizations/{organizationId}/settings/search?q=<query>&limit=5
```

This path is intentionally under `/organizations/{organizationId}` instead of `/admin`. In this codebase, `/api/v1/admin/search` represents SERP system admin search. Settings search belongs to an organization admin, so the route should follow the existing organization-scoped settings routes such as users, departments, and modules.

The controller validates access with `authUtils.canAccessOrganization(organizationId)`. If access is denied, it returns the existing forbidden response style. If `q` is blank after trimming, it returns the existing bad-request response style using the shared search query error message.

Add `SettingsSearchUseCase` to orchestrate:

- Users: search users inside `organizationId`.
- Departments: search departments inside `organizationId`.
- Modules: search modules accessible by `organizationId`; do not return modules outside the organization's access/subscription scope.

The endpoint returns top N results per group, defaulting to 5 and capping at 10. The backend returns `total` for each group so the UI can show whether more results exist.

## Response Contract

Use a response shape parallel to admin global search:

```json
{
  "query": "mai",
  "limit": 5,
  "groups": [
    {
      "type": "USER",
      "title": "Users",
      "total": 12,
      "items": [
        {
          "id": "42",
          "title": "Mai Nguyen",
          "subtitle": "mai@example.com - ACTIVE",
          "url": "/settings/users?search=mai"
        }
      ]
    }
  ]
}
```

Group order is fixed:

1. `USER`
2. `DEPARTMENT`
3. `MODULE`

Item URLs point to existing settings list pages with the same query:

- `/settings/users?search=<encodedQuery>`
- `/settings/departments?search=<encodedQuery>`
- `/settings/modules?search=<encodedQuery>`

## Frontend Design

Add a settings-owned RTK Query endpoint, for example `useGetSettingsGlobalSearchQuery`, under `serp_web/src/modules/settings/services/`. It calls:

```text
/organizations/{organizationId}/settings/search?q=<query>&limit=<limit>
```

with `extraOptions: { service: 'account' }`, matching other settings account-service endpoints.

Update `SettingsHeader`:

- Trim submitted input.
- Navigate to `/settings/search?q=<encodedQuery>` when the query is non-empty.
- When the current route is `/settings/search`, sync the input from the URL query parameter.

Add `serp_web/src/app/settings/search/page.tsx`:

- Read `q` from `useSearchParams`.
- Resolve `organizationId` using the same settings/auth context pattern used by settings users/departments pages.
- Skip the API request when query or organization id is missing.
- Render loading, empty-query, error, and result states.
- Render three grouped sections: Users, Departments, Modules.
- Each section shows `total`, top N item links, and a "View all" link to the corresponding settings page with `search=<q>`.

## Data Flow

1. Organization admin enters a query in `SettingsHeader`.
2. Header navigates to `/settings/search?q=<query>`.
3. Search page reads `q` and current `organizationId`.
4. RTK Query calls the account-service endpoint.
5. Account service verifies organization access and searches organization-scoped resources.
6. Search page renders grouped results and list-page links.

## Error Handling

- Blank query: frontend shows an empty prompt; backend also rejects blank `q`.
- Missing organization context: frontend shows a settings access/login message and does not call the endpoint.
- Forbidden organization access: backend returns the existing forbidden response.
- Backend/API failure: frontend shows a retry state.
- No matches in a group: frontend shows an empty group message rather than hiding the group, keeping the three-group layout predictable.

## Testing And Verification

Backend:

- Add focused use case tests for blank query, limit normalization, group order, organization scoping, and URL generation.
- Add controller test for `GET /api/v1/organizations/{organizationId}/settings/search`.
- Run the focused tests and `mvnw.cmd clean compile` in `account`.

Frontend:

- Run TypeScript and lint checks.
- Run Prettier check on touched settings files.
- If full frontend `format:check` fails due unrelated pre-existing files, report that separately and keep touched-file formatting clean.

## Approved Decisions

- Scope: users, departments, and modules only.
- Approach: backend endpoint dedicated to organization settings search.
- Endpoint: `GET /api/v1/organizations/{organizationId}/settings/search`.
- Result style: grouped search results page at `/settings/search?q=...`.
- UI format: text-described design, no visual mockup.
