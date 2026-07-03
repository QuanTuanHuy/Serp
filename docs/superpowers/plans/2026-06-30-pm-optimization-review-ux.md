# PM Optimization Review UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rework the PM optimization review page into a work-item-first review flow with safer selection/apply behavior and a full-height override sheet.

**Architecture:** Keep the backend decision contract unchanged. Add a small pure frontend helper for meaningful-lane and ready-apply calculations, introduce a combined Review table for the default tab, and replace the compact override dialog with a right-side sheet. The page stays the orchestrator for RTK Query mutations and toast handling.

**Tech Stack:** Next.js 15, React 19, TypeScript, RTK Query, Tailwind CSS, shared shadcn-style UI primitives, lucide-react.

---

## Scope Check

This plan implements only the approved frontend UX spec:

- Default `Review` tab.
- No selected rows on initial run load.
- Work-item-first inline `Accept`, `Reject`, and `Override`.
- Meaningful-lane decision updates.
- `Apply ready` instead of `Apply selected`.
- Overview moved into `Summary`.
- Override editor moved from compact dialog to right-side full-height sheet.

It does not change backend apply semantics. Backend already treats only `ACCEPTED` and `OVERRIDDEN` lanes as actionable.

## File Structure

- Create: `serp_web/src/modules/pm/utils/optimizationReview.ts`
  - Pure helper functions for effective targets, meaningful-lane detection, decision payload construction, and ready apply ids.
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunReviewTable.tsx`
  - Default work-item-first review table that displays assignment and schedule together.
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideSheet.tsx`
  - Full-height sheet replacement for the override dialog.
- Delete: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx`
  - Replaced by `PMOptimizationRunOverrideSheet.tsx`.
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`
  - Use the Review tab as default, remove initial select-all behavior, wire meaningful-lane decisions, filter apply ids, and move overview into Summary.

---

### Task 1: Add Optimization Review Helper

**Files:**
- Create: `serp_web/src/modules/pm/utils/optimizationReview.ts`

- [ ] **Step 1: Create the helper file**

Create `serp_web/src/modules/pm/utils/optimizationReview.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization review helpers
 */

import type {
  PMOptimizationDecision,
  PMOptimizationRunDecisionItemRequest,
  PMOptimizationRunItemApi,
  PMOptimizationScheduleAllocationApi,
} from '../types/api';

export type PMOptimizationReviewChangeScope = {
  canEditAssignment: boolean;
  canEditSchedule: boolean;
};

export type PMOptimizationScheduleRange = {
  start?: number | null;
  end?: number | null;
};

export function getEffectiveAssigneeId(item: PMOptimizationRunItemApi) {
  if (
    item.assignmentDecision === 'OVERRIDDEN' &&
    typeof item.overrideAssigneeId === 'number'
  ) {
    return item.overrideAssigneeId;
  }

  return item.suggestedAssigneeId ?? null;
}

export function getEffectiveAllocationChunks(item: PMOptimizationRunItemApi) {
  if (
    item.scheduleDecision === 'OVERRIDDEN' &&
    item.overrideAllocationChunks?.length
  ) {
    return item.overrideAllocationChunks;
  }

  return item.allocationChunks || [];
}

export function getAllocationRange(
  chunks: PMOptimizationScheduleAllocationApi[]
): PMOptimizationScheduleRange {
  if (!chunks.length) {
    return { start: null, end: null };
  }

  return {
    start: Math.min(...chunks.map((chunk) => chunk.start)),
    end: Math.max(...chunks.map((chunk) => chunk.end)),
  };
}

export function getEffectiveScheduleRange(
  item: PMOptimizationRunItemApi
): PMOptimizationScheduleRange {
  const chunks = getEffectiveAllocationChunks(item);

  if (item.scheduleDecision === 'OVERRIDDEN' && chunks.length) {
    return getAllocationRange(chunks);
  }

  if (
    item.scheduleDecision === 'OVERRIDDEN' &&
    typeof item.overridePlannedStart === 'number' &&
    typeof item.overridePlannedEnd === 'number'
  ) {
    return {
      start: item.overridePlannedStart,
      end: item.overridePlannedEnd,
    };
  }

  return {
    start: item.suggestedPlannedStart,
    end: item.suggestedPlannedEnd,
  };
}

export function hasMeaningfulAssignmentChange(
  item: PMOptimizationRunItemApi,
  canEditAssignment: boolean
) {
  if (!canEditAssignment) {
    return false;
  }

  const targetAssigneeId = getEffectiveAssigneeId(item);
  return (
    typeof targetAssigneeId === 'number' &&
    targetAssigneeId !== item.currentAssigneeId
  );
}

export function hasMeaningfulScheduleChange(
  item: PMOptimizationRunItemApi,
  canEditSchedule: boolean
) {
  if (!canEditSchedule) {
    return false;
  }

  const range = getEffectiveScheduleRange(item);
  const hasValidRange =
    typeof range.start === 'number' &&
    typeof range.end === 'number' &&
    range.start < range.end;

  if (!hasValidRange) {
    return false;
  }

  const rangeChanged =
    range.start !== item.currentPlannedStart ||
    range.end !== item.currentPlannedEnd;

  return rangeChanged || getEffectiveAllocationChunks(item).length > 0;
}

export function buildMeaningfulDecisionItem(
  item: PMOptimizationRunItemApi,
  decision: Extract<PMOptimizationDecision, 'ACCEPTED' | 'REJECTED'>,
  scope: PMOptimizationReviewChangeScope
): PMOptimizationRunDecisionItemRequest | null {
  const updateAssignment = hasMeaningfulAssignmentChange(
    item,
    scope.canEditAssignment
  );
  const updateSchedule = hasMeaningfulScheduleChange(
    item,
    scope.canEditSchedule
  );

  if (!updateAssignment && !updateSchedule) {
    return null;
  }

  return {
    workItemId: item.workItemId,
    assignmentDecision: updateAssignment ? decision : undefined,
    scheduleDecision: updateSchedule ? decision : undefined,
  };
}

export function hasActionableDecision(
  item: PMOptimizationRunItemApi,
  scope: PMOptimizationReviewChangeScope
) {
  const assignmentReady =
    scope.canEditAssignment &&
    (item.assignmentDecision === 'ACCEPTED' ||
      item.assignmentDecision === 'OVERRIDDEN');
  const scheduleReady =
    scope.canEditSchedule &&
    (item.scheduleDecision === 'ACCEPTED' ||
      item.scheduleDecision === 'OVERRIDDEN');

  return assignmentReady || scheduleReady;
}

export function getReadyApplyWorkItemIds(
  items: PMOptimizationRunItemApi[],
  selectedWorkItemIds: number[],
  scope: PMOptimizationReviewChangeScope
) {
  const selected = new Set(selectedWorkItemIds);

  return items
    .filter(
      (item) =>
        selected.has(item.workItemId) && hasActionableDecision(item, scope)
    )
    .map((item) => item.workItemId);
}
```

- [ ] **Step 2: Run frontend type-check**

Run from `serp_web/`:

```bash
npm.cmd run type-check
```

Expected: PASS.

- [ ] **Step 3: Commit helper**

Run from repo root:

```bash
git add serp_web/src/modules/pm/utils/optimizationReview.ts
git commit -m "feat(pm-web): add optimization review decision helpers"
```

---

### Task 2: Add Work-Item Review Table

**Files:**
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunReviewTable.tsx`

- [ ] **Step 1: Create the combined review table**

Create `serp_web/src/modules/pm/components/optimization/PMOptimizationRunReviewTable.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization combined review table
 */

'use client';

import { Check, PenLine, X } from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  ScrollArea,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  getEffectiveAllocationChunks,
  getEffectiveAssigneeId,
  getEffectiveScheduleRange,
  hasMeaningfulAssignmentChange,
  hasMeaningfulScheduleChange,
} from '../../utils/optimizationReview';
import type {
  PMOptimizationDecision,
  PMOptimizationRunItemApi,
  PMOptimizationScheduleAllocationApi,
  PMOptimizationUserSummaryApi,
} from '../../types/api';

type PMOptimizationRunReviewTableProps = {
  items: PMOptimizationRunItemApi[];
  selectedIds: number[];
  canEditAssignment: boolean;
  canEditSchedule: boolean;
  onToggleApply: (workItemId: number) => void;
  onAccept: (item: PMOptimizationRunItemApi) => void;
  onReject: (item: PMOptimizationRunItemApi) => void;
  onOverride: (item: PMOptimizationRunItemApi) => void;
  disabled?: boolean;
};

const DECISION_LABELS: Record<PMOptimizationDecision, string> = {
  ACCEPTED: 'Accept',
  REJECTED: 'Reject',
  OVERRIDDEN: 'Override',
  PENDING: 'Pending',
};

export function PMOptimizationRunReviewTable({
  items,
  selectedIds,
  canEditAssignment,
  canEditSchedule,
  onToggleApply,
  onAccept,
  onReject,
  onOverride,
  disabled = false,
}: PMOptimizationRunReviewTableProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>Review work items</CardTitle>
      </CardHeader>
      <CardContent className='p-0'>
        <ScrollArea className='h-[640px]'>
          <div className='divide-y'>
            {items.length ? (
              items.map((item) => {
                const effectiveAssigneeId = getEffectiveAssigneeId(item);
                const effectiveRange = getEffectiveScheduleRange(item);
                const allocationChunks = getEffectiveAllocationChunks(item);
                const meaningfulAssignment = hasMeaningfulAssignmentChange(
                  item,
                  canEditAssignment
                );
                const meaningfulSchedule = hasMeaningfulScheduleChange(
                  item,
                  canEditSchedule
                );
                const hasMeaningfulChange =
                  meaningfulAssignment || meaningfulSchedule;

                return (
                  <div
                    key={item.id}
                    className={cn(
                      'grid gap-3 px-4 py-3 xl:grid-cols-[28px_minmax(0,1.2fr)_minmax(220px,0.8fr)_minmax(260px,1fr)_170px]',
                      disabled && 'opacity-60'
                    )}
                  >
                    <div className='pt-1'>
                      <Checkbox
                        checked={selectedIds.includes(item.workItemId)}
                        disabled={disabled}
                        onCheckedChange={() => onToggleApply(item.workItemId)}
                      />
                    </div>

                    <div className='min-w-0'>
                      <div className='flex flex-wrap items-center gap-2'>
                        <span className='text-xs font-semibold text-primary'>
                          {item.workItem?.key || `#${item.workItemId}`}
                        </span>
                        <DecisionBadge
                          label='A'
                          decision={item.assignmentDecision}
                        />
                        <DecisionBadge
                          label='S'
                          decision={item.scheduleDecision}
                        />
                      </div>
                      {item.workItem?.summary ? (
                        <div className='mt-1 truncate text-sm font-medium'>
                          {item.workItem.summary}
                        </div>
                      ) : null}
                      <div className='mt-2 flex flex-wrap gap-1.5'>
                        {[
                          item.workItem?.issueTypeName,
                          item.workItem?.statusName,
                          item.workItem?.priorityName,
                        ]
                          .filter((label): label is string => Boolean(label))
                          .map((label, index) => (
                            <Badge
                              key={`${label}-${index}`}
                              variant='outline'
                              className='h-5 px-1.5 text-xs'
                            >
                              {label}
                            </Badge>
                          ))}
                      </div>
                    </div>

                    <ReviewLane
                      title='Assignment'
                      current={formatAssignee(
                        item.currentAssignee,
                        item.currentAssigneeId
                      )}
                      target={formatAssignee(
                        item.suggestedAssignee,
                        effectiveAssigneeId
                      )}
                      active={meaningfulAssignment}
                      disabled={!canEditAssignment}
                    />

                    <div className='space-y-2'>
                      <ReviewLane
                        title='Schedule'
                        current={`${formatDateTime(
                          item.currentPlannedStart
                        )} -> ${formatDateTime(item.currentPlannedEnd)}`}
                        target={`${formatDateTime(
                          effectiveRange.start
                        )} -> ${formatDateTime(effectiveRange.end)}`}
                        active={meaningfulSchedule}
                        disabled={!canEditSchedule}
                      />
                      <AllocationPreview chunks={allocationChunks} />
                    </div>

                    <div className='flex flex-wrap gap-2 xl:justify-end'>
                      <Button
                        type='button'
                        size='sm'
                        variant='outline'
                        onClick={() => onAccept(item)}
                        disabled={disabled || !hasMeaningfulChange}
                      >
                        <Check className='mr-2 h-4 w-4' />
                        Accept
                      </Button>
                      <Button
                        type='button'
                        size='sm'
                        variant='outline'
                        onClick={() => onReject(item)}
                        disabled={disabled || !hasMeaningfulChange}
                      >
                        <X className='mr-2 h-4 w-4' />
                        Reject
                      </Button>
                      <Button
                        type='button'
                        size='sm'
                        variant='ghost'
                        onClick={() => onOverride(item)}
                        disabled={disabled}
                      >
                        <PenLine className='mr-2 h-4 w-4' />
                        Override
                      </Button>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className='px-4 py-10 text-sm text-muted-foreground'>
                No suggestions.
              </div>
            )}
          </div>
        </ScrollArea>
      </CardContent>
    </Card>
  );
}

function DecisionBadge({
  label,
  decision,
}: {
  label: string;
  decision?: PMOptimizationDecision | null;
}) {
  return (
    <Badge variant='secondary' className='h-5 px-1.5 text-xs'>
      {label}: {DECISION_LABELS[decision || 'PENDING']}
    </Badge>
  );
}

function ReviewLane({
  title,
  current,
  target,
  active,
  disabled,
}: {
  title: string;
  current: string;
  target: string;
  active: boolean;
  disabled: boolean;
}) {
  return (
    <div
      className={cn(
        'rounded-md border bg-muted/20 px-3 py-2 text-sm',
        active && 'border-primary/40 bg-primary/5',
        disabled && 'opacity-60'
      )}
    >
      <div className='mb-1 flex items-center justify-between gap-2'>
        <p className='font-medium'>{title}</p>
        {active ? (
          <Badge variant='secondary' className='h-5 px-1.5 text-xs'>
            Changed
          </Badge>
        ) : null}
      </div>
      <p className='truncate text-muted-foreground'>Current: {current}</p>
      <p className='truncate text-muted-foreground'>Target: {target}</p>
    </div>
  );
}

function AllocationPreview({
  chunks,
}: {
  chunks: PMOptimizationScheduleAllocationApi[];
}) {
  if (!chunks.length) {
    return null;
  }

  return (
    <div className='rounded-md border bg-muted/20 px-3 py-2 text-xs text-muted-foreground'>
      {chunks.slice(0, 2).map((chunk, index) => (
        <div key={`${chunk.assigneeId}-${chunk.start}-${index}`}>
          User #{chunk.assigneeId}: {formatDateTime(chunk.start)} ->{' '}
          {formatDateTime(chunk.end)} ({formatEffort(chunk.effortMillis)})
        </div>
      ))}
      {chunks.length > 2 ? <div>+{chunks.length - 2} more chunks</div> : null}
    </div>
  );
}

function formatDateTime(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}

function formatEffort(value?: number | null) {
  if (!value) return '0m';
  const minutes = Math.round(value / 60000);
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return remainder ? `${hours}h ${remainder}m` : `${hours}h`;
}

function formatAssignee(
  user?: PMOptimizationUserSummaryApi | null,
  userId?: number | null
) {
  if (!userId) return '-';
  return user?.displayName || `User #${userId}`;
}
```

- [ ] **Step 2: Run frontend type-check**

Run from `serp_web/`:

```bash
npm.cmd run type-check
```

Expected: PASS.

- [ ] **Step 3: Commit review table**

Run from repo root:

```bash
git add serp_web/src/modules/pm/components/optimization/PMOptimizationRunReviewTable.tsx
git commit -m "feat(pm-web): add optimization work item review table"
```

---

### Task 3: Replace Override Dialog With Sheet

**Files:**
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideSheet.tsx`
- Delete: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx`

- [ ] **Step 1: Move the dialog file to a sheet file**

Run from repo root:

```bash
git mv serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideSheet.tsx
```

- [ ] **Step 2: Replace dialog primitives with sheet primitives**

In `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideSheet.tsx`, replace the shared UI import section with:

```tsx
import {
  Badge,
  Button,
  Checkbox,
  Input,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui';
```

Rename the props type and exported component:

```tsx
type PMOptimizationRunOverrideSheetProps = {
  open: boolean;
  item: PMOptimizationRunItemApi | null;
  users: { id: number; label: string }[];
  assignmentDecision: PMOptimizationDecision;
  scheduleDecision: PMOptimizationDecision;
  overrideAssigneeId: string;
  overridePlannedStart: string;
  overridePlannedEnd: string;
  overrideAllocationChunks: PMOptimizationScheduleAllocationApi[];
  projectPeople: PMProjectPersonApi[];
  onAssignmentDecisionChange: (value: PMOptimizationDecision) => void;
  onScheduleDecisionChange: (value: PMOptimizationDecision) => void;
  onOverrideAssigneeIdChange: (value: string) => void;
  onOverridePlannedStartChange: (value: string) => void;
  onOverridePlannedEndChange: (value: string) => void;
  onOverrideAllocationChunksChange: (
    value: PMOptimizationScheduleAllocationApi[]
  ) => void;
  onSave: () => void;
  onClose: () => void;
  isSaving?: boolean;
};

export function PMOptimizationRunOverrideSheet({
  open,
  item,
  users,
  assignmentDecision,
  scheduleDecision,
  overrideAssigneeId,
  overridePlannedStart,
  overridePlannedEnd,
  overrideAllocationChunks,
  projectPeople,
  onAssignmentDecisionChange,
  onScheduleDecisionChange,
  onOverrideAssigneeIdChange,
  onOverridePlannedStartChange,
  onOverridePlannedEndChange,
  onOverrideAllocationChunksChange,
  onSave,
  onClose,
  isSaving,
}: PMOptimizationRunOverrideSheetProps) {
```

- [ ] **Step 3: Replace the component wrapper with a full-height sheet**

Replace the opening wrapper:

```tsx
  return (
    <Dialog open={open} onOpenChange={(nextOpen) => !nextOpen && onClose()}>
      <DialogContent className='max-h-[85vh] max-w-4xl overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Override suggestion</DialogTitle>
        </DialogHeader>
        {item ? (
          <div className='space-y-4'>
```

with:

```tsx
  return (
    <Sheet open={open} onOpenChange={(nextOpen) => !nextOpen && onClose()}>
      <SheetContent
        side='right'
        className='w-full gap-0 p-0 sm:max-w-2xl lg:max-w-3xl xl:max-w-4xl'
      >
        <SheetHeader className='border-b px-5 py-4'>
          <SheetTitle>Override suggestion</SheetTitle>
          <SheetDescription>
            {item
              ? item.workItem?.key || `Work item #${item.workItemId}`
              : ''}
          </SheetDescription>
        </SheetHeader>

        <div className='min-h-0 flex-1 overflow-y-auto px-5 py-4'>
          {item ? (
            <div className='space-y-5'>
```

Replace the closing wrapper:

```tsx
          </div>
        ) : null}
        <DialogFooter>
          <Button type='button' variant='outline' onClick={onClose}>
            Cancel
          </Button>
          <Button type='button' onClick={onSave} disabled={isSaving}>
            Save override
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
```

with:

```tsx
          </div>
        ) : null}
        </div>

        <SheetFooter className='border-t bg-background px-5 py-4 sm:flex-row sm:justify-end'>
          <Button type='button' variant='outline' onClick={onClose}>
            Cancel
          </Button>
          <Button type='button' onClick={onSave} disabled={isSaving}>
            Save override
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
```

- [ ] **Step 4: Keep schedule planned range read-only**

In the schedule override branch, keep only derived range and chunk editing. The
`PMDatePicker` planned start/end block should remain only in the non-overridden
branch.

The overridden branch must include this read-only range block:

```tsx
<div className='rounded-md border bg-muted/20 px-3 py-2 text-sm'>
  Derived range: {formatDateTime(derivedStart)} -{' '}
  {formatDateTime(derivedEnd)}
</div>
```

Keep the current add-chunk button, show-all-members checkbox, and
`overrideAllocationChunks.map(...)` chunk rows in the overridden branch. Keep the
current `PMDatePicker` planned start and planned end controls only in the
non-overridden branch.

- [ ] **Step 5: Update the page import and component name**

In `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`, replace:

```tsx
import { PMOptimizationRunOverrideDialog } from '../components/optimization/PMOptimizationRunOverrideDialog';
```

with:

```tsx
import { PMOptimizationRunOverrideSheet } from '../components/optimization/PMOptimizationRunOverrideSheet';
```

Replace:

```tsx
      <PMOptimizationRunOverrideDialog
```

with:

```tsx
      <PMOptimizationRunOverrideSheet
```

- [ ] **Step 6: Run frontend type-check**

Run from `serp_web/`:

```bash
npm.cmd run type-check
```

Expected: PASS.

- [ ] **Step 7: Commit sheet rename and import update**

Run from repo root:

```bash
git add serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideSheet.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx
git commit -m "feat(pm-web): move optimization override editor to sheet"
```

---

### Task 4: Wire Hybrid Review Page

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`

- [ ] **Step 1: Update imports**

In `PMProjectOptimizationRunPage.tsx`, replace the override dialog import and add the review table plus helpers:

```tsx
import { PMOptimizationRunItemTable } from '../components/optimization/PMOptimizationRunItemTable';
import { PMOptimizationRunOverview } from '../components/optimization/PMOptimizationRunOverview';
import { PMOptimizationRunOverrideSheet } from '../components/optimization/PMOptimizationRunOverrideSheet';
import { PMOptimizationRunReviewTable } from '../components/optimization/PMOptimizationRunReviewTable';
import {
  buildMeaningfulDecisionItem,
  getReadyApplyWorkItemIds,
} from '../utils/optimizationReview';
```

- [ ] **Step 2: Default to the Review tab and remove initial select-all**

Replace the selection state block:

```tsx
  const [selectedApplyIds, setSelectedApplyIds] = useState<number[]>([]);
  const [hasInitializedSelection, setHasInitializedSelection] = useState(false);
```

with:

```tsx
  const [selectedApplyIds, setSelectedApplyIds] = useState<number[]>([]);
```

Replace:

```tsx
  const [activeTab, setActiveTab] = useState('summary');
```

with:

```tsx
  const [activeTab, setActiveTab] = useState('review');
```

Delete this effect:

```tsx
  useEffect(() => {
    if (data?.items?.length && !hasInitializedSelection) {
      setSelectedApplyIds(data.items.map((item) => item.workItemId));
      setHasInitializedSelection(true);
    }
  }, [data?.items, hasInitializedSelection]);
```

Keep the run-change reset, but remove `setHasInitializedSelection(false)` from it:

```tsx
  useEffect(() => {
    setSelectedApplyIds([]);
  }, [numericRunId]);
```

- [ ] **Step 3: Add selected ready calculations**

After `canEditAssignment` and `canEditSchedule`, add:

```tsx
  const reviewScope = useMemo(
    () => ({ canEditAssignment, canEditSchedule }),
    [canEditAssignment, canEditSchedule]
  );
  const selectedReadyApplyIds = useMemo(
    () => getReadyApplyWorkItemIds(items, selectedApplyIds, reviewScope),
    [items, reviewScope, selectedApplyIds]
  );
```

- [ ] **Step 4: Add meaningful-lane row decision handler**

Replace the existing `handleBulkDecision` implementation with:

```tsx
  const handleMeaningfulDecision = (
    item: PMOptimizationRunItemApi,
    decision: 'ACCEPTED' | 'REJECTED'
  ) => {
    const decisionItem = buildMeaningfulDecisionItem(
      item,
      decision,
      reviewScope
    );

    if (!decisionItem) {
      toast.error('No applicable optimization change for this work item.');
      return;
    }

    void handleDecisionBatch(
      [decisionItem],
      decision === 'ACCEPTED'
        ? 'Suggestion accepted.'
        : 'Suggestion rejected.'
    );
  };

  const handleBulkDecision = (decision: 'ACCEPTED' | 'REJECTED') => {
    const selectedItems = items.filter((item) =>
      selectedApplyIds.includes(item.workItemId)
    );
    if (!selectedItems.length) {
      toast.error('Select at least one work item to review.');
      return;
    }

    const decisionItems = selectedItems
      .map((item) => buildMeaningfulDecisionItem(item, decision, reviewScope))
      .filter(
        (item): item is PMOptimizationRunDecisionItemRequest => Boolean(item)
      );

    if (!decisionItems.length) {
      toast.error('Selected work items have no applicable optimization changes.');
      return;
    }

    void handleDecisionBatch(
      decisionItems,
      decision === 'ACCEPTED'
        ? 'Selected suggestions accepted.'
        : 'Selected suggestions rejected.'
    );
  };
```

- [ ] **Step 5: Filter apply ids to ready selected rows**

Replace the top of `handleApply`:

```tsx
    if (!run || selectedApplyIds.length === 0) {
      toast.error('Select at least one work item to apply.');
      return;
    }
```

with:

```tsx
    if (!run || selectedReadyApplyIds.length === 0) {
      toast.error('Select reviewed work items to apply.');
      return;
    }
```

Replace the request body work item ids:

```tsx
          workItemIds: selectedApplyIds,
```

with:

```tsx
          workItemIds: selectedReadyApplyIds,
```

- [ ] **Step 6: Update action bar disabled states and labels**

Replace `bulkReviewDisabled` with:

```tsx
  const bulkReviewDisabled =
    updateState.isLoading || selectedApplyIds.length === 0;
  const applyReadyDisabled =
    applyState.isLoading || selectedReadyApplyIds.length === 0;
```

Replace the apply button:

```tsx
          <Button
            type='button'
            onClick={handleApply}
            disabled={applyState.isLoading || selectedApplyIds.length === 0}
          >
            <PlayCircle className='mr-2 h-4 w-4' />
            Apply selected
          </Button>
```

with:

```tsx
          <Button type='button' onClick={handleApply} disabled={applyReadyDisabled}>
            <PlayCircle className='mr-2 h-4 w-4' />
            Apply ready
            {selectedReadyApplyIds.length ? ` (${selectedReadyApplyIds.length})` : ''}
          </Button>
```

- [ ] **Step 7: Move overview into Summary and add Review tab**

Delete the standalone overview above the tabs:

```tsx
      <PMOptimizationRunOverview run={run} summary={summary} />
```

Change the tabs list from five columns to six columns and add `Review` first:

```tsx
        <TabsList className='grid w-full grid-cols-6'>
          <TabsTrigger value='review'>Review</TabsTrigger>
          <TabsTrigger value='summary'>Summary</TabsTrigger>
          <TabsTrigger value='assignment' disabled={assignmentTabDisabled}>
            Assignment
          </TabsTrigger>
          <TabsTrigger value='schedule' disabled={scheduleTabDisabled}>
            Schedule
          </TabsTrigger>
          <TabsTrigger value='risks'>Risks</TabsTrigger>
          <TabsTrigger value='history'>History</TabsTrigger>
        </TabsList>
```

Add this Review tab before Summary:

```tsx
        <TabsContent value='review' className='space-y-4'>
          {selectedApplyIds.length === 0 ? (
            <div className='rounded-md border border-dashed px-4 py-3 text-sm text-muted-foreground'>
              Select reviewed work items to apply.
            </div>
          ) : null}
          <PMOptimizationRunReviewTable
            items={items}
            selectedIds={selectedApplyIds}
            canEditAssignment={canEditAssignment}
            canEditSchedule={canEditSchedule}
            onToggleApply={handleToggleApply}
            onAccept={(item) => handleMeaningfulDecision(item, 'ACCEPTED')}
            onReject={(item) => handleMeaningfulDecision(item, 'REJECTED')}
            onOverride={openOverride}
            disabled={updateState.isLoading}
          />
        </TabsContent>
```

At the top of the existing Summary tab content, add:

```tsx
          <PMOptimizationRunOverview run={run} summary={summary} />
```

- [ ] **Step 8: Run frontend type-check**

Run from `serp_web/`:

```bash
npm.cmd run type-check
```

Expected: PASS.

- [ ] **Step 9: Commit page review flow changes**

Run from repo root:

```bash
git add serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx
git commit -m "feat(pm-web): use work item first optimization review"
```

---

### Task 5: Final Frontend Verification

**Files:**
- Check all modified frontend files.

- [ ] **Step 1: Run frontend lint**

Run from `serp_web/`:

```bash
npm.cmd run lint
```

Expected: PASS.

- [ ] **Step 2: Run frontend type-check**

Run from `serp_web/`:

```bash
npm.cmd run type-check
```

Expected: PASS.

- [ ] **Step 3: Run Prettier check for touched files**

Run from `serp_web/`:

```bash
npx.cmd prettier --check src/modules/pm/utils/optimizationReview.ts src/modules/pm/pages/PMProjectOptimizationRunPage.tsx src/modules/pm/components/optimization/PMOptimizationRunReviewTable.tsx src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx src/modules/pm/components/optimization/PMOptimizationRunOverrideSheet.tsx
```

Expected: PASS.

If this check fails only on touched files, run:

```bash
npx.cmd prettier --write src/modules/pm/utils/optimizationReview.ts src/modules/pm/pages/PMProjectOptimizationRunPage.tsx src/modules/pm/components/optimization/PMOptimizationRunReviewTable.tsx src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx src/modules/pm/components/optimization/PMOptimizationRunOverrideSheet.tsx
```

Then rerun the Prettier check.

- [ ] **Step 4: Run full frontend format check**

Run from `serp_web/`:

```bash
npm.cmd run format:check
```

Expected: May fail because the current repo baseline already contains unrelated formatting issues. If it fails, confirm the touched-file Prettier check from Step 3 passes and report the baseline failure clearly.

- [ ] **Step 5: Inspect git status**

Run from repo root:

```bash
git status --short
```

Expected: only intentional PM optimization UX files are changed, or clean if all commits succeeded.

- [ ] **Step 6: Commit verification fixes if needed**

If Step 1, Step 2, or Step 3 required fixes, commit only those fixes:

```bash
git add serp_web/src/modules/pm/utils/optimizationReview.ts serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunReviewTable.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideSheet.tsx
git commit -m "chore(pm-web): verify optimization review ux"
```

If no fixes were needed, do not create an empty commit.

---

## Self-Review

Spec coverage:

- Default Review tab: Task 4.
- No default selected rows: Task 4.
- Work-item-first row with assignment and schedule together: Task 2 and Task 4.
- Inline actions without checkbox selection: Task 2 and Task 4.
- Meaningful-lane accept/reject: Task 1 and Task 4.
- Bulk accept/reject on selected rows: Task 1 and Task 4.
- `Apply ready` only for actionable selected rows: Task 1 and Task 4.
- Summary overview moved into Summary tab: Task 4.
- Override right sheet: Task 3 and Task 4.
- Read-only derived schedule range from chunks: Task 3.
- Verification commands: Task 5.

Type consistency:

- Helper functions use existing `PMOptimizationRunItemApi`, `PMOptimizationRunDecisionItemRequest`, and `PMOptimizationScheduleAllocationApi`.
- Decision strings remain the existing `PMOptimizationDecision` union values.
- The page remains the only place that calls RTK Query mutations.

Known verification note:

- `serp_web` currently has no frontend test framework or `test` script.
- `npm.cmd run format:check` may fail on unrelated baseline files; touched PM files must pass the explicit Prettier check in Task 5.
