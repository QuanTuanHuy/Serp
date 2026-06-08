# PM Project Settings Secondary Sidebar Design

## Context

`PMProjectSettingsPage.tsx` currently renders project profile, people, components,
configuration schemes, and project permissions in one long page. The permission
matrix adds enough density that the page needs a second-level settings navigation
to keep each settings area focused and easier to maintain.

The global PM settings page already uses a left settings switcher driven by a
URL query parameter. Project settings should follow the same interaction model
so sections can be deep-linked and refreshed without losing context.

## Goals

- Add a second-level settings sidebar to project settings.
- Split the current long settings surface into focused sections.
- Use URL query state so each section has a stable link.
- Load permission-specific data only when the permissions section is active.
- Keep the existing project top tab behavior unchanged.
- Preserve existing edit, archive, scheme links, and permission save/reset
  behavior.

## Non-Goals

- Do not add nested settings routes in this iteration.
- Do not redesign project-level top tabs.
- Do not add new backend APIs.
- Do not change permission scheme semantics.
- Do not add frontend tests unless a test framework is introduced separately.

## Sections

The sidebar should expose these sections:

- `general`: project profile and project state summary.
- `people`: project lead, member count, role count, and link to the project
  people page.
- `components`: component preview and link to the project components page.
- `schemes`: project configuration scheme assignments and links to global PM
  settings where applicable.
- `permissions`: permission scheme name, permission matrix, unmanaged grants,
  save, and reset.

The default section is `general`. Invalid section values should resolve to
`general`.

## Routing

The page should use a query parameter:

```text
/pm/projects/:projectId/settings?section=permissions
```

Changing the sidebar item should call router navigation or replacement with the
new query string. Back and forward browser navigation should restore the section.

## Layout

Desktop layout should use a two-column grid:

```text
sidebar: 260px
content: minmax(0, 1fr)
```

The existing page header remains above this grid. The sidebar should be an
`aside` containing section buttons with an icon, title, and short description.
The active section should use the same visual language as `PMSettingsPage`:
muted hover state and primary-tinted active state.

On smaller screens, the sidebar should move above the content and behave as a
compact horizontal or wrapped navigation surface. It should not create a fixed
off-canvas drawer for this iteration.

## Data Flow

The settings overview query remains page-level because the header, sidebar, and
most sections depend on project context.

The permissions query should run only when:

- the `projectId` is numeric, and
- `section === 'permissions'`.

The project roles query should use the same condition because it is only needed
to render the permissions matrix.

When leaving and returning to the permissions section, RTK Query cache can reuse
the most recent data. Local permission draft state should still reset from the
server grants when the permission query returns fresh data.

## Components

To keep `PMProjectSettingsPage.tsx` maintainable, implementation should extract
section-level components into the PM project settings area. Preferred files:

- `PMProjectSettingsSidebar.tsx`
- `PMProjectSettingsGeneralSection.tsx`
- `PMProjectSettingsPeopleSection.tsx`
- `PMProjectSettingsComponentsSection.tsx`
- `PMProjectSettingsSchemesSection.tsx`
- `PMProjectPermissionsSection.tsx`

If the first implementation keeps very small sections in the page file, it
should still extract the permissions section because that section already has
substantial matrix logic.

## Error Handling

Overview loading and failure remain page-level because the project context is
required for the settings shell.

Permission query failures are section-level. A permission failure should render
an unavailable message inside the permissions section and should not prevent the
user from using general, people, components, or schemes.

Mutation errors should keep using `.unwrap()` with `try/catch`,
`getErrorMessage(...)`, and `toast` feedback.

## Verification

Run from `serp_web/`:

```bash
npm run type-check
npm run lint
npx prettier --check src/modules/pm/pages/PMProjectSettingsPage.tsx src/modules/pm/components/projects/settings/*.tsx
```

Run `npm run format:check` only if the working tree is expected to already pass
repository-wide formatting.
