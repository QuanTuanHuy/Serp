# PM Project Settings Overview Design

## Context

`pm_core` models a project as a company-managed container that binds effective scheme configuration. Existing project detail data already exposes the project core fields and scheme ids, while `/pm/settings` manages global PM configuration such as work types, workflow schemes, and priority schemes.

The current `PMProjectSettingsPage` is already a project-level settings route, but it behaves like a thin overview assembled from several frontend queries. The desired direction is to make the project settings tab an overview and launcher, not an inline project administration console.

## Scope

Build a project settings overview at `/pm/projects/{projectId}/settings`.

The v1 feature includes:

- Project identity and details.
- Project lead, people, and roles summary.
- Components count and short preview.
- Bound scheme list for all configured project scheme families.
- Launcher actions to project-scoped pages.
- Section-level deep links to global PM settings sections.
- Archive and unarchive actions using existing project mutations.

The v1 feature excludes:

- Inline editing of project fields.
- Inline people, role, component, or scheme management.
- Record-level focus links in global PM settings.
- Frontend permission gating or capability-based action hiding.
- Readiness checks, warnings, and operational metrics.

## Backend Design

Add a read endpoint:

- `GET /api/v1/projects/{projectId}/settings-overview`

Add a query handler under the project application query layer, for example `GetProjectSettingsOverviewQueryHandler`. The handler loads data tenant-scoped and returns a dedicated view contract instead of reusing raw project detail response types.

People summary semantics:

- `memberCount` is the number of distinct visible project people, including the project lead even when the lead has no role actor row.
- `roleCount` is the number of distinct project roles currently assigned to visible project people.

Response shape:

```ts
interface PMProjectSettingsOverviewApi {
  project: {
    id: number;
    key: string;
    name: string;
    description?: string | null;
    url?: string | null;
    projectTypeKey: string;
    isArchived: boolean;
    archivedAt?: number | string | null;
    leadUserId?: number | null;
    leadUserName?: string | null;
    category?: { id: number; name: string } | null;
  };
  people: {
    leadUserId?: number | null;
    leadUserName?: string | null;
    memberCount: number;
    roleCount: number;
  };
  components: {
    totalCount: number;
    preview: Array<{
      id: number;
      name: string;
      description?: string | null;
      issueCount: number;
      assigneeType: string;
    }>;
  };
  schemes: Array<{
    type:
      | 'ISSUE_TYPE'
      | 'WORKFLOW'
      | 'FIELD_CONFIG'
      | 'SCREEN'
      | 'PERMISSION'
      | 'NOTIFICATION'
      | 'PRIORITY'
      | 'ISSUE_SECURITY';
    label: string;
    schemeId?: number | null;
    schemeName?: string | null;
    globalSection?: string | null;
    available: boolean;
  }>;
}
```

Scheme bindings:

- `ISSUE_TYPE` maps to `/pm/settings?section=work-type-schemes`.
- `WORKFLOW` maps to `/pm/settings?section=workflow-schemes`.
- `PRIORITY` maps to `/pm/settings?section=priority-schemes`.
- Scheme families without a current global settings page return `globalSection: null`.
- Scheme families whose names cannot yet be resolved return `schemeName: null` while still exposing the bound `schemeId`.
- `available` means the settings family has a supported global destination in the current frontend. It does not mean the bound scheme itself is valid or invalid.

The endpoint does not return frontend capabilities in v1. Mutations and route access continue to rely on existing backend authorization checks.

## Frontend Design

Add a new RTK Query endpoint in `serp_web/src/modules/pm/api/projectApi.ts`:

- `getPmProjectSettingsOverview`

Add matching contract types in `project-api.types.ts`.

`PMProjectSettingsPage` should call the overview query and render four launcher sections:

1. Project profile
   - Name, key, project type, category, lead, archive state, and description.
   - Actions: `Back to project`, `Edit project`, and `Archive` or `Unarchive`.

2. People and roles
   - Lead, member count, and role count.
   - Actions: open people page and edit ownership through the project edit page.

3. Components
   - Total component count and a preview of a few components.
   - Action: open project components page.

4. Configuration schemes
   - List all scheme bindings from the backend response.
   - If `globalSection` is present, link to `/pm/settings?section=<globalSection>`.
   - If `globalSection` is absent, show the binding as read-only with no navigation action.
   - If `schemeName` is absent but `schemeId` exists, show `#<schemeId>`.

The layout should stay compact and utilitarian, matching existing PM pages. It should not add a nested settings sidebar because this page is a launcher, not a full project admin console.

## Data Flow

1. The project settings route passes `projectId` into `PMProjectSettingsPage`.
2. `PMProjectSettingsPage` calls `GET /projects/{projectId}/settings-overview`.
3. The page renders project profile, people summary, components preview, and scheme bindings from the overview response.
4. Archive and unarchive keep using existing endpoints:
   - `POST /projects/{projectId}/archive`
   - `POST /projects/{projectId}/unarchive`
5. Archive and unarchive success invalidates both project detail/list cache and the project settings overview cache.
6. Launcher actions navigate directly with `router.push` or `Link`.

## Error Handling

Backend errors use existing domain exceptions and the `GeneralResponse<?>` envelope.

Expected backend failure cases:

- Missing tenant claim.
- Project not found in current tenant.
- Insufficient permission when existing project mutation endpoints are used.

Frontend behavior:

- Loading state shows a compact page-level skeleton or loading panel.
- Project unavailable state shows a card with a back action.
- Missing optional fields render stable fallbacks such as `-`, `Unassigned`, `No category`, and `No components`.
- Scheme bindings without a global settings section render read-only.
- Archive and unarchive errors use `toast.error` with `getErrorMessage(error)`.

## Testing

Backend tests:

- Query handler returns project core configuration for the requested tenant.
- Query handler returns people member and role counts.
- Query handler returns component total count and preview rows.
- Query handler returns all eight scheme families.
- Query handler resolves names and global sections for issue type, workflow, and priority scheme bindings.
- Query handler keeps unresolved or unsupported scheme families read-only.

Frontend verification:

- No frontend test framework exists in `serp_web`.
- Run `npm run lint`, `npm run type-check`, and `npm run format:check` from `serp_web`.

Backend verification:

- Run focused handler/controller tests for the new overview query.
- Run `./mvnw.cmd clean compile` from `pm_core`.

## Open Decisions

None. The approved v1 is a backend-composed project settings overview and launcher with section-level global settings links.
