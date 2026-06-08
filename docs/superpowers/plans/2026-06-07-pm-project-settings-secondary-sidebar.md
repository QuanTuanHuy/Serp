# PM Project Settings Secondary Sidebar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the PM project settings page into a URL-driven settings shell with a second-level sidebar and focused section components.

**Architecture:** Keep `PMProjectSettingsPage.tsx` as the client shell that owns routing, RTK Query calls, mutation handlers, and section selection. Move presentational settings sections into `src/modules/pm/components/projects/settings/` so each section is independently readable and the permissions matrix no longer bloats the page file.

**Tech Stack:** Next.js 15 App Router client component, React 19, TypeScript strict mode, RTK Query, Tailwind CSS, shared Shadcn-style UI primitives, lucide-react icons.

---

## File Structure

- Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsField.tsx`
  - Small reusable label/value field used by project settings sections.
- Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsSidebar.tsx`
  - Owns the section key union, section metadata, and sidebar rendering.
- Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsGeneralSection.tsx`
  - Renders project profile fields.
- Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsPeopleSection.tsx`
  - Renders people summary and link action.
- Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsComponentsSection.tsx`
  - Renders components preview and link action.
- Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsSchemesSection.tsx`
  - Renders configuration scheme assignment list.
- Create `serp_web/src/modules/pm/components/projects/settings/PMProjectPermissionsSection.tsx`
  - Renders permissions matrix, unmanaged grants, and permission helper functions.
- Modify `serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx`
  - Add `useSearchParams`, section normalization, sidebar layout, lazy permissions queries, and section switching.

Frontend tests are not configured in `serp_web`, so this plan uses type-check, lint, and Prettier verification.

---

### Task 1: Add The Settings Field Primitive

**Files:**
- Create: `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsField.tsx`

- [ ] **Step 1: Create the reusable field component**

Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsField.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings field
 */

interface PMProjectSettingsFieldProps {
  label: string;
  value: string;
}

export function PMProjectSettingsField({
  label,
  value,
}: PMProjectSettingsFieldProps) {
  return (
    <div className='rounded-md border px-3 py-2'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {label}
      </p>
      <p className='mt-1 text-sm font-medium'>{value}</p>
    </div>
  );
}
```

- [ ] **Step 2: Run a narrow type check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: pass, because this file is not imported yet.

- [ ] **Step 3: Commit**

```bash
git add src/modules/pm/components/projects/settings/PMProjectSettingsField.tsx
git commit -m "refactor: add project settings field component"
```

---

### Task 2: Add The Sidebar Contract And Navigation Component

**Files:**
- Create: `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsSidebar.tsx`

- [ ] **Step 1: Create the section key and sidebar**

Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsSidebar.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings sidebar
 */

'use client';

import {
  Blocks,
  FolderKanban,
  Settings2,
  ShieldCheck,
  UserRoundCog,
} from 'lucide-react';

import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui';
import { cn } from '@/shared/utils';

export type PMProjectSettingsSectionKey =
  | 'general'
  | 'people'
  | 'components'
  | 'schemes'
  | 'permissions';

export const DEFAULT_PROJECT_SETTINGS_SECTION: PMProjectSettingsSectionKey =
  'general';

export const PROJECT_SETTINGS_SECTION_KEYS: PMProjectSettingsSectionKey[] = [
  'general',
  'people',
  'components',
  'schemes',
  'permissions',
];

const PROJECT_SETTINGS_SECTION_ITEMS: Array<{
  key: PMProjectSettingsSectionKey;
  title: string;
  description: string;
  icon: typeof Settings2;
}> = [
  {
    key: 'general',
    title: 'General',
    description: 'Project profile and state',
    icon: Settings2,
  },
  {
    key: 'people',
    title: 'People',
    description: 'Lead, members, and roles',
    icon: UserRoundCog,
  },
  {
    key: 'components',
    title: 'Components',
    description: 'Project component preview',
    icon: FolderKanban,
  },
  {
    key: 'schemes',
    title: 'Schemes',
    description: 'Configuration assignments',
    icon: Blocks,
  },
  {
    key: 'permissions',
    title: 'Permissions',
    description: 'Project access grants',
    icon: ShieldCheck,
  },
];

interface PMProjectSettingsSidebarProps {
  activeSection: PMProjectSettingsSectionKey;
  onSectionChange: (section: PMProjectSettingsSectionKey) => void;
}

export function PMProjectSettingsSidebar({
  activeSection,
  onSectionChange,
}: PMProjectSettingsSidebarProps) {
  return (
    <aside className='lg:sticky lg:top-4 lg:h-fit'>
      <Card className='border-border/60 bg-background/90 shadow-sm'>
        <CardHeader className='border-b py-4'>
          <CardTitle className='text-sm'>Project settings</CardTitle>
        </CardHeader>
        <CardContent className='p-2'>
          <nav
            className='flex gap-2 overflow-x-auto p-1 lg:block lg:space-y-1 lg:overflow-visible'
            aria-label='Project settings sections'
          >
            {PROJECT_SETTINGS_SECTION_ITEMS.map((item) => {
              const Icon = item.icon;
              const active = item.key === activeSection;

              return (
                <button
                  key={item.key}
                  type='button'
                  onClick={() => onSectionChange(item.key)}
                  className={cn(
                    'flex min-w-44 shrink-0 items-start gap-3 rounded-md px-3 py-2 text-left transition-colors hover:bg-muted lg:min-w-0 lg:w-full',
                    active && 'bg-primary/10 text-primary'
                  )}
                  aria-current={active ? 'page' : undefined}
                >
                  <Icon className='mt-0.5 h-4 w-4 shrink-0' />
                  <span className='min-w-0'>
                    <span className='block text-sm font-medium'>
                      {item.title}
                    </span>
                    <span className='block text-xs text-muted-foreground'>
                      {item.description}
                    </span>
                  </span>
                </button>
              );
            })}
          </nav>
        </CardContent>
      </Card>
    </aside>
  );
}
```

- [ ] **Step 2: Run a narrow type check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: pass.

- [ ] **Step 3: Commit**

```bash
git add src/modules/pm/components/projects/settings/PMProjectSettingsSidebar.tsx
git commit -m "feat: add project settings sidebar component"
```

---

### Task 3: Extract General, People, Components, And Schemes Sections

**Files:**
- Create: `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsGeneralSection.tsx`
- Create: `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsPeopleSection.tsx`
- Create: `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsComponentsSection.tsx`
- Create: `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsSchemesSection.tsx`

- [ ] **Step 1: Create the general section**

Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsGeneralSection.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings general section
 */

import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui';

import type { PMProjectSettingsOverviewApi } from '../../../types/api';
import { PMProjectSettingsField } from './PMProjectSettingsField';

interface PMProjectSettingsGeneralSectionProps {
  project: PMProjectSettingsOverviewApi['project'];
}

export function PMProjectSettingsGeneralSection({
  project,
}: PMProjectSettingsGeneralSectionProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>Project profile</CardTitle>
      </CardHeader>
      <CardContent className='grid gap-3 p-4 md:grid-cols-2'>
        <PMProjectSettingsField label='Name' value={project.name} />
        <PMProjectSettingsField label='Key' value={project.key} />
        <PMProjectSettingsField
          label='Lead'
          value={project.leadUserName || 'Unassigned'}
        />
        <PMProjectSettingsField
          label='Category'
          value={project.category?.name || 'No category'}
        />
        <PMProjectSettingsField
          label='Project type'
          value={project.projectTypeKey}
        />
        <PMProjectSettingsField
          label='State'
          value={project.isArchived ? 'Archived' : 'Active'}
        />
        <div className='md:col-span-2'>
          <PMProjectSettingsField
            label='Description'
            value={project.description || '-'}
          />
        </div>
      </CardContent>
    </Card>
  );
}
```

- [ ] **Step 2: Create the people section**

Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsPeopleSection.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings people section
 */

import { Users } from 'lucide-react';

import { Button, Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui';

import type { PMProjectSettingsOverviewApi } from '../../../types/api';
import { PMProjectSettingsField } from './PMProjectSettingsField';

interface PMProjectSettingsPeopleSectionProps {
  people: PMProjectSettingsOverviewApi['people'];
  onOpenPeople: () => void;
}

export function PMProjectSettingsPeopleSection({
  people,
  onOpenPeople,
}: PMProjectSettingsPeopleSectionProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>People & roles</CardTitle>
      </CardHeader>
      <CardContent className='space-y-3 p-4'>
        <PMProjectSettingsField
          label='Project lead'
          value={people.leadUserName || 'Unassigned'}
        />
        <div className='grid gap-3 sm:grid-cols-2'>
          <PMProjectSettingsField
            label='Members'
            value={String(people.memberCount)}
          />
          <PMProjectSettingsField
            label='Roles used'
            value={String(people.roleCount)}
          />
        </div>
        <Button
          type='button'
          variant='outline'
          className='w-full justify-start'
          onClick={onOpenPeople}
        >
          <Users className='mr-2 h-4 w-4' />
          Open people
        </Button>
      </CardContent>
    </Card>
  );
}
```

- [ ] **Step 3: Create the components section**

Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsComponentsSection.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings components section
 */

import { FolderKanban } from 'lucide-react';

import { Button, Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui';

import type { PMProjectSettingsOverviewApi } from '../../../types/api';

interface PMProjectSettingsComponentsSectionProps {
  components: PMProjectSettingsOverviewApi['components'];
  onOpenComponents: () => void;
}

export function PMProjectSettingsComponentsSection({
  components,
  onOpenComponents,
}: PMProjectSettingsComponentsSectionProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>Components</CardTitle>
      </CardHeader>
      <CardContent className='p-0'>
        <div className='flex items-center justify-between border-b px-4 py-3 text-sm text-muted-foreground'>
          <span>{components.totalCount} total</span>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={onOpenComponents}
          >
            <FolderKanban className='mr-2 h-4 w-4' />
            Open components
          </Button>
        </div>
        {components.preview.length > 0 ? (
          <div className='divide-y'>
            {components.preview.map((component) => (
              <div
                key={component.id}
                className='grid gap-3 px-4 py-3 md:grid-cols-[minmax(0,1fr)_120px_160px]'
              >
                <div className='min-w-0'>
                  <p className='font-medium'>{component.name}</p>
                  <p className='truncate text-sm text-muted-foreground'>
                    {component.description || '-'}
                  </p>
                </div>
                <div className='text-sm text-muted-foreground'>
                  {component.issueCount} issues
                </div>
                <div className='text-sm text-muted-foreground'>
                  {component.assigneeType}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className='px-4 py-6 text-sm text-muted-foreground'>
            No components.
          </div>
        )}
      </CardContent>
    </Card>
  );
}
```

- [ ] **Step 4: Create the schemes section**

Create `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsSchemesSection.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings schemes section
 */

import Link from 'next/link';

import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui';

import type { PMProjectSettingsOverviewApi } from '../../../types/api';

interface PMProjectSettingsSchemesSectionProps {
  schemes: PMProjectSettingsOverviewApi['schemes'];
}

export function PMProjectSettingsSchemesSection({
  schemes,
}: PMProjectSettingsSchemesSectionProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>Configuration schemes</CardTitle>
      </CardHeader>
      <CardContent className='space-y-2 p-4'>
        {schemes.map((scheme) => (
          <div key={scheme.type} className='rounded-md border p-3'>
            <div className='flex items-start justify-between gap-3'>
              <div className='min-w-0'>
                <p className='text-sm font-medium'>{scheme.label}</p>
                <p className='truncate text-sm text-muted-foreground'>
                  {scheme.schemeName ||
                    (typeof scheme.schemeId === 'number'
                      ? `#${scheme.schemeId}`
                      : '-')}
                </p>
              </div>
              {scheme.globalSection ? (
                <Button asChild variant='ghost' size='sm'>
                  <Link href={`/pm/settings?section=${scheme.globalSection}`}>
                    Open
                  </Link>
                </Button>
              ) : (
                <Badge variant='secondary'>Read-only</Badge>
              )}
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
```

- [ ] **Step 5: Run type check and lint**

Run from `serp_web/`:

```bash
npm run type-check
npm run lint
```

Expected: both pass.

- [ ] **Step 6: Commit**

```bash
git add src/modules/pm/components/projects/settings/PMProjectSettingsGeneralSection.tsx src/modules/pm/components/projects/settings/PMProjectSettingsPeopleSection.tsx src/modules/pm/components/projects/settings/PMProjectSettingsComponentsSection.tsx src/modules/pm/components/projects/settings/PMProjectSettingsSchemesSection.tsx
git commit -m "refactor: extract project settings overview sections"
```

---

### Task 4: Extract The Permissions Section

**Files:**
- Create: `serp_web/src/modules/pm/components/projects/settings/PMProjectPermissionsSection.tsx`
- Modify: `serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx`

- [ ] **Step 1: Create the extracted permissions section**

Create `serp_web/src/modules/pm/components/projects/settings/PMProjectPermissionsSection.tsx` by moving the existing `ProjectPermissionSection`, `PermissionGroupRows`, and helper functions from `PMProjectSettingsPage.tsx` into this file:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project permissions settings section
 */

'use client';

import { useMemo } from 'react';
import { RotateCcw, Save, ShieldCheck } from 'lucide-react';

import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';

import type {
  PMProjectPermissionDefinitionApi,
  PMProjectPermissionGrantApi,
} from '../../../types/api';

export type PMEditablePermissionGranteeType = 'PROJECT_LEAD' | 'PROJECT_ROLE';

interface PMProjectPermissionsSectionProps {
  permissions: PMProjectPermissionDefinitionApi[];
  grants: PMProjectPermissionGrantApi[];
  roles: Array<{ id: number; name: string; isSystem: boolean }>;
  schemeName?: string;
  isLoading: boolean;
  errorMessage?: string;
  isSaving: boolean;
  isDirty: boolean;
  onToggleGrant: (
    permissionKey: string,
    granteeType: PMEditablePermissionGranteeType,
    granteeRef?: string | null
  ) => void;
  onReset: () => void;
  onSave: () => void;
}

export function PMProjectPermissionsSection({
  permissions,
  grants,
  roles,
  schemeName,
  isLoading,
  errorMessage,
  isSaving,
  isDirty,
  onToggleGrant,
  onReset,
  onSave,
}: PMProjectPermissionsSectionProps) {
  const permissionGroups = useMemo(
    () => groupPermissionsByCategory(permissions),
    [permissions]
  );
  const unmanagedGrants = useMemo(
    () =>
      grants.filter(
        (grant) =>
          grant.granteeType !== 'PROJECT_LEAD' &&
          grant.granteeType !== 'PROJECT_ROLE'
      ),
    [grants]
  );

  return (
    <Card className='shadow-sm'>
      <CardHeader className='gap-3 border-b md:flex-row md:items-center md:justify-between'>
        <div className='min-w-0'>
          <CardTitle className='flex items-center gap-2 text-base'>
            <ShieldCheck className='h-4 w-4' />
            Project permissions
          </CardTitle>
          <p className='mt-1 truncate text-sm text-muted-foreground'>
            {schemeName || 'Permission scheme'}
          </p>
        </div>
        <div className='flex flex-wrap gap-2'>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={onReset}
            disabled={!isDirty || isSaving}
          >
            <RotateCcw className='mr-2 h-4 w-4' />
            Reset
          </Button>
          <Button
            type='button'
            size='sm'
            onClick={onSave}
            disabled={!isDirty || isSaving}
          >
            <Save className='mr-2 h-4 w-4' />
            Save
          </Button>
        </div>
      </CardHeader>
      <CardContent className='p-0'>
        {isLoading ? (
          <div className='space-y-3 p-4'>
            {Array.from({ length: 5 }).map((_, index) => (
              <Skeleton key={index} className='h-12 w-full' />
            ))}
          </div>
        ) : errorMessage ? (
          <div className='p-6 text-sm text-muted-foreground'>
            Project permissions unavailable: {errorMessage}
          </div>
        ) : (
          <div className='overflow-x-auto'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className='min-w-72'>Permission</TableHead>
                  <TableHead className='w-32 text-center'>
                    Project lead
                  </TableHead>
                  {roles.map((role) => (
                    <TableHead key={role.id} className='w-36 text-center'>
                      <span className='line-clamp-2'>{role.name}</span>
                    </TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {permissionGroups.map((group) => (
                  <PermissionGroupRows
                    key={group.category}
                    category={group.category}
                    permissions={group.permissions}
                    grants={grants}
                    roles={roles}
                    onToggleGrant={onToggleGrant}
                  />
                ))}
                {permissionGroups.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={roles.length + 2}
                      className='h-24 text-center text-sm text-muted-foreground'
                    >
                      No permission definitions found.
                    </TableCell>
                  </TableRow>
                ) : null}
              </TableBody>
            </Table>
          </div>
        )}
        {unmanagedGrants.length > 0 ? (
          <div className='border-t p-4'>
            <p className='text-xs font-semibold uppercase text-muted-foreground'>
              Other grants
            </p>
            <div className='mt-2 flex flex-wrap gap-2'>
              {unmanagedGrants.map((grant) => (
                <Badge key={grantKey(grant)} variant='outline'>
                  {grant.permissionKey}: {grant.granteeType}
                  {grant.granteeRef ? ` ${grant.granteeRef}` : ''}
                </Badge>
              ))}
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

function PermissionGroupRows({
  category,
  permissions,
  grants,
  roles,
  onToggleGrant,
}: {
  category: string;
  permissions: PMProjectPermissionDefinitionApi[];
  grants: PMProjectPermissionGrantApi[];
  roles: Array<{ id: number; name: string; isSystem: boolean }>;
  onToggleGrant: PMProjectPermissionsSectionProps['onToggleGrant'];
}) {
  return (
    <>
      <TableRow className='bg-muted/50 hover:bg-muted/50'>
        <TableCell
          colSpan={roles.length + 2}
          className='py-2 text-xs font-semibold uppercase text-muted-foreground'
        >
          {formatPermissionCategory(category)}
        </TableCell>
      </TableRow>
      {permissions.map((permission) => (
        <TableRow key={permission.permissionKey}>
          <TableCell>
            <div className='space-y-1'>
              <p className='font-medium'>{permission.name}</p>
              <p className='text-xs text-muted-foreground'>
                {permission.description || permission.permissionKey}
              </p>
            </div>
          </TableCell>
          <TableCell className='text-center'>
            <Checkbox
              checked={grants.some((grant) =>
                isGrantMatch(
                  grant,
                  permission.permissionKey,
                  'PROJECT_LEAD',
                  null
                )
              )}
              onCheckedChange={() =>
                onToggleGrant(permission.permissionKey, 'PROJECT_LEAD', null)
              }
              aria-label={`${permission.name} for project lead`}
            />
          </TableCell>
          {roles.map((role) => (
            <TableCell key={role.id} className='text-center'>
              <Checkbox
                checked={grants.some((grant) =>
                  isGrantMatch(
                    grant,
                    permission.permissionKey,
                    'PROJECT_ROLE',
                    role.name
                  )
                )}
                onCheckedChange={() =>
                  onToggleGrant(
                    permission.permissionKey,
                    'PROJECT_ROLE',
                    role.name
                  )
                }
                aria-label={`${permission.name} for ${role.name}`}
              />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  );
}

function groupPermissionsByCategory(
  permissions: PMProjectPermissionDefinitionApi[]
) {
  const groups = new Map<string, PMProjectPermissionDefinitionApi[]>();
  for (const permission of permissions) {
    const category = permission.category || 'OTHER';
    groups.set(category, [...(groups.get(category) ?? []), permission]);
  }
  return Array.from(groups.entries()).map(([category, items]) => ({
    category,
    permissions: items,
  }));
}

export function isGrantMatch(
  grant: PMProjectPermissionGrantApi,
  permissionKey: string,
  granteeType: string,
  granteeRef?: string | null
) {
  return (
    grant.permissionKey === permissionKey &&
    grant.granteeType === granteeType &&
    normalizeGrantRef(grant.granteeRef) === normalizeGrantRef(granteeRef)
  );
}

export function buildGrantStateKey(grants: PMProjectPermissionGrantApi[]) {
  return grants.map(grantKey).sort().join('|');
}

function grantKey(grant: PMProjectPermissionGrantApi) {
  return [
    grant.permissionKey,
    grant.granteeType,
    normalizeGrantRef(grant.granteeRef) ?? '',
    grant.customFieldId ?? '',
  ].join(':');
}

function normalizeGrantRef(value?: string | null) {
  const normalized = value?.trim();
  return normalized ? normalized.toLowerCase() : null;
}

function formatPermissionCategory(category: string) {
  return category.toLowerCase().replaceAll('_', ' ');
}
```

- [ ] **Step 2: Remove moved permission section code from the page**

In `serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx`, remove these local declarations after the page component:

```tsx
function SettingField(...)
interface ProjectPermissionSectionProps ...
function ProjectPermissionSection(...)
function PermissionGroupRows(...)
function groupPermissionsByCategory(...)
function isGrantMatch(...)
function buildGrantStateKey(...)
function grantKey(...)
function normalizeGrantRef(...)
function formatPermissionCategory(...)
```

Then import the moved helpers at the top of the page:

```tsx
import {
  buildGrantStateKey,
  isGrantMatch,
  PMProjectPermissionsSection,
  type PMEditablePermissionGranteeType,
} from '../components/projects/settings/PMProjectPermissionsSection';
```

Update the toggle handler signature:

```tsx
const handleTogglePermissionGrant = (
  permissionKey: string,
  granteeType: PMEditablePermissionGranteeType,
  granteeRef?: string | null
) => {
  setPermissionGrantDraft((current) => {
    const exists = current.some((grant) =>
      isGrantMatch(grant, permissionKey, granteeType, granteeRef)
    );

    if (exists) {
      return current.filter(
        (grant) =>
          !isGrantMatch(grant, permissionKey, granteeType, granteeRef)
      );
    }

    return [
      ...current,
      {
        permissionKey,
        granteeType,
        granteeRef: granteeRef ?? null,
        customFieldId: null,
      },
    ];
  });
};
```

- [ ] **Step 3: Replace page usage with the extracted component**

Replace the existing JSX usage:

```tsx
<ProjectPermissionSection
  permissions={permissionsQuery.data?.permissions ?? []}
  grants={permissionGrantDraft}
  roles={rolesQuery.data?.data.items ?? []}
  schemeName={permissionsQuery.data?.scheme.name}
  isLoading={permissionsQuery.isLoading || rolesQuery.isLoading}
  errorMessage={
    permissionsQuery.error
      ? getErrorMessage(permissionsQuery.error)
      : undefined
  }
  isSaving={replacePermissionGrantsState.isLoading}
  isDirty={hasPermissionChanges}
  onToggleGrant={handleTogglePermissionGrant}
  onReset={handleResetPermissionGrants}
  onSave={handleSavePermissionGrants}
/>
```

with:

```tsx
<PMProjectPermissionsSection
  permissions={permissionsQuery.data?.permissions ?? []}
  grants={permissionGrantDraft}
  roles={rolesQuery.data?.data.items ?? []}
  schemeName={permissionsQuery.data?.scheme.name}
  isLoading={permissionsQuery.isLoading || rolesQuery.isLoading}
  errorMessage={
    permissionsQuery.error
      ? getErrorMessage(permissionsQuery.error)
      : undefined
  }
  isSaving={replacePermissionGrantsState.isLoading}
  isDirty={hasPermissionChanges}
  onToggleGrant={handleTogglePermissionGrant}
  onReset={handleResetPermissionGrants}
  onSave={handleSavePermissionGrants}
/>
```

- [ ] **Step 4: Run type check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add src/modules/pm/components/projects/settings/PMProjectPermissionsSection.tsx src/modules/pm/pages/PMProjectSettingsPage.tsx
git commit -m "refactor: extract project permissions settings section"
```

---

### Task 5: Convert The Page Into A URL-Driven Settings Shell

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx`

- [ ] **Step 1: Add section imports and URL state**

Update imports in `PMProjectSettingsPage.tsx`:

```tsx
import { useRouter, useSearchParams } from 'next/navigation';
import { Archive, ArrowLeft, PenLine } from 'lucide-react';
```

Add section component imports:

```tsx
import { PMProjectSettingsComponentsSection } from '../components/projects/settings/PMProjectSettingsComponentsSection';
import { PMProjectSettingsGeneralSection } from '../components/projects/settings/PMProjectSettingsGeneralSection';
import { PMProjectSettingsPeopleSection } from '../components/projects/settings/PMProjectSettingsPeopleSection';
import { PMProjectSettingsSchemesSection } from '../components/projects/settings/PMProjectSettingsSchemesSection';
import {
  DEFAULT_PROJECT_SETTINGS_SECTION,
  PMProjectSettingsSidebar,
  PROJECT_SETTINGS_SECTION_KEYS,
  type PMProjectSettingsSectionKey,
} from '../components/projects/settings/PMProjectSettingsSidebar';
```

Inside the component, add:

```tsx
const searchParams = useSearchParams();
const activeSection = normalizeSettingsSection(searchParams.get('section'));
const canLoadPermissions =
  canLoadProjectScopedData && activeSection === 'permissions';
```

Change permission queries to:

```tsx
const permissionsQuery = useGetPmProjectPermissionsQuery(numericProjectId, {
  skip: !canLoadPermissions,
});
const rolesQuery = useGetPmProjectRolesQuery(
  {
    page: 0,
    pageSize: 100,
  },
  {
    skip: !canLoadPermissions,
  }
);
```

Add the section change handler:

```tsx
const handleSectionChange = (section: PMProjectSettingsSectionKey) => {
  const nextParams = new URLSearchParams(searchParams.toString());
  if (section === DEFAULT_PROJECT_SETTINGS_SECTION) {
    nextParams.delete('section');
  } else {
    nextParams.set('section', section);
  }

  const query = nextParams.toString();
  router.push(
    query
      ? `/pm/projects/${projectId}/settings?${query}`
      : `/pm/projects/${projectId}/settings`
  );
};
```

Add the normalizer outside the component:

```tsx
function normalizeSettingsSection(
  value: string | null
): PMProjectSettingsSectionKey {
  if (
    value &&
    PROJECT_SETTINGS_SECTION_KEYS.includes(
      value as PMProjectSettingsSectionKey
    )
  ) {
    return value as PMProjectSettingsSectionKey;
  }

  return DEFAULT_PROJECT_SETTINGS_SECTION;
}
```

- [ ] **Step 2: Replace the long body with sidebar layout**

After the header block and after destructuring:

```tsx
const { project, people, components, schemes } = overview;
```

replace the existing overview grids and permissions section with:

```tsx
<div className='grid gap-6 lg:grid-cols-[260px_minmax(0,1fr)]'>
  <PMProjectSettingsSidebar
    activeSection={activeSection}
    onSectionChange={handleSectionChange}
  />

  <main className='min-w-0 space-y-5'>
    {activeSection === 'general' ? (
      <PMProjectSettingsGeneralSection project={project} />
    ) : null}

    {activeSection === 'people' ? (
      <PMProjectSettingsPeopleSection
        people={people}
        onOpenPeople={() => router.push(`/pm/projects/${projectId}/people`)}
      />
    ) : null}

    {activeSection === 'components' ? (
      <PMProjectSettingsComponentsSection
        components={components}
        onOpenComponents={() =>
          router.push(`/pm/projects/${projectId}/components`)
        }
      />
    ) : null}

    {activeSection === 'schemes' ? (
      <PMProjectSettingsSchemesSection schemes={schemes} />
    ) : null}

    {activeSection === 'permissions' ? (
      <PMProjectPermissionsSection
        permissions={permissionsQuery.data?.permissions ?? []}
        grants={permissionGrantDraft}
        roles={rolesQuery.data?.data.items ?? []}
        schemeName={permissionsQuery.data?.scheme.name}
        isLoading={permissionsQuery.isLoading || rolesQuery.isLoading}
        errorMessage={
          permissionsQuery.error
            ? getErrorMessage(permissionsQuery.error)
            : undefined
        }
        isSaving={replacePermissionGrantsState.isLoading}
        isDirty={hasPermissionChanges}
        onToggleGrant={handleTogglePermissionGrant}
        onReset={handleResetPermissionGrants}
        onSave={handleSavePermissionGrants}
      />
    ) : null}
  </main>
</div>
```

Remove local variables that become unused after the replacement:

```tsx
const componentPreview = components.preview;
```

- [ ] **Step 3: Run type check to catch import and unused symbol errors**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: pass.

- [ ] **Step 4: Run lint to catch hook and import ordering issues**

Run from `serp_web/`:

```bash
npm run lint
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add src/modules/pm/pages/PMProjectSettingsPage.tsx
git commit -m "feat: add project settings section navigation"
```

---

### Task 6: Format And Verify The Full Frontend Change

**Files:**
- Modify only if formatter reports changes:
  - `serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx`
  - `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsField.tsx`
  - `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsSidebar.tsx`
  - `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsGeneralSection.tsx`
  - `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsPeopleSection.tsx`
  - `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsComponentsSection.tsx`
  - `serp_web/src/modules/pm/components/projects/settings/PMProjectSettingsSchemesSection.tsx`
  - `serp_web/src/modules/pm/components/projects/settings/PMProjectPermissionsSection.tsx`

- [ ] **Step 1: Run Prettier check on touched files**

Run from `serp_web/`:

```bash
npx prettier --check src/modules/pm/pages/PMProjectSettingsPage.tsx src/modules/pm/components/projects/settings/*.tsx
```

Expected: pass.

- [ ] **Step 2: If Prettier fails, format only touched files**

Run from `serp_web/`:

```bash
npx prettier --write src/modules/pm/pages/PMProjectSettingsPage.tsx src/modules/pm/components/projects/settings/*.tsx
```

Expected: Prettier writes only the page and new settings component files.

- [ ] **Step 3: Re-run the required frontend gates**

Run from `serp_web/`:

```bash
npm run type-check
npm run lint
npx prettier --check src/modules/pm/pages/PMProjectSettingsPage.tsx src/modules/pm/components/projects/settings/*.tsx
```

Expected: all pass.

- [ ] **Step 4: Check repository diff hygiene**

Run from repo root:

```bash
git diff --check
git status --short
```

Expected: `git diff --check` exits with status 0. `git status --short` shows only files from this plan and any pre-existing unrelated files.

- [ ] **Step 5: Commit formatting changes if Step 2 changed files**

```bash
git add src/modules/pm/pages/PMProjectSettingsPage.tsx src/modules/pm/components/projects/settings
git commit -m "style: format project settings sidebar files"
```

Skip this commit when Step 1 already passed and Step 2 was not run.

---

## Self-Review

- Spec coverage: The plan implements URL query navigation, focused sections, sidebar layout, lazy permission and roles queries, section-specific permission error handling, and current edit/archive/link behavior preservation.
- Placeholder scan: The plan contains no `TBD`, no `TODO`, no incomplete task names, and no open implementation slots.
- Type consistency: The section key type is defined once in `PMProjectSettingsSidebar.tsx` and reused by the page. Permission grant matching helpers are exported from `PMProjectPermissionsSection.tsx` and consumed by the page mutation state.
