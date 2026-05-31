# Work Item Links Detail Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve PM linked work item presentation, show inward `Blocks` links as `is blocked by`, make the section collapsible, and add a full work item detail route.

**Architecture:** Keep the backend contract unchanged. Refactor the existing dialog into reusable detail content, then use the same content from a new App Router page. Keep linked item grouping and navigation inside the existing relation list component so dialog and page behavior stay consistent.

**Tech Stack:** Next.js App Router, React 19, TypeScript, RTK Query, Tailwind CSS, lucide-react, existing SERP shared UI primitives.

---

## File Structure

- Modify `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx`
  - Extract shared `PMWorkItemDetailContent`.
  - Add reusable full-height/page-safe layout props.
  - Add collapsible state for the linked work items section.
  - Pass `projectId` into `WorkItemLinksList`.
- Modify `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemRelationLists.tsx`
  - Group links by `linkType.description`.
  - Render compact Jira-like rows.
  - Navigate linked rows to `/pm/projects/:projectId/work-items/:workItemId`.
  - Stop row navigation when deleting a link.
- Create `serp_web/src/modules/pm/pages/PMWorkItemDetailPage.tsx`
  - Validate route params.
  - Render shared `PMWorkItemDetailContent` in a page shell.
  - Use router back behavior for the close button.
- Create `serp_web/src/app/pm/projects/[projectId]/(detail)/work-items/[workItemId]/page.tsx`
  - Thin App Router wrapper that passes params to the module page.
- Modify `serp_web/src/modules/pm/components/work-items/detail/index.ts`
  - Export `PMWorkItemDetailContent` if needed by the module page.

No `pm_core` files should be changed for this feature.

---

### Task 1: Extract Shared Detail Content

**Files:**
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/index.ts`

- [ ] **Step 1: Update the public props and add shared content props**

In `PMWorkItemDetailDialog.tsx`, keep `PMWorkItemDetailDialogProps` local and add this exported props type immediately after it:

```tsx
export interface PMWorkItemDetailContentProps {
  projectId: number;
  workItemId?: number;
  fallbackItem?: PMWorkItemDetailFallback;
  onClose?: () => void;
  className?: string;
}
```

- [ ] **Step 2: Replace dialog-owned fetching with shared content**

Change `PMWorkItemDetailDialog` so it only renders the dialog shell:

```tsx
export function PMWorkItemDetailDialog({
  projectId,
  workItemId,
  open,
  fallbackItem,
  onOpenChange,
}: PMWorkItemDetailDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className='h-[min(860px,calc(100vh-6rem))] w-[calc(100vw-1rem)] !max-w-[1280px] gap-0 overflow-hidden p-0 sm:rounded-xl lg:w-[min(1280px,calc(100vw-2rem))]'
        showCloseButton={false}
      >
        <PMWorkItemDetailContent
          projectId={projectId}
          workItemId={workItemId}
          fallbackItem={fallbackItem}
          onClose={() => onOpenChange(false)}
        />
      </DialogContent>
    </Dialog>
  );
}
```

- [ ] **Step 3: Add `PMWorkItemDetailContent` below the dialog component**

Move the current `activityTab`, `shouldFetch`, RTK Query hooks, `item`, loading/error/content rendering from the old dialog body into this new component:

```tsx
export function PMWorkItemDetailContent({
  projectId,
  workItemId,
  fallbackItem,
  onClose,
  className,
}: PMWorkItemDetailContentProps) {
  const [activityTab, setActivityTab] = useState<ActivityTab>('comments');
  const shouldFetch = Boolean(workItemId);
  const showComments = shouldFetch && activityTab === 'comments';
  const showHistory = shouldFetch && activityTab === 'history';

  const detailQuery = useGetPmWorkItemByIdQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !shouldFetch }
  );
  const childrenQuery = useGetPmWorkItemChildrenQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !shouldFetch }
  );
  const linksQuery = useGetPmWorkItemLinksQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !shouldFetch }
  );
  const commentsQuery = useGetPmWorkItemCommentsQuery(
    { projectId, workItemId: workItemId ?? 0, page: 0, size: 20 },
    { skip: !showComments }
  );
  const activitiesQuery = useGetPmWorkItemActivitiesQuery(
    {
      projectId,
      workItemId: workItemId ?? 0,
      page: 0,
      size: 20,
      type: getActivityType(activityTab),
    },
    { skip: !showHistory }
  );

  const item = toDetailModel(workItemId, detailQuery.data, fallbackItem);

  if (detailQuery.isLoading && !fallbackItem) {
    return <PMWorkItemDetailSkeleton />;
  }

  if (detailQuery.error) {
    return (
      <div className='p-6'>
        <Alert variant='destructive'>
          <AlertTitle>Detail unavailable</AlertTitle>
          <AlertDescription>{getErrorMessage(detailQuery.error)}</AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <>
      <DialogTitle className='sr-only'>{item.summary}</DialogTitle>
      <DialogDescription className='sr-only'>
        Work item detail
      </DialogDescription>
      <div
        className={cn(
          'flex h-full min-h-0 flex-col bg-background text-foreground',
          className
        )}
      >
        <PMWorkItemDetailHeader
          item={item}
          isFetching={detailQuery.isFetching}
          onClose={onClose}
        />
        <div className='grid min-h-0 flex-1 overflow-hidden lg:grid-cols-[minmax(0,1fr)_400px] xl:grid-cols-[minmax(0,1fr)_432px]'>
          <PMWorkItemDetailMain
            projectId={projectId}
            workItemId={workItemId}
            item={item}
            activityTab={activityTab}
            activitiesQuery={activitiesQuery}
            childrenQuery={childrenQuery}
            commentsQuery={commentsQuery}
            linksQuery={linksQuery}
            onActivityTabChange={setActivityTab}
          />
          <PMWorkItemDetailSidebar
            projectId={projectId}
            workItemId={workItemId}
            item={item}
          />
        </div>
      </div>
    </>
  );
}
```

Add `cn` to imports:

```tsx
import { cn } from '@/shared/utils';
```

- [ ] **Step 4: Make the header close button optional**

Update `PMWorkItemDetailHeader` props:

```tsx
function PMWorkItemDetailHeader({
  item,
  isFetching,
  onClose,
}: {
  item: WorkItemDetailModel;
  isFetching: boolean;
  onClose?: () => void;
}) {
```

Render the close button only when `onClose` exists:

```tsx
{onClose ? (
  <Button
    variant='ghost'
    size='icon'
    className='h-8 w-8'
    onClick={onClose}
  >
    <X className='h-4 w-4' />
  </Button>
) : null}
```

- [ ] **Step 5: Export the shared component**

Update `serp_web/src/modules/pm/components/work-items/detail/index.ts`:

```tsx
export {
  PMWorkItemDetailContent,
  PMWorkItemDetailDialog,
  type PMWorkItemDetailFallback,
} from './PMWorkItemDetailDialog';
```

- [ ] **Step 6: Run focused verification**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: TypeScript completes without errors from the extraction. Existing unrelated errors, if any, must be recorded before continuing.

- [ ] **Step 7: Commit Task 1**

```bash
git add src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx src/modules/pm/components/work-items/detail/index.ts
git commit -m "refactor: share PM work item detail content"
```

---

### Task 2: Add Collapsible Linked Work Items Section

**Files:**
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx`

- [ ] **Step 1: Add local open state**

Inside `PMWorkItemDetailMain`, near the existing mutation state:

```tsx
const [linkedItemsOpen, setLinkedItemsOpen] = useState(true);
```

- [ ] **Step 2: Replace the linked section header with a collapsible header**

Replace the current `DetailSection` wrapper for linked work items with:

```tsx
<section className='space-y-2'>
  <div className='flex items-center justify-between gap-3'>
    <button
      type='button'
      className='flex min-w-0 items-center gap-2 text-left text-base font-semibold'
      aria-expanded={linkedItemsOpen}
      onClick={() => setLinkedItemsOpen((current) => !current)}
    >
      {linkedItemsOpen ? (
        <ChevronDown className='h-4 w-4 shrink-0 text-muted-foreground' />
      ) : (
        <ChevronRight className='h-4 w-4 shrink-0 text-muted-foreground' />
      )}
      <span className='truncate'>
        Linked work items
        {item.linkTotal !== undefined ? ` (${item.linkTotal})` : ''}
      </span>
    </button>
    <PMWorkItemLinkActions projectId={projectId} workItemId={workItemId} />
  </div>
  {linkedItemsOpen ? (
    <WorkItemLinksList
      projectId={projectId}
      query={linksQuery}
      onDeleteLink={handleDeleteLink}
      deletingLinkId={deleteWorkItemLinkState.isLoading ? deletingLinkId : null}
    />
  ) : null}
</section>
```

- [ ] **Step 3: Run focused verification**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: TypeScript accepts the new `projectId` prop once Task 3 updates `WorkItemLinksList`. If this task is run before Task 3, expect a single prop type error for `WorkItemLinksList`; continue directly to Task 3 before committing.

---

### Task 3: Group And Navigate Linked Work Item Rows

**Files:**
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemRelationLists.tsx`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx`

- [ ] **Step 1: Add Next navigation imports**

At the top of `PMWorkItemRelationLists.tsx`, add:

```tsx
import { useRouter } from 'next/navigation';
```

Keep lucide imports aligned with actual usage:

```tsx
import { CheckSquare, Link2, Loader2, Trash2 } from 'lucide-react';
```

- [ ] **Step 2: Add grouping helpers above `WorkItemLinksList`**

```tsx
type WorkItemLinkGroup = {
  label: string;
  links: PMWorkItemLinkApi[];
};

function getWorkItemLinkLabel(link: PMWorkItemLinkApi): string {
  return (
    link.linkType?.description?.trim() ||
    link.linkType?.name?.trim() ||
    'Linked'
  );
}

function groupWorkItemLinks(links: PMWorkItemLinkApi[]): WorkItemLinkGroup[] {
  const groups = new Map<string, PMWorkItemLinkApi[]>();

  for (const link of links) {
    const label = getWorkItemLinkLabel(link);
    const current = groups.get(label) ?? [];
    current.push(link);
    groups.set(label, current);
  }

  return Array.from(groups.entries()).map(([label, groupedLinks]) => ({
    label,
    links: groupedLinks,
  }));
}
```

- [ ] **Step 3: Update `WorkItemLinksList` props**

Change the function signature:

```tsx
export function WorkItemLinksList({
  projectId,
  query,
  onDeleteLink,
  deletingLinkId,
}: {
  projectId: number;
  query: DetailQueryState<PMWorkItemLinkApi[]>;
  onDeleteLink?: (linkId: number) => void;
  deletingLinkId?: number | null;
}) {
```

Add the router:

```tsx
const router = useRouter();
```

- [ ] **Step 4: Replace the card list with grouped compact rows**

After the empty state, render grouped links:

```tsx
const groups = groupWorkItemLinks(links);

return (
  <div className='space-y-4'>
    {groups.map((group) => (
      <div key={group.label} className='space-y-2'>
        <h3 className='text-sm font-semibold text-muted-foreground'>
          {group.label}
        </h3>
        <div className='space-y-2'>
          {group.links.map((link) => {
            const linkedWorkItemId = link.workItem?.id;
            const linkedProjectId = link.workItem?.projectId ?? projectId;
            const canNavigate = Boolean(linkedWorkItemId);

            return (
              <button
                key={link.id}
                type='button'
                className='flex w-full items-center justify-between gap-3 rounded-md border bg-card px-3 py-2 text-left hover:bg-muted/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'
                disabled={!canNavigate}
                onClick={() => {
                  if (!linkedWorkItemId) return;
                  router.push(
                    `/pm/projects/${linkedProjectId}/work-items/${linkedWorkItemId}`
                  );
                }}
              >
                <span className='flex min-w-0 items-center gap-2'>
                  <CheckSquare className='h-4 w-4 shrink-0 text-primary' />
                  <span className='shrink-0 text-sm font-semibold text-primary'>
                    {link.workItem?.key ?? `#${linkedWorkItemId ?? link.id}`}
                  </span>
                  <span className='truncate text-sm font-medium'>
                    {link.workItem?.summary ?? 'Linked work item'}
                  </span>
                </span>
                <span className='flex shrink-0 items-center gap-2'>
                  <Badge variant='secondary'>
                    {link.workItem?.status?.name ?? 'Status'}
                  </Badge>
                  <PriorityValue priority={link.workItem?.priority} />
                  {onDeleteLink ? (
                    <Button
                      type='button'
                      variant='ghost'
                      size='icon'
                      className='h-7 w-7'
                      onClick={(event) => {
                        event.preventDefault();
                        event.stopPropagation();
                        onDeleteLink(link.id);
                      }}
                      disabled={deletingLinkId === link.id}
                    >
                      {deletingLinkId === link.id ? (
                        <Loader2 className='h-3.5 w-3.5 animate-spin' />
                      ) : (
                        <Trash2 className='h-3.5 w-3.5' />
                      )}
                    </Button>
                  ) : null}
                </span>
              </button>
            );
          })}
        </div>
      </div>
    ))}
  </div>
);
```

- [ ] **Step 5: Remove unused relation list imports**

If `Link2` is no longer used after replacing the card list, remove it from the lucide import:

```tsx
import { CheckSquare, Loader2, Trash2 } from 'lucide-react';
```

- [ ] **Step 6: Run focused verification**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: TypeScript accepts the `projectId` prop and router usage.

- [ ] **Step 7: Commit Tasks 2 and 3 together**

```bash
git add src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx src/modules/pm/components/work-items/detail/PMWorkItemRelationLists.tsx
git commit -m "feat: group PM linked work items"
```

---

### Task 4: Add Full Work Item Detail Page

**Files:**
- Create: `serp_web/src/modules/pm/pages/PMWorkItemDetailPage.tsx`
- Create: `serp_web/src/app/pm/projects/[projectId]/(detail)/work-items/[workItemId]/page.tsx`

- [ ] **Step 1: Create the module page**

Create `serp_web/src/modules/pm/pages/PMWorkItemDetailPage.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail page
 */

'use client';

import { useRouter } from 'next/navigation';
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/shared/components/ui';
import { PMWorkItemDetailContent } from '../components/work-items/detail';

interface PMWorkItemDetailPageProps {
  projectId: string;
  workItemId: string;
}

function toRouteNumber(value: string): number | null {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export function PMWorkItemDetailPage({
  projectId,
  workItemId,
}: PMWorkItemDetailPageProps) {
  const router = useRouter();
  const numericProjectId = toRouteNumber(projectId);
  const numericWorkItemId = toRouteNumber(workItemId);

  if (!numericProjectId || !numericWorkItemId) {
    return (
      <div className='p-6'>
        <Alert variant='destructive'>
          <AlertTitle>Invalid work item route</AlertTitle>
          <AlertDescription>
            Project and work item identifiers must be positive numbers.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className='h-[calc(100vh-9rem)] min-h-[720px] overflow-hidden rounded-lg border bg-background'>
      <PMWorkItemDetailContent
        projectId={numericProjectId}
        workItemId={numericWorkItemId}
        onClose={() => router.back()}
      />
    </div>
  );
}
```

- [ ] **Step 2: Create the App Router page**

Create `serp_web/src/app/pm/projects/[projectId]/(detail)/work-items/[workItemId]/page.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail route
 */

import { PMWorkItemDetailPage as PMWorkItemDetailModulePage } from '@/modules/pm/pages/PMWorkItemDetailPage';

interface PMWorkItemDetailRoutePageProps {
  params: Promise<{ projectId: string; workItemId: string }>;
}

export default async function PMWorkItemDetailRoutePage({
  params,
}: PMWorkItemDetailRoutePageProps) {
  const { projectId, workItemId } = await params;

  return (
    <PMWorkItemDetailModulePage
      projectId={projectId}
      workItemId={workItemId}
    />
  );
}
```

- [ ] **Step 3: Run route verification**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: App Router page and module page compile under strict TypeScript.

- [ ] **Step 4: Commit Task 4**

```bash
git add src/modules/pm/pages/PMWorkItemDetailPage.tsx 'src/app/pm/projects/[projectId]/(detail)/work-items/[workItemId]/page.tsx'
git commit -m "feat: add PM work item detail page"
```

---

### Task 5: Full Verification And Cleanup

**Files:**
- Verify all modified frontend files.

- [ ] **Step 1: Run lint**

Run from `serp_web/`:

```bash
npm run lint
```

Expected: ESLint exits successfully. If it reports unrelated pre-existing errors, record exact files and messages before handoff.

- [ ] **Step 2: Run type check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: TypeScript exits successfully.

- [ ] **Step 3: Run format check**

Run from `serp_web/`:

```bash
npm run format:check
```

Expected: Prettier exits successfully. If only touched files fail, run:

```bash
npx prettier --write src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx src/modules/pm/components/work-items/detail/PMWorkItemRelationLists.tsx src/modules/pm/pages/PMWorkItemDetailPage.tsx 'src/app/pm/projects/[projectId]/(detail)/work-items/[workItemId]/page.tsx'
```

Then rerun `npm run format:check`.

- [ ] **Step 4: Run build when route wiring changed**

Run from `serp_web/`:

```bash
npm run build
```

Expected: Next.js build completes and recognizes the new work item detail route.

- [ ] **Step 5: Manual behavior checks**

Run the app from `serp_web/`:

```bash
npm run dev
```

Open a PM project work item detail dialog and verify:

- `Linked work items` starts expanded.
- Clicking its chevron collapses the section.
- A `Blocks` link where the current item is source appears under `blocks`.
- A `Blocks` link where the current item is target appears under `is blocked by`.
- Clicking a linked work item row navigates to `/pm/projects/:projectId/work-items/:workItemId`.
- Clicking the delete icon deletes the link and does not navigate.
- The full detail page renders the same editable content as the dialog.

- [ ] **Step 6: Commit verification fixes only if needed**

If formatting or lint cleanup changed files, commit only those touched files:

```bash
git add src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx src/modules/pm/components/work-items/detail/PMWorkItemRelationLists.tsx src/modules/pm/pages/PMWorkItemDetailPage.tsx 'src/app/pm/projects/[projectId]/(detail)/work-items/[workItemId]/page.tsx'
git commit -m "chore: polish PM work item detail route"
```

---

## Self-Review

- Spec coverage: The plan covers grouped link labels, `is blocked by` through API-provided inward descriptions, collapsible section behavior, linked row navigation, shared dialog/page content, invalid route handling, and verification.
- Placeholder scan: The plan contains no deferred implementation markers.
- Type consistency: The plan uses existing `PMWorkItemLinkApi`, `PMWorkItemDetailFallback`, `DetailQueryState`, `WorkItemDetailModel`, and existing RTK Query hooks. New props are `PMWorkItemDetailContentProps.projectId`, `workItemId`, `fallbackItem`, `onClose`, and `className`.
